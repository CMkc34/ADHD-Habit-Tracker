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

/**
 * When the user switched automatic freezes on or off. A switch counts from its day on, the last
 * switch of a day for the whole day. Without any switch, freezes are on.
 */
data class AutoFreezeSwitches(val changes: Map<LocalDate, Boolean> = emptyMap()) {
    /** The current setting, as shown by the switch on the energy page. */
    val enabled: Boolean
        get() = changes.maxByOrNull { it.key }?.value ?: true

    fun isOn(day: LocalDate): Boolean =
        changes.filterKeys { it <= day }.maxByOrNull { it.key }?.value ?: true

    fun switched(day: LocalDate, enabled: Boolean): AutoFreezeSwitches =
        AutoFreezeSwitches(changes + (day to enabled))

    /** Joins two records; for a day in both, this record wins. */
    fun mergedWith(other: AutoFreezeSwitches): AutoFreezeSwitches =
        AutoFreezeSwitches(other.changes + changes)
}
