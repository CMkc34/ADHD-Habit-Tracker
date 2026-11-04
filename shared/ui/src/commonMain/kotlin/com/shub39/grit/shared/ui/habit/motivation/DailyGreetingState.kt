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
import com.shub39.grit.core.motivation.DayTime
import com.shub39.grit.core.motivation.GreetingKind

/**
 * Data for the greeting on the first app start of a day: the plant as it is right now, with its
 * stage, mood, pot, accessories, decorations and weather.
 *
 * @property activityStreak Active days in a row across all routines.
 */
@Immutable
data class DailyGreetingState(
    val kind: GreetingKind,
    val dayTime: DayTime,
    val avatar: AvatarState,
    val activityStreak: Int,
)
