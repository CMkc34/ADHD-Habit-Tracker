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
package com.shub39.grit.shared.ui.habit.minigame

import com.shub39.grit.core.avatar.AvatarState
import com.shub39.grit.core.minigame.MiniGame
import com.shub39.grit.core.minigame.MiniGameState
import com.shub39.grit.core.minigame.StartedRun

/**
 * The games that have a screen already. The cards of the others say "Coming soon" and start no
 * run; each game issue (#42, #43, #44) adds its game here.
 */
internal val PLAYABLE_GAMES: Set<MiniGame> = emptySet()

/** Everything the Games tab shows (SPEC 10, #41); owned by `MiniGamesViewModel`. */
data class MiniGamesPageState(
    val games: MiniGameState? = null,
    val avatar: AvatarState? = null,
    val run: RunState? = null,
)

/** A run that is being played, and its result once it is over. */
data class RunState(val started: StartedRun, val result: RunResult? = null) {
    val game: MiniGame
        get() = started.game
}

data class RunResult(val score: Int, val newBest: Boolean)

/** What the user does on the Games tab. */
sealed interface MiniGamesAction {
    /** Starts a run of [game]; uses one of today's free runs. */
    data class Start(val game: MiniGame) : MiniGamesAction

    /** The game screen reports the final [score]; it is stored at once. */
    data class Finish(val score: Int) : MiniGamesAction

    /** Back to the list, after the end screen or in the middle (the run stays used). */
    data object Close : MiniGamesAction
}
