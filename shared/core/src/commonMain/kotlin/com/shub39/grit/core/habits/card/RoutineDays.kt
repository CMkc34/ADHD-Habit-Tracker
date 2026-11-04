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
package com.shub39.grit.core.habits.card

import com.shub39.grit.core.habits.Habit
import com.shub39.grit.core.habits.HabitStatus
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/** How a day box on the routine card looks. */
enum class RoutineDayState {
    /** Checked off; for a measurable routine the target is reached. */
    DONE,

    /** Measurable routine with some progress below the target. */
    PARTIAL,

    /** Planned, but not (yet) checked off. */
    OPEN,

    /** The routine is not planned on this weekday. */
    NOT_PLANNED,

    /** The routine did not exist yet. */
    BEFORE_START,
}

/**
 * One box of the routine card.
 *
 * @property progress Share of the target reached, `1` for a checked-off yes/no routine and `0`
 *   without a check-in.
 * @property value Value of the check-in, `0` without one (passed on when the box is tapped).
 * @property tappable Whether tapping the box adds or removes the check-in of this day.
 */
data class RoutineDay(
    val date: LocalDate,
    val state: RoutineDayState,
    val progress: Float,
    val value: Double,
    val isToday: Boolean,
    val tappable: Boolean,
)

object RoutineDays {
    /** Days on the card: 2 rows × 7 boxes (two weeks). */
    const val COUNT = 14

    /**
     * The last [count] calendar days up to [today] for [habit], oldest first, so they read like
     * text with today in the bottom right. The habit's start date is the date of [Habit.time].
     */
    fun lastDays(
        habit: Habit,
        statuses: List<HabitStatus>,
        today: LocalDate,
        count: Int = COUNT,
    ): List<RoutineDay> {
        val byDate = statuses.associateBy { it.date }
        val start = habit.time.date
        return (count - 1 downTo 0).map { daysAgo ->
            val date = today.minus(daysAgo, DateTimeUnit.DAY)
            val status = byDate[date]
            val planned = date.dayOfWeek in habit.days
            val beforeStart = date < start
            val progress =
                when {
                    status == null -> 0f
                    habit.isMeasurable ->
                        (status.value / habit.targetValue).toFloat().coerceIn(0f, 1f)
                    else -> 1f
                }
            val state =
                when {
                    status != null && progress >= 1f -> RoutineDayState.DONE
                    status != null -> RoutineDayState.PARTIAL
                    beforeStart -> RoutineDayState.BEFORE_START
                    !planned -> RoutineDayState.NOT_PLANNED
                    else -> RoutineDayState.OPEN
                }
            RoutineDay(
                date = date,
                state = state,
                progress = progress,
                value = status?.value ?: 0.0,
                isToday = daysAgo == 0,
                tappable = planned && !beforeStart,
            )
        }
    }
}
