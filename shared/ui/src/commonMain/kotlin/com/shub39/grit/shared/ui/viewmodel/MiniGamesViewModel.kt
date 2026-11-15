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
package com.shub39.grit.shared.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shub39.grit.core.avatar.AvatarRepo
import com.shub39.grit.core.minigame.MiniGame
import com.shub39.grit.core.minigame.MiniGameRepo
import com.shub39.grit.shared.ui.habit.minigame.MiniGamesAction
import com.shub39.grit.shared.ui.habit.minigame.MiniGamesPageState
import com.shub39.grit.shared.ui.habit.minigame.PLAYABLE_GAMES
import com.shub39.grit.shared.ui.habit.minigame.RunResult
import com.shub39.grit.shared.ui.habit.minigame.RunState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

/**
 * Owns the Games tab (SPEC 10, #41): the cards with runs left and best scores, and the run that is
 * being played. A run uses one of the day's free runs when it starts; its score is stored when the
 * game screen reports it. The games themselves are pure simulations driven by their screens.
 */
@KoinViewModel
class MiniGamesViewModel(
    @Provided private val miniGameRepo: MiniGameRepo,
    @Provided private val avatarRepo: AvatarRepo,
) : ViewModel() {

    /** The games that can be started; tests open all of them. */
    internal var playable: Set<MiniGame> = PLAYABLE_GAMES

    private var gamesJob: Job? = null
    private var avatarJob: Job? = null

    // set while a run is being started or finished, so two quick taps count once
    private var busy = false

    private val _state = MutableStateFlow(MiniGamesPageState())

    val state: StateFlow<MiniGamesPageState> =
        _state
            .asStateFlow()
            .onStart {
                observeGames()
                observeAvatar()
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MiniGamesPageState())

    fun onAction(action: MiniGamesAction) {
        when (action) {
            is MiniGamesAction.Start -> start(action.game)
            is MiniGamesAction.Finish -> finish(action.score)
            MiniGamesAction.Close -> _state.update { it.copy(run = null) }
        }
    }

    private fun start(game: MiniGame) {
        val current = _state.value
        val games = current.games ?: return
        if (busy || current.run != null || game !in playable || !games.canStart(game)) return
        busy = true
        viewModelScope.launch {
            try {
                val started = miniGameRepo.startRun(game) ?: return@launch
                _state.update { it.copy(run = RunState(started)) }
            } finally {
                busy = false
            }
        }
    }

    private fun finish(score: Int) {
        val run = _state.value.run ?: return
        if (busy || run.result != null) return
        busy = true
        viewModelScope.launch {
            try {
                val newBest = miniGameRepo.finishRun(run.started, score)
                _state.update { state ->
                    state.copy(
                        run = state.run?.copy(result = RunResult(score.coerceAtLeast(0), newBest))
                    )
                }
            } finally {
                busy = false
            }
        }
    }

    private fun observeGames() {
        gamesJob?.cancel()
        gamesJob =
            miniGameRepo
                .getMiniGameState()
                .onEach { games -> _state.update { it.copy(games = games) } }
                .launchIn(viewModelScope)
    }

    private fun observeAvatar() {
        avatarJob?.cancel()
        avatarJob =
            avatarRepo
                .getAvatarState()
                .onEach { avatar -> _state.update { it.copy(avatar = avatar) } }
                .launchIn(viewModelScope)
    }
}
