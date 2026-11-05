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

import com.shub39.grit.core.habits.Habit
import com.shub39.grit.core.habits.HabitStatus

/**
 * XP per stat, computed from the stored check-ins: one XP for every routine and date on which the
 * routine is done. A measurable routine counts once its target is reached, like the
 * [com.shub39.grit.core.activity.DailyBonus]. All history counts; check-ins of deleted routines
 * don't, and when a routine changes its stat its XP moves along.
 */
object StatXp {
    fun of(
        habits: List<Habit>,
        statuses: List<HabitStatus>,
        statOf: (habitId: Long) -> FighterStat,
    ): Map<FighterStat, Int> {
        val habitsById = habits.associateBy { it.id }
        val counts =
            statuses
                .filter { status -> habitsById[status.habitId]?.isDoneBy(status) == true }
                .distinctBy { it.habitId to it.date }
                .groupingBy { statOf(it.habitId) }
                .eachCount()
        return FighterStat.entries.associateWith { counts[it] ?: 0 }
    }
}
