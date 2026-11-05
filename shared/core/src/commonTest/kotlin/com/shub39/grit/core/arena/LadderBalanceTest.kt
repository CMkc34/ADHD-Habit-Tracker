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

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Guards the difficulty curve (SPEC 9.5): a typical fighter for opponent n (rank n + 1, every stat
 * level n + 1, special when ready, else strike) wins 55 to 75 % of 1000 seeded fights. If a rule
 * change in [ArenaRules] breaks this, retune the [Opponent] values.
 */
class LadderBalanceTest {

    private fun typicalFighter(opponent: Opponent): Combatant {
        val level = opponent.recommendedRankNumber
        return Combatant.of(
            "typical",
            rankNumber = level,
            strengthLevel = level,
            speedLevel = level,
            staminaLevel = level,
            focusLevel = level,
        )
    }

    private fun winRate(opponent: Opponent): Double {
        val player = typicalFighter(opponent)
        val wins =
            (0 until FIGHTS).count { seed ->
                val random = Random(seed)
                var state = Battle.start(player, opponent.combatant, random)
                while (!state.isOver) {
                    val action =
                        if (state.player.specialReady) BattleAction.SPECIAL else BattleAction.STRIKE
                    state = Battle.act(state, action, random)
                }
                state.outcome == Outcome.WIN
            }
        return wins.toDouble() / FIGHTS
    }

    @Test
    fun `a typical fighter wins 55 to 75 percent against every opponent`() {
        val rates = Ladder.opponents.associateWith { winRate(it) }
        val off = rates.filterValues { it !in 0.55..0.75 }
        assertTrue(off.isEmpty(), "Win rates out of 55..75 %: $off (all: $rates)")
    }

    @Test
    fun `the final opponent needs a high rank`() {
        assertTrue(Opponent.PROCRASTINATION_DRAGON.recommendedRankNumber >= 11)
    }

    private companion object {
        const val FIGHTS = 1000
    }
}
