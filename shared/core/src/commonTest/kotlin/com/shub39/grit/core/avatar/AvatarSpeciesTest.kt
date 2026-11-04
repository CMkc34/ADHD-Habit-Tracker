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
package com.shub39.grit.core.avatar

import kotlin.test.Test
import kotlin.test.assertEquals

class AvatarSpeciesTest {

    @Test
    fun `ids map back to their species`() {
        AvatarSpecies.entries.forEach { assertEquals(it, AvatarSpecies.fromId(it.id)) }
    }

    @Test
    fun `missing or unknown ids give sprout`() {
        assertEquals(AvatarSpecies.SPROUT, AvatarSpecies.fromId(null))
        assertEquals(AvatarSpecies.SPROUT, AvatarSpecies.fromId("cactus"))
    }

    @Test
    fun `a restored backup brings back the chosen species`() {
        val cherry = AvatarData(species = AvatarSpecies.CHERRY)

        assertEquals(AvatarSpecies.CHERRY, AvatarData().mergedWith(cherry).species)
        assertEquals(AvatarSpecies.CHERRY, cherry.mergedWith(AvatarData()).species)
    }
}
