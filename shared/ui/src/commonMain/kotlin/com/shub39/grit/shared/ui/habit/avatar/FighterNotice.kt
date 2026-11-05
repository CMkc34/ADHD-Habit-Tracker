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
package com.shub39.grit.shared.ui.habit.avatar

import androidx.compose.runtime.Immutable
import com.shub39.grit.core.avatar.AvatarState
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.core.fighter.FighterStat
import com.shub39.grit.shared.ui.habit.HabitState

/**
 * A notice about the fighter's progress (#9), shown app-wide right after the check-in that earned
 * it. Each one appears once: dismissing it stores it as seen.
 */
@Immutable
sealed interface FighterNotice {
    val avatar: AvatarState

    /** The fighter reached [rank] and wears its new belt. */
    data class NewRank(override val avatar: AvatarState, val rank: FighterRank) : FighterNotice

    /** One or more stats reached a new level; [levels] holds the new level of each. */
    data class NewStatLevels(override val avatar: AvatarState, val levels: Map<FighterStat, Int>) :
        FighterNotice

    companion object {
        /**
         * The notice to show now, `null` when there is nothing new or another pop-up is open: the
         * notice waits until the motivation pop-up, the bonus, the greeting and the name setup are
         * gone. A new rank comes first, new stat levels after it.
         */
        fun of(state: HabitState): FighterNotice? {
            if (
                state.motivationPopup != null ||
                    state.bonusPopup != null ||
                    state.dailyGreeting != null ||
                    state.nameSetup
            )
                return null
            val avatar = state.avatar ?: return null
            avatar.newRank?.let {
                return NewRank(avatar, it)
            }
            return avatar.newStatLevels
                .takeIf { it.isNotEmpty() }
                ?.let { NewStatLevels(avatar, it) }
        }
    }
}
