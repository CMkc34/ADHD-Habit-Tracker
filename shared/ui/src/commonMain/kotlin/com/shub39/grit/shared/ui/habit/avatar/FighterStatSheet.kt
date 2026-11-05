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

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.core.fighter.FighterStat
import com.shub39.grit.core.fighter.FighterState
import com.shub39.grit.core.fighter.StatCurve
import com.shub39.grit.shared.ui.GritPreviewWrapper
import com.shub39.grit.shared.ui.habit.fighter.StatIcon
import com.shub39.grit.shared.ui.habit.fighter.label
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.fighter_stat_level
import grit.shared.ui.generated.resources.fighter_stat_no_routine
import grit.shared.ui.generated.resources.fighter_stat_xp
import grit.shared.ui.generated.resources.fighter_stats_hide
import grit.shared.ui.generated.resources.fighter_stats_show
import grit.shared.ui.generated.resources.fighter_stats_title
import kotlin.math.abs
import org.jetbrains.compose.resources.stringResource

// how far a drag or how fast a fling has to go to open or close the sheet
private const val DRAG_THRESHOLD = 24f
private const val FLING_THRESHOLD = 300f

val SHEET_CORNER = 28.dp

/**
 * The fighter's four stats as a sheet at the bottom of the Fighter tab (#9). Closed it shows each
 * stat's icon, level and progress in one row; pulled up (or tapped) it lists every stat with its XP
 * bar and the routines that train it. Tapping a routine line opens the routine list.
 */
@Composable
fun FighterStatSheet(
    fighter: FighterState,
    routines: Map<FighterStat, List<String>>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onOpenRoutines: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var drag by remember { mutableFloatStateOf(0f) }
    val toggleLabel =
        stringResource(
            if (expanded) Res.string.fighter_stats_hide else Res.string.fighter_stats_show
        )

    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .draggable(
                    state = rememberDraggableState { drag += it },
                    orientation = Orientation.Vertical,
                    onDragStarted = { drag = 0f },
                    onDragStopped = { velocity ->
                        val push = if (abs(velocity) > FLING_THRESHOLD) velocity else drag
                        if (push < -DRAG_THRESHOLD) onExpandedChange(true)
                        if (push > DRAG_THRESHOLD) onExpandedChange(false)
                    },
                ),
        shape = RoundedCornerShape(topStart = SHEET_CORNER, topEnd = SHEET_CORNER),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = 6.dp,
    ) {
        Column(modifier = Modifier.animateContentSize().padding(bottom = 12.dp)) {
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .clickable(onClickLabel = toggleLabel, role = Role.Button) {
                            onExpandedChange(!expanded)
                        }
                        .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier =
                        Modifier.size(width = 32.dp, height = 4.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                )
            }

            if (expanded) {
                Column(
                    modifier =
                        Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.fighter_stats_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    FighterStat.entries.forEach { stat ->
                        StatRow(
                            stat = stat,
                            xp = fighter.xp(stat),
                            routines = routines[stat].orEmpty(),
                            onOpenRoutines = onOpenRoutines,
                        )
                    }
                }
            } else {
                Row(
                    modifier =
                        Modifier.fillMaxWidth()
                            .clickable(onClickLabel = toggleLabel, role = Role.Button) {
                                onExpandedChange(true)
                            }
                            .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    FighterStat.entries.forEach { stat ->
                        StatChip(stat = stat, xp = fighter.xp(stat), modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/** Icon, level and a short progress bar, for the closed sheet. */
@Composable
private fun StatChip(stat: FighterStat, xp: Int, modifier: Modifier = Modifier) {
    val progress by animateFloatAsState(StatCurve.progress(xp), label = "progress")
    Column(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        StatIcon(stat = stat, modifier = Modifier.size(28.dp))
        Text(
            text = stringResource(Res.string.fighter_stat_level, StatCurve.levelFor(xp)),
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.width(44.dp).height(4.dp),
        )
    }
}

/** One stat with name, level, XP bar and the routines that train it, for the open sheet. */
@Composable
private fun StatRow(
    stat: FighterStat,
    xp: Int,
    routines: List<String>,
    onOpenRoutines: () -> Unit,
) {
    val level = StatCurve.levelFor(xp)
    val progress by animateFloatAsState(StatCurve.progress(xp), label = "progress")

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatIcon(stat = stat, modifier = Modifier.size(36.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stat.label(),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(Res.string.fighter_stat_level, level),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text =
                        if (routines.isEmpty()) stringResource(Res.string.fighter_stat_no_routine)
                        else routines.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier =
                        Modifier.weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable(onClick = onOpenRoutines),
                )
                Text(
                    text =
                        stringResource(
                            Res.string.fighter_stat_xp,
                            xp,
                            StatCurve.xpForLevel(level + 1),
                        ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private val previewFighter =
    FighterState(
        activeDays = 20,
        rank = FighterRank.BRAWLER,
        mood = FighterMood.READY,
        xp = mapOf(FighterStat.STRENGTH to 22, FighterStat.SPEED to 3, FighterStat.STAMINA to 41),
    )

private val previewRoutines =
    mapOf(
        FighterStat.STRENGTH to listOf("Push-ups"),
        FighterStat.SPEED to listOf("Morning walk", "Tidy up for 5 minutes"),
        FighterStat.STAMINA to listOf("Drink water"),
        FighterStat.FOCUS to emptyList(),
    )

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun FighterStatSheetClosedPreview() {
    FighterStatSheet(
        fighter = previewFighter,
        routines = previewRoutines,
        expanded = false,
        onExpandedChange = {},
        onOpenRoutines = {},
    )
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun FighterStatSheetOpenPreview() {
    FighterStatSheet(
        fighter = previewFighter,
        routines = previewRoutines,
        expanded = true,
        onExpandedChange = {},
        onOpenRoutines = {},
    )
}
