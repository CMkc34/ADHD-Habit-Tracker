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
package com.shub39.grit.core.activity

import com.shub39.grit.core.habits.Habit
import com.shub39.grit.core.habits.HabitStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * One extra energy on every active day with at least [MIN_ROUTINES] different routines done. Like
 * the daily energy it is derived from the stored check-ins, so taking a check back takes the bonus
 * back and a restored backup brings it along.
 */
object DailyBonus {
    const val ENERGY = 1
    const val MIN_ROUTINES = 2

    /**
     * Dates on which at least [MIN_ROUTINES] of [habits] are done; a measurable routine counts once
     * its target is reached. Whether the day was active is checked by [ActivityPlanner].
     */
    fun days(habits: List<Habit>, statuses: List<HabitStatus>): Set<LocalDate> {
        val habitsById = habits.associateBy { it.id }
        return statuses
            .filter { status ->
                val habit = habitsById[status.habitId] ?: return@filter false
                !habit.isMeasurable || status.value >= habit.targetValue
            }
            .groupBy({ it.date }, { it.habitId })
            .filterValues { it.distinct().size >= MIN_ROUTINES }
            .keys
    }
}

/** The dates that earn the [DailyBonus], recalculated whenever routines or check-ins change. */
fun interface BonusDaySource {
    fun getBonusDays(): Flow<Set<LocalDate>>
}
