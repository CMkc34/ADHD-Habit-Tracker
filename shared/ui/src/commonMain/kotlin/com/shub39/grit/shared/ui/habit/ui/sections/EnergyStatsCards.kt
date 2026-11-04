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

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.shub39.grit.core.activity.ActivityPlanner
import com.shub39.grit.core.stats.MonthlyActiveDays
import com.shub39.grit.core.stats.MonthlyEnergy
import com.shub39.grit.core.stats.StatsCalculator
import com.shub39.grit.shared.ui.GritPreviewWrapper
import com.shub39.grit.shared.ui.habit.ui.component.BarSeries
import com.shub39.grit.shared.ui.habit.ui.component.StatsBarChart
import com.shub39.grit.shared.ui.habit.ui.component.StatsCard
import com.shub39.grit.shared.ui.habit.ui.component.StatsLegend
import com.shub39.grit.shared.ui.habit.ui.component.monthLabel
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.stats_active_days_per_month
import grit.shared.ui.generated.resources.stats_energy_earned
import grit.shared.ui.generated.resources.stats_energy_per_month
import grit.shared.ui.generated.resources.stats_energy_spent
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

/** Active days of the last months; months before the start date stay empty. */
@Composable
fun MonthlyActiveDaysCard(months: List<MonthlyActiveDays>, modifier: Modifier = Modifier) {
    StatsCard(title = stringResource(Res.string.stats_active_days_per_month), modifier = modifier) {
        StatsBarChart(
            labels = months.map { monthLabel(it.month) },
            series =
                listOf(
                    BarSeries(
                        color = MaterialTheme.colorScheme.primary,
                        values = months.map { if (it.beforeStart) null else it.activeDays },
                    )
                ),
        )
    }
}

/** Energy earned by active days and spent on freezes per month. */
@Composable
fun MonthlyEnergyCard(months: List<MonthlyEnergy>, modifier: Modifier = Modifier) {
    val earnedColor = MaterialTheme.colorScheme.primary
    val spentColor = MaterialTheme.colorScheme.tertiary

    StatsCard(title = stringResource(Res.string.stats_energy_per_month), modifier = modifier) {
        StatsBarChart(
            labels = months.map { monthLabel(it.month) },
            series =
                listOf(
                    BarSeries(earnedColor, months.map { if (it.beforeStart) null else it.earned }),
                    BarSeries(spentColor, months.map { if (it.beforeStart) null else it.spent }),
                ),
        )
        StatsLegend(
            items =
                listOf(
                    earnedColor to stringResource(Res.string.stats_energy_earned),
                    spentColor to stringResource(Res.string.stats_energy_spent),
                )
        )
    }
}

private val previewReport =
    ActivityPlanner.plan(
        activeDays =
            (1..21).filterNot { it in listOf(6, 13) }.map { LocalDate(2026, 10, it) }.toSet() +
                (5..30).map { LocalDate(2026, 9, it) },
        startDate = LocalDate(2026, 9, 5),
        today = LocalDate(2026, 10, 21),
    )

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun MonthlyActiveDaysCardPreview() {
    MonthlyActiveDaysCard(
        months = StatsCalculator.activeDaysPerMonth(previewReport, LocalDate(2026, 10, 21))
    )
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun MonthlyEnergyCardPreview() {
    MonthlyEnergyCard(
        months = StatsCalculator.energyPerMonth(previewReport, LocalDate(2026, 10, 21))
    )
}
