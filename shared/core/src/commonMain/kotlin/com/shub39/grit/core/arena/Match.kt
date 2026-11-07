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

/**
 * A whole arena fight: best of three rounds (decided 2026-11-07). Each round is one [Battle] that
 * starts with full HP and no charge for both sides; the first to win [ArenaRules.ROUNDS_TO_WIN]
 * rounds wins the match. One ticket pays for the whole match.
 *
 * When a round ends and the match goes on, [needsNextRound] is true and the UI calls
 * [Match.nextRound] once it has shown the end of the round.
 */
data class MatchState(
    /** The round being fought, or the round that just ended. */
    val battle: BattleState,
    /** 1-based number of [battle]. */
    val round: Int = 1,
    val playerWins: Int = 0,
    val opponentWins: Int = 0,
) {
    val outcome: Outcome?
        get() =
            when {
                playerWins >= ArenaRules.ROUNDS_TO_WIN -> Outcome.WIN
                opponentWins >= ArenaRules.ROUNDS_TO_WIN -> Outcome.LOSS
                else -> null
            }

    val isOver: Boolean
        get() = outcome != null

    /** The current round is over but nobody has won the match yet. */
    val needsNextRound: Boolean
        get() = battle.isOver && !isOver

    fun wins(side: Side): Int = if (side == Side.PLAYER) playerWins else opponentWins
}

object Match {

    /** Starts the match with round 1; the CPU's opening move may already be in it. */
    fun start(player: Combatant, opponent: Combatant, random: Random = Random.Default): MatchState =
        scored(MatchState(Battle.start(player, opponent, random)))

    /** The player's [action] in the current round, see [Battle.act]. */
    fun act(state: MatchState, action: BattleAction, random: Random): MatchState =
        scored(state.copy(battle = Battle.act(state.battle, action, random)))

    /** Starts the next round with full HP for both sides. */
    fun nextRound(state: MatchState, random: Random = Random.Default): MatchState {
        check(state.needsNextRound) { "No round to start" }
        val battle =
            Battle.start(state.battle.player.combatant, state.battle.opponent.combatant, random)
        return scored(state.copy(battle = battle, round = state.round + 1))
    }

    /** Counts the round once its battle has an outcome; a draw cannot happen any more. */
    private fun scored(state: MatchState): MatchState =
        when (state.battle.outcome) {
            Outcome.WIN -> state.copy(playerWins = state.playerWins + 1)
            Outcome.LOSS,
            Outcome.DRAW -> state.copy(opponentWins = state.opponentWins + 1)
            null -> state
        }
}
