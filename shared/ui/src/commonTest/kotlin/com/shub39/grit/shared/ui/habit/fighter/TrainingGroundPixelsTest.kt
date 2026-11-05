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

import com.shub39.grit.core.avatar.Weather
import com.shub39.grit.core.fighter.FighterCharacter
import com.shub39.grit.core.fighter.FighterItems
import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.shared.ui.habit.fighter.TrainingGroundPixels.GROUND_HEIGHT
import com.shub39.grit.shared.ui.habit.fighter.TrainingGroundPixels.SkyFrame
import com.shub39.grit.shared.ui.habit.fighter.TrainingGroundPixels.WIDTH
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TrainingGroundPixelsTest {

    private val all = FighterItems.props.toSet()

    @Test
    fun `every prop but the mat has its own spot`() {
        val spots = TrainingGroundPixels.propSpots.map { it.id }
        assertEquals(spots.size, spots.toSet().size)
        assertEquals(FighterItems.props.toSet() - FighterItems.MAT, spots.toSet())
    }

    @Test
    fun `every weather renders its own sky`() {
        val skies =
            Weather.entries.map { weather ->
                val sky = TrainingGroundPixels.renderSky(weather, height = 2 * GROUND_HEIGHT)
                assertEquals(WIDTH, sky.width)
                assertEquals(2 * GROUND_HEIGHT, sky.height)
                // the sky is painted everywhere, the ground covers it later
                assertTrue(sky.argb.none { it == 0 }, weather.name)
                sky.argb.toList()
            }
        assertEquals(skies.size, skies.toSet().size)
    }

    @Test
    fun `only sunny, fair and hazy skies have the sun`() {
        val sun = PixelPainter.argb(0xFFD45C)
        for (weather in Weather.entries) {
            val sky = TrainingGroundPixels.renderSky(weather, height = GROUND_HEIGHT)
            val shines = sky.argb.any { it == sun }
            assertEquals(weather <= Weather.HAZY, shines, weather.name)
        }
    }

    @Test
    fun `the sky moves only from frame to frame`() {
        val still = TrainingGroundPixels.renderSky(Weather.FAIR, GROUND_HEIGHT)
        val again =
            TrainingGroundPixels.renderSky(Weather.FAIR, GROUND_HEIGHT, frame = SkyFrame(0, 0))
        assertEquals(still.argb.toList(), again.argb.toList())
        val moved =
            TrainingGroundPixels.renderSky(Weather.FAIR, GROUND_HEIGHT, frame = SkyFrame(2, 8))
        assertNotEquals(still.argb.toList(), moved.argb.toList())
    }

    @Test
    fun `sky frames loop with the clouds drifting to and fro`() {
        assertEquals(SkyFrame.STILL, SkyFrame.at(0f))
        assertEquals(SkyFrame.at(5f), SkyFrame.at(5f + TrainingGroundPixels.LOOP_SECONDS))
        val frames = (0 until 600).map { SkyFrame.at(it / 10f) }
        assertEquals(TrainingGroundPixels.CLOUD_DRIFT, frames.maxOf { it.drift })
        assertEquals(-TrainingGroundPixels.CLOUD_DRIFT, frames.minOf { it.drift })
        assertTrue(frames.all { it.rayAngle in 0 until 36 })
        assertEquals(9, frames.map { it.rayAngle }.toSet().size)
    }

    @Test
    fun `the sun and the clouds move down with the sky top`() {
        val top = TrainingGroundPixels.renderSky(Weather.SUNNY, 2 * GROUND_HEIGHT)
        val lower = TrainingGroundPixels.renderSky(Weather.SUNNY, 2 * GROUND_HEIGHT, skyTop = 20)
        for (y in 0 until 2 * GROUND_HEIGHT - 20) {
            for (x in 0 until WIDTH) {
                val sky = top[x, 0]
                if (top[x, y] != sky) assertEquals(top[x, y], lower[x, y + 20], "$x, $y")
            }
        }
    }

    @Test
    fun `the ground fills its whole width and every prop shows`() {
        val empty = TrainingGroundPixels.renderGround(emptySet())
        assertEquals(WIDTH, empty.width)
        assertEquals(GROUND_HEIGHT, empty.height)
        // the floor reaches from edge to edge
        for (x in 0 until WIDTH) assertNotEquals(0, empty[x, GROUND_HEIGHT - 1])
        for (prop in FighterItems.props) {
            val with = TrainingGroundPixels.renderGround(setOf(prop))
            assertNotEquals(empty.argb.toList(), with.argb.toList(), prop)
        }
    }

    @Test
    fun `a tired fighter's ground is greyer`() {
        val awake = TrainingGroundPixels.renderGround(all)
        val tired = TrainingGroundPixels.renderGround(all, tired = true)
        fun saturation(image: PixelImage) =
            image.argb
                .filter { it != 0 }
                .sumOf { c ->
                    val ch = listOf(16, 8, 0).map { (c shr it) and 0xFF }
                    ch.max() - ch.min()
                }
        assertTrue(saturation(tired) < saturation(awake))
    }

    @Test
    fun `no prop overlaps any fighter at any size`() {
        val ground = TrainingGroundPixels.renderGround(all)
        val bare = TrainingGroundPixels.renderGround(setOf(FighterItems.MAT))
        // the scene pixels that only the props (not the floor, fence or mat) paint
        val prop = BooleanArray(WIDTH * GROUND_HEIGHT) { ground.argb[it] != bare.argb[it] }
        val scales = listOf(1f, 1.3f, TrainingGroundPixels.MAX_FIGHTER_SCALE)
        for (character in FighterCharacter.entries) {
            for (mood in FighterMood.entries) {
                for (frame in 0 until FighterPixels.FRAMES) {
                    val fighter =
                        FighterPixels.render(
                            character,
                            FighterRank.LEGEND,
                            mood,
                            gear = FighterItems.gear.toSet(),
                            frame = frame,
                        )
                    for (scale in scales) {
                        assertClear(fighter, scale, prop, "$character $mood $frame $scale")
                    }
                }
            }
        }
    }

    private fun assertClear(fighter: PixelImage, scale: Float, prop: BooleanArray, what: String) {
        val left = TrainingGroundPixels.fighterLeft(scale)
        val top = TrainingGroundPixels.fighterTop(scale)
        for (fy in 0 until fighter.height) for (fx in 0 until fighter.width) {
            if (fighter[fx, fy] == 0) continue
            val x0 = floor(left + fx * scale).toInt()
            val x1 = ceil(left + (fx + 1) * scale).toInt()
            val y0 = floor(top + fy * scale).toInt()
            val y1 = ceil(top + (fy + 1) * scale).toInt()
            for (y in y0 until y1) for (x in x0 until x1) {
                if (x in 0 until WIDTH && y in 0 until GROUND_HEIGHT) {
                    assertFalse(prop[y * WIDTH + x], "$what covers a prop at $x, $y")
                }
            }
        }
    }
}
