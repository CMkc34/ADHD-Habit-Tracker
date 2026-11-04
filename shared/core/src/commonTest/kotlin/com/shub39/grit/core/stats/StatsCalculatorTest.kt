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
import com.shub39.grit.core.habits.Habit
import com.shub39.grit.core.habits.HabitStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.YearMonth

class StatsCalculatorTest {

    private val everyDay = DayOfWeek.entries.toSet()
    private val weekdays = everyDay - DayOfWeek.SATURDAY - DayOfWeek.SUNDAY

    private fun date(month: Int, day: Int) = LocalDate(2026, month, day)

    private fun habit(id: Long, days: Set<DayOfWeek> = everyDay) =
        Habit(
            id = id,
            title = "Habit $id",
            description = "",
            time = LocalDateTime(2026, 1, 1, 8, 0),
            days = days,
            index = id.toInt(),
            reminder = false,
        )

    private fun status(habitId: Long, date: LocalDate) = HabitStatus(habitId = habitId, date = date)

    // active days and energy per month

    @Test
    fun `active days and energy are counted per month`() {
        // 1 October is missed: two active days do not pay for a freeze
        val active = setOf(date(9, 29), date(9, 30), date(10, 2), date(10, 3))
        val report = ActivityPlanner.plan(active, startDate = date(9, 29), today = date(10, 3))
        val today = date(10, 3)

        val activeDays = StatsCalculator.activeDaysPerMonth(report, today, months = 3)
        assertEquals(
            listOf(YearMonth(2026, 8), YearMonth(2026, 9), YearMonth(2026, 10)),
            activeDays.map { it.month },
        )
        assertEquals(listOf(0, 2, 2), activeDays.map { it.activeDays })
        assertEquals(listOf(true, false, false), activeDays.map { it.beforeStart })

        val energy = StatsCalculator.energyPerMonth(report, today, months = 3)
        assertEquals(listOf(0, 2, 2), energy.map { it.earned })
        assertEquals(listOf(0, 0, 0), energy.map { it.spent })
    }

    @Test
    fun `frozen days count as spent energy in their month`() {
        // seven active days in September earn enough for one freeze on 1 October
        val september = (24..30).map { date(9, it) }
        val report =
            ActivityPlanner.plan(
                activeDays = (september + date(10, 2)).toSet(),
                startDate = date(9, 24),
                today = date(10, 2),
            )

        val energy = StatsCalculator.energyPerMonth(report, date(10, 2), months = 2)

        assertEquals(listOf(7, 1), energy.map { it.earned })
        assertEquals(listOf(0, ActivityPlanner.FREEZE_COST), energy.map { it.spent })
    }

    @Test
    fun `bonus energy counts as earned in its month`() {
        val report =
            ActivityPlanner.plan(
                activeDays = setOf(date(9, 30), date(10, 1)),
                startDate = date(9, 30),
                today = date(10, 1),
                bonusDays = setOf(date(9, 30), date(10, 1)),
            )

        val energy = StatsCalculator.energyPerMonth(report, date(10, 1), months = 2)

        assertEquals(listOf(2, 2), energy.map { it.earned })
    }

    // month completion

    @Test
    fun `completion counts planned days from the first check-in up to today`() {
        val statuses = listOf(status(1, date(10, 5)), status(1, date(10, 6)))

        val completion =
            StatsCalculator.monthCompletion(
                habits = listOf(habit(1)),
                statuses = statuses,
                month = YearMonth(2026, 10),
                today = date(10, 8),
            )

        // 5, 6, 7, 8 October are planned, two of them are checked
        assertEquals(Completion(done = 2, planned = 4), completion)
    }

