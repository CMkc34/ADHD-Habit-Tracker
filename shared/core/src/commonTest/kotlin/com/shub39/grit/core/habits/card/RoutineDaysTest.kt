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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

class RoutineDaysTest {

    // Monday
    private val today = LocalDate(2026, 10, 26)

    private fun habit(
        start: LocalDate = LocalDate(2026, 1, 1),
        days: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
        target: Double = 0.0,
    ) =
        Habit(
            id = 1,
            title = "Read",
            description = "",
            time = LocalDateTime(start, LocalTime(8, 0)),
            days = days,
            index = 0,
            reminder = false,
            targetValue = target,
            targetUnit = if (target > 0) "pages" else "",
        )

    private fun status(day: Int, value: Double = 1.0) =
        HabitStatus(habitId = 1, date = LocalDate(2026, 10, day), value = value)

    @Test
    fun `fourteen days oldest first with today last`() {
        val days = RoutineDays.lastDays(habit(), emptyList(), today)

        assertEquals(14, days.size)
        assertEquals(LocalDate(2026, 10, 13), days.first().date)
        assertEquals(today, days.last().date)
        assertTrue(days.last().isToday)
        assertEquals(1, days.count { it.isToday })
    }

    @Test
    fun `done and open days`() {
        val days = RoutineDays.lastDays(habit(), listOf(status(25), status(26)), today)

        assertEquals(RoutineDayState.DONE, days[13].state)
        assertEquals(RoutineDayState.DONE, days[12].state)
        assertEquals(RoutineDayState.OPEN, days[11].state)
        assertEquals(1f, days[13].progress)
        assertTrue(days.all { it.tappable })
    }

    @Test
    fun `measurable routine shows partial progress`() {
        val days =
            RoutineDays.lastDays(
                habit(target = 10.0),
                listOf(status(24, 4.0), status(25, 10.0), status(26, 12.0)),
                today,
            )

        assertEquals(RoutineDayState.PARTIAL, days[11].state)
        assertEquals(0.4f, days[11].progress)
        assertEquals(4.0, days[11].value)
        assertEquals(RoutineDayState.DONE, days[12].state)
        assertEquals(RoutineDayState.DONE, days[13].state)
        assertEquals(1f, days[13].progress)
    }

    @Test
    fun `days off are not planned and not tappable`() {
        val weekdays = DayOfWeek.entries.toSet() - DayOfWeek.SATURDAY - DayOfWeek.SUNDAY
        val days = RoutineDays.lastDays(habit(days = weekdays), emptyList(), today)

        // 24 and 25 October 2026 are Saturday and Sunday
        assertEquals(RoutineDayState.NOT_PLANNED, days[11].state)
        assertEquals(RoutineDayState.NOT_PLANNED, days[12].state)
        assertFalse(days[11].tappable)
        assertEquals(RoutineDayState.OPEN, days[13].state)
    }

    @Test
    fun `days before the routine was created`() {
        val days = RoutineDays.lastDays(habit(start = LocalDate(2026, 10, 23)), emptyList(), today)

        assertEquals(10, days.count { it.state == RoutineDayState.BEFORE_START })
        assertFalse(days[9].tappable)
        assertEquals(RoutineDayState.OPEN, days[10].state)
        assertTrue(days[10].tappable)
    }

    @Test
    fun `window moves on by one day each day`() {
        val tomorrow = LocalDate(2026, 10, 27)
        val days = RoutineDays.lastDays(habit(), listOf(status(26)), tomorrow)

        assertEquals(LocalDate(2026, 10, 14), days.first().date)
        assertEquals(tomorrow, days.last().date)
        assertTrue(days.last().isToday)
        assertTrue(days.last().tappable)
        assertEquals(RoutineDayState.DONE, days[12].state)
    }
}
