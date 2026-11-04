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
package com.shub39.grit.core.stats

import com.shub39.grit.core.activity.ActivityReport
import com.shub39.grit.core.habits.HabitWithAnalytics
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

/** All statistics added by the fork (phase 3), derived from data the app already has. */
data class ForkStats(
    val activeDaysPerMonth: List<MonthlyActiveDays> = emptyList(),
    val energyPerMonth: List<MonthlyEnergy> = emptyList(),
    val monthReview: MonthReview = MonthReview(current = null, previous = null),
    val streakOverview: List<HabitStreak> = emptyList(),
    val checksPerWeek: List<WeeklyChecks> = emptyList(),
    /** Comebacks per habit id. */
    val comebacks: Map<Long, Int> = emptyMap(),
) {
    val totalComebacks: Int
        get() = comebacks.values.sum()

    companion object {
        fun of(
            habits: List<HabitWithAnalytics>,
            activity: ActivityReport?,
            startingDay: DayOfWeek,
            today: LocalDate,
        ): ForkStats {
            val statuses = habits.flatMap { it.statuses }
            return ForkStats(
                activeDaysPerMonth =
                    activity?.let { StatsCalculator.activeDaysPerMonth(it, today) }.orEmpty(),
                energyPerMonth =
                    activity?.let { StatsCalculator.energyPerMonth(it, today) }.orEmpty(),
                monthReview = StatsCalculator.monthReview(habits.map { it.habit }, statuses, today),
                streakOverview = StatsCalculator.streakOverview(habits),
                checksPerWeek = StatsCalculator.checksPerWeek(statuses, startingDay, today),
                comebacks =
                    habits.associate { habit ->
                        habit.habit.id to
                            StatsCalculator.comebacks(
                                habit.statuses.map { it.date },
                                habit.habit.days,
                            )
                    },
            )
        }
    }
}
