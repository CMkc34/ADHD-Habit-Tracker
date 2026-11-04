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

import com.shub39.grit.core.activity.ActivityReport
import com.shub39.grit.core.activity.DayStatus
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/** One cell of the energy page's day strip; [status] is `null` before the start date. */
data class RecentDay(val date: LocalDate, val status: DayStatus?)

/**
 * The last [count] days up to today (the report's last day), oldest first. Days before the start
 * date have no status, so the strip keeps its length on a fresh install.
 */
fun ActivityReport.recentDays(count: Int = 30): List<RecentDay> {
    val today = days.keys.lastOrNull() ?: return emptyList()
    return (count - 1 downTo 0).map { daysAgo ->
        val date = today.minus(daysAgo, DateTimeUnit.DAY)
        RecentDay(date, days[date])
    }
}
