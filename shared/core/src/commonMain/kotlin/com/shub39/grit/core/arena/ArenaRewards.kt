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
package com.shub39.grit.core.arena

import com.shub39.grit.core.activity.Reward

/**
 * Water for the arena (SPEC 9.6, #17): the first win against opponent n gives [WATER_PER_PLACE] × n
 * once; rematches give nothing. Derived from the fight log, so a restored backup keeps the water.
 * Water only buys cosmetics, so winning never buys stats.
 */
object ArenaRewards {
    const val WATER_PER_PLACE = 5

    /** What the first win against [opponent] pays. */
    fun water(opponent: Opponent): Int = WATER_PER_PLACE * opponent.number

    /**
     * One [Reward] per opponent won against, on the day of the first win, oldest first. Records of
     * unknown opponents pay nothing.
     */
    fun of(records: List<FightRecord>): List<Reward> =
        records
            .filter { it.outcome == Outcome.WIN }
            .mapNotNull { record -> record.opponent?.let { it to record.date } }
            .groupBy({ it.first }, { it.second })
            .map { (opponent, dates) -> Reward(dates.min(), water(opponent)) }
            .sortedWith(compareBy({ it.date }, { it.amount }))
}
