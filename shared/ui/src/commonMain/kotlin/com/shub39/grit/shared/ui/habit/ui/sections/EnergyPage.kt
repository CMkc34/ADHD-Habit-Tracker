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
package com.shub39.grit.shared.ui.habit.ui.sections

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.shub39.grit.core.activity.ActivityPlanner
import com.shub39.grit.core.activity.ActivityReport
import com.shub39.grit.core.activity.DailyBonus
import com.shub39.grit.core.activity.DayStatus
import com.shub39.grit.core.stats.ForkStats
import com.shub39.grit.shared.ui.GritPreviewWrapper
import com.shub39.grit.shared.ui.LocalWindowSizeClass
import com.shub39.grit.shared.ui.WindowSize.Companion.isExpanded
import com.shub39.grit.shared.ui.habit.HabitsAction
import com.shub39.grit.shared.ui.habit.activity.RecentDay
import com.shub39.grit.shared.ui.habit.activity.recentDays
import com.shub39.grit.shared.ui.habit.ui.component.WATER_MARK
import com.shub39.grit.shared.ui.habit.ui.component.WaterText
import com.shub39.grit.shared.ui.theme.flexFontEmphasis
import com.shub39.grit.shared.ui.theme.flexFontRounded
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.energy_auto_freeze_off
import grit.shared.ui.generated.resources.energy_auto_freeze_on
import grit.shared.ui.generated.resources.energy_auto_freeze_title
import grit.shared.ui.generated.resources.energy_balance
import grit.shared.ui.generated.resources.energy_current_streak
import grit.shared.ui.generated.resources.energy_days
import grit.shared.ui.generated.resources.energy_last_days
import grit.shared.ui.generated.resources.energy_legend_active
import grit.shared.ui.generated.resources.energy_legend_before_start
import grit.shared.ui.generated.resources.energy_legend_frozen
import grit.shared.ui.generated.resources.energy_legend_missed
import grit.shared.ui.generated.resources.energy_legend_open
import grit.shared.ui.generated.resources.energy_longest_streak
import grit.shared.ui.generated.resources.energy_rule_active
import grit.shared.ui.generated.resources.energy_rule_auto_freeze
import grit.shared.ui.generated.resources.energy_rule_bonus
import grit.shared.ui.generated.resources.energy_rule_break
import grit.shared.ui.generated.resources.energy_rule_energy
import grit.shared.ui.generated.resources.energy_rule_freeze
import grit.shared.ui.generated.resources.energy_rule_frozen_day
import grit.shared.ui.generated.resources.energy_rules_title
import grit.shared.ui.generated.resources.energy_title
import grit.shared.ui.generated.resources.nav_arrow_back
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

private const val DAYS_SHOWN = 30
private const val DAYS_PER_ROW = 10

/**
 * Energy balance, activity streak, the last 30 days, monthly statistics and the rules. The plant
 * and its shop live on the [com.shub39.grit.shared.ui.habit.avatar.AvatarPage].
 */
@Composable
fun EnergyPage(
    activity: ActivityReport?,
    onNavigateBack: () -> Unit,
    stats: ForkStats = ForkStats(),
    onAction: (HabitsAction) -> Unit = {},
    modifier: Modifier = Modifier,
    showNavigateBack: Boolean = true,
) {
    val windowSizeClass = LocalWindowSizeClass.current

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            colors =
                TopAppBarDefaults.topAppBarColors(
                    scrolledContainerColor = Color.Transparent,
                    containerColor = Color.Transparent,
                ),
            title = {
                Text(
                    text = stringResource(Res.string.energy_title),
                    fontFamily = flexFontEmphasis(),
                )
            },
            windowInsets =
                if (windowSizeClass.isExpanded()) {
                    WindowInsets(0)
                } else {
                    TopAppBarDefaults.windowInsets
                },
            navigationIcon = {
                if (showNavigateBack) {
                    FilledTonalIconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.nav_arrow_back),
                            contentDescription = "Navigate Back",
                        )
                    }
                }
            },
        )

        if (activity == null) return@Column

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 60.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item { EnergyBalance(energy = activity.energy) }

            item {
                Row(
                    modifier = Modifier.widthIn(max = 500.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    StreakCard(
                        emoji = "🔥",
                        days = activity.currentStreak,
                        label = stringResource(Res.string.energy_current_streak),
                        modifier = Modifier.weight(1f),
                    )
                    StreakCard(
                        emoji = "🏆",
                        days = activity.longestStreak,
                        label = stringResource(Res.string.energy_longest_streak),
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item { RecentDaysCard(days = activity.recentDays(DAYS_SHOWN)) }

            if (stats.activeDaysPerMonth.isNotEmpty()) {
                item { MonthlyActiveDaysCard(months = stats.activeDaysPerMonth) }
            }

            if (stats.energyPerMonth.isNotEmpty()) {
                item { MonthlyEnergyCard(months = stats.energyPerMonth) }
            }

            item {
                AutoFreezeCard(
                    enabled = activity.autoFreeze,
                    onToggle = { onAction(HabitsAction.SetAutoFreeze(it)) },
                )
            }

            item { RulesCard() }
        }
    }
}

@Composable
internal fun EnergyBalance(energy: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.widthIn(max = 500.dp).fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            WaterText(
                text = "$WATER_MARK $energy",
                style =
                    MaterialTheme.typography.displayLarge.copy(
                        fontFamily = flexFontEmphasis(),
                        fontWeight = FontWeight.Bold,
                    ),
            )
            Text(
                text = stringResource(Res.string.energy_balance),
                fontFamily = flexFontRounded(),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun StreakCard(emoji: String, days: Int, label: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = MaterialTheme.shapes.large) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "$emoji " + pluralStringResource(Res.plurals.energy_days, days, days),
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontFamily = flexFontEmphasis(),
                        fontWeight = FontWeight.Bold,
                    ),
                textAlign = TextAlign.Center,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun RecentDaysCard(days: List<RecentDay>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.widthIn(max = 500.dp).fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(Res.string.energy_last_days),
                fontFamily = flexFontRounded(),
                style = MaterialTheme.typography.titleMedium,
            )

            // oldest top left, today bottom right
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                days.chunked(DAYS_PER_ROW).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        row.forEach { DayCell(status = it.status, modifier = Modifier.weight(1f)) }
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                LegendItem(DayStatus.ACTIVE, stringResource(Res.string.energy_legend_active))
                LegendItem(DayStatus.FROZEN, stringResource(Res.string.energy_legend_frozen))
                LegendItem(DayStatus.MISSED, stringResource(Res.string.energy_legend_missed))
                LegendItem(DayStatus.OPEN, stringResource(Res.string.energy_legend_open))
                if (days.any { it.status == null }) {
                    LegendItem(null, stringResource(Res.string.energy_legend_before_start))
                }
            }
        }
    }
}

