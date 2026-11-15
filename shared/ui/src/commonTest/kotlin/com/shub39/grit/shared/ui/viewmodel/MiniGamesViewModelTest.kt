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

import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.core.fighter.FighterState
import com.shub39.grit.core.minigame.MiniGame
import com.shub39.grit.core.minigame.MiniGameRepo
import com.shub39.grit.core.minigame.MiniGameState
import com.shub39.grit.core.minigame.RunLog
import com.shub39.grit.core.minigame.RunRecord
import com.shub39.grit.core.minigame.StartedRun
import com.shub39.grit.shared.ui.fakes.FakeAvatarRepo
import com.shub39.grit.shared.ui.habit.minigame.MiniGamesAction
import com.shub39.grit.shared.ui.habit.minigame.RunResult
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate

/** The run log in memory, with the real rules of [RunLog]. */
private class FakeMiniGameRepo : MiniGameRepo {
    val today = LocalDate(2026, 11, 15)
    val runs = MutableStateFlow<List<RunRecord>>(emptyList())
    private val fighter = FighterState(0, FighterRank.ROOKIE, FighterMood.READY)

    override fun getMiniGameState(): Flow<MiniGameState> =
        runs.map { MiniGameState.of(it, fighter, today) }

    override suspend fun startRun(game: MiniGame): StartedRun? {
        if (RunLog.runsLeft(runs.value, game, today) == 0) return null
        val record = RunLog.start(game, today, now = 1, records = runs.value)
        runs.update { it + record }
        return StartedRun(record, MiniGameState.of(runs.value, fighter, today).stats)
    }

    override suspend fun finishRun(run: StartedRun, score: Int): Boolean {
        val newBest = RunLog.isNewBest(runs.value, run.record, score)
        runs.update { list ->
            list.map { if (it.isSameRun(run.record)) it.copy(score = score) else it }
        }
        return newBest
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MiniGamesViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repo = FakeMiniGameRepo()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.viewModel(): MiniGamesViewModel {
        val viewModel = MiniGamesViewModel(repo, FakeAvatarRepo())
        viewModel.playable = MiniGame.entries.toSet()
        backgroundScope.launch { viewModel.state.collect {} }
        return viewModel
    }

    @Test
    fun `starting a run uses one of the day's runs`() =
        runTest(dispatcher) {
            val viewModel = viewModel()

            viewModel.onAction(MiniGamesAction.Start(MiniGame.KI_BARRAGE))

            assertEquals(MiniGame.KI_BARRAGE, viewModel.state.value.run?.game)
            val games = assertNotNull(viewModel.state.value.games)
            assertEquals(2, games.entry(MiniGame.KI_BARRAGE).runsLeft)
            assertEquals(3, games.entry(MiniGame.BOARD_BREAK).runsLeft)
        }

    @Test
    fun `a second start while a run is open does nothing`() =
        runTest(dispatcher) {
            val viewModel = viewModel()

            viewModel.onAction(MiniGamesAction.Start(MiniGame.KI_BARRAGE))
            viewModel.onAction(MiniGamesAction.Start(MiniGame.BOARD_BREAK))

            assertEquals(1, repo.runs.value.size)
            assertEquals(MiniGame.KI_BARRAGE, viewModel.state.value.run?.game)
        }

    @Test
    fun `the score is stored and a new best is reported once`() =
        runTest(dispatcher) {
            val viewModel = viewModel()

            viewModel.onAction(MiniGamesAction.Start(MiniGame.CLUTTER_SMASH))
            viewModel.onAction(MiniGamesAction.Finish(320))
            viewModel.onAction(MiniGamesAction.Finish(999))

            assertEquals(RunResult(320, newBest = true), viewModel.state.value.run?.result)
            assertEquals(320, viewModel.state.value.games?.entry(MiniGame.CLUTTER_SMASH)?.best)

            viewModel.onAction(MiniGamesAction.Close)
            assertNull(viewModel.state.value.run)
            viewModel.onAction(MiniGamesAction.Start(MiniGame.CLUTTER_SMASH))
            viewModel.onAction(MiniGamesAction.Finish(100))
            assertEquals(RunResult(100, newBest = false), viewModel.state.value.run?.result)
        }

    @Test
    fun `no run after the three free runs of a game`() =
        runTest(dispatcher) {
            val viewModel = viewModel()

            repeat(3) {
                viewModel.onAction(MiniGamesAction.Start(MiniGame.BOARD_BREAK))
                viewModel.onAction(MiniGamesAction.Close)
            }
            viewModel.onAction(MiniGamesAction.Start(MiniGame.BOARD_BREAK))

            assertNull(viewModel.state.value.run)
            assertEquals(3, repo.runs.value.size)
        }

    @Test
    fun `games without a screen yet start no run`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            viewModel.playable = emptySet()

            viewModel.onAction(MiniGamesAction.Start(MiniGame.KI_BARRAGE))

            assertNull(viewModel.state.value.run)
            assertEquals(emptyList(), repo.runs.value)
        }
}
