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

class ActivityPlannerTest {

    private val start = LocalDate(2026, 10, 1)

    /** Day [n] after the start date, day 0 is the start date itself. */
    private fun day(n: Int) = LocalDate.fromEpochDays(start.toEpochDays() + n)

    @Test
    fun `no active days means no streak and no energy`() {
        val report =
            ActivityPlanner.plan(activeDays = emptySet(), startDate = start, today = day(5))

        assertEquals(0, report.currentStreak)
        assertEquals(0, report.energy)
    }

    @Test
    fun `consecutive active days build streak and energy`() {
        val report =
            ActivityPlanner.plan(
                activeDays = (0..4).map(::day).toSet(),
                startDate = start,
                today = day(4),
            )

        assertEquals(5, report.currentStreak)
        assertEquals(5, report.energy)
    }

    @Test
    fun `streak survives today until the day is over`() {
        val report =
            ActivityPlanner.plan(
                activeDays = (0..2).map(::day).toSet(),
                startDate = start,
                today = day(3),
            )

        assertEquals(3, report.currentStreak)
    }

    @Test
    fun `a missed day is frozen when there is enough energy`() {
        val active = (0..6).map(::day).toSet() + day(8)

        val report = ActivityPlanner.plan(activeDays = active, startDate = start, today = day(8))

        assertEquals(setOf(day(7)), report.frozenDays)
        assertEquals(8, report.currentStreak) // the frozen day keeps the streak but does not count
        assertEquals(7 - 7 + 1, report.energy)
    }

    @Test
    fun `without enough energy the streak breaks but the energy stays`() {
        val active = (0..3).map(::day).toSet() + day(5)

        val report = ActivityPlanner.plan(activeDays = active, startDate = start, today = day(5))

        assertEquals(1, report.currentStreak)
        assertEquals(5, report.energy)
        assertEquals(emptySet(), report.frozenDays)
    }

    @Test
    fun `three missed days break the streak and cost no energy`() {
        val active = (0..20).map(::day).toSet() + day(24)

        val report = ActivityPlanner.plan(activeDays = active, startDate = start, today = day(24))

        assertEquals(1, report.currentStreak)
        assertEquals(emptySet(), report.frozenDays)
        assertEquals(22, report.energy)
    }

    @Test
    fun `two missed days in a row are frozen`() {
        val active = (0..13).map(::day).toSet() + day(16)

        val report = ActivityPlanner.plan(activeDays = active, startDate = start, today = day(16))

        assertEquals(setOf(day(14), day(15)), report.frozenDays)
        assertEquals(15, report.currentStreak)
        assertEquals(14 - 14 + 1, report.energy)
    }

    @Test
    fun `longest streak survives a break`() {
        val active = (0..5).map(::day).toSet() + (10..11).map(::day)

        val report = ActivityPlanner.plan(activeDays = active, startDate = start, today = day(11))

        assertEquals(2, report.currentStreak)
        assertEquals(6, report.longestStreak)
    }

    @Test
    fun `days before the start date do not count`() {
        val active = setOf(day(-3), day(-2), day(-1), day(0))

        val report = ActivityPlanner.plan(activeDays = active, startDate = start, today = day(0))

        assertEquals(1, report.currentStreak)
        assertEquals(1, report.energy)
    }

    @Test
    fun `every day from the start date gets a status`() {
        val active = (0..6).map(::day).toSet() + day(8) + day(11)

        val report = ActivityPlanner.plan(activeDays = active, startDate = start, today = day(12))

        assertEquals(DayStatus.ACTIVE, report.days.getValue(day(0)))
        assertEquals(DayStatus.FROZEN, report.days.getValue(day(7)))
        assertEquals(DayStatus.MISSED, report.days.getValue(day(9)))
        assertEquals(DayStatus.MISSED, report.days.getValue(day(10)))
        assertEquals(DayStatus.ACTIVE, report.days.getValue(day(11)))
        assertEquals(DayStatus.OPEN, report.days.getValue(day(12)))
        assertEquals(13, report.days.size)
    }

    @Test
    fun `a bonus day earns one more energy`() {
        val report =
            ActivityPlanner.plan(
                activeDays = (0..2).map(::day).toSet(),
                startDate = start,
                today = day(2),
                bonusDays = setOf(day(1)),
            )

        assertEquals(4, report.energy)
        assertEquals(setOf(day(1)), report.bonusDays)
    }

    @Test
    fun `a bonus day without activity earns nothing`() {
        val report =
            ActivityPlanner.plan(
                activeDays = setOf(day(0), day(2)),
                startDate = start,
                today = day(2),
                bonusDays = setOf(day(1)),
            )

        assertEquals(2, report.energy)
        assertEquals(emptySet(), report.bonusDays)
    }
}
