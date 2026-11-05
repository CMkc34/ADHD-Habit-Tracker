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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shub39.grit.core.avatar.AvatarData
import com.shub39.grit.core.avatar.AvatarState
import com.shub39.grit.core.fighter.FighterCharacter
import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.core.fighter.FighterStat
import com.shub39.grit.core.fighter.FighterState
import com.shub39.grit.shared.ui.GritPreviewWrapper
import com.shub39.grit.shared.ui.components.GritDialog
import com.shub39.grit.shared.ui.habit.HabitsAction
import com.shub39.grit.shared.ui.habit.fighter.FighterAvatar
import com.shub39.grit.shared.ui.habit.fighter.FighterPose
import com.shub39.grit.shared.ui.habit.fighter.StatIcon
import com.shub39.grit.shared.ui.habit.fighter.label
import com.shub39.grit.shared.ui.theme.flexFontEmphasis
import com.shub39.grit.shared.ui.viewmodel.HabitViewModel
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.fighter_new_rank_text
import grit.shared.ui.generated.resources.fighter_new_rank_title
import grit.shared.ui.generated.resources.fighter_new_stat_level
import grit.shared.ui.generated.resources.fighter_new_stat_text
import grit.shared.ui.generated.resources.fighter_notice_ok
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Shows rank-up and stat level-up notices on any tab, see [FighterNotice]. */
@Composable
fun FighterNoticeHost() {
    val viewModel = koinViewModel<HabitViewModel>()
    val notice by viewModel.fighterNotice.collectAsStateWithLifecycle()

    when (val current = notice) {
        is FighterNotice.NewRank ->
            NewRankDialog(
                avatar = current.avatar,
                rank = current.rank,
                onDismiss = { viewModel.onAction(HabitsAction.DismissNewRank) },
            )
        is FighterNotice.NewStatLevels ->
            NewStatLevelsDialog(
                levels = current.levels,
                onDismiss = { viewModel.onAction(HabitsAction.DismissNewStatLevels) },
            )
        null -> Unit
    }
}

/** The fighter gathers ki in its new belt: "Kai is now a Brawler! 🥋". */
@Composable
fun NewRankDialog(avatar: AvatarState, rank: FighterRank, onDismiss: () -> Unit) {
    GritDialog(onDismissRequest = onDismiss) {
        FighterAvatar(
            character = FighterCharacter.of(avatar.data.look),
            rank = rank,
            mood = FighterMood.READY,
            outfit = avatar.data.outfit,
            gear = avatar.data.accessories,
            pose = FighterPose.CHARGE,
            modifier = Modifier.widthIn(max = 160.dp).fillMaxWidth(),
        )
        Text(
            text = stringResource(Res.string.fighter_new_rank_title, avatar.name, rank.label()),
            style = MaterialTheme.typography.titleLarge.copy(fontFamily = flexFontEmphasis()),
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.fighter_new_rank_text, rank.belt.label()),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        TextButton(onClick = onDismiss) { Text(stringResource(Res.string.fighter_notice_ok)) }
    }
}

/** The smaller notice: "Strength level 4!", one line for each stat that went up. */
@Composable
fun NewStatLevelsDialog(levels: Map<FighterStat, Int>, onDismiss: () -> Unit) {
    GritDialog(onDismissRequest = onDismiss) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // in the fixed stat order, not in the order the map happens to have
            FighterStat.entries
                .filter { it in levels }
                .forEach { stat ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        StatIcon(stat = stat, modifier = Modifier.size(40.dp))
                        Text(
                            text =
                                stringResource(
                                    Res.string.fighter_new_stat_level,
                                    stat.label(),
                                    levels.getValue(stat),
                                ),
                            style =
                                MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = flexFontEmphasis()
                                ),
                        )
                    }
                }
        }
        Text(
            text = stringResource(Res.string.fighter_new_stat_text),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        TextButton(onClick = onDismiss) { Text(stringResource(Res.string.fighter_notice_ok)) }
    }
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun NewRankDialogPreview() {
    NewRankDialog(
        avatar =
            AvatarState(
                fighter =
                    FighterState(
                        activeDays = 14,
                        rank = FighterRank.BRAWLER,
                        mood = FighterMood.READY,
                    ),
                energy = 0,
                data = AvatarData(name = "Kai"),
            ),
        rank = FighterRank.BRAWLER,
        onDismiss = {},
    )
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun NewStatLevelsDialogPreview() {
    NewStatLevelsDialog(
        levels = mapOf(FighterStat.STRENGTH to 4, FighterStat.FOCUS to 2),
        onDismiss = {},
    )
}
