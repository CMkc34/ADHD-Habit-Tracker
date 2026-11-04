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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.shub39.grit.shared.ui.GritPreviewWrapper
import com.shub39.grit.shared.ui.theme.flexFontEmphasis
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.stats_comebacks
import grit.shared.ui.generated.resources.stats_comebacks_habit
import grit.shared.ui.generated.resources.stats_comebacks_none
import grit.shared.ui.generated.resources.stats_comebacks_total
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * How often a new streak started after a break. Rewards getting back on track instead of
 * perfection, so zero is shown neutrally.
 */
@Composable
fun ComebacksCard(comebacks: Int, overall: Boolean, modifier: Modifier = Modifier) {
    StatsCard(title = stringResource(Res.string.stats_comebacks), modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "↺ $comebacks",
                style =
                    MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = flexFontEmphasis(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    ),
            )
            Text(
                text =
                    when {
                        comebacks == 0 -> stringResource(Res.string.stats_comebacks_none)
                        overall ->
                            pluralStringResource(
                                Res.plurals.stats_comebacks_total,
                                comebacks,
                                comebacks,
                            )
                        else ->
                            pluralStringResource(
                                Res.plurals.stats_comebacks_habit,
                                comebacks,
                                comebacks,
                            )
                    },
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun ComebacksCardPreview() {
    ComebacksCard(comebacks = 3, overall = false)
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun NoComebacksCardPreview() {
    ComebacksCard(comebacks = 0, overall = true)
}
