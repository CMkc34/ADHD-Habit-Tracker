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
package com.shub39.grit.shared.ui.habit.minigame

import com.shub39.grit.core.minigame.clutter.ClutterSmash
import com.shub39.grit.core.minigame.clutter.JunkKind
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter.Companion.INK
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter.Companion.argb
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ClutterSmashPixelsTest {

    @Test
    fun `every junk sprite matches its hitbox and cracks when damaged`() {
        for (kind in JunkKind.entries) {
            val intact = ClutterSmashPixels.junk(kind, damaged = false)
            val cracked = ClutterSmashPixels.junk(kind, damaged = true)
            assertEquals(kind.width.toInt(), intact.width, "$kind width")
            assertEquals(kind.height.toInt(), intact.height, "$kind height")
            assertTrue(intact.width <= ClutterSmash.COLUMN_WIDTH.toInt(), "$kind fits column")
            assertNotEquals(intact.argb.toList(), cracked.argb.toList(), "$kind crack")
        }
    }

    @Test
    fun `every junk sprite has an ink outline and its own colour`() {
        for (kind in JunkKind.entries) {
            val image = ClutterSmashPixels.junk(kind)
            assertTrue(INK in image.argb.toList(), "$kind has no outline")
            assertTrue(ClutterSmashPixels.color(kind) in image.argb.toList(), "$kind colour")
        }
        val colors = JunkKind.entries.map { ClutterSmashPixels.color(it) }.toSet()
        assertEquals(JunkKind.entries.size, colors.size)
    }

    @Test
    fun `the bolt and the Ki Wave glow in the fighter's ki colour`() {
        val ki = argb(0xFF9A3C)
        val bolt = ClutterSmashPixels.bolt(ki)
        assertEquals(9, bolt.width)
        assertEquals(5, bolt.height)
        assertEquals(ki, bolt[0, 2])
        assertNotEquals(ki, bolt[7, 2])

        val wave = ClutterSmashPixels.wave(ki)
        assertEquals(14, wave.width)
        assertEquals(72, wave.height)
        assertTrue(wave.argb.any { it == ki })
        assertTrue(wave.argb.any { it != 0 && it != ki })
    }

    @Test
    fun `the laundry backdrop fills the field with the floor at the bottom`() {
        val image = ClutterSmashPixels.backdrop()
        assertEquals(ClutterSmash.WIDTH.toInt(), image.width)
        assertEquals(ClutterSmash.HEIGHT.toInt(), image.height)
        assertTrue(image.argb.none { it == 0 })
        assertTrue(ClutterSmashPixels.FLOOR_TOP < ClutterSmash.FLOOR_Y)
    }
}
