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
package com.shub39.grit.shared.ui.habit.activity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shub39.grit.shared.ui.habit.HabitsAction
import com.shub39.grit.shared.ui.habit.ui.component.FreezeNoticeDialog
import com.shub39.grit.shared.ui.viewmodel.HabitViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Shows the freeze notice app-wide, so it appears when the app opens on any page. Shares the
 * [HabitViewModel] instance with the habit screens.
 */
@Composable
fun FreezeNoticeHost() {
    val viewModel = koinViewModel<HabitViewModel>()
    val notice by viewModel.freezeNotice.collectAsStateWithLifecycle()

    notice?.let {
        FreezeNoticeDialog(
            notice = it,
            onDismiss = { viewModel.onAction(HabitsAction.DismissFreezeNotice) },
        )
    }
}
