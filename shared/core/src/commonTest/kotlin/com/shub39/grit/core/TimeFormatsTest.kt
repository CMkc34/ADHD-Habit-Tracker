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
package com.shub39.grit.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.format.MonthNames

class TimeFormatsTest {
    private val german =
        MonthNames(
            listOf(
                "Jan",
                "Feb",
                "Mär",
                "Apr",
                "Mai",
                "Jun",
                "Jul",
                "Aug",
                "Sep",
                "Okt",
                "Nov",
                "Dez",
            )
        )

    @Test
    fun `dates keep the english month names by default`() {
        assertEquals("01 Jul 2026", LocalDate(2026, 7, 1).toFormattedString())
        assertEquals("Oct 2026", YearMonth(2026, 10).toFormattedString())
    }

    @Test
    fun `dates use the month names they are given`() {
        // the UI passes the names of the app's language (#18)
        assertEquals("01 Mär 2026", LocalDate(2026, 3, 1).toFormattedString(german))
        assertEquals("Okt 2026", YearMonth(2026, 10).toFormattedString(german))
    }
}