    @Test
    fun `completion only counts the habit's weekdays`() {
        // 2026-10-05 is a Monday; the weekend 10-11 October is not planned
        val statuses = (5..9).map { status(1, date(10, it)) } + status(1, date(10, 10))

        val completion =
            StatsCalculator.monthCompletion(
                habits = listOf(habit(1, weekdays)),
                statuses = statuses,
                month = YearMonth(2026, 10),
                today = date(10, 12),
            )

        assertEquals(Completion(done = 5, planned = 6), completion)
    }

    @Test
    fun `completion adds up all habits`() {
        val statuses =
            listOf(status(1, date(10, 1)), status(1, date(10, 2)), status(2, date(10, 2)))

        val completion =
            StatsCalculator.monthCompletion(
                habits = listOf(habit(1), habit(2)),
                statuses = statuses,
                month = YearMonth(2026, 10),
                today = date(10, 2),
            )

        assertEquals(Completion(done = 3, planned = 3), completion)
    }

    @Test
    fun `nothing planned gives no completion`() {
        assertNull(
            StatsCalculator.monthCompletion(
                habits = listOf(habit(1)),
                statuses = emptyList(),
                month = YearMonth(2026, 10),
                today = date(10, 2),
            )
        )
    }

    @Test
    fun `month review compares with the previous month`() {
        val statuses = listOf(status(1, date(9, 29)), status(1, date(10, 1)))

        val review =
            StatsCalculator.monthReview(
                habits = listOf(habit(1)),
                statuses = statuses,
                today = date(10, 2),
            )

        assertEquals(Completion(done = 1, planned = 2), review.previous) // 29 + 30 September
        assertEquals(Completion(done = 1, planned = 2), review.current) // 1 + 2 October
    }

    // comebacks

    @Test
    fun `no comeback without a break`() {
        assertEquals(0, StatsCalculator.comebacks((1..5).map { date(10, it) }, everyDay))
        assertEquals(0, StatsCalculator.comebacks(emptyList(), everyDay))
    }

    @Test
    fun `every new streak after a break is a comeback`() {
        val dates = listOf(date(10, 1), date(10, 2), date(10, 4), date(10, 8), date(10, 9))

        assertEquals(2, StatsCalculator.comebacks(dates, everyDay))
    }

    @Test
    fun `days that are not planned do not break a streak`() {
        // Friday 9 October, then Monday 12 October with a weekday-only habit
        val dates = listOf(date(10, 9), date(10, 12))

        assertEquals(0, StatsCalculator.comebacks(dates, weekdays))
    }

    @Test
    fun `a break across the month boundary is one comeback`() {
        val dates = listOf(date(9, 29), date(10, 2), date(10, 3))

        assertEquals(1, StatsCalculator.comebacks(dates, everyDay))
    }

    // checks per week

    @Test
    fun `checks are grouped by week with the current week last`() {
        // 2026-10-14 is a Wednesday
        val statuses =
            listOf(
                status(1, date(10, 12)), // Monday, current week
                status(2, date(10, 14)), // Wednesday, current week
                status(1, date(10, 11)), // Sunday, previous week
            )

        val weeks =
            StatsCalculator.checksPerWeek(
                statuses = statuses,
                startingDay = DayOfWeek.MONDAY,
                today = date(10, 14),
                weeks = 2,
            )

        assertEquals(listOf(date(10, 5), date(10, 12)), weeks.map { it.weekStart })
        assertEquals(listOf(1, 2), weeks.map { it.checks })
    }

    @Test
    fun `weeks follow the starting day setting`() {
        val weeks =
            StatsCalculator.checksPerWeek(
                statuses = listOf(status(1, date(10, 11))), // Sunday
                startingDay = DayOfWeek.SUNDAY,
                today = date(10, 14),
                weeks = 1,
            )

        assertEquals(date(10, 11), weeks.single().weekStart)
        assertEquals(1, weeks.single().checks)
    }

    @Test
    fun `rate is zero without planned days`() {
        assertTrue(Completion(0, 0).rate == 0f)
        assertEquals(0.5f, Completion(1, 2).rate)
    }
}
