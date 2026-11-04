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

import com.shub39.grit.core.avatar.AvatarSpecies
import com.shub39.grit.core.habits.Habit
import com.shub39.grit.core.habits.style.RoutineStyle
import kotlinx.datetime.LocalDate

/**
 * Represents the set of user actions or events that can occur on the Habits screen. This sealed
 * interface is used to pass events from the UI to the ViewModel for processing.
 */
sealed interface HabitsAction {
    /** toggling the compact view mode for the habits list. */
    data class OnToggleCompactView(val pref: Boolean) : HabitsAction

    /** toggle habit reordering */
    data class OnToggleEditState(val pref: Boolean) : HabitsAction

    /** reorder a single habit [from]: from Index [to]: to Index */
    data class OnTransientHabitReorder(val from: Int, val to: Int) : HabitsAction

    data object DismissAddHabitDialog : HabitsAction

    /** open habit upsert sheet */
    data object OnAddHabitClicked : HabitsAction

    /** set [habit]'s id to view analytics for */
    data class PrepareAnalytics(val habit: Habit?) : HabitsAction

    /** fork: [style] is saved under the id the new habit gets */
    data class AddHabit(val habit: Habit, val style: RoutineStyle = RoutineStyle.DEFAULT) :
        HabitsAction

    data class DeleteHabit(val habit: Habit) : HabitsAction

    /** Add/Remove status for [habit] at given [date] */
    data class InsertStatus(val habit: Habit, val date: LocalDate) : HabitsAction

    data class InsertMeasuredStatus(val habit: Habit, val date: LocalDate, val value: Double) :
        HabitsAction

    data class UpdateHabit(val habit: Habit) : HabitsAction

    data object ReorderHabits : HabitsAction

    data class FetchCompletedHabitsForDate(val date: LocalDate?) : HabitsAction

    data object OnHabitsOpened : HabitsAction

    data object OnOverallAnalyticsViewed : HabitsAction

    /** close the popup shown after a habit check-in */
    data object DismissMotivationPopup : HabitsAction

    /** close the popup for the daily bonus */
    data object DismissBonusPopup : HabitsAction

    /** show the plant's greeting if it was not shown today yet, sent when the app comes up */
    data object CheckDailyGreeting : HabitsAction

    /** close the daily greeting; the welcome greeting opens the add sheet afterwards */
    data object DismissDailyGreeting : HabitsAction

    /** finish the first start setup with the user's [name]; empty skips it (#139) */
    data class FinishNameSetup(
        val name: String,
        val species: AvatarSpecies = AvatarSpecies.SPROUT,
    ) : HabitsAction

    /** close the freeze notice and remember its freezes as seen */
    data object DismissFreezeNotice : HabitsAction

    /** give the plant avatar a new name, blank brings back the default */
    data class RenameAvatar(val name: String) : HabitsAction

    /** close the notice about a new plant stage and remember the stage as seen */
    data object DismissNewStage : HabitsAction

    /** buy an item from the plant's shop with energy */
    data class BuyShopItem(val itemId: String) : HabitsAction

    /** put an owned shop item on the plant or take it off */
    data class WearShopItem(val itemId: String, val worn: Boolean) : HabitsAction

    /** switch automatic freezes on or off from today on */
    data class SetAutoFreeze(val enabled: Boolean) : HabitsAction

    /** give the habit with [habitId] a colour and an icon */
    data class SetRoutineStyle(val habitId: Long, val style: RoutineStyle) : HabitsAction
}
