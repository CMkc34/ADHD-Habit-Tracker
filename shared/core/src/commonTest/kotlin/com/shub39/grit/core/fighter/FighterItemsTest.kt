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

class FighterItemsTest {

    @Test
    fun `ids are unique and outfits and gear do not overlap`() {
        val all = FighterItems.outfits + FighterItems.gear
        assertEquals(all.size, all.toSet().size)
    }

    @Test
    fun `the default outfit is the white gi`() {
        assertEquals(FighterItems.GI_WHITE, FighterItems.DEFAULT_OUTFIT)
        assertTrue(FighterItems.DEFAULT_OUTFIT in FighterItems.outfits)
    }
}
