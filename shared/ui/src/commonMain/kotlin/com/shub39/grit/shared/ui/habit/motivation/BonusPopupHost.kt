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
import com.shub39.grit.shared.ui.habit.ui.component.BonusPopup
import com.shub39.grit.shared.ui.viewmodel.HabitViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Shows the daily bonus popup app-wide. It waits until the motivation popup of the same check-in is
 * closed, so the two celebrations come one after the other.
 */
@Composable
fun BonusPopupHost() {
    val viewModel = koinViewModel<HabitViewModel>()
    val motivation by viewModel.motivationPopup.collectAsStateWithLifecycle()
    val bonus by viewModel.bonusPopup.collectAsStateWithLifecycle()

    if (motivation == null) {
        bonus?.let {
            BonusPopup(
                energy = it.energy,
                onDismiss = { viewModel.onAction(HabitsAction.DismissBonusPopup) },
            )
        }
    }
}
