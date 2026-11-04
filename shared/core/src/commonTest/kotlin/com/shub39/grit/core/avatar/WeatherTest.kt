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
package com.shub39.grit.core.avatar

import com.shub39.grit.core.activity.ActivityPlanner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate

class WeatherTest {

    private val start = LocalDate(2026, 1, 1)

    private fun day(n: Int) = LocalDate.fromEpochDays(start.toEpochDays() + n)

    private fun weather(activeDays: Iterable<Int>, today: Int) =
        Weather.of(
            ActivityPlanner.plan(
                activeDays = activeDays.map(::day).toSet(),
                startDate = start,
                today = day(today),
            )
        )

    @Test
    fun `the sun shines more the longer the streak runs`() {
        assertEquals(Weather.HAZY, weather(0..1, today = 2))
        assertEquals(Weather.FAIR, weather(0..2, today = 3))
        assertEquals(Weather.FAIR, weather(0..5, today = 6))
        assertEquals(Weather.SUNNY, weather(0..6, today = 7))
    }

    @Test
    fun `nothing done yet peeks the sun out`() {
        assertEquals(Weather.HAZY, weather(emptyList(), today = 0))
    }

    @Test
    fun `a frozen day keeps the sun`() {
        // eight active days pay for the freeze of day 8
        assertEquals(Weather.SUNNY, weather(0..7, today = 9))
    }

    @Test
    fun `the longer the user stays away the cloudier it gets`() {
        // three active days are too little energy for a freeze, so the streak breaks
        assertEquals(Weather.CLOUDY, weather(0..2, today = 4))
        assertEquals(Weather.CLOUDY, weather(0..2, today = 9))
        assertEquals(Weather.OVERCAST, weather(0..2, today = 10))
        assertEquals(Weather.OVERCAST, weather(0..2, today = 40))
    }

    @Test
    fun `inactive days skip today and stop at the last check-in`() {
        val report =
            ActivityPlanner.plan(
                activeDays = (0..2).map(::day).toSet(),
                startDate = start,
                today = day(6),
            )
        assertEquals(3, Weather.inactiveDays(report))
    }
}
