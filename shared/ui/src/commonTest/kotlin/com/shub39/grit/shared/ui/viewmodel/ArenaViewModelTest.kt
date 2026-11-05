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

import com.shub39.grit.core.arena.BattleAction
import com.shub39.grit.core.arena.BattleEvent
import com.shub39.grit.core.arena.LadderStatus
import com.shub39.grit.core.arena.Opponent
import com.shub39.grit.core.arena.Outcome
import com.shub39.grit.core.arena.Side
import com.shub39.grit.shared.ui.fakes.FakeArenaRepo
import com.shub39.grit.shared.ui.fakes.FakeAvatarRepo
import com.shub39.grit.shared.ui.habit.arena.ArenaAction
import com.shub39.grit.shared.ui.habit.arena.ArenaStage
import com.shub39.grit.shared.ui.habit.arena.FightState
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class ArenaViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repo = FakeArenaRepo()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.viewModel(seed: Int = 7): ArenaViewModel {
        val viewModel = ArenaViewModel(repo, FakeAvatarRepo())
        viewModel.random = Random(seed)
        viewModel.looks = Random(seed)
        backgroundScope.launch { viewModel.state.collect {} }
        return viewModel
    }

    private val ArenaViewModel.fight: FightState?
        get() = state.value.fight

    /** Reports every event as played, like the UI with animations off. */
    private fun ArenaViewModel.playAll() {
        while (true) {
            val index = fight?.playback?.playingIndex ?: return
            if (index < 0) return
            onAction(ArenaAction.EventPlayed(index))
        }
    }

    private fun ArenaViewModel.start(opponent: Opponent = Opponent.SNOOZE_BUTTON) {
        onAction(ArenaAction.Select(opponent))
        onAction(ArenaAction.StartFight)
        playAll()
    }

    /** Plays the running fight to its end with [move] only. */
    private fun ArenaViewModel.fightWith(move: BattleAction) {
        while (fight?.result == null) {
            onAction(ArenaAction.Act(move))
            playAll()
        }
    }

    @Test
    fun `starting a fight uses exactly one ticket`() =
        runTest(dispatcher) {
            val viewModel = viewModel()

            viewModel.start()

            assertNotNull(viewModel.fight)
            assertNull(viewModel.state.value.selected)
            assertEquals(2, viewModel.state.value.arena?.tickets?.available)
            assertEquals(1, repo.fights.value.size)
        }

    @Test
    fun `no fight starts without a ticket`() =
        runTest(dispatcher) {
            repo.earned.value = 0
            val viewModel = viewModel()

            viewModel.start()

            assertNull(viewModel.fight)
            assertTrue(repo.fights.value.isEmpty())
        }

    @Test
    fun `a locked opponent cannot be fought`() =
        runTest(dispatcher) {
            val viewModel = viewModel()

            viewModel.start(Opponent.TIME_THIEF)

            assertNull(viewModel.fight)
        }

    @Test
    fun `a full fight can be played with strike only`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            viewModel.start()

            viewModel.fightWith(BattleAction.STRIKE)

            val fight = assertNotNull(viewModel.fight)
            val result = assertNotNull(fight.result)
            assertEquals(result.outcome, repo.fights.value.single().outcome)
            assertFalse(fight.canAct)
        }

    @Test
    fun `the same seed plays the same fight`() =
        runTest(dispatcher) {
            fun play(): List<BattleEvent> {
                val viewModel = ArenaViewModel(repo, FakeAvatarRepo())
                viewModel.random = Random(3)
                backgroundScope.launch { viewModel.state.collect {} }
                viewModel.start()
                viewModel.fightWith(BattleAction.STRIKE)
                return viewModel.fight!!.battle.log
            }

            assertEquals(play(), play())
        }

    @Test
    fun `special is unavailable without three charge`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            viewModel.start()
            val before = viewModel.fight!!

            assertFalse(before.battle.player.specialReady)
            assertFalse(before.canSpecial)
            viewModel.onAction(ArenaAction.Act(BattleAction.SPECIAL))

            assertEquals(before, viewModel.fight)
        }

    @Test
    fun `special works once the charge is full`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            viewModel.start(Opponent.SNOOZE_BUTTON)
            while (viewModel.fight?.canSpecial != true) {
                viewModel.onAction(ArenaAction.Act(BattleAction.GUARD))
                viewModel.playAll()
            }

            viewModel.onAction(ArenaAction.Act(BattleAction.SPECIAL))

            val first = viewModel.fight!!.playback.playing
            assertTrue(first is BattleEvent.Special && first.actor == Side.PLAYER)
        }

    @Test
    fun `moves wait until the last move has played`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            viewModel.start()
            viewModel.onAction(ArenaAction.Act(BattleAction.STRIKE))
            val playing = viewModel.fight!!

            assertFalse(playing.canAct)
            viewModel.onAction(ArenaAction.Act(BattleAction.STRIKE))
            assertEquals(playing, viewModel.fight)

            // a stale report for another event does nothing
            viewModel.onAction(ArenaAction.EventPlayed(playing.playback.playingIndex + 1))
            assertEquals(playing, viewModel.fight)

            viewModel.playAll()
            assertTrue(viewModel.fight!!.canAct)
        }

    @Test
    fun `beating the next opponent unlocks the following one at once`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            viewModel.start()

            viewModel.fightWith(BattleAction.STRIKE)

            val result = viewModel.fight!!.result!!
            assertEquals(Outcome.WIN, result.outcome)
            assertEquals(Opponent.COUCH_GREMLIN, result.unlocked)
            assertEquals(5, result.shakes) // first win against opponent 1 (#17)
            val ladder = viewModel.state.value.arena!!.ladder
            assertEquals(LadderStatus.BEATEN, ladder[0].status)
            assertEquals(LadderStatus.NEXT, ladder[1].status)
        }

    @Test
    fun `a rematch win unlocks nothing new`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            viewModel.start()
            viewModel.fightWith(BattleAction.STRIKE)
            viewModel.onAction(ArenaAction.CloseFight)

            viewModel.start()
            viewModel.fightWith(BattleAction.STRIKE)

            assertEquals(Outcome.WIN, viewModel.fight!!.result!!.outcome)
            assertNull(viewModel.fight!!.result!!.unlocked)
            assertEquals(0, viewModel.fight!!.result!!.shakes)
        }

    @Test
    fun `giving up asks first and keeps the loss`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            viewModel.start()

            viewModel.onAction(ArenaAction.AskGiveUp)
            assertTrue(viewModel.fight!!.askingGiveUp)
            assertFalse(viewModel.fight!!.canAct)

            viewModel.onAction(ArenaAction.KeepFighting)
            assertTrue(viewModel.fight!!.canAct)

            viewModel.onAction(ArenaAction.AskGiveUp)
            viewModel.onAction(ArenaAction.GiveUp)
            assertNull(viewModel.fight)
            assertEquals(Outcome.LOSS, repo.fights.value.single().outcome)
            assertEquals(2, viewModel.state.value.arena?.tickets?.available)
        }

    @Test
    fun `back after the end closes without asking`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            viewModel.start()
            viewModel.fightWith(BattleAction.STRIKE)

            viewModel.onAction(ArenaAction.AskGiveUp)

            assertNull(viewModel.fight)
        }

    @Test
    fun `the stage belongs to the opponent and the log keeps three lines`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            viewModel.start()
            viewModel.fightWith(BattleAction.STRIKE)
            val fight = viewModel.fight!!

            assertEquals(ArenaStage.BEDROOM, fight.stage)
            assertEquals(fight.battle.log.takeLast(3), fight.logLines)
            assertTrue(fight.kicks.all { fight.battle.log[it].actor == Side.PLAYER })
        }

    @Test
    fun `stages follow the ladder in pairs`() {
        assertEquals(ArenaStage.BEDROOM, ArenaStage.of(Opponent.COUCH_GREMLIN))
        assertEquals(ArenaStage.SCREEN_WORLD, ArenaStage.of(Opponent.DOOMSCROLL_IMP))
        assertEquals(ArenaStage.LAUNDRY, ArenaStage.of(Opponent.DISTRACTION_SWARM))
        assertEquals(ArenaStage.CLOCK_TOWER, ArenaStage.of(Opponent.DEADLINE_GOLEM))
        assertEquals(ArenaStage.VOLCANO, ArenaStage.of(Opponent.PROCRASTINATION_DRAGON))
    }
}
