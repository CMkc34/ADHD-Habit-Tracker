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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate

class FightLogTest {

    private val today = LocalDate(2026, 11, 5)
    private val yesterday = LocalDate(2026, 11, 4)

    private fun fight(
        opponent: Opponent,
        outcome: Outcome,
        startedAt: Long,
        date: LocalDate = today,
    ) = FightRecord(date, opponent.id, outcome, startedAt)

    @Test
    fun `only wins beat an opponent`() {
        val log =
            listOf(
                fight(Opponent.SNOOZE_BUTTON, Outcome.LOSS, 1),
                fight(Opponent.SNOOZE_BUTTON, Outcome.WIN, 2),
                fight(Opponent.COUCH_GREMLIN, Outcome.DRAW, 3),
                FightRecord(today, "future_boss", Outcome.WIN, 4),
            )

        assertEquals(setOf(Opponent.SNOOZE_BUTTON), FightLog.beaten(log))
    }

    @Test
    fun `every fight started today uses a ticket, whatever its outcome`() {
        val log =
            listOf(
                fight(Opponent.SNOOZE_BUTTON, Outcome.WIN, 1, date = yesterday),
                fight(Opponent.SNOOZE_BUTTON, Outcome.LOSS, 2),
                fight(Opponent.SNOOZE_BUTTON, Outcome.LOSS, 3),
                fight(Opponent.COUCH_GREMLIN, Outcome.WIN, 4),
            )

        assertEquals(3, FightLog.fightsStartedOn(log, today))
        assertEquals(1, FightLog.fightsStartedOn(log, yesterday))
    }

    @Test
    fun `a started fight is a loss until it is finished`() {
        val started = FightLog.start(Opponent.SNOOZE_BUTTON, today, now = 10)

        assertEquals(Outcome.LOSS, started.outcome)
        assertEquals(emptySet(), FightLog.beaten(listOf(started)))
        assertEquals(1, FightLog.fightsStartedOn(listOf(started), today))
    }

    @Test
    fun `a new fight starts after every logged one`() {
        val log = listOf(fight(Opponent.SNOOZE_BUTTON, Outcome.LOSS, 50))

        assertEquals(51, FightLog.start(Opponent.SNOOZE_BUTTON, today, now = 50, log).startedAt)
        assertEquals(80, FightLog.start(Opponent.SNOOZE_BUTTON, today, now = 80, log).startedAt)
    }

    @Test
    fun `finishing updates only that fight`() {
        val first = fight(Opponent.SNOOZE_BUTTON, Outcome.LOSS, 1)
        val running = fight(Opponent.SNOOZE_BUTTON, Outcome.LOSS, 2)

        val log = FightLog.finish(listOf(first, running), running, Outcome.WIN)

        assertEquals(listOf(first, running.copy(outcome = Outcome.WIN)), log)
        assertEquals(2, FightLog.fightsStartedOn(log, today))
    }

    @Test
    fun `merging keeps every fight of both logs once`() {
        val shared = fight(Opponent.SNOOZE_BUTTON, Outcome.WIN, 1, date = yesterday)
        val device = listOf(shared, fight(Opponent.COUCH_GREMLIN, Outcome.LOSS, 3))
        val backup = listOf(shared, fight(Opponent.COUCH_GREMLIN, Outcome.WIN, 2))

        val merged = FightLog.merge(device, backup)

        assertEquals(
            listOf(
                shared,
                fight(Opponent.COUCH_GREMLIN, Outcome.WIN, 2),
                fight(Opponent.COUCH_GREMLIN, Outcome.LOSS, 3),
            ),
            merged,
        )
        assertEquals(setOf(Opponent.SNOOZE_BUTTON, Opponent.COUCH_GREMLIN), FightLog.beaten(merged))
    }

    @Test
    fun `merging keeps the better outcome of the same fight`() {
        val duringFight = fight(Opponent.SNOOZE_BUTTON, Outcome.LOSS, 5)
        val afterFight = duringFight.copy(outcome = Outcome.WIN)

        assertEquals(listOf(afterFight), FightLog.merge(listOf(duringFight), listOf(afterFight)))
        assertEquals(listOf(afterFight), FightLog.merge(listOf(afterFight), listOf(duringFight)))
    }

    @Test
    fun `merging with an empty log changes nothing`() {
        val log = listOf(fight(Opponent.SNOOZE_BUTTON, Outcome.WIN, 1))

        assertEquals(log, FightLog.merge(log, emptyList()))
        assertEquals(log, FightLog.merge(emptyList(), log))
    }
}
