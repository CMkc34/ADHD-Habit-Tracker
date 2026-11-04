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
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate

class AutoFreezeTest {

    private val start = LocalDate(2026, 1, 1)

    private fun day(n: Int) = LocalDate.fromEpochDays(start.toEpochDays() + n)

    // 14 active days earn enough energy for two freezes
    private val active = (0..13).toList()

    private fun plan(active: List<Int>, today: Int, switches: AutoFreezeSwitches) =
        ActivityPlanner.plan(
            activeDays = active.map(::day).toSet(),
            startDate = start,
            today = day(today),
            autoFreeze = switches,
        )

    @Test
    fun `switches count from their day on`() {
        val switches = AutoFreezeSwitches().switched(day(5), false).switched(day(9), true)

        assertTrue(switches.isOn(day(4)))
        assertFalse(switches.isOn(day(5)))
        assertFalse(switches.isOn(day(8)))
        assertTrue(switches.isOn(day(9)))
        assertTrue(switches.enabled)
        assertFalse(switches.switched(day(9), false).enabled)
    }

    @Test
    fun `without switches freezes stay on`() {
        val report = plan(active + 15, today = 15, switches = AutoFreezeSwitches())

        assertEquals(DayStatus.FROZEN, report.days[day(14)])
        assertTrue(report.autoFreeze)
    }

    @Test
    fun `switched off, a gap breaks the streak and keeps the energy`() {
        val report =
            plan(active + 15, today = 15, switches = AutoFreezeSwitches().switched(day(14), false))

        assertEquals(DayStatus.MISSED, report.days[day(14)])
        assertEquals(1, report.currentStreak)
        assertEquals(15, report.energy)
        assertFalse(report.autoFreeze)
    }

    @Test
    fun `switching off keeps earlier freezes`() {
        // day 14 was frozen before the switch on day 15
        val report =
            plan(active + 15, today = 16, switches = AutoFreezeSwitches().switched(day(15), false))

        assertEquals(DayStatus.FROZEN, report.days[day(14)])
        assertEquals(15, report.currentStreak)
    }

    @Test
    fun `a gap from a time switched off is not frozen later`() {
        val switches = AutoFreezeSwitches().switched(day(14), false).switched(day(15), true)
        val report = plan(active + 15, today = 15, switches = switches)

        assertEquals(DayStatus.MISSED, report.days[day(14)])
        assertTrue(report.autoFreeze)
    }

    @Test
    fun `merging keeps both records and prefers this one`() {
        val here = AutoFreezeSwitches().switched(day(3), false)
        val backup = AutoFreezeSwitches().switched(day(3), true).switched(day(1), false)

        assertEquals(mapOf(day(1) to false, day(3) to false), here.mergedWith(backup).changes)
    }
}
