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
import com.shub39.grit.core.fighter.Belt
import com.shub39.grit.core.fighter.FighterRank
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.fighter_belt_black
import grit.shared.ui.generated.resources.fighter_belt_black_one_stripe
import grit.shared.ui.generated.resources.fighter_belt_black_two_stripes
import grit.shared.ui.generated.resources.fighter_belt_blue
import grit.shared.ui.generated.resources.fighter_belt_brown
import grit.shared.ui.generated.resources.fighter_belt_gold
import grit.shared.ui.generated.resources.fighter_belt_green
import grit.shared.ui.generated.resources.fighter_belt_orange
import grit.shared.ui.generated.resources.fighter_belt_purple
import grit.shared.ui.generated.resources.fighter_belt_red
import grit.shared.ui.generated.resources.fighter_belt_white
import grit.shared.ui.generated.resources.fighter_belt_yellow
import grit.shared.ui.generated.resources.fighter_rank_1
import grit.shared.ui.generated.resources.fighter_rank_10
import grit.shared.ui.generated.resources.fighter_rank_11
import grit.shared.ui.generated.resources.fighter_rank_12
import grit.shared.ui.generated.resources.fighter_rank_2
import grit.shared.ui.generated.resources.fighter_rank_3
import grit.shared.ui.generated.resources.fighter_rank_4
import grit.shared.ui.generated.resources.fighter_rank_5
import grit.shared.ui.generated.resources.fighter_rank_6
import grit.shared.ui.generated.resources.fighter_rank_7
import grit.shared.ui.generated.resources.fighter_rank_8
import grit.shared.ui.generated.resources.fighter_rank_9
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Name of the rank, e.g. "Brawler". */
@Composable fun FighterRank.label(): String = stringResource(nameRes())

/** Name of the belt, e.g. "green belt". */
@Composable fun Belt.label(): String = stringResource(nameRes())

internal fun FighterRank.nameRes(): StringResource =
    when (this) {
        FighterRank.ROOKIE -> Res.string.fighter_rank_1
        FighterRank.TRAINEE -> Res.string.fighter_rank_2
        FighterRank.SPARRING_PARTNER -> Res.string.fighter_rank_3
        FighterRank.BRAWLER -> Res.string.fighter_rank_4
        FighterRank.CONTENDER -> Res.string.fighter_rank_5
        FighterRank.FIGHTER -> Res.string.fighter_rank_6
        FighterRank.CHALLENGER -> Res.string.fighter_rank_7
        FighterRank.VETERAN -> Res.string.fighter_rank_8
        FighterRank.CHAMPION -> Res.string.fighter_rank_9
        FighterRank.MASTER -> Res.string.fighter_rank_10
        FighterRank.GRANDMASTER -> Res.string.fighter_rank_11
        FighterRank.LEGEND -> Res.string.fighter_rank_12
    }

internal fun Belt.nameRes(): StringResource =
    when (this) {
        Belt.WHITE -> Res.string.fighter_belt_white
        Belt.YELLOW -> Res.string.fighter_belt_yellow
        Belt.ORANGE -> Res.string.fighter_belt_orange
        Belt.GREEN -> Res.string.fighter_belt_green
        Belt.BLUE -> Res.string.fighter_belt_blue
        Belt.PURPLE -> Res.string.fighter_belt_purple
        Belt.BROWN -> Res.string.fighter_belt_brown
        Belt.RED -> Res.string.fighter_belt_red
        Belt.BLACK -> Res.string.fighter_belt_black
        Belt.BLACK_ONE_STRIPE -> Res.string.fighter_belt_black_one_stripe
        Belt.BLACK_TWO_STRIPES -> Res.string.fighter_belt_black_two_stripes
        Belt.GOLD -> Res.string.fighter_belt_gold
    }
