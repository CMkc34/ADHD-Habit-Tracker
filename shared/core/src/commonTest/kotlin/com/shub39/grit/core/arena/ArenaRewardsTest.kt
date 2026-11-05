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

import com.shub39.grit.core.activity.ActivityPlanner
import com.shub39.grit.core.activity.Reward
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate

class ArenaRewardsTest {

    private val monday = LocalDate(2026, 11, 2)
    private val tuesday = LocalDate(2026, 11, 3)

    private fun fight(
        date: LocalDate,
        opponent: Opponent,
        outcome: Outcome,
        startedAt: Long = date.toEpochDays() * 1000L,
    ) = FightRecord(date, opponent.id, outcome, startedAt)

    @Test
    fun `the first win against opponent n pays 5 times n`() {
        assertEquals(5, ArenaRewards.water(Opponent.SNOOZE_BUTTON))
        assertEquals(15, ArenaRewards.water(Opponent.DOOMSCROLL_IMP))
        assertEquals(50, ArenaRewards.water(Opponent.PROCRASTINATION_DRAGON))
    }

    @Test
    fun `a win pays on its day`() {
        val log = listOf(fight(monday, Opponent.SNOOZE_BUTTON, Outcome.WIN))

        assertEquals(listOf(Reward(monday, 5)), ArenaRewards.of(log))
    }

    @Test
    fun `losses and draws pay nothing`() {
        val log =
            listOf(
                fight(monday, Opponent.SNOOZE_BUTTON, Outcome.LOSS),
                fight(monday, Opponent.SNOOZE_BUTTON, Outcome.DRAW, startedAt = 1),
            )

        assertEquals(emptyList(), ArenaRewards.of(log))
    }

    @Test
    fun `a rematch win is not paid twice`() {
        val log =
            listOf(
                fight(monday, Opponent.SNOOZE_BUTTON, Outcome.WIN),
                fight(monday, Opponent.SNOOZE_BUTTON, Outcome.WIN, startedAt = 1),
                fight(tuesday, Opponent.SNOOZE_BUTTON, Outcome.WIN),
                fight(tuesday, Opponent.COUCH_GREMLIN, Outcome.WIN),
            )

        assertEquals(
            listOf(Reward(monday, 5), Reward(tuesday, 10)),
            ArenaRewards.of(log),
        )
    }

    @Test
    fun `a first win after a loss pays on the day of the win`() {
        val log =
            listOf(
                fight(monday, Opponent.SNOOZE_BUTTON, Outcome.LOSS),
                fight(tuesday, Opponent.SNOOZE_BUTTON, Outcome.WIN),
            )

        assertEquals(listOf(Reward(tuesday, 5)), ArenaRewards.of(log))
    }

    @Test
    fun `unknown opponents pay nothing`() {
        val log = listOf(FightRecord(monday, "boss_from_the_future", Outcome.WIN, 1))

        assertEquals(emptyList(), ArenaRewards.of(log))
    }

    @Test
    fun `the same win in a merged backup is paid once`() {
        val win = fight(monday, Opponent.SNOOZE_BUTTON, Outcome.WIN)
        val merged = FightLog.merge(listOf(win), listOf(win))

        val report =
            ActivityPlanner.plan(
                activeDays = setOf(monday),
                startDate = monday,
                today = monday,
                rewards = ArenaRewards.of(merged),
            )

        assertEquals(1 + 5, report.energy)
    }
}
