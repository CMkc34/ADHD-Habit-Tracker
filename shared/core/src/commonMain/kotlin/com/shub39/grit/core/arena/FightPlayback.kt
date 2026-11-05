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

/**
 * Plays the [BattleEvent]s of one exchange one at a time, for the arena animation (#14).
 *
 * [Battle] resolves the player's move and the CPU's reply in one go. The arena screen shows them
 * one after the other: [playing] is the event being animated, and the HP, charge and log shown on
 * screen include it. Once every event has been played ([isDone]) the screen shows [after].
 */
data class FightPlayback(
    /** What the screen showed before this exchange. */
    val before: BattleState,
    /** The engine's state after this exchange. */
    val after: BattleState,
    /** How many of the new [events] have finished playing. */
    val played: Int = 0,
) {
    /** The events of this exchange, in order. */
    val events: List<BattleEvent>
        get() = after.log.drop(before.log.size)

    /** Index of [playing] in [BattleState.log], or -1 when [isDone]. */
    val playingIndex: Int
        get() = if (isDone) -1 else before.log.size + played

    /** The event being animated now, `null` when all are done. */
    val playing: BattleEvent?
        get() = events.getOrNull(played)

    val isDone: Boolean
        get() = played >= events.size

    /** The events the screen has shown so far, [playing] included. */
    val shownLog: List<BattleEvent>
        get() = after.log.take(before.log.size + minOf(played + 1, events.size))

    /** Moves on after [playing] has been animated. */
    fun next(): FightPlayback = copy(played = minOf(played + 1, events.size))

    /** HP of [side] as shown now; it drops as soon as the hit starts playing. */
    fun hp(side: Side): Int {
        if (isDone) return after.corner(side).hp
        val taken = shownNew().sumOf { if (it.actor == side.other) it.damageDealt else 0 }
        return (before.corner(side).hp - taken).coerceAtLeast(0)
    }

    /** Charge of [side] as shown now: a played special empties it, the rest shows at the end. */
    fun charge(side: Side): Int {
        if (isDone) return after.corner(side).charge
        val usedSpecial = shownNew().any { it is BattleEvent.Special && it.actor == side }
        return if (usedSpecial) 0 else before.corner(side).charge
    }

    private fun shownNew(): List<BattleEvent> = events.take(played + 1)

    companion object {
        /** The opening of a fight: [started] may already hold the CPU's first move. */
        fun opening(started: BattleState): FightPlayback =
            FightPlayback(
                before =
                    BattleState(
                        player = Corner(started.player.combatant, started.player.combatant.maxHp),
                        opponent =
                            Corner(started.opponent.combatant, started.opponent.combatant.maxHp),
                    ),
                after = started,
            )
    }
}

/** Damage this event deals to the other side. */
val BattleEvent.damageDealt: Int
    get() =
        when (this) {
            is BattleEvent.Hit -> damage
            is BattleEvent.Special -> damage
            else -> 0
        }
