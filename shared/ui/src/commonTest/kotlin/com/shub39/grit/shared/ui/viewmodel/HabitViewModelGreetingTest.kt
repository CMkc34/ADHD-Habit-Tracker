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

import com.shub39.grit.core.avatar.AvatarData
import com.shub39.grit.core.avatar.AvatarSpecies
import com.shub39.grit.core.habits.Habit
import com.shub39.grit.core.motivation.GreetingKind
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
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.minus

@OptIn(ExperimentalCoroutinesApi::class)
class HabitViewModelGreetingTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val today = LocalDate.now()
    private val yesterday = today.minus(1, DateTimeUnit.DAY)
    private val meditate =
        Habit(
            id = 1,
            title = "Meditate",
            description = "",
            time = LocalDateTime(2026, 1, 1, 8, 0),
            days = DayOfWeek.entries.toSet(),
            index = 0,
            reminder = false,
        )
    private val activityRepo = FakeActivityRepo()
    private val motivationSettings = FakeMotivationSettings()
    private val avatarRepo = FakeAvatarRepo(activityRepo)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.viewModel(habits: List<Habit> = listOf(meditate)): HabitViewModel {
        val viewModel =
            HabitViewModel(
                FakeAlarmScheduler(),
                FakeHabitRepo(habits, activityRepo),
                FakeSettingsDatastore(),
                FakeAnalytics(),
                motivationSettings,
                activityRepo,
                avatarRepo,
                FakeRoutineStyleStore(),
            )
        backgroundScope.launch { viewModel.state.collect {} }
        return viewModel
    }

    @Test
    fun `first start without routines welcomes and opens the add sheet afterwards`() =
        runTest(dispatcher) {
            val viewModel = viewModel(habits = emptyList())

            viewModel.onAction(HabitsAction.CheckDailyGreeting)

            // the very first start asks for the name first
            assertTrue(viewModel.nameSetup.value)
            assertNull(viewModel.dailyGreeting.value)

            viewModel.onAction(HabitsAction.FinishNameSetup("  Lena "))

            assertFalse(viewModel.nameSetup.value)
            assertTrue(motivationSettings.setupDone.value)
            assertEquals("Lena", avatarRepo.data.value.userName)
            val greeting = assertNotNull(viewModel.dailyGreeting.value)
            assertEquals(GreetingKind.WELCOME, greeting.kind)
            assertEquals(today, motivationSettings.lastGreetingDay.value)

            viewModel.onAction(HabitsAction.DismissDailyGreeting)

            assertNull(viewModel.dailyGreeting.value)
            assertTrue(viewModel.state.value.showHabitAddSheet)
        }

    @Test
    fun `skipping the name setup keeps the name empty`() =
        runTest(dispatcher) {
            val viewModel = viewModel(habits = emptyList())

            viewModel.onAction(HabitsAction.CheckDailyGreeting)
            viewModel.onAction(HabitsAction.FinishNameSetup(""))

            assertEquals("", avatarRepo.data.value.userName)
            assertEquals(AvatarSpecies.SPROUT, avatarRepo.data.value.species)
            assertTrue(motivationSettings.setupDone.value)
            assertNotNull(viewModel.dailyGreeting.value)
        }

    @Test
    fun `the plant chosen in the setup is stored`() =
        runTest(dispatcher) {
            val viewModel = viewModel(habits = emptyList())

            viewModel.onAction(HabitsAction.CheckDailyGreeting)
            viewModel.onAction(HabitsAction.FinishNameSetup("Lena", AvatarSpecies.CHERRY))

            assertEquals(AvatarSpecies.CHERRY, avatarRepo.data.value.species)
            assertNotNull(viewModel.dailyGreeting.value)
        }

    @Test
    fun `an install already in use never gets the name setup`() =
        runTest(dispatcher) {
            val viewModel = viewModel()

            viewModel.onAction(HabitsAction.CheckDailyGreeting)

            assertFalse(viewModel.nameSetup.value)
            assertTrue(motivationSettings.setupDone.value)
            assertNotNull(viewModel.dailyGreeting.value)
        }

    @Test
    fun `greeting shows the current plant`() =
        runTest(dispatcher) {
            avatarRepo.data.value = AvatarData(name = "Fritz", potColor = "pot_blue")
            activityRepo.markActive(yesterday)
            val viewModel = viewModel()

            viewModel.onAction(HabitsAction.CheckDailyGreeting)

            val greeting = assertNotNull(viewModel.dailyGreeting.value)
            assertEquals(GreetingKind.GROWING, greeting.kind)
            assertEquals("Fritz", greeting.avatar.data.name)
            assertEquals("pot_blue", greeting.avatar.data.potColor)
            assertEquals(1, greeting.activityStreak)

            viewModel.onAction(HabitsAction.DismissDailyGreeting)
            assertFalse(viewModel.state.value.showHabitAddSheet)
        }

    @Test
    fun `greeting shows once a day`() =
        runTest(dispatcher) {
            motivationSettings.lastGreetingDay.value = today
            val viewModel = viewModel()

            viewModel.onAction(HabitsAction.CheckDailyGreeting)

            assertNull(viewModel.dailyGreeting.value)
        }

    @Test
    fun `greeting comes back on the next day`() =
        runTest(dispatcher) {
            motivationSettings.lastGreetingDay.value = yesterday
            val viewModel = viewModel()

            viewModel.onAction(HabitsAction.CheckDailyGreeting)

            assertEquals(GreetingKind.FIRST_DAY, assertNotNull(viewModel.dailyGreeting.value).kind)
        }

    @Test
    fun `no greeting when turned off`() =
        runTest(dispatcher) {
            motivationSettings.greetingEnabled.value = false
            val viewModel = viewModel()

            viewModel.onAction(HabitsAction.CheckDailyGreeting)

            assertNull(viewModel.dailyGreeting.value)
            assertNull(motivationSettings.lastGreetingDay.value)
        }
}
