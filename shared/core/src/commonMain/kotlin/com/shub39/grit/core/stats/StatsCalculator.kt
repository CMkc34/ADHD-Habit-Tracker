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

import com.shub39.grit.core.activity.ActivityPlanner
import com.shub39.grit.core.activity.ActivityReport
import com.shub39.grit.core.activity.DailyBonus
import com.shub39.grit.core.activity.DayStatus
import com.shub39.grit.core.habits.Habit
import com.shub39.grit.core.habits.HabitStatus
import com.shub39.grit.core.habits.HabitWithAnalytics
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.minusMonth
import kotlinx.datetime.plus
import kotlinx.datetime.yearMonth

/** Active days of one month; [beforeStart] marks months that ended before the start date. */
data class MonthlyActiveDays(val month: YearMonth, val activeDays: Int, val beforeStart: Boolean)

/** Energy earned by active days and spent on freezes in one month. */
data class MonthlyEnergy(
    val month: YearMonth,
    val earned: Int,
    val spent: Int,
    val beforeStart: Boolean,
)

/** Checked days out of planned days. */
data class Completion(val done: Int, val planned: Int) {
    val rate: Float
        get() = if (planned > 0) done.toFloat() / planned else 0f
}

/** Completion of the current and the previous month, `null` when nothing was planned. */
data class MonthReview(val current: Completion?, val previous: Completion?)

data class HabitStreak(val title: String, val currentStreak: Int, val bestStreak: Int)

data class WeeklyChecks(val weekStart: LocalDate, val checks: Int)

/** Pure calculations behind the fork's statistics (phase 3). */
object StatsCalculator {

    fun activeDaysPerMonth(
        report: ActivityReport,
        today: LocalDate,
        months: Int = 6,
    ): List<MonthlyActiveDays> {
        val startDate = report.days.keys.firstOrNull()
        return lastMonths(today, months).map { month ->
            MonthlyActiveDays(
                month = month,
                activeDays = report.countIn(month, DayStatus.ACTIVE),
                beforeStart = startDate == null || month.lastDay < startDate,
            )
        }
    }

    fun energyPerMonth(
        report: ActivityReport,
        today: LocalDate,
        months: Int = 6,
    ): List<MonthlyEnergy> {
        val startDate = report.days.keys.firstOrNull()
        return lastMonths(today, months).map { month ->
            MonthlyEnergy(
                month = month,
                earned =
                    report.countIn(month, DayStatus.ACTIVE) *
                        ActivityPlanner.ENERGY_PER_ACTIVE_DAY +
                        report.bonusDays.count { it.yearMonth == month } * DailyBonus.ENERGY,
                spent = report.countIn(month, DayStatus.FROZEN) * ActivityPlanner.FREEZE_COST,
                beforeStart = startDate == null || month.lastDay < startDate,
            )
        }
    }

    fun monthReview(
        habits: List<Habit>,
        statuses: List<HabitStatus>,
        today: LocalDate,
    ): MonthReview {
        val month = today.yearMonth
        return MonthReview(
            current = monthCompletion(habits, statuses, month, today),
            previous = monthCompletion(habits, statuses, month.minusMonth(), today),
        )
    }

    /**
     * Checked / planned days of all habits in [month]. A day is planned on the habit's weekdays,
     * from its first check-in up to [today] at most (like the habit's consistency).
     */
    fun monthCompletion(
        habits: List<Habit>,
        statuses: List<HabitStatus>,
        month: YearMonth,
        today: LocalDate,
    ): Completion? {
        var done = 0
        var planned = 0
        val datesByHabit = statuses.groupBy({ it.habitId }, { it.date })

        habits.forEach { habit ->
            val dates =
                datesByHabit[habit.id].orEmpty().filter { it.dayOfWeek in habit.days }.toSet()
            val firstCheck = dates.minOrNull() ?: return@forEach
            val from = maxOf(month.firstDay, firstCheck)
            val to = minOf(month.lastDay, today)
            if (from > to) return@forEach

            for (epochDay in from.toEpochDays()..to.toEpochDays()) {
                val day = LocalDate.fromEpochDays(epochDay)
                if (day.dayOfWeek !in habit.days) continue
                planned++
                if (day in dates) done++
            }
        }

        return if (planned > 0) Completion(done, planned) else null
    }

    /** How often a new streak started after at least one missed planned day. */
    fun comebacks(dates: List<LocalDate>, eligibleWeekdays: Set<DayOfWeek>): Int {
        val sorted = dates.filter { it.dayOfWeek in eligibleWeekdays }.distinct().sorted()
        return sorted.zipWithNext().count { (previous, next) ->
            nextEligibleDay(previous, eligibleWeekdays) != next
        }
    }

    /** All habits, highest current streak first; ties are broken by the best streak. */
    fun streakOverview(habits: List<HabitWithAnalytics>): List<HabitStreak> =
        habits
            .map { HabitStreak(it.habit.title, it.currentStreak, it.bestStreak) }
            .sortedWith(
                compareByDescending<HabitStreak> { it.currentStreak }
                    .thenByDescending { it.bestStreak }
            )

    /** Check-ins per week, oldest first; the week containing [today] comes last. */
    fun checksPerWeek(
        statuses: List<HabitStatus>,
        startingDay: DayOfWeek,
        today: LocalDate,
        weeks: Int = 12,
    ): List<WeeklyChecks> {
        val offset = (today.dayOfWeek.isoDayNumber - startingDay.isoDayNumber + 7) % 7
        val currentWeekStart = today.minus(offset, DateTimeUnit.DAY)

        return (weeks - 1 downTo 0).map { weeksAgo ->
            val weekStart = currentWeekStart.minus(weeksAgo * 7, DateTimeUnit.DAY)
            val weekEnd = weekStart.plus(6, DateTimeUnit.DAY)
            WeeklyChecks(weekStart, statuses.count { it.date in weekStart..weekEnd })
        }
    }

    private fun lastMonths(today: LocalDate, months: Int): List<YearMonth> =
        (months - 1 downTo 0).map { today.yearMonth.minus(it, DateTimeUnit.MONTH) }

    private fun ActivityReport.countIn(month: YearMonth, status: DayStatus): Int =
        days.count { (day, dayStatus) -> day.yearMonth == month && dayStatus == status }

    private fun nextEligibleDay(day: LocalDate, eligibleWeekdays: Set<DayOfWeek>): LocalDate {
        var next = day.plus(1, DateTimeUnit.DAY)
        while (next.dayOfWeek !in eligibleWeekdays) next = next.plus(1, DateTimeUnit.DAY)
        return next
    }
}
