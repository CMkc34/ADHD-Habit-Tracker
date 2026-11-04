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
package com.shub39.grit.shared.ui.habit.motivation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shub39.grit.shared.ui.habit.HabitsAction
import com.shub39.grit.shared.ui.habit.ui.component.HabitMotivationPopup
import com.shub39.grit.shared.ui.viewmodel.HabitViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Shows the motivation popup app-wide, so a check-in from the list, the habit details or the
 * calendar gets its popup right away. Shares the [HabitViewModel] instance with the habit screens.
 */
@Composable
fun MotivationPopupHost() {
    val viewModel = koinViewModel<HabitViewModel>()
    val popup by viewModel.motivationPopup.collectAsStateWithLifecycle()

    popup?.let {
        HabitMotivationPopup(
            habitTitle = it.habitTitle,
            activeDays = it.activeDays,
            quote = it.quote,
            activityStreak = it.activityStreak,
            avatar = it.avatar,
            onDismiss = { viewModel.onAction(HabitsAction.DismissMotivationPopup) },
        )
    }
}
