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

import com.shub39.grit.core.habits.Habit
import com.shub39.grit.core.habits.style.RoutineColor
import com.shub39.grit.core.habits.style.RoutineIcon
import com.shub39.grit.core.habits.style.RoutineStyle
import com.shub39.grit.shared.ui.fakes.FakeActivityRepo
import com.shub39.grit.shared.ui.fakes.FakeAlarmScheduler
import com.shub39.grit.shared.ui.fakes.FakeAnalytics
import com.shub39.grit.shared.ui.fakes.FakeAvatarRepo
import com.shub39.grit.shared.ui.fakes.FakeHabitRepo
import com.shub39.grit.shared.ui.fakes.FakeMotivationSettings
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

@OptIn(ExperimentalCoroutinesApi::class)
class HabitViewModelRoutineStyleTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val activityRepo = FakeActivityRepo()
    private val styleStore = FakeRoutineStyleStore()
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
            styleStore,
        )

    @Test
    fun `a chosen style shows up in the state`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            backgroundScope.launch { viewModel.state.collect {} }
            val style = RoutineStyle(RoutineColor.CORAL, RoutineIcon.BOOK)

            viewModel.onAction(HabitsAction.SetRoutineStyle(habit.id, style))

            assertEquals(mapOf(habit.id to style), viewModel.state.value.routineStyles)
        }

    @Test
    fun `deleting a routine removes its style`() =
        runTest(dispatcher) {
            styleStore.styles.value =
                mapOf(habit.id to RoutineStyle(RoutineColor.RED, RoutineIcon.RUN))
            val viewModel = viewModel()
            backgroundScope.launch { viewModel.state.collect {} }

            viewModel.onAction(HabitsAction.DeleteHabit(habit))

            assertEquals(emptyMap(), viewModel.state.value.routineStyles)
        }

    @Test
    fun `a new routine keeps the style chosen in the form`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            backgroundScope.launch { viewModel.state.collect {} }
            val newHabit = habit.copy(id = 2, title = "Run")
            val style = RoutineStyle(RoutineColor.BLUE, RoutineIcon.RUN)

            viewModel.onAction(HabitsAction.AddHabit(newHabit, style))

            assertEquals(style, viewModel.state.value.routineStyles[2])
        }
}
