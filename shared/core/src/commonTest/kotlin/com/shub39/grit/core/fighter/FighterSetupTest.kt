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

import com.shub39.grit.core.avatar.AvatarData
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FighterSetupTest {

    @Test
    fun `stepping wraps around at both ends`() {
        assertEquals(
            FighterCharacter.STREET_BRAWLER,
            FighterSetup.step(FighterCharacter.WIND_MONK, 1),
        )
        assertEquals(FighterCharacter.KARATEKA, FighterSetup.step(FighterCharacter.WIND_MONK, -1))
        assertEquals(FighterCharacter.WIND_MONK, FighterSetup.step(FighterCharacter.KARATEKA, 1))
        FighterCharacter.entries.forEach {
            assertEquals(it, FighterSetup.step(it, FighterCharacter.entries.size))
        }
    }

    @Test
    fun `random colours are always valid presets`() {
        val random = Random(7)
        repeat(200) { assertTrue(FighterSetup.randomColors(random).isValid()) }
    }

    @Test
    fun `a slot reads and sets its own index only`() {
        val colors = FighterColors(skin = 1, hair = 2, main = 3, accent = 4)
        FighterColorSlot.entries.forEach { slot ->
            val changed = slot.set(colors, 0)
            assertEquals(0, slot.indexIn(changed))
            FighterColorSlot.entries
                .filter { it != slot }
                .forEach { other -> assertEquals(other.indexIn(colors), other.indexIn(changed)) }
        }
    }

    @Test
    fun `setting a slot wraps the index into the presets`() {
        val colors = FighterCharacter.WIND_MONK.defaultColors
        FighterColorSlot.entries.forEach { slot ->
            assertEquals(slot.presets.size - 1, slot.indexIn(slot.set(colors, -1)))
            assertEquals(0, slot.indexIn(slot.set(colors, slot.presets.size)))
            assertTrue(slot.set(colors, 99).isValid())
        }
    }

    @Test
    fun `every fighter has its own default name`() {
        val names = FighterCharacter.entries.map { it.defaultName }
        assertEquals(names.size, names.toSet().size)
        assertTrue(names.all { it.isNotBlank() })
    }

    @Test
    fun `the old looks keep their names`() {
        assertEquals(FighterLook.KAI.defaultName, FighterCharacter.of(FighterLook.KAI).defaultName)
        assertEquals(
            FighterLook.MIRA.defaultName,
            FighterCharacter.of(FighterLook.MIRA).defaultName,
        )
    }

    @Test
    fun `users without a choice keep their look in its default colours`() {
        val mira = AvatarData(look = FighterLook.MIRA)
        assertNull(mira.fighterCharacter)
        assertEquals(FighterCharacter.SPARK_PRODIGY, mira.character)
        assertEquals(FighterCharacter.SPARK_PRODIGY.defaultColors, mira.colors)
        assertEquals("Mira", mira.displayName)
        assertEquals(FighterCharacter.WIND_MONK, AvatarData().character)
    }

    @Test
    fun `the picked fighter wins over the old look`() {
        val colors = FighterColors(skin = 0, hair = 1, main = 2, accent = 3)
        val data =
            AvatarData(
                look = FighterLook.MIRA,
                fighterCharacter = FighterCharacter.SHADOW_RUNNER,
                fighterColors = colors,
            )
        assertEquals(FighterCharacter.SHADOW_RUNNER, data.character)
        assertEquals(colors, data.colors)
        assertEquals("Nyx", data.displayName)
        assertEquals("Bolt", data.copy(name = "Bolt").displayName)
    }

    @Test
    fun `a backup fills in a missing fighter choice with its colours`() {
        val colors = FighterColors(skin = 0, hair = 1, main = 2, accent = 3)
        val backup =
            AvatarData(fighterCharacter = FighterCharacter.KARATEKA, fighterColors = colors)
        val restored = AvatarData().mergedWith(backup)
        assertEquals(FighterCharacter.KARATEKA, restored.character)
        assertEquals(colors, restored.colors)

        val own = AvatarData(fighterCharacter = FighterCharacter.STREET_BRAWLER)
        val merged = own.mergedWith(backup)
        assertEquals(FighterCharacter.STREET_BRAWLER, merged.character)
        assertEquals(FighterCharacter.STREET_BRAWLER.defaultColors, merged.colors)
    }
}
