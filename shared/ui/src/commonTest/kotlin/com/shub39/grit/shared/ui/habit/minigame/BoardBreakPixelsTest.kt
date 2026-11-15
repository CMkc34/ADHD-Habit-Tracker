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

import com.shub39.grit.core.fighter.FighterCharacter
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.core.minigame.boardbreak.StrikeMaterial
import com.shub39.grit.shared.ui.habit.fighter.FighterPixels
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter.Companion.INK
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BoardBreakPixelsTest {

    @Test
    fun `intact and split layers for boards, bricks and ice have ink outlines and distinct colors`() {
        for (material in StrikeMaterial.entries) {
            val intact = BoardBreakPixels.layer(material)
            val left = BoardBreakPixels.halfLayer(material, rightHalf = false)
            val right = BoardBreakPixels.halfLayer(material, rightHalf = true)

            assertEquals(BoardBreakPixels.LAYER_WIDTH, intact.width)
            assertEquals(BoardBreakPixels.LAYER_HEIGHT, intact.height)
            assertEquals(17, left.width)
            assertEquals(BoardBreakPixels.LAYER_HEIGHT, left.height)
            assertEquals(17, right.width)
            assertEquals(BoardBreakPixels.LAYER_HEIGHT, right.height)

            assertTrue(intact.argb.any { it == INK })
            assertTrue(intact.argb.any { it == BoardBreakPixels.color(material) })
            assertTrue(left.argb.any { it == BoardBreakPixels.color(material) })
            assertTrue(right.argb.any { it == BoardBreakPixels.color(material) })
        }
    }

    @Test
    fun `stand and impact burst render non-empty sprites`() {
        val stand = BoardBreakPixels.stand()
        assertEquals(BoardBreakPixels.STAND_WIDTH, stand.width)
        assertEquals(BoardBreakPixels.STAND_BASE_HEIGHT, stand.height)
        assertTrue(stand.argb.any { it == INK })
        assertTrue(stand.argb.count { it != 0 } > 80)

        val ki = FighterPixels.kiColor(FighterCharacter.WIND_MONK, FighterRank.BRAWLER)
        val burst = BoardBreakPixels.impactBurst(ki)
        assertEquals(15, burst.width)
        assertEquals(15, burst.height)
        assertTrue(burst.argb.count { it != 0 } > 25)
    }
}
