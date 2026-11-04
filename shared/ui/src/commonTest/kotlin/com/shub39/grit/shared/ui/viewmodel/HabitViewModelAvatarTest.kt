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

import com.shub39.grit.core.activity.ActivityReport
import com.shub39.grit.core.activity.DayStatus
import com.shub39.grit.core.avatar.AvatarData
import com.shub39.grit.core.avatar.PlantStage
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
import kotlin.test.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HabitViewModelAvatarTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val activityRepo = FakeActivityRepo()
    private val avatarRepo = FakeAvatarRepo(activityRepo)

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
            FakeHabitRepo(emptyList()),
            FakeSettingsDatastore(),
            FakeAnalytics(),
            FakeMotivationSettings(),
            activityRepo,
            avatarRepo,
            FakeRoutineStyleStore(),
        )

    /** A report with [count] active days in a row, ending with a running streak. */
    private fun activeDays(count: Int): ActivityReport =
        ActivityReport(
            currentStreak = count,
            longestStreak = count,
            energy = count,
            days = (1..count).associate { LocalDate.fromEpochDays(20_000 + it) to DayStatus.ACTIVE },
        )

    @Test
    fun `state shows the plant`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            backgroundScope.launch { viewModel.state.collect {} }

            activityRepo.report.value = activeDays(8)

            assertEquals(PlantStage.SEEDLING, viewModel.state.value.avatar?.plant?.stage)
        }

    @Test
    fun `rename stores the trimmed name`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            backgroundScope.launch { viewModel.state.collect {} }

            viewModel.onAction(HabitsAction.RenameAvatar("  Fridolin "))

            assertEquals("Fridolin", viewModel.state.value.avatar?.data?.name)
        }

    @Test
    fun `a new stage is announced until it is dismissed`() =
        runTest(dispatcher) {
            avatarRepo.data.value = AvatarData(seenStage = PlantStage.GERM.number)
            activityRepo.report.value = activeDays(7)
            val viewModel = viewModel()
            backgroundScope.launch { viewModel.state.collect {} }

            assertEquals(PlantStage.SEEDLING, viewModel.state.value.avatar?.newStage)

            viewModel.onAction(HabitsAction.DismissNewStage)

            assertNull(viewModel.state.value.avatar?.newStage)
        }

    @Test
    fun `shop actions reach the repository`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            backgroundScope.launch { viewModel.state.collect {} }

            viewModel.onAction(HabitsAction.BuyShopItem("acc_ladybug"))
            viewModel.onAction(HabitsAction.WearShopItem("acc_bird", worn = true))

            assertEquals(listOf("acc_ladybug"), avatarRepo.bought)
            assertEquals(setOf("acc_bird"), viewModel.state.value.avatar?.data?.accessories)
        }
}
