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
package com.shub39.grit.shared.ui.habit

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.shub39.grit.core.activity.ActivityReport
import com.shub39.grit.core.avatar.AvatarState
import com.shub39.grit.core.habits.HabitWithAnalytics
import com.shub39.grit.core.habits.OverallAnalytics
import com.shub39.grit.core.habits.style.RoutineStyle
import com.shub39.grit.core.stats.ForkStats
import com.shub39.grit.shared.ui.habit.motivation.BonusPopupState
import com.shub39.grit.shared.ui.habit.motivation.DailyGreetingState
import com.shub39.grit.shared.ui.habit.motivation.MotivationPopupState
import kotlinx.datetime.DayOfWeek

/**
 * Represents the overall state for the habits feature.
 *
 * @property habitsWithAnalytics A list of all habits, each bundled with its analytical data.
 * @property completedHabitIds A list of IDs for habits that have been marked as completed for the
 *   current day.
 * @property overallAnalytics Aggregated analytics data across all habits.
 * @property analyticsHabitId The ID of the habit currently selected for detailed analytics view, or
 *   null if none is selected.
 * @property showHabitAddSheet A boolean flag to control the visibility of the "add new habit"
 *   bottom sheet.
 * @property editState A boolean flag indicating if the UI is in an "edit" mode, allowing for
 *   reordering or deletion of habits.
 * @property compactHabitView A user preference flag to display habits in a compact or detailed
 *   view.
 * @property is24Hr A user preference flag to display time in 24-hour format.
 * @property startingDay A user preference for the day of the week the calendar/week view should
 *   start on.
 * @property motivationPopup Popup shown after a habit check-in, `null` when hidden.
 * @property bonusPopup Popup for the daily bonus, shown after [motivationPopup]; `null` when
 *   hidden.
 * @property dailyGreeting Greeting on the first app start of a day, `null` when hidden.
 * @property nameSetup The plant asks for the user's name on the very first start (#139).
 * @property activity Activity streak and energy across all habits, `null` until loaded.
 * @property avatar The plant avatar on the avatar page, `null` until loaded.
 * @property routineStyles Colour and icon by habit id; habits without an entry use
 *   [RoutineStyle.DEFAULT].
 */
@Stable
@Immutable
data class HabitState(
    val habitsWithAnalytics: List<HabitWithAnalytics> = emptyList(),
    val completedHabitIds: List<Long> = emptyList(),
    val overallAnalytics: OverallAnalytics = OverallAnalytics(),
    val analyticsHabitId: Long? = null,
    val showHabitAddSheet: Boolean = false,
    val editState: Boolean = false,
    val motivationPopup: MotivationPopupState? = null,
    val bonusPopup: BonusPopupState? = null,
    val dailyGreeting: DailyGreetingState? = null,
    val nameSetup: Boolean = false,
    val activity: ActivityReport? = null,
    val stats: ForkStats = ForkStats(),
    val avatar: AvatarState? = null,
    val routineStyles: Map<Long, RoutineStyle> = emptyMap(),

    // datastore
    val compactHabitView: Boolean = false,
    val is24Hr: Boolean = false,
    val startingDay: DayOfWeek = DayOfWeek.MONDAY,
)
