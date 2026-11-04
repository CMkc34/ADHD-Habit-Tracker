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
package com.shub39.grit.core.avatar

import com.shub39.grit.core.activity.ActivityReport
import com.shub39.grit.core.activity.DayStatus

/**
 * The sky above the plant's meadow. The sun shines more the longer the streak runs; after the
 * streak broke it hides, and the longer the user stays away the more clouds cover the sky.
 */
enum class Weather {
    /** A streak of [SUNNY_FROM_STREAK] days or more: a big sun with only a few small clouds. */
    SUNNY,
    /** A streak of [FAIR_FROM_STREAK] days or more: sun and some clouds. */
    FAIR,
    /** A streak that just started (or nothing done yet): the sun peeks out behind a cloud. */
    HAZY,
    /** The streak broke less than [OVERCAST_FROM_INACTIVE_DAYS] days ago: clouds, no sun. */
    CLOUDY,
    /** Away for [OVERCAST_FROM_INACTIVE_DAYS] days or more: a dull sky full of grey clouds. */
    OVERCAST;

    companion object {
        const val FAIR_FROM_STREAK = 3
        const val SUNNY_FROM_STREAK = 7
        const val OVERCAST_FROM_INACTIVE_DAYS = 7

        fun of(report: ActivityReport): Weather {
            val streak = report.currentStreak
            val inactive = inactiveDays(report)
            return when {
                streak >= SUNNY_FROM_STREAK -> SUNNY
                streak >= FAIR_FROM_STREAK -> FAIR
                streak > 0 || inactive == 0 -> HAZY
                inactive >= OVERCAST_FROM_INACTIVE_DAYS -> OVERCAST
                else -> CLOUDY
            }
        }

        /** Missed days since the last check-in; today without a check-in does not count. */
        fun inactiveDays(report: ActivityReport): Int =
            report.days.values
                .reversed()
                .dropWhile { it == DayStatus.OPEN }
                .takeWhile { it == DayStatus.MISSED }
                .size
    }
}
