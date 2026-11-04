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

interface ActivityRepo {
    /** Activity streak, energy and freezes, recalculated whenever the stored activity changes. */
    fun getActivityReport(): Flow<ActivityReport>

    /** Freezes that saved the current streak and were not shown yet, `null` when there are none. */
    fun getFreezeNotice(): Flow<FreezeNotice?>

    /** Remembers [days] as shown, so their freezes are not announced again. */
    suspend fun markFreezesSeen(days: Set<LocalDate>)

    /** Switches automatic freezes on or off from today on; earlier freezes stay. */
    suspend fun setAutoFreeze(enabled: Boolean)
}
