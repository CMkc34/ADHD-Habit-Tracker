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
import com.shub39.grit.core.avatar.AvatarData
import com.shub39.grit.core.habits.Habit
import com.shub39.grit.core.habits.HabitStatus
import com.shub39.grit.core.motivation.StreakTier
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
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
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
class HabitViewModelMotivationTest {

    private val dispatcher = UnconfinedTestDispatcher()
    // relative to the real date: the ViewModel only celebrates check-ins for today or yesterday
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
    private val water =
        meditate.copy(id = 2, title = "Water", targetValue = 8.0, targetUnit = "glasses")
    private val activityRepo = FakeActivityRepo()
    private val repo = FakeHabitRepo(listOf(meditate, water), activityRepo)
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

    /** ViewModel whose state is collected, like the habits screen does. */
    private fun TestScope.viewModel(): HabitViewModel {
        val viewModel =
            HabitViewModel(
                FakeAlarmScheduler(),
                repo,
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
    fun `check-in shows the popup with the streak from the repository`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            repo.currentStreaks[meditate.id] = 5

            viewModel.onAction(HabitsAction.InsertStatus(meditate, today))

            val popup = assertNotNull(viewModel.state.value.motivationPopup)
            assertEquals("Meditate", popup.habitTitle)
            assertEquals(5, popup.activeDays)
            assertEquals(StreakTier.ESTABLISHED, popup.quote.tier)
        }

    @Test
    fun `popup shows the user's plant`() =
        runTest(dispatcher) {
            avatarRepo.data.value = AvatarData(potColor = "pot_blue")
            val viewModel = viewModel()
            repo.currentStreaks[meditate.id] = 5

            viewModel.onAction(HabitsAction.InsertStatus(meditate, today))

            val popup = assertNotNull(viewModel.state.value.motivationPopup)
            assertEquals("pot_blue", assertNotNull(popup.avatar).data.potColor)
        }

    @Test
    fun `un-check shows no popup`() =
        runTest(dispatcher) {
            repo.insertHabitStatus(HabitStatus(habitId = meditate.id, date = today))
            repo.currentStreaks[meditate.id] = 5
            val viewModel = viewModel()

            viewModel.onAction(HabitsAction.InsertStatus(meditate, today))

            assertNull(viewModel.state.value.motivationPopup)
        }

    @Test
    fun `check-in without an active streak shows no popup`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            repo.currentStreaks[meditate.id] = 0

            viewModel.onAction(HabitsAction.InsertStatus(meditate, yesterday))

            assertNull(viewModel.state.value.motivationPopup)
        }

    @Test
    fun `dismiss hides the popup`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            repo.currentStreaks[meditate.id] = 3
            viewModel.onAction(HabitsAction.InsertStatus(meditate, today))

            viewModel.onAction(HabitsAction.DismissMotivationPopup)

