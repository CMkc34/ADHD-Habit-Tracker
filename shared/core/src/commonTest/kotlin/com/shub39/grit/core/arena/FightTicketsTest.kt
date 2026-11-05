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
package com.shub39.grit.core.arena

import com.shub39.grit.core.habits.Habit
import com.shub39.grit.core.habits.HabitStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

class FightTicketsTest {

    private val today = LocalDate(2026, 11, 5)
    private val yesterday = LocalDate(2026, 11, 4)

    private val habits = (1L..5L).map { habit(it) }
    private val water = habit(9, target = 8.0)

    private fun habit(id: Long, target: Double = 0.0) =
        Habit(
            id = id,
            title = "Routine $id",
            description = "",
            time = LocalDateTime(2026, 10, 1, 9, 0),
            days = DayOfWeek.entries.toSet(),
            index = id.toInt(),
            reminder = false,
            targetValue = target,
        )

    private fun done(vararg ids: Long, date: LocalDate = today) =
        ids.map { HabitStatus(habitId = it, date = date) }

    private fun earned(statuses: List<HabitStatus>, all: List<Habit> = habits + water) =
        FightTickets.earned(all, statuses, today)

    @Test
    fun `one ticket per routine done today`() {
        assertEquals(0, earned(emptyList()))
        assertEquals(1, earned(done(1)))
        assertEquals(2, earned(done(1, 2)))
    }

    @Test
    fun `at most three tickets a day`() {
        assertEquals(3, earned(done(1, 2, 3)))
        assertEquals(3, earned(done(1, 2, 3, 4, 5)))
    }

    @Test
    fun `the same routine twice earns one ticket`() {
        assertEquals(1, earned(done(1, 1)))
    }

    @Test
    fun `measurable routine below target earns no ticket`() {
        val below = listOf(HabitStatus(habitId = 9, date = today, value = 7.5))
        val reached = listOf(HabitStatus(habitId = 9, date = today, value = 8.0))
        assertEquals(0, earned(below))
        assertEquals(1, earned(reached))
    }

    @Test
    fun `check-ins for yesterday do not earn today's tickets`() {
        // Backfilling yesterday today still stores yesterday's date.
        assertEquals(0, earned(done(1, 2, 3, date = yesterday)))
        assertEquals(1, earned(done(1, 2, date = yesterday) + done(3)))
    }

    @Test
    fun `deleted routines earn nothing`() {
        assertEquals(0, earned(done(1, 2), all = listOf(habit(7))))
    }

    @Test
    fun `fights started use up tickets`() {
        assertEquals(3, FightTickets.available(earned = 3, fightsStartedToday = 0))
        assertEquals(1, FightTickets.available(earned = 3, fightsStartedToday = 2))
        assertEquals(0, FightTickets.available(earned = 3, fightsStartedToday = 3))
    }

    @Test
    fun `tickets never go negative after unchecking`() {
        // Two fights started with two tickets, then one routine is unchecked.
        val afterUncheck = earned(done(1))
        assertEquals(0, FightTickets.available(afterUncheck, fightsStartedToday = 2))
        assertEquals(0, FightTickets.available(earned = 0, fightsStartedToday = 5))
    }

    @Test
    fun `status combines earned, available and the hint`() {
        val status = FightTickets.status(habits, done(1, 2), today, fightsStartedToday = 1)
        assertEquals(TicketStatus(earned = 2, available = 1), status)
        assertTrue(status.showNextTicketHint)

        val full = FightTickets.status(habits, done(1, 2, 3, 4), today, fightsStartedToday = 0)
        assertEquals(TicketStatus(earned = 3, available = 3), full)
        assertFalse(full.showNextTicketHint)
    }
}
