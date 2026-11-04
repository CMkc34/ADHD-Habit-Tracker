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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shub39.grit.core.habits.Habit
import com.shub39.grit.core.habits.HabitStatus
import com.shub39.grit.core.habits.HabitWithAnalytics
import com.shub39.grit.core.habits.MeasurableTargets
import com.shub39.grit.core.habits.Target
import com.shub39.grit.core.habits.card.RoutineDay
import com.shub39.grit.core.habits.card.RoutineDayState
import com.shub39.grit.core.habits.card.RoutineDays
import com.shub39.grit.core.habits.style.RoutineColor
import com.shub39.grit.core.habits.style.RoutineIcon
import com.shub39.grit.core.habits.style.RoutineStyle
import com.shub39.grit.core.now
import com.shub39.grit.core.theme.AppTheme
import com.shub39.grit.core.theme.Theme
import com.shub39.grit.core.toFormattedString
import com.shub39.grit.shared.ui.habit.style.color
import com.shub39.grit.shared.ui.habit.style.contentColor
import com.shub39.grit.shared.ui.theme.GritTheme
import com.shub39.grit.shared.ui.theme.paperShadow
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.analytics
import grit.shared.ui.generated.resources.check
import grit.shared.ui.generated.resources.heat
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.minus
import org.jetbrains.compose.resources.vectorResource

private const val DAYS_PER_ROW = 7
private const val TODAY_TINT = 0.18f

/**
 * Routine card of the habits list (fork, #81): header with name, flame with the streak and the
 * statistics button (name in capitals); below a divider the icon tile on the left and the last 14
 * days as 2 × 7 boxes on the right (oldest top left, today bottom right). Tapping the card checks
 * off today, tapping a box that day. The compact view shows only the header with a small tile.
 */
