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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shub39.grit.core.stats.Completion
import com.shub39.grit.core.stats.ForkStats
import com.shub39.grit.core.stats.HabitStreak
import com.shub39.grit.core.stats.MonthReview
import com.shub39.grit.core.stats.WeeklyChecks
import com.shub39.grit.shared.ui.GritPreviewWrapper
import com.shub39.grit.shared.ui.habit.ui.component.BarSeries
import com.shub39.grit.shared.ui.habit.ui.component.ComebacksCard
import com.shub39.grit.shared.ui.habit.ui.component.StatsBarChart
import com.shub39.grit.shared.ui.habit.ui.component.StatsCard
import com.shub39.grit.shared.ui.theme.flexFontEmphasis
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.stats_change_points
import grit.shared.ui.generated.resources.stats_checks_per_week
import grit.shared.ui.generated.resources.stats_month_review
import grit.shared.ui.generated.resources.stats_no_planned_days
import grit.shared.ui.generated.resources.stats_previous_month
import grit.shared.ui.generated.resources.stats_streak_overview
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

// every n-th week gets a date label, counted from the current week
private const val WEEK_LABEL_EVERY = 4

/** The fork's cards on the overall analytics page (phase 3). */
fun LazyStaggeredGridScope.forkOverallStats(stats: ForkStats, maxWidth: Dp) {
    item {
        MonthReviewCard(review = stats.monthReview, modifier = Modifier.widthIn(max = maxWidth))
    }

    item {
        ComebacksCard(
            comebacks = stats.totalComebacks,
            overall = true,
            modifier = Modifier.widthIn(max = maxWidth),
        )
    }

    if (stats.streakOverview.isNotEmpty()) {
        item {
            StreakOverviewCard(
                streaks = stats.streakOverview,
                modifier = Modifier.widthIn(max = maxWidth),
            )
        }
    }

    if (stats.checksPerWeek.isNotEmpty()) {
        item {
            WeeklyChecksCard(
                weeks = stats.checksPerWeek,
                modifier = Modifier.widthIn(max = maxWidth),
            )
        }
    }
}

private fun Completion.percent(): Int = (rate * 100).roundToInt()

@Composable
fun MonthReviewCard(review: MonthReview, modifier: Modifier = Modifier) {
    StatsCard(title = stringResource(Res.string.stats_month_review), modifier = modifier) {
        val current = review.current
        if (current == null) {
            Text(
                text = stringResource(Res.string.stats_no_planned_days),
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            CompletionSummary(current = current, previous = review.previous)
        }
    }
}

@Composable
private fun CompletionSummary(current: Completion, previous: Completion?) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "${current.percent()}%",
            style =
                MaterialTheme.typography.displaySmall.copy(
                    fontFamily = flexFontEmphasis(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                ),
        )
        Text(
            text = "${current.done}/${current.planned}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    if (previous != null) {
        val change = current.percent() - previous.percent()
        val arrow =
            when {
                change > 0 -> "↑"
                change < 0 -> "↓"
                else -> "→"
            }
        val points =
            pluralStringResource(Res.plurals.stats_change_points, abs(change), arrow, abs(change))
        Text(
            text =
                stringResource(Res.string.stats_previous_month, "${previous.percent()}%") +
                    " · $points",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun StreakOverviewCard(streaks: List<HabitStreak>, modifier: Modifier = Modifier) {
    StatsCard(title = stringResource(Res.string.stats_streak_overview), modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            streaks.forEach { streak ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = streak.title,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "🔥 ${streak.currentStreak}",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = "🏆 ${streak.bestStreak}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyChecksCard(weeks: List<WeeklyChecks>, modifier: Modifier = Modifier) {
    StatsCard(title = stringResource(Res.string.stats_checks_per_week), modifier = modifier) {
        StatsBarChart(
            labels =
                weeks.mapIndexed { index, week ->
                    if ((weeks.lastIndex - index) % WEEK_LABEL_EVERY == 0) week.weekStart.label()
                    else ""
                },
            series = listOf(BarSeries(MaterialTheme.colorScheme.secondary, weeks.map { it.checks })),
        )
    }
}

private fun LocalDate.label(): String = "$day.${month.number}."

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun MonthReviewCardPreview() {
    MonthReviewCard(
        review =
            MonthReview(
                current = Completion(done = 34, planned = 42),
                previous = Completion(done = 51, planned = 70),
            )
    )
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun StreakOverviewCardPreview() {
    StreakOverviewCard(
        streaks =
            listOf(
                HabitStreak("Meditate", currentStreak = 12, bestStreak = 30),
                HabitStreak("Read a few pages before going to bed", 5, 5),
                HabitStreak("Water the plants", 0, 8),
            )
    )
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun WeeklyChecksCardPreview() {
    val start = LocalDate(2026, 7, 27)
    WeeklyChecksCard(
        weeks =
            (0 until 12).map { week ->
                WeeklyChecks(
                    weekStart = LocalDate.fromEpochDays(start.toEpochDays() + week * 7),
                    checks = listOf(8, 12, 5, 0, 14, 20, 18, 9, 11, 17, 21, 6)[week],
                )
            }
    )
}
