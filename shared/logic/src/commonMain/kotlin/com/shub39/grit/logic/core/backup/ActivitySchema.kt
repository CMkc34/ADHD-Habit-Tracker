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
package com.shub39.grit.logic.core.backup

import com.shub39.grit.core.activity.ActivityData
import com.shub39.grit.core.activity.AutoFreezeSwitches
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/** Activity streak and energy data in a backup; dates are ISO strings (`2026-10-21`). */
@Serializable
data class ActivitySchema(
    val startDate: String? = null,
    val activeDays: List<String> = emptyList(),
    val seenFreezes: List<String> = emptyList(),
    /** Day to whether automatic freezes were on from then; missing in older backups. */
    val autoFreeze: Map<String, Boolean> = emptyMap(),
)

fun ActivityData.toActivitySchema(): ActivitySchema {
    return ActivitySchema(
        startDate = startDate?.toString(),
        activeDays = activeDays.sorted().map(LocalDate::toString),
        seenFreezes = seenFreezes.sorted().map(LocalDate::toString),
        autoFreeze = autoFreeze.changes.mapKeys { it.key.toString() },
    )
}

fun ActivitySchema.toActivityData(): ActivityData {
    return ActivityData(
        startDate = startDate?.let(LocalDate::parse),
        activeDays = activeDays.mapTo(mutableSetOf(), LocalDate::parse),
        seenFreezes = seenFreezes.mapTo(mutableSetOf(), LocalDate::parse),
        autoFreeze = AutoFreezeSwitches(autoFreeze.mapKeys { LocalDate.parse(it.key) }),
    )
}
