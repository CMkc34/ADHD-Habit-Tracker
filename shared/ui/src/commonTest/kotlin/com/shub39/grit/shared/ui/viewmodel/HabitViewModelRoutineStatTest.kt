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

import com.shub39.grit.core.fighter.FighterStat
import com.shub39.grit.core.fighter.statOf
import com.shub39.grit.core.habits.Habit
import com.shub39.grit.shared.ui.fakes.FakeActivityRepo
import com.shub39.grit.shared.ui.fakes.FakeAlarmScheduler
import com.shub39.grit.shared.ui.fakes.FakeAnalytics
import com.shub39.grit.shared.ui.fakes.FakeAvatarRepo
import com.shub39.grit.shared.ui.fakes.FakeHabitRepo
import com.shub39.grit.shared.ui.fakes.FakeMotivationSettings
import com.shub39.grit.shared.ui.fakes.FakeRoutineStatStore
import com.shub39.grit.shared.ui.fakes.FakeRoutineStyleStore
import com.shub39.grit.shared.ui.fakes.FakeSettingsDatastore
import com.shub39.grit.shared.ui.habit.HabitsAction
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDateTime

/** The stat a routine trains, chosen in the routine form (#4). */
@OptIn(ExperimentalCoroutinesApi::class)
class HabitViewModelRoutineStatTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val activityRepo = FakeActivityRepo()
    private val statStore = FakeRoutineStatStore()
    private val habit =
        Habit(
            id = 1,
            title = "Read",
            description = "",
            time = LocalDateTime(2026, 10, 26, 8, 0),
            days = emptySet(),
            index = 0,
            reminder = false,
        )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() =
        HabitViewModel(
            FakeAlarmScheduler(),
            FakeHabitRepo(listOf(habit)),
            FakeSettingsDatastore(),
            FakeAnalytics(),
            FakeMotivationSettings(),
            activityRepo,
            FakeAvatarRepo(activityRepo),
            FakeRoutineStyleStore(),
            statStore,
        )

    @Test
    fun `a new routine with untouched chips stores nothing and trains stamina`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            backgroundScope.launch { viewModel.state.collect {} }

            viewModel.onAction(HabitsAction.AddHabit(habit.copy(id = 2, title = "Run")))

            assertEquals(emptyMap(), statStore.stats.value)
            assertEquals(FighterStat.STAMINA, viewModel.state.value.routineStats.statOf(2))
        }

    @Test
    fun `a new routine keeps the stat chosen in the form`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            backgroundScope.launch { viewModel.state.collect {} }

            viewModel.onAction(
                HabitsAction.AddHabit(habit.copy(id = 2, title = "Run"), stat = FighterStat.SPEED)
            )

            assertEquals(mapOf(2L to FighterStat.SPEED), viewModel.state.value.routineStats)
        }

    @Test
    fun `changing the stat of a routine shows up in the state`() =
        runTest(dispatcher) {
            statStore.stats.value = mapOf(habit.id to FighterStat.FOCUS)
            val viewModel = viewModel()
            backgroundScope.launch { viewModel.state.collect {} }

            viewModel.onAction(HabitsAction.SetRoutineStat(habit.id, FighterStat.STRENGTH))

            assertEquals(mapOf(habit.id to FighterStat.STRENGTH), statStore.stats.value)
            assertEquals(FighterStat.STRENGTH, viewModel.state.value.routineStats.statOf(habit.id))
        }
}
