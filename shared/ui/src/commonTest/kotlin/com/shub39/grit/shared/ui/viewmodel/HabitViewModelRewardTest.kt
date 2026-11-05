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
import com.shub39.grit.core.habits.Habit
import com.shub39.grit.core.habits.HabitStatus
import com.shub39.grit.core.now
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
import com.shub39.grit.shared.ui.habit.motivation.MotivationPopupState
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

/** The XP, level-up and fight ticket lines of the motivation popup (#15). */
@OptIn(ExperimentalCoroutinesApi::class)
class HabitViewModelRewardTest {

    private val dispatcher = UnconfinedTestDispatcher()
    // relative to the real date: the ViewModel only celebrates check-ins for today or yesterday
    private val today = LocalDate.now()
    private val yesterday = today.minus(1, DateTimeUnit.DAY)

    private val pushUps = routine(1, "Push-ups")
    private val read = routine(2, "Read")
    private val walk = routine(3, "Walk")
    private val tidy = routine(4, "Tidy up")
    private val water = routine(5, "Water").copy(targetValue = 8.0, targetUnit = "glasses")
    private val routines = listOf(pushUps, read, walk, tidy, water)

    private val activityRepo = FakeActivityRepo()
    private val repo = FakeHabitRepo(routines, activityRepo)
    private val motivationSettings = FakeMotivationSettings()
    private val statStore = FakeRoutineStatStore()

    private fun routine(id: Long, title: String) =
        Habit(
            id = id,
            title = title,
            description = "",
            time = LocalDateTime(2026, 1, 1, 8, 0),
            days = DayOfWeek.entries.toSet(),
            index = id.toInt(),
            reminder = false,
        )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        routines.forEach { repo.currentStreaks[it.id] = 3 }
        statStore.stats.value = mapOf(pushUps.id to FighterStat.STRENGTH)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.viewModel(): HabitViewModel {
        val viewModel =
            HabitViewModel(
                FakeAlarmScheduler(),
                repo,
                FakeSettingsDatastore(),
                FakeAnalytics(),
                motivationSettings,
                activityRepo,
                FakeAvatarRepo(activityRepo),
                FakeRoutineStyleStore(),
                statStore,
            )
        backgroundScope.launch { viewModel.state.collect {} }
        return viewModel
    }

    private fun HabitViewModel.checkIn(
        habit: Habit,
        date: LocalDate = today,
    ): MotivationPopupState {
        onAction(HabitsAction.InsertStatus(habit, date))
        val popup = assertNotNull(state.value.motivationPopup)
        onAction(HabitsAction.DismissMotivationPopup)
        return popup
    }

    @Test
    fun `check-in shows the XP line of the routine's stat`() =
        runTest(dispatcher) {
            val viewModel = viewModel()

            assertEquals(FighterStat.STRENGTH, viewModel.checkIn(pushUps).statGain)
            assertEquals(FighterStat.STAMINA, viewModel.checkIn(read).statGain)
        }

    @Test
    fun `measured value below its target shows no XP line`() =
        runTest(dispatcher) {
            val viewModel = viewModel()

            viewModel.onAction(HabitsAction.InsertMeasuredStatus(water, today, 3.0))

            val popup = assertNotNull(viewModel.state.value.motivationPopup)
            assertNull(popup.statGain)
            assertFalse(popup.ticketEarned)
        }

    @Test
    fun `measured value at its target shows the XP line`() =
        runTest(dispatcher) {
            val viewModel = viewModel()

            viewModel.onAction(HabitsAction.InsertMeasuredStatus(water, today, 8.0))

            val popup = assertNotNull(viewModel.state.value.motivationPopup)
            assertEquals(FighterStat.STAMINA, popup.statGain)
            assertTrue(popup.ticketEarned)
        }

    @Test
    fun `correcting a measured value shows no XP line`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            viewModel.onAction(HabitsAction.InsertMeasuredStatus(water, today, 3.0))
            viewModel.onAction(HabitsAction.DismissMotivationPopup)

            viewModel.onAction(HabitsAction.InsertMeasuredStatus(water, today, 8.0))

            assertNull(viewModel.state.value.motivationPopup)
        }

    @Test
    fun `checking again after un-checking shows no XP or ticket line`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            viewModel.checkIn(pushUps)
            viewModel.onAction(HabitsAction.InsertStatus(pushUps, today)) // un-check

            val popup = viewModel.checkIn(pushUps)

            assertNull(popup.statGain)
            assertFalse(popup.ticketEarned)
        }

    @Test
    fun `level-up line at the threshold`() =
        runTest(dispatcher) {
            // 4 XP from earlier days, the fifth check-in reaches Strength level 2
            (2..5).forEach {
                repo.insertHabitStatus(
                    HabitStatus(habitId = pushUps.id, date = today.minus(it, DateTimeUnit.DAY))
                )
            }
            val viewModel = viewModel()

            val popup = viewModel.checkIn(pushUps)

            assertEquals(FighterStat.STRENGTH, popup.statGain)
            assertEquals(2, popup.newStatLevel)
        }

    @Test
    fun `no level-up line below the threshold`() =
        runTest(dispatcher) {
            val viewModel = viewModel()

            assertNull(viewModel.checkIn(pushUps).newStatLevel)
        }

    @Test
    fun `ticket line for the first three routines of today but not the fourth`() =
        runTest(dispatcher) {
            val viewModel = viewModel()

            val tickets =
                listOf(pushUps, read, walk, tidy).map { viewModel.checkIn(it).ticketEarned }

            assertEquals(listOf(true, true, true, false), tickets)
        }

    @Test
    fun `the fourth routine of today still shows its XP`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            listOf(read, walk, tidy).forEach { viewModel.checkIn(it) }

            assertEquals(FighterStat.STRENGTH, viewModel.checkIn(pushUps).statGain)
        }

    @Test
    fun `check-in for yesterday shows the XP line but no ticket`() =
        runTest(dispatcher) {
            val viewModel = viewModel()

            val popup = viewModel.checkIn(pushUps, yesterday)

            assertEquals(FighterStat.STRENGTH, popup.statGain)
            assertFalse(popup.ticketEarned)
        }

    @Test
    fun `nothing is shown while the popup is turned off`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            motivationSettings.setPopupEnabled(false)

            viewModel.onAction(HabitsAction.InsertStatus(pushUps, today))

            assertNull(viewModel.state.value.motivationPopup)
        }

    @Test
    fun `un-checking shows nothing`() =
        runTest(dispatcher) {
            repo.insertHabitStatus(HabitStatus(habitId = pushUps.id, date = today))
            val viewModel = viewModel()

            viewModel.onAction(HabitsAction.InsertStatus(pushUps, today))

            assertNull(viewModel.state.value.motivationPopup)
        }
}