            assertNull(viewModel.state.value.motivationPopup)
        }

    @Test
    fun `consecutive popups never repeat the same quote`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            repo.currentStreaks[meditate.id] = 3

            val quoteIds =
                List(50) {
                    viewModel.onAction(HabitsAction.InsertStatus(meditate, today))
                    val id = assertNotNull(viewModel.state.value.motivationPopup).quote.id
                    viewModel.onAction(HabitsAction.DismissMotivationPopup)
                    viewModel.onAction(HabitsAction.InsertStatus(meditate, today)) // un-check
                    id
                }

            quoteIds.zipWithNext().forEach { (previous, next) -> assertNotEquals(previous, next) }
        }

    @Test
    fun `first measured value of a day shows the popup`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            repo.currentStreaks[water.id] = 14

            viewModel.onAction(HabitsAction.InsertMeasuredStatus(water, today, 3.0))

            val popup = assertNotNull(viewModel.state.value.motivationPopup)
            assertEquals("Water", popup.habitTitle)
            assertEquals(14, popup.activeDays)
            assertEquals(StreakTier.LEGEND, popup.quote.tier)
        }

    @Test
    fun `correcting a measured value shows no popup`() =
        runTest(dispatcher) {
            repo.insertHabitStatus(HabitStatus(habitId = water.id, date = today, value = 3.0))
            repo.currentStreaks[water.id] = 14
            val viewModel = viewModel()

            viewModel.onAction(HabitsAction.InsertMeasuredStatus(water, today, 5.0))

            assertNull(viewModel.state.value.motivationPopup)
        }

    @Test
    fun `setting a measured value to zero shows no popup`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            repo.currentStreaks[water.id] = 14

            viewModel.onAction(HabitsAction.InsertMeasuredStatus(water, today, 0.0))

            assertNull(viewModel.state.value.motivationPopup)
        }

    @Test
    fun `check-in for yesterday shows the popup`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            repo.currentStreaks[meditate.id] = 2

            viewModel.onAction(HabitsAction.InsertStatus(meditate, yesterday))

            assertEquals(2, assertNotNull(viewModel.state.value.motivationPopup).activeDays)
        }

    @Test
    fun `catching up on older days shows no popup`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            repo.currentStreaks[meditate.id] = 4
            repo.currentStreaks[water.id] = 4

            viewModel.onAction(
                HabitsAction.InsertStatus(meditate, today.minus(2, DateTimeUnit.DAY))
            )
            viewModel.onAction(
                HabitsAction.InsertMeasuredStatus(water, today.minus(5, DateTimeUnit.DAY), 3.0)
            )

            assertNull(viewModel.state.value.motivationPopup)
        }

    @Test
    fun `check-in shows no popup while it is turned off`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            repo.currentStreaks[meditate.id] = 5
            repo.currentStreaks[water.id] = 5
            motivationSettings.setPopupEnabled(false)

            viewModel.onAction(HabitsAction.InsertStatus(meditate, today))
            viewModel.onAction(HabitsAction.InsertMeasuredStatus(water, today, 3.0))

            assertNull(viewModel.state.value.motivationPopup)
        }

    @Test
    fun `turning the popup back on shows it again`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            repo.currentStreaks[meditate.id] = 5
            motivationSettings.setPopupEnabled(false)
            viewModel.onAction(HabitsAction.InsertStatus(meditate, today))
            viewModel.onAction(HabitsAction.InsertStatus(meditate, today)) // un-check

            motivationSettings.setPopupEnabled(true)
            viewModel.onAction(HabitsAction.InsertStatus(meditate, today))

            assertNotNull(viewModel.state.value.motivationPopup)
        }

    @Test
    fun `popup flow follows check-in and dismiss`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            repo.currentStreaks[meditate.id] = 1

            viewModel.onAction(HabitsAction.InsertStatus(meditate, today))
            assertEquals(viewModel.state.value.motivationPopup, viewModel.motivationPopup.value)
            assertNotNull(viewModel.motivationPopup.value)

            viewModel.onAction(HabitsAction.DismissMotivationPopup)
            assertNull(viewModel.motivationPopup.value)
        }

    @Test
    fun `first check-in of the day shows the energy line with the activity streak`() =
        runTest(dispatcher) {
            activityRepo.report.value =
                ActivityReport(
                    currentStreak = 12,
                    longestStreak = 12,
                    energy = 12,
                    days = emptyMap(),
                )
            val viewModel = viewModel()
            repo.currentStreaks[meditate.id] = 5

            viewModel.onAction(HabitsAction.InsertStatus(meditate, today))

            assertEquals(13, assertNotNull(viewModel.state.value.motivationPopup).activityStreak)
        }

    @Test
    fun `second check-in of the day shows no energy line`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            repo.currentStreaks[meditate.id] = 5
            repo.currentStreaks[water.id] = 5
            viewModel.onAction(HabitsAction.InsertStatus(meditate, today))
            viewModel.onAction(HabitsAction.DismissMotivationPopup)

            viewModel.onAction(HabitsAction.InsertMeasuredStatus(water, today, 3.0))

            val popup = assertNotNull(viewModel.state.value.motivationPopup)
            assertEquals("Water", popup.habitTitle)
            assertNull(popup.activityStreak)
        }

    @Test
    fun `checking the same habit again after un-checking shows no energy line`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            repo.currentStreaks[meditate.id] = 5
            viewModel.onAction(HabitsAction.InsertStatus(meditate, today))
            viewModel.onAction(HabitsAction.DismissMotivationPopup)
            viewModel.onAction(HabitsAction.InsertStatus(meditate, today)) // un-check

            viewModel.onAction(HabitsAction.InsertStatus(meditate, today))

            assertNull(assertNotNull(viewModel.state.value.motivationPopup).activityStreak)
        }

    @Test
    fun `first measured value of the day shows the energy line`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            repo.currentStreaks[water.id] = 2

            viewModel.onAction(HabitsAction.InsertMeasuredStatus(water, today, 3.0))

            assertEquals(1, assertNotNull(viewModel.state.value.motivationPopup).activityStreak)
        }

    @Test
    fun `catching up on yesterday as first check-in of the day shows the energy line`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            repo.currentStreaks[meditate.id] = 2

            viewModel.onAction(HabitsAction.InsertStatus(meditate, yesterday))

            assertEquals(1, assertNotNull(viewModel.state.value.motivationPopup).activityStreak)
        }

    @Test
    fun `second routine of the day shows the bonus popup with the water`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            repo.currentStreaks[meditate.id] = 5
            repo.currentStreaks[water.id] = 5
            viewModel.onAction(HabitsAction.InsertStatus(meditate, today))
            assertNull(viewModel.bonusPopup.value)
            viewModel.onAction(HabitsAction.DismissMotivationPopup)

            viewModel.onAction(HabitsAction.InsertMeasuredStatus(water, today, 8.0))

            assertNotNull(viewModel.state.value.motivationPopup)
            val bonus = assertNotNull(viewModel.bonusPopup.value)
            assertEquals(activityRepo.report.value.energy, bonus.energy)

            viewModel.onAction(HabitsAction.DismissBonusPopup)
            assertNull(viewModel.bonusPopup.value)
        }

    @Test
    fun `measured routine below its target earns no bonus popup`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            repo.currentStreaks[meditate.id] = 5
            repo.currentStreaks[water.id] = 5
            viewModel.onAction(HabitsAction.InsertStatus(meditate, today))

            viewModel.onAction(HabitsAction.InsertMeasuredStatus(water, today, 3.0))

            assertNull(viewModel.bonusPopup.value)
        }

    @Test
    fun `bonus popup stays hidden while popups are turned off`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            repo.currentStreaks[meditate.id] = 5
            repo.currentStreaks[water.id] = 5
            motivationSettings.setPopupEnabled(false)
            viewModel.onAction(HabitsAction.InsertStatus(meditate, today))

            viewModel.onAction(HabitsAction.InsertMeasuredStatus(water, today, 8.0))

            assertNull(viewModel.bonusPopup.value)
        }
}
