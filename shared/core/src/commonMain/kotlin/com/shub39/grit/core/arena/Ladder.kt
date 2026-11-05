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
 * The ladder of [Opponent]s (SPEC 9.5). The first opponent is open from the start; every further
 * one opens after the first win against the one below. Beaten opponents can be fought again.
 *
 * Takes the set of opponents beaten at least once, so it doesn't depend on how fights are stored.
 */
object Ladder {
    val opponents: List<Opponent>
        get() = Opponent.entries

    fun isUnlocked(opponent: Opponent, beaten: Set<Opponent>): Boolean {
        val previous = opponent.previous ?: return true
        return previous in beaten
    }

    /** All opponents the player may fight, in ladder order. */
    fun unlocked(beaten: Set<Opponent>): List<Opponent> =
        opponents.filter { isUnlocked(it, beaten) }

    /** The lowest unlocked opponent not beaten yet, or null once the whole ladder is beaten. */
    fun nextChallenge(beaten: Set<Opponent>): Opponent? =
        opponents.firstOrNull { it !in beaten && isUnlocked(it, beaten) }
}
