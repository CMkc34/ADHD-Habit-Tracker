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
import kotlin.test.assertNull

class FighterRankTest {

    @Test
    fun `ranks use exactly the plant thresholds`() {
        // the former PlantStage thresholds: the rank after the update equals the plant stage
        assertEquals(12, FighterRank.entries.size)
        assertEquals(
            listOf(0, 3, 7, 14, 21, 30, 45, 60, 90, 150, 240, 365),
            FighterRank.entries.map { it.minActiveDays },
        )
        assertEquals(12, FighterRank.LEGEND.number)
    }

    @Test
    fun `every threshold starts its rank`() {
        FighterRank.entries.forEach { rank ->
            assertEquals(rank, FighterRank.forActiveDays(rank.minActiveDays))
            if (rank.minActiveDays > 0) {
                assertEquals(
                    FighterRank.entries[rank.ordinal - 1],
                    FighterRank.forActiveDays(rank.minActiveDays - 1),
                )
            }
        }
        assertEquals(FighterRank.LEGEND, FighterRank.forActiveDays(1000))
        assertNull(FighterRank.LEGEND.next)
        assertEquals(FighterRank.TRAINEE, FighterRank.ROOKIE.next)
    }

    @Test
    fun `each rank has its own belt`() {
        assertEquals(FighterRank.entries.size, FighterRank.entries.map { it.belt }.toSet().size)
        assertEquals(Belt.WHITE, FighterRank.ROOKIE.belt)
        assertEquals(Belt.BLACK, FighterRank.CHAMPION.belt)
        assertEquals(1, FighterRank.MASTER.belt.goldStripes)
        assertEquals(2, FighterRank.GRANDMASTER.belt.goldStripes)
        assertEquals(Belt.GOLD, FighterRank.LEGEND.belt)
    }
}
