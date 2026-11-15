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

import com.shub39.grit.core.minigame.kibarrage.KiBarrage
import com.shub39.grit.core.minigame.kibarrage.MinionKind
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter.Companion.INK
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter.Companion.argb
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class KiBarragePixelsTest {

    @Test
    fun `every minion fits its hitbox and moves between its frames`() {
        for (kind in MinionKind.entries) {
            val a = KiBarragePixels.minion(kind, 0)
            val b = KiBarragePixels.minion(kind, 1)
            assertTrue(a.width <= KiBarrage.MINION_SIZE.toInt(), "$kind is ${a.width} wide")
            assertTrue(a.height <= KiBarrage.MINION_SIZE.toInt(), "$kind is ${a.height} high")
            assertNotEquals(a.argb.toList(), b.argb.toList(), "$kind does not move")
            assertEquals(a.argb.toList(), KiBarragePixels.minion(kind, 2).argb.toList())
        }
    }

    @Test
    fun `minions have an ink outline and their own colours`() {
        for (kind in MinionKind.entries) {
            val image = KiBarragePixels.minion(kind, 0)
            assertTrue(INK in image.argb.toList(), "$kind has no outline")
            assertTrue(KiBarragePixels.color(kind) in image.argb.toList(), "$kind")
        }
        val colors = MinionKind.entries.map { KiBarragePixels.color(it) }.toSet()
        assertEquals(MinionKind.entries.size, colors.size)
    }

    @Test
    fun `the bolt glows in the fighter's ki colour`() {
        val ki = argb(0x4FD1B0)
        val bolt = KiBarragePixels.bolt(ki)
        assertEquals(3, bolt.width)
        assertEquals(7, bolt.height)
        assertEquals(ki, bolt[1, 6])
        assertEquals(0, bolt[0, 6])
        assertNotEquals(ki, bolt[1, 0])
    }

    @Test
    fun `the backdrop fills the field with the stage floor at the bottom`() {
        val image = KiBarragePixels.backdrop()
        assertEquals(KiBarrage.WIDTH.toInt(), image.width)
        assertEquals(KiBarrage.HEIGHT.toInt(), image.height)
        assertTrue(image.argb.none { it == 0 })
        // the fighter stands on the floor strip
        assertTrue(KiBarragePixels.FLOOR_Y < KiBarrage.FIGHTER_TOP + KiBarrage.FIGHTER_SIZE)
    }

    @Test
    fun `full and empty hearts differ`() {
        assertNotEquals(
            KiBarragePixels.heart(true).argb.toList(),
            KiBarragePixels.heart(false).argb.toList(),
        )
        assertTrue(KiBarragePixels.shot.argb.any { it != 0 && it != INK })
    }
}
