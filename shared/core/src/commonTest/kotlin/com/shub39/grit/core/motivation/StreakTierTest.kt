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
package com.shub39.grit.core.motivation

import kotlin.test.Test
import kotlin.test.assertEquals

class StreakTierTest {

    @Test
    fun `streak maps to tier at every boundary`() {
        val expected =
            mapOf(
                1 to StreakTier.STARTER,
                2 to StreakTier.BUILDING,
                4 to StreakTier.BUILDING,
                5 to StreakTier.ESTABLISHED,
                13 to StreakTier.ESTABLISHED,
                14 to StreakTier.LEGEND,
                1000 to StreakTier.LEGEND,
                Int.MAX_VALUE to StreakTier.LEGEND,
            )

        expected.forEach { (days, tier) ->
            assertEquals(tier, StreakTier.forStreak(days), "streak $days")
        }
    }

    @Test
    fun `streak below one counts as starter`() {
        assertEquals(StreakTier.STARTER, StreakTier.forStreak(0))
        assertEquals(StreakTier.STARTER, StreakTier.forStreak(-5))
    }
}
