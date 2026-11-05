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

import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.core.fighter.FighterStat
import com.shub39.grit.core.fighter.FighterState
import com.shub39.grit.core.habits.Habit
import com.shub39.grit.core.habits.HabitStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

class ArenaStateTest {

    private val today = LocalDate(2026, 11, 5)
    private val yesterday = LocalDate(2026, 11, 4)

    private val habits =
        (1L..4L).map {
            Habit(
                id = it,
                title = "Routine $it",
                description = "",
                time = LocalDateTime(2026, 10, 1, 9, 0),
                days = DayOfWeek.entries.toSet(),
                index = it.toInt(),
                reminder = false,
            )
        }

    private val fighter =
        FighterState(
            activeDays = 0,
            rank = FighterRank.ROOKIE,
            mood = FighterMood.READY,
            xp = mapOf(FighterStat.STRENGTH to 5, FighterStat.STAMINA to 15),
        )

    private fun done(vararg ids: Long, date: LocalDate = today) =
        ids.map { HabitStatus(habitId = it, date = date) }

    private fun state(fights: List<FightRecord>, statuses: List<HabitStatus> = done(1, 2)) =
        ArenaState.of(fights, habits, statuses, fighter, "Kai", today)

    private fun fight(
        opponent: Opponent,
        outcome: Outcome,
        startedAt: Long,
        date: LocalDate = today,
    ) = FightRecord(date, opponent.id, outcome, startedAt)

    private fun ArenaState.statuses() = ladder.map { it.status }

    @Test
    fun `empty log opens only the first opponent`() {
        val state = state(emptyList())

        assertEquals(listOf(LadderStatus.NEXT) + List(9) { LadderStatus.LOCKED }, state.statuses())
        assertTrue(state.canStart(Opponent.SNOOZE_BUTTON))
        assertFalse(state.canStart(Opponent.COUCH_GREMLIN))
    }

    @Test
    fun `wins move the ladder on, losses and draws don't`() {
        val state =
            state(
                listOf(
                    fight(Opponent.SNOOZE_BUTTON, Outcome.WIN, 1, date = yesterday),
                    fight(Opponent.COUCH_GREMLIN, Outcome.WIN, 2, date = yesterday),
                    fight(Opponent.DOOMSCROLL_IMP, Outcome.DRAW, 3, date = yesterday),
                    fight(Opponent.DOOMSCROLL_IMP, Outcome.LOSS, 4, date = yesterday),
                )
            )

        assertEquals(
            listOf(LadderStatus.BEATEN, LadderStatus.BEATEN, LadderStatus.NEXT) +
                List(7) { LadderStatus.LOCKED },
            state.statuses(),
        )
        assertTrue(state.canStart(Opponent.SNOOZE_BUTTON), "rematch")
        assertFalse(state.isComplete)
    }

    @Test
    fun `whole ladder beaten`() {
        val log = Opponent.entries.mapIndexed { i, it -> fight(it, Outcome.WIN, i.toLong()) }

        assertTrue(state(log).isComplete)
    }

    @Test
    fun `fights started today use today's tickets`() {
        val log =
            listOf(
                fight(Opponent.SNOOZE_BUTTON, Outcome.LOSS, 1, date = yesterday),
                fight(Opponent.SNOOZE_BUTTON, Outcome.WIN, 2),
            )

        val tickets = state(log, statuses = done(1, 2, 3)).tickets

        assertEquals(TicketStatus(earned = 3, available = 2), tickets)
    }

    @Test
    fun `a fight left in the middle still used its ticket`() {
        val running = FightLog.start(Opponent.SNOOZE_BUTTON, today, now = 7)

        val state = state(listOf(running), statuses = done(1))

        assertEquals(TicketStatus(earned = 1, available = 0), state.tickets)
        assertFalse(state.canStart(Opponent.SNOOZE_BUTTON))
        assertEquals(LadderStatus.NEXT, state.entry(Opponent.SNOOZE_BUTTON).status)
    }

    @Test
    fun `player values come from rank and stat levels`() {
        val player = state(emptyList()).player

        // rank 1, strength level 2, stamina level 3, speed and focus level 1
        assertEquals(Combatant.of("Kai", 1, 2, 1, 3, 1), player)
        assertEquals("Kai", player.name)
    }
}
