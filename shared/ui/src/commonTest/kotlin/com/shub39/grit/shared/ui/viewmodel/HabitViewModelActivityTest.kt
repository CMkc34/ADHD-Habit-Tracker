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

import com.shub39.grit.core.activity.ActivityPlanner
import com.shub39.grit.core.activity.ActivityReport
import com.shub39.grit.core.activity.FreezeNotice
import com.shub39.grit.core.now
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
import kotlinx.datetime.yearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class HabitViewModelActivityTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val activityRepo = FakeActivityRepo()

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
            FakeAvatarRepo(activityRepo),
            FakeRoutineStyleStore(),
        )

    @Test
    fun `state follows the activity report`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            backgroundScope.launch { viewModel.state.collect {} }

            val report =
                ActivityReport(
                    currentStreak = 12,
                    longestStreak = 20,
                    energy = 14,
                    days = emptyMap(),
                )
            activityRepo.report.value = report

            assertEquals(report, viewModel.state.value.activity)
        }

    @Test
    fun `statistics follow the activity report`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            backgroundScope.launch { viewModel.state.collect {} }

            val today = LocalDate.now()
            activityRepo.report.value =
                ActivityPlanner.plan(activeDays = setOf(today), startDate = today, today = today)

            val thisMonth = viewModel.state.value.stats.activeDaysPerMonth.last()
            assertEquals(today.yearMonth, thisMonth.month)
            assertEquals(1, thisMonth.activeDays)
        }

    @Test
    fun `freeze notice is shown until it is dismissed`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            backgroundScope.launch { viewModel.freezeNotice.collect {} }

            val notice =
                FreezeNotice(
                    streak = 12,
                    frozenDays = setOf(LocalDate(2026, 10, 20)),
                    energyLeft = 7,
                )
            activityRepo.freezeNotice.value = notice
            assertEquals(notice, viewModel.freezeNotice.value)

            viewModel.onAction(HabitsAction.DismissFreezeNotice)

            assertEquals(notice.frozenDays, activityRepo.seenFreezes)
            assertNull(viewModel.freezeNotice.value)
        }

    @Test
    fun `auto freeze switch reaches the activity report`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            backgroundScope.launch { viewModel.state.collect {} }

            viewModel.onAction(HabitsAction.SetAutoFreeze(false))

            assertEquals(false, viewModel.state.value.activity?.autoFreeze)
        }
}
