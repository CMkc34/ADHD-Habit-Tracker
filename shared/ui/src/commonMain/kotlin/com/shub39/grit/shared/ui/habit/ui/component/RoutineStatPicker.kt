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
package com.shub39.grit.shared.ui.habit.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shub39.grit.core.fighter.FighterStat
import com.shub39.grit.core.theme.AppTheme
import com.shub39.grit.core.theme.Theme
import com.shub39.grit.shared.ui.habit.fighter.hint
import com.shub39.grit.shared.ui.habit.fighter.icon
import com.shub39.grit.shared.ui.habit.fighter.label
import com.shub39.grit.shared.ui.theme.GritTheme
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.routine_stat_moves_xp
import grit.shared.ui.generated.resources.routine_stat_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

private const val CHIPS_PER_ROW = 2

/**
 * Chooses the stat a routine trains (#4): four chips with icon, name and a one-line hint, two per
 * row so the hints stay readable on a phone. [showXpHint] adds the note that past check-ins move
 * with the stat, for an edited routine whose stat changed.
 */
@Composable
fun RoutineStatPicker(
    selected: FighterStat,
    onSelect: (FighterStat) -> Unit,
    modifier: Modifier = Modifier,
    showXpHint: Boolean = false,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = stringResource(Res.string.routine_stat_title))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.selectableGroup(),
        ) {
            FighterStat.entries.chunked(CHIPS_PER_ROW).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { stat ->
                        StatChip(
                            stat = stat,
                            selected = stat == selected,
                            onClick = { onSelect(stat) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = showXpHint) {
            Text(
                text = stringResource(Res.string.routine_stat_moves_xp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StatChip(
    stat: FighterStat,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (selected) colors.secondaryContainer else colors.surfaceContainerHighest,
        contentColor = if (selected) colors.onSecondaryContainer else colors.onSurface,
        border = if (selected) BorderStroke(2.dp, colors.secondary) else null,
        modifier =
            modifier
                .heightIn(min = 56.dp)
                .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Icon(
                imageVector = vectorResource(stat.icon()),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            Column {
                Text(
                    text = stat.label(),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stat.hint(),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun RoutineStatPickerPreview(theme: Theme) {
    GritTheme(theme = theme) {
        Surface {
            var stat by remember { mutableStateOf(FighterStat.DEFAULT) }
            RoutineStatPicker(
                selected = stat,
                onSelect = { stat = it },
                showXpHint = stat != FighterStat.DEFAULT,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            )
        }
    }
}

@Preview
@Composable
private fun RoutineStatPickerLightPreview() {
    RoutineStatPickerPreview(Theme(appTheme = AppTheme.LIGHT))
}

@Preview
@Composable
private fun RoutineStatPickerDarkPreview() {
    RoutineStatPickerPreview(Theme(appTheme = AppTheme.DARK))
}
