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

import com.shub39.grit.core.fighter.isDoneBy
import com.shub39.grit.core.habits.Habit
import com.shub39.grit.core.habits.HabitStatus
import kotlinx.datetime.LocalDate

/** Today's fight tickets. */
data class TicketStatus(
    /** Tickets earned today, 0 to [ArenaRules.TICKETS_PER_DAY]. */
    val earned: Int,
    /** Tickets left to start a fight with. Never negative. */
    val available: Int,
) {
    /** Show "Check off one more routine for a ticket" while today can still earn more. */
    val showNextTicketHint: Boolean
        get() = earned < ArenaRules.TICKETS_PER_DAY
}

/**
 * Daily fight tickets (SPEC 9.6). Each routine done for today earns one ticket, at most
 * [ArenaRules.TICKETS_PER_DAY]. A fight costs its ticket when it starts. Tickets are computed,
 * never stored, so unused ones expire at midnight on their own.
 */
object FightTickets {

    /**
     * Distinct routines done for [today], capped. "Done" follows the XP rule ([isDoneBy]), so a
     * measurable routine below its target earns nothing, and check-ins for other dates (also ones
     * backfilled today) don't count. Check-ins of deleted routines don't count either.
     */
    fun earned(habits: List<Habit>, statuses: List<HabitStatus>, today: LocalDate): Int {
        val habitsById = habits.associateBy { it.id }
        val done =
            statuses
                .filter { it.date == today && habitsById[it.habitId]?.isDoneBy(it) == true }
                .distinctBy { it.habitId }
                .size
        return minOf(done, ArenaRules.TICKETS_PER_DAY)
    }

    /**
     * Tickets left after [fightsStartedToday]. Unchecking a routine can lower [earned] below the
     * fights already started; the result then stays at 0.
     */
    fun available(earned: Int, fightsStartedToday: Int): Int =
        (earned - fightsStartedToday).coerceAtLeast(0)

    fun status(
        habits: List<Habit>,
        statuses: List<HabitStatus>,
        today: LocalDate,
        fightsStartedToday: Int,
    ): TicketStatus {
        val earned = earned(habits, statuses, today)
        return TicketStatus(earned = earned, available = available(earned, fightsStartedToday))
    }
}
