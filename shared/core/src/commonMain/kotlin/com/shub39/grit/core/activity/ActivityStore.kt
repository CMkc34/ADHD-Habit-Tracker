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

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/** What is stored about the user's activity; everything else is derived by [ActivityPlanner]. */
data class ActivityData(
    val startDate: LocalDate? = null,
    val activeDays: Set<LocalDate> = emptySet(),
    val seenFreezes: Set<LocalDate> = emptySet(),
    val autoFreeze: AutoFreezeSwitches = AutoFreezeSwitches(),
) {
    /**
     * Combines two records, e.g. the current one and a restored backup: the earlier start date
     * wins, active days, seen freezes and freeze switches are joined.
     */
    fun mergedWith(other: ActivityData): ActivityData =
        ActivityData(
            startDate = listOfNotNull(startDate, other.startDate).minOrNull(),
            activeDays = activeDays + other.activeDays,
            seenFreezes = seenFreezes + other.seenFreezes,
            autoFreeze = autoFreeze.mergedWith(other.autoFreeze),
        )
}

interface ActivityStore {
    fun getActivity(): Flow<ActivityData>

    /** Sets the start date to [day] unless one is already stored. */
    suspend fun ensureStartDate(day: LocalDate)

    /** Marks [day] as a day the user made a check-in; also sets the start date if missing. */
    suspend fun markActive(day: LocalDate)

    /** Remembers [days] as freezes the user has already been told about. */
    suspend fun markFreezesSeen(days: Set<LocalDate>)

    /** Switches automatic freezes on or off from [day] on. */
    suspend fun setAutoFreeze(day: LocalDate, enabled: Boolean)

    /** Replaces everything stored with [data], used when restoring a backup. */
    suspend fun replaceAll(data: ActivityData)
}
