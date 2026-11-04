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
import com.shub39.grit.core.habits.HabitWithAnalytics
import com.shub39.grit.core.habits.MeasurableTargets
import com.shub39.grit.core.habits.Target
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

class ForkStatsTest {

    private val today = LocalDate(2026, 10, 14)
    private val noTarget = Target(0.0, 0.0)

    private fun habit(id: Long, checkedDays: List<Int>, currentStreak: Int, bestStreak: Int) =
        HabitWithAnalytics(
            habit =
                Habit(
                    id = id,
                    title = "Habit $id",
                    description = "",
                    time = LocalDateTime(2026, 1, 1, 8, 0),
                    days = DayOfWeek.entries.toSet(),
                    index = id.toInt(),
                    reminder = false,
                ),
            consistency = 0f,
            statuses =
                checkedDays.map { HabitStatus(habitId = id, date = LocalDate(2026, 10, it)) },
            weeklyComparisonData = emptyList(),
            weekDayFrequencyData = emptyMap(),
            measurableTargets = MeasurableTargets(noTarget, noTarget, noTarget, noTarget, noTarget),
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            startedDaysAgo = 0,
        )

    @Test
    fun `combines all statistics`() {
        val habits =
            listOf(
                habit(1, checkedDays = listOf(10, 11, 13, 14), currentStreak = 2, bestStreak = 2),
                habit(2, checkedDays = listOf(12, 13, 14), currentStreak = 3, bestStreak = 3),
            )
        val activity =
            ActivityPlanner.plan(
                activeDays = (10..14).map { LocalDate(2026, 10, it) }.toSet(),
                startDate = LocalDate(2026, 10, 10),
                today = today,
            )

        val stats = ForkStats.of(habits, activity, DayOfWeek.MONDAY, today)

        assertEquals(6, stats.activeDaysPerMonth.size)
        assertEquals(5, stats.activeDaysPerMonth.last().activeDays)
        assertEquals(5, stats.energyPerMonth.last().earned)
        assertEquals(listOf("Habit 2", "Habit 1"), stats.streakOverview.map { it.title })
        assertEquals(mapOf(1L to 1, 2L to 0), stats.comebacks)
        assertEquals(1, stats.totalComebacks)
        // Monday 12 to Wednesday 14 October
        assertEquals(5, stats.checksPerWeek.last().checks)
        // habit 1: 10..14 planned, 4 checked; habit 2: 12..14 planned, 3 checked
        assertEquals(Completion(done = 7, planned = 8), stats.monthReview.current)
    }

    @Test
    fun `missing activity report leaves the energy statistics empty`() {
        val stats = ForkStats.of(emptyList(), activity = null, DayOfWeek.MONDAY, today)

        assertTrue(stats.activeDaysPerMonth.isEmpty())
        assertTrue(stats.energyPerMonth.isEmpty())
        assertEquals(0, stats.totalComebacks)
    }
}
