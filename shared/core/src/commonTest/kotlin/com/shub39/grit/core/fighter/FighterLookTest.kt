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
import kotlin.test.Test
import kotlin.test.assertEquals

class FighterLookTest {

    @Test
    fun `the plant species map to the looks`() {
        assertEquals(FighterLook.KAI, FighterLook.fromId("sprout"))
        assertEquals(FighterLook.MIRA, FighterLook.fromId("cherry"))
    }

    @Test
    fun `missing or unknown ids give kai`() {
        assertEquals(FighterLook.KAI, FighterLook.fromId(null))
        assertEquals(FighterLook.KAI, FighterLook.fromId("cactus"))
    }

    @Test
    fun `ids round-trip and never change`() {
        FighterLook.entries.forEach { assertEquals(it, FighterLook.fromId(it.id)) }
        // stored under avatar_species and in backups, also read by older app versions
        assertEquals(listOf("sprout", "cherry"), FighterLook.entries.map { it.id })
    }

    @Test
    fun `default names follow the look`() {
        assertEquals("Kai", FighterLook.KAI.defaultName)
        assertEquals("Mira", FighterLook.MIRA.defaultName)
    }

    @Test
    fun `a restored backup brings back the chosen look`() {
        val mira = AvatarData(look = FighterLook.MIRA)

        assertEquals(FighterLook.MIRA, AvatarData().mergedWith(mira).look)
        assertEquals(FighterLook.MIRA, mira.mergedWith(AvatarData()).look)
    }
}
