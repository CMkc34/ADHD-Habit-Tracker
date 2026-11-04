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

/** Streak categories that decide which quotes fit the user's current run. */
enum class StreakTier(val range: IntRange) {
    STARTER(1..1),
    BUILDING(2..4),
    ESTABLISHED(5..13),
    LEGEND(14..Int.MAX_VALUE);

    companion object {
        /** Tier for a streak of [days]; values below 1 count as [STARTER]. */
        fun forStreak(days: Int): StreakTier {
            val streak = days.coerceAtLeast(1)
            return entries.first { streak in it.range }
        }
    }
}
