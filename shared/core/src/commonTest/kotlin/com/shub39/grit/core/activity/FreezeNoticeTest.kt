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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate

class FreezeNoticeTest {

    private val start = LocalDate(2026, 10, 1)

    /** Day [n] after the start date, day 0 is the start date itself. */
    private fun day(n: Int) = LocalDate.fromEpochDays(start.toEpochDays() + n)

    private fun days(range: IntRange) = range.map(::day).toSet()

    private fun notice(
        activeDays: Set<LocalDate>,
        today: Int,
        seenFreezes: Set<LocalDate> = emptySet(),
    ): FreezeNotice? =
        FreezeNotice.of(
            ActivityPlanner.plan(activeDays = activeDays, startDate = start, today = day(today)),
            seenFreezes,
        )

    @Test
    fun `no freeze means no notice`() {
        assertNull(notice(days(0..4), today = 4))
    }

    @Test
    fun `a new freeze is announced with streak and energy`() {
        val notice = assertNotNull(notice(days(0..6) + day(8), today = 8))

        assertEquals(8, notice.streak)
        assertEquals(setOf(day(7)), notice.frozenDays)
        assertEquals(7, notice.energySpent)
        assertEquals(1, notice.energyLeft)
    }

    @Test
    fun `yesterday's freeze is announced before today's check-in`() {
        val notice = assertNotNull(notice(days(0..6), today = 8))

        assertEquals(7, notice.streak)
        assertEquals(setOf(day(7)), notice.frozenDays)
        assertEquals(0, notice.energyLeft)
    }

    @Test
    fun `a seen freeze is not announced again`() {
        assertNull(notice(days(0..6) + day(8), today = 8, seenFreezes = setOf(day(7))))
    }

    @Test
    fun `several new freezes share one notice`() {
        val notice = assertNotNull(notice(days(0..13) + day(16), today = 16))

        assertEquals(setOf(day(14), day(15)), notice.frozenDays)
        assertEquals(14, notice.energySpent)
        assertEquals(1, notice.energyLeft)
    }

    @Test
    fun `only the freezes not seen yet are announced`() {
        val active = days(0..13) + days(15..20) + day(22)

        val notice = assertNotNull(notice(active, today = 22, seenFreezes = setOf(day(14))))

        assertEquals(setOf(day(21)), notice.frozenDays)
        assertEquals(7, notice.energySpent)
    }

    @Test
    fun `a broken streak gets no notice`() {
        // day 14 was frozen, but days 16 to 18 are missed and break the streak
        assertNull(notice(days(0..13) + day(15), today = 19))
    }

    @Test
    fun `freezes of an earlier streak are not announced`() {
        // day 7 was frozen, then days 9 to 11 broke that streak; a new one started on day 12
        assertNull(notice(days(0..6) + day(8) + day(12), today = 12))
    }
}