/** A day of the strip; `null` is a day before the start date. */
@Composable
private fun DayCell(status: DayStatus?, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.small
    val (background, symbol) =
        when (status) {
            DayStatus.ACTIVE -> colors.primary to "✓"
            DayStatus.FROZEN -> colors.tertiaryContainer to "❄️"
            DayStatus.MISSED -> colors.surfaceVariant to "–"
            DayStatus.OPEN,
            null -> Color.Transparent to ""
        }

    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .aspectRatio(1f)
                .background(background, shape)
                .then(
                    when (status) {
                        DayStatus.OPEN -> Modifier.border(BorderStroke(2.dp, colors.primary), shape)
                        null -> Modifier.border(BorderStroke(1.dp, colors.outlineVariant), shape)
                        else -> Modifier
                    }
                ),
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.labelMedium,
            color =
                when (status) {
                    DayStatus.ACTIVE -> colors.onPrimary
                    DayStatus.FROZEN -> colors.onTertiaryContainer
                    else -> colors.onSurfaceVariant
                },
        )
    }
}

@Composable
private fun LegendItem(status: DayStatus?, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        DayCell(status = status, modifier = Modifier.size(20.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun AutoFreezeCard(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.widthIn(max = 500.dp).fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(Res.string.energy_auto_freeze_title),
                    fontFamily = flexFontRounded(),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text =
                        stringResource(
                            if (enabled) Res.string.energy_auto_freeze_on
                            else Res.string.energy_auto_freeze_off
                        ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
    }
}

@Composable
private fun RulesCard(modifier: Modifier = Modifier) {
    val rules =
        listOf(
            stringResource(Res.string.energy_rule_active),
            stringResource(Res.string.energy_rule_energy, ActivityPlanner.ENERGY_PER_ACTIVE_DAY),
            stringResource(
                Res.string.energy_rule_bonus,
                DailyBonus.ENERGY,
                DailyBonus.MIN_ROUTINES,
            ),
            stringResource(
                Res.string.energy_rule_freeze,
                ActivityPlanner.FREEZE_COST,
                ActivityPlanner.MAX_FROZEN_DAYS_IN_A_ROW,
            ),
            stringResource(Res.string.energy_rule_frozen_day),
            stringResource(Res.string.energy_rule_break),
            stringResource(Res.string.energy_rule_auto_freeze),
        )

    Card(
        modifier = modifier.widthIn(max = 500.dp).fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(Res.string.energy_rules_title),
                fontFamily = flexFontRounded(),
                style = MaterialTheme.typography.titleMedium,
            )
            rules.forEach { rule ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "•", style = MaterialTheme.typography.bodyMedium)
                    WaterText(text = rule, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun EnergyPagePreview() {
    val today = LocalDate(2026, 10, 21)
    val activeDays =
        (1..21).filterNot { it in listOf(6, 13) }.map { LocalDate(2026, 10, it) }.toSet()
    EnergyPage(
        activity =
            ActivityPlanner.plan(
                activeDays = activeDays,
                startDate = LocalDate(2026, 10, 1),
                today = today,
            ),
        onNavigateBack = {},
    )
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun EnergyPageFreshInstallPreview() {
    val today = LocalDate(2026, 10, 21)
    EnergyPage(
        activity = ActivityPlanner.plan(activeDays = emptySet(), startDate = today, today = today),
        onNavigateBack = {},
    )
}
