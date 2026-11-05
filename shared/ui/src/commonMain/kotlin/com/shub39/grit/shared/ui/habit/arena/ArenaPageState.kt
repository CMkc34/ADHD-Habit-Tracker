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
package com.shub39.grit.shared.ui.habit.arena

import com.shub39.grit.core.arena.ArenaRewards
import com.shub39.grit.core.arena.ArenaState
import com.shub39.grit.core.arena.BattleAction
import com.shub39.grit.core.arena.BattleEvent
import com.shub39.grit.core.arena.BattleState
import com.shub39.grit.core.arena.FightPlayback
import com.shub39.grit.core.arena.LadderStatus
import com.shub39.grit.core.arena.Opponent
import com.shub39.grit.core.arena.Outcome
import com.shub39.grit.core.arena.Side
import com.shub39.grit.core.arena.StartedFight
import com.shub39.grit.core.avatar.AvatarState

/** Everything the Arena tab and the fight screen show (#14); owned by `ArenaViewModel`. */
data class ArenaPageState(
    /** Tickets, ladder and the player's values; `null` while loading. */
    val arena: ArenaState? = null,
    /** The fighter's look; `null` while loading. */
    val avatar: AvatarState? = null,
    /** Opponent whose pre-fight sheet is open. */
    val selected: Opponent? = null,
    /** The running fight, shown full screen. */
    val fight: FightState? = null,
)

/** A running fight as the battle screen shows it. */
data class FightState(
    val started: StartedFight,
    val playback: FightPlayback,
    /** Whether [opponent] was the next one on the ladder, so a win unlocks the following one. */
    val wasNext: Boolean,
    val stage: ArenaStage,
    val lighting: ArenaLighting,
    /** Indices in [BattleState.log] of the player's strikes that are shown as a kick. */
    val kicks: Set<Int> = emptySet(),
    /** "Give up? This counts as a loss." is open. */
    val askingGiveUp: Boolean = false,
) {
    val opponent: Opponent
        get() = started.opponent

    /** The engine's state after the last move. */
    val battle: BattleState
        get() = playback.after

    /** The player may pick a move: nothing is playing and the fight goes on. */
    val canAct: Boolean
        get() = playback.isDone && !battle.isOver && !askingGiveUp

    val canSpecial: Boolean
        get() = canAct && battle.player.specialReady

    /** The last few events for the log box, oldest first. */
    val logLines: List<BattleEvent>
        get() = playback.shownLog.takeLast(LOG_LINES)

    /** Whether the event at [index] in the log is a player strike shown as a kick. */
    fun isKick(index: Int): Boolean = index in kicks

    /** The result dialog, once the last move has played. */
    val result: FightResult?
        get() {
            val outcome = battle.outcome ?: return null
            if (!playback.isDone) return null
            val firstWin = outcome == Outcome.WIN && wasNext
            return FightResult(
                outcome = outcome,
                unlocked = if (firstWin) opponent.next else null,
                shakes = if (firstWin) ArenaRewards.shakes(opponent) else 0,
            )
        }

    fun hp(side: Side): Int = playback.hp(side)

    fun charge(side: Side): Int = playback.charge(side)

    companion object {
        const val LOG_LINES = 3

        fun wasNext(arena: ArenaState, opponent: Opponent): Boolean =
            arena.entry(opponent).status == LadderStatus.NEXT
    }
}

/**
 * How a fight ended; [unlocked] is the opponent a win opened up, if any, and [shakes] what the
 * first win against this opponent paid (#17), 0 otherwise.
 */
data class FightResult(val outcome: Outcome, val unlocked: Opponent?, val shakes: Int = 0)

/** What the user does on the Arena tab and in a fight. */
sealed interface ArenaAction {
    /** Opens the pre-fight sheet for [opponent]. */
    data class Select(val opponent: Opponent) : ArenaAction

    data object CloseSheet : ArenaAction

    /** Starts a fight against the selected opponent; uses one ticket. */
    data object StartFight : ArenaAction

    /** The player's move. */
    data class Act(val action: BattleAction) : ArenaAction

    /** The UI finished animating the event at [index] in the battle log. */
    data class EventPlayed(val index: Int) : ArenaAction

    /** Back during a fight: asks first. */
    data object AskGiveUp : ArenaAction

    data object KeepFighting : ArenaAction

    /** Leaves the fight; it stays stored as a loss. */
    data object GiveUp : ArenaAction

    /** Closes the result dialog and returns to the tab. */
    data object CloseFight : ArenaAction
}
