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
package com.shub39.grit.shared.ui.habit.fighter

import androidx.compose.runtime.Composable
import com.shub39.grit.core.fighter.FighterStat
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.fighter_stat_focus
import grit.shared.ui.generated.resources.fighter_stat_focus_hint
import grit.shared.ui.generated.resources.fighter_stat_speed
import grit.shared.ui.generated.resources.fighter_stat_speed_hint
import grit.shared.ui.generated.resources.fighter_stat_stamina
import grit.shared.ui.generated.resources.fighter_stat_stamina_hint
import grit.shared.ui.generated.resources.fighter_stat_strength
import grit.shared.ui.generated.resources.fighter_stat_strength_hint
import grit.shared.ui.generated.resources.routine_icon_bolt
import grit.shared.ui.generated.resources.routine_icon_dumbbell
import grit.shared.ui.generated.resources.routine_icon_heart
import grit.shared.ui.generated.resources.routine_icon_target
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Name of the stat, e.g. "Strength". */
@Composable fun FighterStat.label(): String = stringResource(nameRes())

/** One-line hint what the stat does in a fight, e.g. "hits harder". */
@Composable fun FighterStat.hint(): String = stringResource(hintRes())

internal fun FighterStat.nameRes(): StringResource =
    when (this) {
        FighterStat.STRENGTH -> Res.string.fighter_stat_strength
        FighterStat.SPEED -> Res.string.fighter_stat_speed
        FighterStat.STAMINA -> Res.string.fighter_stat_stamina
        FighterStat.FOCUS -> Res.string.fighter_stat_focus
    }

internal fun FighterStat.hintRes(): StringResource =
    when (this) {
        FighterStat.STRENGTH -> Res.string.fighter_stat_strength_hint
        FighterStat.SPEED -> Res.string.fighter_stat_speed_hint
        FighterStat.STAMINA -> Res.string.fighter_stat_stamina_hint
        FighterStat.FOCUS -> Res.string.fighter_stat_focus_hint
    }

/** Icon of the stat, reusing the routine icons: dumbbell, bolt, heart and target. */
fun FighterStat.icon(): DrawableResource =
    when (this) {
        FighterStat.STRENGTH -> Res.drawable.routine_icon_dumbbell
        FighterStat.SPEED -> Res.drawable.routine_icon_bolt
        FighterStat.STAMINA -> Res.drawable.routine_icon_heart
        FighterStat.FOCUS -> Res.drawable.routine_icon_target
    }
