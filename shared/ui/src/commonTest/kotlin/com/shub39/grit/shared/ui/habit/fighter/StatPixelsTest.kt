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
package com.shub39.grit.shared.ui.habit.fighter

import com.shub39.grit.core.fighter.FighterStat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StatPixelsTest {

    @Test
    fun `every stat has an outlined icon`() {
        for (stat in FighterStat.entries) {
            val image = StatPixels.render(stat)
            assertEquals(StatPixels.SIZE, image.width, "$stat")
            assertEquals(StatPixels.SIZE, image.height, "$stat")
            assertFalse(image.isBlank, "$stat")
            assertTrue(image.argb.any { it == PixelPainter.INK }, "$stat has no outline")
        }
    }

    @Test
    fun `the outline stays inside the icon`() {
        for (stat in FighterStat.entries) {
            val image = StatPixels.render(stat)
            val last = StatPixels.SIZE - 1
            for (i in 0..last) {
                // nothing is cut off: every edge pixel is empty or outline
                for ((x, y) in listOf(i to 0, i to last, 0 to i, last to i)) {
                    val c = image[x, y]
                    assertTrue(c == 0 || c == PixelPainter.INK, "$stat at $x,$y")
                }
            }
        }
    }

    @Test
    fun `the icons differ from each other`() {
        val icons = FighterStat.entries.map { StatPixels.render(it).argb.toList() }
        assertEquals(icons.size, icons.toSet().size)
    }
}
