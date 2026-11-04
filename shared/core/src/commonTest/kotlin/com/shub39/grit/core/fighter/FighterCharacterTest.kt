/*
 * Copyright (C) 2026  Shubham Gorai
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.shub39.grit.core.fighter

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FighterCharacterTest {

    @Test
    fun `ids are unique and read back, unknown ids give the default`() {
        val ids = FighterCharacter.entries.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        for (character in FighterCharacter.entries) {
            assertEquals(character, FighterCharacter.fromId(character.id))
        }
        assertEquals(FighterCharacter.DEFAULT, FighterCharacter.fromId("gone"))
        assertEquals(FighterCharacter.DEFAULT, FighterCharacter.fromId(null))
    }

    @Test
    fun `the old looks map to a character`() {
        assertEquals(FighterCharacter.WIND_MONK, FighterCharacter.of(FighterLook.KAI))
        assertEquals(FighterCharacter.SPARK_PRODIGY, FighterCharacter.of(FighterLook.MIRA))
    }

    @Test
    fun `default colours are valid presets`() {
        assertTrue(FighterCharacter.entries.all { it.defaultColors.isValid() })
    }

    @Test
    fun `colours survive encode and decode`() {
        val colors = FighterColors(skin = 7, hair = 2, main = 8, accent = 5)
        assertEquals(colors, FighterColors.decode(colors.encode(), FighterColors(0, 0, 0, 0)))
    }

    @Test
    fun `broken or out of range colours fall back`() {
        val fallback = FighterCharacter.KARATEKA.defaultColors
        for (value in listOf(null, "", "1|2|3", "a|b|c|d", "1|2|3|4|5", "99|0|0|0", "0|0|0|-1")) {
            assertEquals(fallback, FighterColors.decode(value, fallback), "$value")
        }
    }

    @Test
    fun `presets are opaque and distinct`() {
        for (list in listOf(FighterPalette.skins, FighterPalette.hairs, FighterPalette.mains)) {
            assertEquals(list.size, list.toSet().size)
            assertTrue(list.all { (it ushr 24) == 0xFF })
        }
        assertEquals(FighterPalette.accents.size, FighterPalette.accents.toSet().size)
    }
}
