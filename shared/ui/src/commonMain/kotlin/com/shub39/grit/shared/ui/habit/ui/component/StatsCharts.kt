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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shub39.grit.shared.ui.theme.flexFontRounded
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.stats_month_1
import grit.shared.ui.generated.resources.stats_month_10
import grit.shared.ui.generated.resources.stats_month_11
import grit.shared.ui.generated.resources.stats_month_12
import grit.shared.ui.generated.resources.stats_month_2
import grit.shared.ui.generated.resources.stats_month_3
import grit.shared.ui.generated.resources.stats_month_4
import grit.shared.ui.generated.resources.stats_month_5
import grit.shared.ui.generated.resources.stats_month_6
import grit.shared.ui.generated.resources.stats_month_7
import grit.shared.ui.generated.resources.stats_month_8
import grit.shared.ui.generated.resources.stats_month_9
import kotlinx.datetime.YearMonth
import org.jetbrains.compose.resources.stringResource

private val BAR_AREA_HEIGHT = 120.dp

private val monthNames =
    listOf(
        Res.string.stats_month_1,
        Res.string.stats_month_2,
        Res.string.stats_month_3,
        Res.string.stats_month_4,
        Res.string.stats_month_5,
        Res.string.stats_month_6,
        Res.string.stats_month_7,
        Res.string.stats_month_8,
        Res.string.stats_month_9,
        Res.string.stats_month_10,
        Res.string.stats_month_11,
        Res.string.stats_month_12,
    )

/** One series of a [StatsBarChart]: a color and one value per label, `null` draws no bar. */
data class BarSeries(val color: Color, val values: List<Int?>)

@Composable
fun monthLabel(month: YearMonth): String = stringResource(monthNames[month.month.ordinal])

/** Card of the fork's statistics, styled like the cards on the energy page. */
@Composable
fun StatsCard(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.widthIn(max = 500.dp).fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                fontFamily = flexFontRounded(),
                style = MaterialTheme.typography.titleMedium,
            )
            content()
        }
    }
}

/** Simple bar chart: one group of bars per label, the value above each bar. */
@Composable
fun StatsBarChart(labels: List<String>, series: List<BarSeries>, modifier: Modifier = Modifier) {
    val max = series.flatMap { it.values }.filterNotNull().maxOrNull()?.takeIf { it > 0 } ?: 1

    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        labels.forEachIndexed { index, label ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier.height(BAR_AREA_HEIGHT + 20.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    series.forEach { (color, values) ->
                        val value = values.getOrNull(index)
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            if (value != null) {
                                Text(
                                    text = value.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center,
                                )
                                Box(
                                    modifier =
                                        Modifier.fillMaxWidth()
                                            .height(BAR_AREA_HEIGHT * value / max)
                                            .background(color, MaterialTheme.shapes.extraSmall)
                                )
                            }
                        }
                    }
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    // short date labels may be wider than a bar
                    softWrap = false,
                    overflow = TextOverflow.Visible,
                )
            }
        }
    }
}

/** Legend below a chart with more than one series. */
@Composable
fun StatsLegend(items: List<Pair<Color, String>>, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items.forEach { (color, label) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(Modifier.size(12.dp).background(color, MaterialTheme.shapes.extraSmall))
                WaterText(text = label, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
