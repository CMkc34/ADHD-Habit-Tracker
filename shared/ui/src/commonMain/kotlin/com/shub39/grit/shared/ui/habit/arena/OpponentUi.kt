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
package com.shub39.grit.shared.ui.habit.arena

import androidx.compose.runtime.Composable
import com.shub39.grit.core.arena.Opponent
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.arena_opponent_1
import grit.shared.ui.generated.resources.arena_opponent_10
import grit.shared.ui.generated.resources.arena_opponent_2
import grit.shared.ui.generated.resources.arena_opponent_3
import grit.shared.ui.generated.resources.arena_opponent_4
import grit.shared.ui.generated.resources.arena_opponent_5
import grit.shared.ui.generated.resources.arena_opponent_6
import grit.shared.ui.generated.resources.arena_opponent_7
import grit.shared.ui.generated.resources.arena_opponent_8
import grit.shared.ui.generated.resources.arena_opponent_9
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Name of the opponent, e.g. "Snooze Button". */
@Composable fun Opponent.label(): String = stringResource(nameRes())

internal fun Opponent.nameRes(): StringResource =
    when (this) {
        Opponent.SNOOZE_BUTTON -> Res.string.arena_opponent_1
        Opponent.COUCH_GREMLIN -> Res.string.arena_opponent_2
        Opponent.DOOMSCROLL_IMP -> Res.string.arena_opponent_3
        Opponent.LAUNDRY_MOUNTAIN -> Res.string.arena_opponent_4
        Opponent.INBOX_HYDRA -> Res.string.arena_opponent_5
        Opponent.DISTRACTION_SWARM -> Res.string.arena_opponent_6
        Opponent.TIME_THIEF -> Res.string.arena_opponent_7
        Opponent.DEADLINE_GOLEM -> Res.string.arena_opponent_8
        Opponent.OVERWHELM_OGRE -> Res.string.arena_opponent_9
        Opponent.PROCRASTINATION_DRAGON -> Res.string.arena_opponent_10
    }
