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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ArenaStagePixelsTest {

    @Test
    fun `every stage fills the whole picture in every light`() {
        for (stage in ArenaStage.entries) for (lighting in ArenaLighting.entries) {
            val image = ArenaStagePixels.render(stage, lighting)
            assertEquals(ArenaStagePixels.WIDTH, image.width)
            assertEquals(ArenaStagePixels.HEIGHT, image.height)
            assertTrue(image.argb.none { it == 0 }, "$stage $lighting has holes")
        }
    }

    @Test
    fun `the stages look different from each other`() {
        val images =
            ArenaStage.entries.map { ArenaStagePixels.render(it, ArenaLighting.DAY).argb.toList() }
        assertEquals(images.size, images.toSet().size)
    }

    @Test
    fun `the light changes how a stage looks`() {
        for (stage in ArenaStage.entries) {
            val day = ArenaStagePixels.render(stage, ArenaLighting.DAY).argb.toList()
            val dusk = ArenaStagePixels.render(stage, ArenaLighting.DUSK).argb.toList()
            val night = ArenaStagePixels.render(stage, ArenaLighting.NIGHT).argb.toList()
            assertNotEquals(day, dusk, "$stage")
            assertNotEquals(day, night, "$stage")
            assertNotEquals(dusk, night, "$stage")
        }
    }

    @Test
    fun `both fighters stand inside the stage on the floor`() {
        assertTrue(ArenaStagePixels.FOOT_Y in ArenaStagePixels.FLOOR_TOP..ArenaStagePixels.HEIGHT)
        assertTrue(ArenaStagePixels.FOOT_Y - 48 >= 0)
        assertTrue(ArenaStagePixels.PLAYER_LEFT + 48 <= ArenaStagePixels.OPPONENT_LEFT)
        assertTrue(ArenaStagePixels.OPPONENT_LEFT + 48 <= ArenaStagePixels.WIDTH)
    }
}