@Composable
fun RoutineCard(
    habitWithAnalytics: HabitWithAnalytics,
    style: RoutineStyle,
    completed: Boolean,
    onInsertStatus: (Habit, LocalDate, Double) -> Unit,
    onPrepareAnalytics: (Habit) -> Unit,
    onNavigateToAnalytics: () -> Unit,
    editState: Boolean,
    compactView: Boolean,
    reorderHandle: @Composable () -> Unit,
    is24Hr: Boolean,
    shape: Shape,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
) {
    val habit = habitWithAnalytics.habit
    val canCompleteToday = today.dayOfWeek in habit.days
    val accent = style.color.color

    val cardBackground by
        animateColorAsState(
            targetValue =
                when {
                    completed -> lerp(colorScheme.surfaceContainer, accent, TODAY_TINT)
                    else ->
                        colorScheme.surfaceContainer.copy(
                            alpha = if (canCompleteToday) 1f else 0.7f
                        )
                },
            animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
            label = "cardBackground",
        )
    val cardContent =
        colorScheme.onSurface.copy(alpha = if (completed || canCompleteToday) 1f else 0.7f)

    Card(
        colors =
            CardDefaults.outlinedCardColors(
                containerColor = cardBackground,
                contentColor = cardContent,
            ),
        onClick = {
            if (canCompleteToday) {
                onInsertStatus(
                    habit,
                    today,
                    habitWithAnalytics.statuses.find { it.date == today }?.value ?: 0.0,
                )
            }
        },
        shape = shape,
        modifier =
            modifier
                // a card that can be ticked lies on the page like a sheet of paper (#128)
                .then(if (cardBackground.alpha > 0.99f) Modifier.paperShadow(shape) else Modifier)
                .animateContentSize(animationSpec = MaterialTheme.motionScheme.fastSpatialSpec()),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (compactView) RoutineStyleTile(style = style, size = 36.dp)

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.title.uppercase(),
                        maxLines = 1,
                        style =
                            MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.basicMarquee(),
                    )
                    if (habit.reminder) {
                        Text(
                            text = habit.time.time.toFormattedString(is24Hr),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.heat),
                        contentDescription = null,
                        tint = accent,
                    )
                    Text(
                        text = habitWithAnalytics.currentStreak.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }

                IconButton(
                    onClick = {
                        onPrepareAnalytics(habit)
                        onNavigateToAnalytics()
                    },
                    modifier =
                        Modifier.size(
                            IconButtonDefaults.smallContainerSize(
                                IconButtonDefaults.IconButtonWidthOption.Wide
                            )
                        ),
                    colors =
                        IconButtonDefaults.iconButtonColors(
                            containerColor = accent,
                            contentColor = style.color.contentColor,
                        ),
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.analytics),
                        contentDescription = "Analytics",
                    )
                }

                AnimatedVisibility(visible = editState) {
                    Row {
                        Spacer(modifier = Modifier.width(4.dp))
                        reorderHandle()
                    }
                }
            }

            if (!compactView) {
                HorizontalDivider(color = colorScheme.outlineVariant)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RoutineStyleTile(style = style, size = 72.dp)

                    val days = RoutineDays.lastDays(habit, habitWithAnalytics.statuses, today)
                    Column(modifier = Modifier.weight(1f)) {
                        days.chunked(DAYS_PER_ROW).forEach { row ->
                            Row {
                                row.forEach { day ->
                                    DayBox(
                                        day = day,
                                        color = style.color,
                                        onClick = { onInsertStatus(habit, day.date, day.value) },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** One day: drawn at 28 dp inside a touch area of at least 40 dp. */
@Composable
private fun DayBox(
    day: RoutineDay,
    color: RoutineColor,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = color.color
    val shape = RoundedCornerShape(8.dp)
    val faint = colorScheme.onSurface.copy(alpha = 0.2f)
    val description = "${day.date.day}.${day.date.month.ordinal + 1}. ${day.state.name.lowercase()}"

    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .height(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(role = Role.Button, enabled = day.tappable, onClick = onClick)
                .semantics { contentDescription = description },
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier.size(28.dp)
                    .then(
                        when (day.state) {
                            RoutineDayState.DONE -> Modifier.background(accent, shape)
                            RoutineDayState.PARTIAL ->
                                Modifier.background(
                                        accent.copy(alpha = day.progress.coerceAtLeast(0.15f)),
                                        shape,
                                    )
                                    .border(BorderStroke(1.5.dp, accent), shape)
                            RoutineDayState.OPEN ->
                                Modifier.border(BorderStroke(1.5.dp, accent), shape)
                            RoutineDayState.NOT_PLANNED,
                            RoutineDayState.BEFORE_START ->
                                Modifier.border(BorderStroke(1.dp, faint), shape)
                        }
                    )
                    .then(
                        if (day.isToday) {
                            Modifier.border(BorderStroke(3.dp, colorScheme.onSurface), shape)
                        } else {
                            Modifier
                        }
                    ),
        ) {
            if (day.state == RoutineDayState.DONE) {
                Icon(
                    imageVector = vectorResource(Res.drawable.check),
                    contentDescription = null,
                    tint = color.contentColor,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

private val previewToday = LocalDate(2026, 10, 26)

private fun previewHabit(
    title: String,
    target: Double = 0.0,
    days: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
    statuses: List<Pair<Int, Double>> = emptyList(),
    streak: Int = 0,
): HabitWithAnalytics {
    val habit =
        Habit(
            id = 1,
            title = title,
            description = "",
            time = LocalDateTime(previewToday.minus(30, DateTimeUnit.DAY), LocalTime(7, 30)),
            days = days,
            index = 0,
            reminder = true,
            targetValue = target,
            targetUnit = if (target > 0) "min" else "",
        )
    val noTarget = Target(0.0, 0.0)
    return HabitWithAnalytics(
        habit = habit,
        consistency = 0f,
        statuses =
            statuses.map { (daysAgo, value) ->
                HabitStatus(
                    habitId = 1,
                    date = previewToday.minus(daysAgo, DateTimeUnit.DAY),
                    value = value,
                )
            },
        weeklyComparisonData = emptyList(),
        weekDayFrequencyData = emptyMap(),
        measurableTargets = MeasurableTargets(noTarget, noTarget, noTarget, noTarget, noTarget),
        currentStreak = streak,
        bestStreak = streak,
        startedDaysAgo = 30,
    )
}

@Composable
private fun RoutineCardPreviews(theme: Theme) {
    GritTheme(theme = theme) {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RoutineCard(
                    habitWithAnalytics =
                        previewHabit(
                            title = "Morgenroutine",
                            days = DayOfWeek.entries.toSet() - DayOfWeek.SUNDAY,
                            statuses = listOf(0, 1, 3, 4, 5, 6).map { it to 1.0 },
                            streak = 4,
                        ),
                    style = RoutineStyle.DEFAULT,
                    completed = true,
                    onInsertStatus = { _, _, _ -> },
                    onPrepareAnalytics = {},
                    onNavigateToAnalytics = {},
                    editState = false,
                    compactView = false,
                    reorderHandle = {},
                    is24Hr = true,
                    shape = RoundedCornerShape(28.dp),
                    today = previewToday,
                )
                RoutineCard(
                    habitWithAnalytics =
                        previewHabit(
                            title = "Lesen",
                            target = 20.0,
                            statuses = listOf(1 to 20.0, 2 to 10.0, 4 to 5.0, 5 to 25.0),
                            streak = 2,
                        ),
                    style = RoutineStyle(RoutineColor.INDIGO, RoutineIcon.BOOK),
                    completed = false,
                    onInsertStatus = { _, _, _ -> },
                    onPrepareAnalytics = {},
                    onNavigateToAnalytics = {},
                    editState = false,
                    compactView = false,
                    reorderHandle = {},
                    is24Hr = true,
                    shape = RoundedCornerShape(28.dp),
                    today = previewToday,
                )
                RoutineCard(
                    habitWithAnalytics = previewHabit(title = "Wasser trinken", streak = 12),
                    style = RoutineStyle(RoutineColor.SKY_BLUE, RoutineIcon.WATER),
                    completed = false,
                    onInsertStatus = { _, _, _ -> },
                    onPrepareAnalytics = {},
                    onNavigateToAnalytics = {},
                    editState = false,
                    compactView = true,
                    reorderHandle = {},
                    is24Hr = true,
                    shape = RoundedCornerShape(28.dp),
                    today = previewToday,
                )
            }
        }
    }
}

@Preview
@Composable
private fun RoutineCardLightPreview() {
    RoutineCardPreviews(Theme(appTheme = AppTheme.LIGHT))
}

@Preview
@Composable
private fun RoutineCardDarkPreview() {
    RoutineCardPreviews(Theme(appTheme = AppTheme.DARK))
}
