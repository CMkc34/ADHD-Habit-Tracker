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
package com.shub39.grit.shared.ui.habit.activity

import com.shub39.grit.core.activity.ActivityPlanner
import com.shub39.grit.core.activity.DayStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate

class RecentDaysTest {

    private val today = LocalDate(2026, 10, 21)

    @Test
    fun `ends today and keeps its length before the start date`() {
        val report =
            ActivityPlanner.plan(
                activeDays = setOf(LocalDate(2026, 10, 19), LocalDate(2026, 10, 20)),
                startDate = LocalDate(2026, 10, 19),
                today = today,
            )

        val days = report.recentDays()

        assertEquals(30, days.size)
        assertEquals(LocalDate(2026, 9, 22), days.first().date)
        assertEquals(today, days.last().date)
        assertTrue(days.dropLast(3).all { it.status == null })
        assertEquals(
            listOf(DayStatus.ACTIVE, DayStatus.ACTIVE, DayStatus.OPEN),
            days.takeLast(3).map { it.status },
        )
    }

    @Test
    fun `shows only the last days of a long history`() {
        val start = LocalDate(2026, 1, 1)
        val report = ActivityPlanner.plan(activeDays = emptySet(), startDate = start, today = today)

        val days = report.recentDays(count = 7)

        assertEquals(7, days.size)
        assertEquals(LocalDate(2026, 10, 15), days.first().date)
        assertTrue(days.dropLast(1).all { it.status == DayStatus.MISSED })
    }
}
