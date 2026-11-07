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
package com.shub39.grit.shared.ui.habit.arena

import com.shub39.grit.core.arena.Opponent
import com.shub39.grit.shared.ui.habit.fighter.PixelImage
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class OpponentPixelsTest {

    private fun all(): List<Triple<Opponent, OpponentPose, Int>> =
        Opponent.entries.flatMap { o ->
            OpponentPose.entries.flatMap { pose ->
                (0 until OpponentPixels.FRAMES).map { Triple(o, pose, it) }
            }
        }

    private fun PixelImage.filled(): List<Int> = argb.indices.filter { argb[it] != 0 }

    @Test
    fun `every opponent renders in every pose and frame with an outline`() {
        for ((o, pose, f) in all()) {
            val image = OpponentPixels.render(o, pose, f)
            assertEquals(OpponentPixels.SIZE, image.width)
            assertEquals(OpponentPixels.SIZE, image.height)
            assertFalse(image.isBlank, "$o $pose $f")
            assertTrue(image.argb.any { it == PixelPainter.INK }, "$o $pose $f has no outline")
        }
    }

    @Test
    fun `nothing of the figure is cut off at the canvas edge`() {
        val last = OpponentPixels.SIZE - 1
        for ((o, pose, f) in all()) {
            val image = OpponentPixels.render(o, pose, f)
            for (i in 0..last) {
                // the bottom row is the ground line, the outline may sit on it
                for ((x, y) in listOf(i to 0, 0 to i, last to i)) {
                    val c = image[x, y]
                    assertTrue(c == 0 || c == PixelPainter.INK, "$o $pose $f at $x,$y")
                }
            }
        }
    }

    @Test
    fun `opponents stand on the ground`() {
        for (o in Opponent.entries) {
            for (pose in listOf(OpponentPose.IDLE, OpponentPose.KO)) {
                val image = OpponentPixels.render(o, pose)
                val bottom = image.filled().maxOf { it / OpponentPixels.SIZE }
                // the swarm floats a little while idle
                val lowest =
                    if (o == Opponent.DISTRACTION_SWARM && pose == OpponentPose.IDLE) 50
                    else OpponentPixels.SIZE - 2
                assertTrue(bottom >= lowest, "$o $pose ends at row $bottom")
            }
        }
    }

    @Test
    fun `the opponents look different from each other`() {
        for (pose in OpponentPose.entries) {
            val images = Opponent.entries.map { OpponentPixels.render(it, pose).argb.toList() }
            assertEquals(images.size, images.toSet().size, "$pose")
        }
    }

    @Test
    fun `all poses look different`() {
        for (o in Opponent.entries) {
            val images = OpponentPose.entries.map { OpponentPixels.render(o, it).argb.toList() }
            assertEquals(images.size, images.toSet().size, "$o")
        }
    }

    @Test
    fun `idle opponents move`() {
        for (o in Opponent.entries) {
            val frames =
                (0 until OpponentPixels.FRAMES).map {
                    OpponentPixels.render(o, OpponentPose.IDLE, it).argb.toList()
                }
            assertTrue(frames.toSet().size > 1, "$o stands still")
        }
    }

    @Test
    fun `big opponents are bigger than small ones`() {
        fun area(o: Opponent) = OpponentPixels.render(o).filled().size
        val snooze = area(Opponent.SNOOZE_BUTTON)
        for (big in listOf(Opponent.OVERWHELM_OGRE, Opponent.PROCRASTINATION_DRAGON)) {
            assertTrue(area(big) > snooze * 3 / 2, "$big is not bigger than the snooze button")
        }
    }

    @Test
    fun `a hit shows the white impact star and knocked out opponents see stars`() {
        val white = PixelPainter.argb(0xFFFFFF)
        val star = PixelPainter.argb(0xFFD34A)
        for (o in Opponent.entries) {
            val hit = OpponentPixels.render(o, OpponentPose.HIT)
            assertTrue(hit.argb.count { it == white } >= 10, "$o has no impact star")
            val ko = OpponentPixels.render(o, OpponentPose.KO)
            assertTrue(ko.argb.count { it == star } >= 8, "$o sees no stars")
        }
    }

    @Test
    fun `the locked silhouette only uses the two theme colours`() {
        val fill = PixelPainter.argb(0x79747E)
        val mark = PixelPainter.argb(0xFEF7FF)
        for (o in Opponent.entries) {
            val image = OpponentPixels.silhouette(o, fill, mark)
            assertEquals(setOf(0, fill, mark), image.argb.toSet(), "$o")
            assertTrue(image.argb.count { it == mark } >= 10, "$o has no question mark")
            // same shape as the opponent
            val shape = OpponentPixels.render(o).filled().size
            assertNotEquals(0, shape)
            assertTrue(image.argb.count { it == fill } <= shape, "$o")
        }
    }

    @Test
    fun `opponents are drawn a fifth bigger than the fighter`() {
        // the ogre fills its design grid, so its height shows the scale
        val image = OpponentPixels.render(Opponent.OVERWHELM_OGRE)
        val rows = image.filled().map { it / OpponentPixels.SIZE }
        val height = rows.max() - rows.min() + 1
        assertTrue(height > 48, "the ogre is only $height px tall")
    }

    @Test
    fun `a guarding opponent shimmers`() {
        val shimmer = PixelPainter.argb(0x8FD8FF)
        for (o in Opponent.entries) {
            val guard = OpponentPixels.render(o, OpponentPose.GUARD)
            assertTrue(guard.argb.count { it == shimmer } >= 20, "$o has no guard shimmer")
        }
    }

    @Test
    fun `the attack leaves from inside the picture, on the fighter's side`() {
        for (o in Opponent.entries) {
            val at = OpponentPixels.launchPoint(o)
            assertTrue(at.x in 0f..OpponentPixels.SIZE / 2f, "$o launches at $at")
            assertTrue(at.y in 0f..OpponentPixels.SIZE.toFloat(), "$o launches at $at")
        }
    }
}
