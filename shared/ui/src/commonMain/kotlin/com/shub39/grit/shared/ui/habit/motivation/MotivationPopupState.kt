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

import androidx.compose.runtime.Immutable
import com.shub39.grit.core.avatar.AvatarState
import com.shub39.grit.core.motivation.MotivationQuote

/**
 * Data for the popup shown after a habit check-in.
 *
 * @property activityStreak Activity streak after the first check-in of the day, shown as "+1 💧"
 *   line; `null` for later check-ins that day, which earn no energy.
 * @property avatar The user's plant shown on the popup, `null` if it could not be read.
 */
@Immutable
data class MotivationPopupState(
    val habitTitle: String,
    val activeDays: Int,
    val quote: MotivationQuote,
    val activityStreak: Int? = null,
    val avatar: AvatarState? = null,
)
