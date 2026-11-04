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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate

class ActivityDataTest {

    private fun day(n: Int) = LocalDate(2026, 10, n)

    @Test
    fun `merging keeps the earlier start date and joins the days`() {
        val current = ActivityData(day(21), setOf(day(21)), emptySet())
        val backup = ActivityData(day(1), setOf(day(1), day(2), day(20)), setOf(day(10)))

        assertEquals(
            ActivityData(day(1), setOf(day(1), day(2), day(20), day(21)), setOf(day(10))),
            current.mergedWith(backup),
        )
    }

    @Test
    fun `merging with empty data changes nothing`() {
        val data = ActivityData(day(5), setOf(day(5), day(6)), setOf(day(7)))

        assertEquals(data, data.mergedWith(ActivityData()))
        assertEquals(data, ActivityData().mergedWith(data))
    }
}
