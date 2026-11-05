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
import com.shub39.grit.core.arena.ArenaRepo
import com.shub39.grit.core.arena.Battle
import com.shub39.grit.core.arena.BattleAction
import com.shub39.grit.core.arena.BattleEvent
import com.shub39.grit.core.arena.FightPlayback
import com.shub39.grit.core.arena.Side
import com.shub39.grit.core.avatar.AvatarRepo
import com.shub39.grit.shared.ui.habit.arena.ArenaAction
import com.shub39.grit.shared.ui.habit.arena.ArenaLighting
import com.shub39.grit.shared.ui.habit.arena.ArenaPageState
import com.shub39.grit.shared.ui.habit.arena.ArenaStage
import com.shub39.grit.shared.ui.habit.arena.FightState
import kotlin.random.Random
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
 * Owns the Arena tab and the running fight (#14). The fight itself is the pure [Battle] engine;
 * this class starts and stores fights through [ArenaRepo] and steps through the events of each
 * move, while the UI only animates the event that is playing and reports back with
 * [ArenaAction.EventPlayed]. With animations off, the UI reports at once and the fight runs at
 * full speed.
 */
@KoinViewModel
class ArenaViewModel(
    @Provided private val arenaRepo: ArenaRepo,
    @Provided private val avatarRepo: AvatarRepo,
) : ViewModel() {

    /** Dice of the fight engine; tests set a seeded one. */
    internal var random: Random = Random.Default

    /** Dice for looks only (lighting, punch or kick), so they never change a fight's result. */
    internal var looks: Random = Random.Default

    private var arenaJob: Job? = null
    private var avatarJob: Job? = null

    // set while a fight is being started, so two quick taps use one ticket
    private var starting = false

    private val _state = MutableStateFlow(ArenaPageState())

    val state: StateFlow<ArenaPageState> =
        _state
            .asStateFlow()
            .onStart {
                observeArena()
                observeAvatar()
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ArenaPageState())

    fun onAction(action: ArenaAction) {
        when (action) {
            is ArenaAction.Select ->
                _state.update { if (it.fight == null) it.copy(selected = action.opponent) else it }

            ArenaAction.CloseSheet -> _state.update { it.copy(selected = null) }

            ArenaAction.StartFight -> startFight()

            is ArenaAction.Act -> act(action.action)

            is ArenaAction.EventPlayed ->
                updateFight { fight ->
                    if (fight.playback.playingIndex != action.index) fight
                    else fight.copy(playback = fight.playback.next())
                }

            ArenaAction.AskGiveUp -> {
                val fight = _state.value.fight ?: return
                if (fight.result != null) _state.update { it.copy(fight = null) }
                else updateFight { it.copy(askingGiveUp = true) }
            }

            ArenaAction.KeepFighting -> updateFight { it.copy(askingGiveUp = false) }

            // the fight was stored as a loss when it started, so leaving needs no write
            ArenaAction.GiveUp,
            ArenaAction.CloseFight -> _state.update { it.copy(fight = null) }
        }
    }

    private fun startFight() {
        val current = _state.value
        val opponent = current.selected ?: return
        val arena = current.arena ?: return
        if (starting || current.fight != null || !arena.canStart(opponent)) return
        starting = true
        val wasNext = FightState.wasNext(arena, opponent)

        viewModelScope.launch {
            try {
                val started = arenaRepo.startFight(opponent) ?: return@launch
                val battle = Battle.start(started.player, opponent.combatant, random)
                val playback = FightPlayback.opening(battle)
                val fight =
                    FightState(
                        started = started,
                        playback = playback,
                        wasNext = wasNext,
                        stage = ArenaStage.of(opponent),
                        lighting = ArenaLighting.entries.random(looks),
                        kicks = kicks(playback),
                    )
                _state.update { it.copy(selected = null, fight = fight) }
                battle.outcome?.let { arenaRepo.finishFight(started, it) }
            } finally {
                starting = false
            }
        }
    }

    private fun act(action: BattleAction) {
        val fight = _state.value.fight ?: return
        if (!fight.canAct || (action == BattleAction.SPECIAL && !fight.canSpecial)) return

        val after = Battle.act(fight.battle, action, random)
        val playback = FightPlayback(fight.battle, after)
        _state.update {
            it.copy(fight = fight.copy(playback = playback, kicks = fight.kicks + kicks(playback)))
        }
        // stored right away, so leaving during the last animation keeps a win
        after.outcome?.let { outcome ->
            viewModelScope.launch { arenaRepo.finishFight(fight.started, outcome) }
        }
    }

    /** Picks punch or kick for every player strike of [playback]; only the look changes. */
    private fun kicks(playback: FightPlayback): Set<Int> {
        val first = playback.after.log.size - playback.events.size
        return playback.events
            .withIndex()
            .filter { (_, event) -> event.actor == Side.PLAYER && event.isStrike() }
            .filter { looks.nextBoolean() }
            .map { first + it.index }
            .toSet()
    }

    private fun BattleEvent.isStrike() = this is BattleEvent.Hit || this is BattleEvent.Dodge

    private inline fun updateFight(crossinline change: (FightState) -> FightState) {
        _state.update { state -> state.fight?.let { state.copy(fight = change(it)) } ?: state }
    }

    private fun observeArena() {
        arenaJob?.cancel()
        arenaJob =
            arenaRepo
                .getArenaState()
                .onEach { arena -> _state.update { it.copy(arena = arena) } }
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
