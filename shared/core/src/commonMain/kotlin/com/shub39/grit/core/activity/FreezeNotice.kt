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
package com.shub39.grit.core.activity

import kotlinx.datetime.LocalDate

/** Tells the user that freezes saved the activity streak since they last looked. */
data class FreezeNotice(val streak: Int, val frozenDays: Set<LocalDate>, val energyLeft: Int) {
    val energySpent: Int
        get() = frozenDays.size * ActivityPlanner.FREEZE_COST

    companion object {
        /**
         * Collects the frozen days of the current streak that are not in [seenFreezes] into one
         * notice. Freezes of an earlier streak that broke later are left out, and a broken streak
         * gets no notice at all.
         */
        fun of(report: ActivityReport, seenFreezes: Set<LocalDate>): FreezeNotice? {
            if (report.currentStreak == 0) return null
            val lastBreak = report.days.filterValues { it == DayStatus.MISSED }.keys.maxOrNull()
            val newFreezes =
                report.frozenDays.filterTo(mutableSetOf()) {
                    (lastBreak == null || it > lastBreak) && it !in seenFreezes
                }
            if (newFreezes.isEmpty()) return null
            return FreezeNotice(
                streak = report.currentStreak,
                frozenDays = newFreezes,
                energyLeft = report.energy,
            )
        }
    }
}
