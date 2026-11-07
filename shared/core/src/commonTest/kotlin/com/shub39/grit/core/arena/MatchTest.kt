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
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MatchTest {

    private val strong = Combatant("P", maxHp = 100, attack = 200, speed = 0, focus = 0)
    private val weak = Combatant("O", maxHp = 10, attack = 1, speed = 0, focus = 0)

    /** One strike that neither misses nor crits: the strong side knocks the weak one out. */
    private fun knockout(state: MatchState) =
        Match.act(state, BattleAction.STRIKE, FixedRandom(MISS, MID, MISS))

    @Test
    fun `winning a round counts it and waits for the next one`() {
        val after = knockout(Match.start(strong, weak))
        assertEquals(1, after.playerWins)
        assertEquals(0, after.opponentWins)
        assertTrue(after.needsNextRound)
        assertNull(after.outcome)
    }

    @Test
    fun `the next round starts with full hp and no charge left over`() {
        val first = knockout(Match.start(strong, weak))
        val second = Match.nextRound(first)
        assertEquals(2, second.round)
        assertEquals(strong.maxHp, second.battle.player.hp)
        assertEquals(weak.maxHp, second.battle.opponent.hp)
        assertTrue(second.battle.log.isEmpty())
        assertFalse(second.battle.isOver)
        assertEquals(1, second.playerWins)
    }

    @Test
    fun `two round wins win the match`() {
        val won = knockout(Match.nextRound(knockout(Match.start(strong, weak))))
        assertEquals(Outcome.WIN, won.outcome)
        assertFalse(won.needsNextRound)
        assertFailsWith<IllegalStateException> { Match.nextRound(won) }
    }

    @Test
    fun `two lost rounds lose the match`() {
        var state = Match.start(weak, strong)
        state = knockout(state)
        assertEquals(1, state.opponentWins)
        state = knockout(Match.nextRound(state))
        assertEquals(Outcome.LOSS, state.outcome)
        assertEquals(2, state.round)
    }

    @Test
    fun `a time up round goes to the opponent`() {
        val tough = Combatant("P", maxHp = 1000, attack = 1, speed = 0, focus = 0)
        var state = Match.start(tough, tough.copy(name = "O"))
        while (!state.battle.isOver) {
            state = Match.act(state, BattleAction.STRIKE, FixedRandom(MISS, MID, MISS))
        }
        assertEquals(1, state.opponentWins)
        assertEquals(BattleEvent.TimeUp, state.battle.log.last())
    }
}
