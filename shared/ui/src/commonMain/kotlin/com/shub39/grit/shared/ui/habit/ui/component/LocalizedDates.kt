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

import androidx.compose.runtime.Composable
import com.shub39.grit.core.habits.WeeklyTimePeriod
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.date_month_full_1
import grit.shared.ui.generated.resources.date_month_full_10
import grit.shared.ui.generated.resources.date_month_full_11
import grit.shared.ui.generated.resources.date_month_full_12
import grit.shared.ui.generated.resources.date_month_full_2
import grit.shared.ui.generated.resources.date_month_full_3
import grit.shared.ui.generated.resources.date_month_full_4
import grit.shared.ui.generated.resources.date_month_full_5
import grit.shared.ui.generated.resources.date_month_full_6
import grit.shared.ui.generated.resources.date_month_full_7
import grit.shared.ui.generated.resources.date_month_full_8
import grit.shared.ui.generated.resources.date_month_full_9
import grit.shared.ui.generated.resources.date_period_1y
import grit.shared.ui.generated.resources.date_period_2m
import grit.shared.ui.generated.resources.date_period_4m
import grit.shared.ui.generated.resources.date_period_8m
import grit.shared.ui.generated.resources.date_weekday_letter_1
import grit.shared.ui.generated.resources.date_weekday_letter_2
import grit.shared.ui.generated.resources.date_weekday_letter_3
import grit.shared.ui.generated.resources.date_weekday_letter_4
import grit.shared.ui.generated.resources.date_weekday_letter_5
import grit.shared.ui.generated.resources.date_weekday_letter_6
import grit.shared.ui.generated.resources.date_weekday_letter_7
import grit.shared.ui.generated.resources.date_weekday_short_1
import grit.shared.ui.generated.resources.date_weekday_short_2
import grit.shared.ui.generated.resources.date_weekday_short_3
import grit.shared.ui.generated.resources.date_weekday_short_4
import grit.shared.ui.generated.resources.date_weekday_short_5
import grit.shared.ui.generated.resources.date_weekday_short_6
import grit.shared.ui.generated.resources.date_weekday_short_7
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
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.isoDayNumber
import org.jetbrains.compose.resources.stringResource

/*
 * Month and weekday names in the app's language (#18). Upstream formats dates with kotlinx-datetime's
 * English names only; these read the names from the string resources instead.
 */

private val shortMonths =
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

private val fullMonths =
    listOf(
        Res.string.date_month_full_1,
        Res.string.date_month_full_2,
        Res.string.date_month_full_3,
        Res.string.date_month_full_4,
        Res.string.date_month_full_5,
        Res.string.date_month_full_6,
        Res.string.date_month_full_7,
        Res.string.date_month_full_8,
        Res.string.date_month_full_9,
        Res.string.date_month_full_10,
        Res.string.date_month_full_11,
        Res.string.date_month_full_12,
    )

private val shortWeekdays =
    listOf(
        Res.string.date_weekday_short_1,
        Res.string.date_weekday_short_2,
        Res.string.date_weekday_short_3,
        Res.string.date_weekday_short_4,
        Res.string.date_weekday_short_5,
        Res.string.date_weekday_short_6,
        Res.string.date_weekday_short_7,
    )

private val weekdayLetters =
    listOf(
        Res.string.date_weekday_letter_1,
        Res.string.date_weekday_letter_2,
        Res.string.date_weekday_letter_3,
        Res.string.date_weekday_letter_4,
        Res.string.date_weekday_letter_5,
        Res.string.date_weekday_letter_6,
        Res.string.date_weekday_letter_7,
    )

/** Short month names ("Jan", "Mär"), for dates like "01 Mär 2026". */
@Composable fun shortMonthNames(): MonthNames = MonthNames(shortMonths.map { stringResource(it) })

/** Full month names ("Januar"), for calendar headers. */
@Composable fun fullMonthNames(): MonthNames = MonthNames(fullMonths.map { stringResource(it) })

/** One letter per weekday for the heat map and the reminder days ("M", "D", ...). */
@Composable fun DayOfWeek.letter(): String = stringResource(weekdayLetters[isoDayNumber - 1])

/**
 * The weekday breakdown keys its data by kotlinx-datetime's English short names ("Mon"); this shows
 * them in the app's language and leaves an unknown key as it is.
 */
@Composable
fun weekdayLabel(englishShortName: String): String {
    val index = DayOfWeekNames.ENGLISH_ABBREVIATED.names.indexOf(englishShortName)
    return if (index < 0) englishShortName else stringResource(shortWeekdays[index])
}

/** The period buttons of the weekly activity chart ("2M", "1Y" / "1J"). */
@Composable
fun WeeklyTimePeriod.label(): String =
    stringResource(
        when (this) {
            WeeklyTimePeriod.MONTHS_2 -> Res.string.date_period_2m
            WeeklyTimePeriod.MONTHS_4 -> Res.string.date_period_4m
            WeeklyTimePeriod.MONTHS_8 -> Res.string.date_period_8m
            WeeklyTimePeriod.YEARS_1 -> Res.string.date_period_1y
        }
    )
