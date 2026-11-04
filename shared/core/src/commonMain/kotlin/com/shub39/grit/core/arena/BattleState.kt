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

enum class BattleAction {
    STRIKE,
    GUARD,
    SPECIAL,
}

enum class Side {
    PLAYER,
    OPPONENT;

    val other: Side
        get() = if (this == PLAYER) OPPONENT else PLAYER
}

/** Result from the player's point of view. */
enum class Outcome {
    WIN,
    LOSS,
    DRAW,
}

/** What happened during a turn, in order, for the UI animation and text. */
sealed interface BattleEvent {
    val actor: Side

    /** A STRIKE landed. [damage] is after crit and guard. */
    data class Hit(
        override val actor: Side,
        val damage: Int,
        val critical: Boolean,
        val guarded: Boolean,
    ) : BattleEvent

    /** The defender dodged [actor]'s STRIKE. */
    data class Dodge(override val actor: Side) : BattleEvent

    data class Guard(override val actor: Side) : BattleEvent

    data class Special(override val actor: Side, val damage: Int) : BattleEvent

    /** [actor] knocked out the other side. */
    data class Ko(override val actor: Side) : BattleEvent

    /** The round limit was reached; the outcome is decided by remaining HP share. */
    data object TimeUp : BattleEvent {
        override val actor: Side = Side.PLAYER
    }
}

/** One side's live values during a fight. */
data class Corner(
    val combatant: Combatant,
    val hp: Int,
    val charge: Int = 0,
    val guarding: Boolean = false,
) {
    val specialReady: Boolean
        get() = charge >= ArenaRules.SPECIAL_COST
}

/**
 * Immutable snapshot of a fight. Whenever the engine hands a state back without an [outcome], it is
 * the player's turn: their charge for this turn is already added and their guard has ended.
 */
data class BattleState(
    val player: Corner,
    val opponent: Corner,
    /** Turns taken by both sides together. */
    val turnsTaken: Int = 0,
    val outcome: Outcome? = null,
    val log: List<BattleEvent> = emptyList(),
) {
    /** 1-based round; a round is one turn of each side. */
    val round: Int
        get() = minOf(turnsTaken / 2 + 1, ArenaRules.MAX_ROUNDS)

    val isOver: Boolean
        get() = outcome != null

    fun corner(side: Side): Corner = if (side == Side.PLAYER) player else opponent

    internal fun withCorner(side: Side, corner: Corner): BattleState =
        if (side == Side.PLAYER) copy(player = corner) else copy(opponent = corner)
}
