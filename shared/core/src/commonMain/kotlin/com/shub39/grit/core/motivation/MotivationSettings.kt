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
package com.shub39.grit.core.motivation

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * User preferences for the motivation popup and the daily greeting, kept apart from the original
 * settings.
 */
interface MotivationSettings {
    /** Whether a check-in shows the motivation popup; on by default. */
    fun getPopupEnabled(): Flow<Boolean>

    suspend fun setPopupEnabled(enabled: Boolean)

    /** Whether the plant greets the user on the first app start of a day; on by default. */
    fun getGreetingEnabled(): Flow<Boolean>

    suspend fun setGreetingEnabled(enabled: Boolean)

    /** Day the [DailyGreeting] was last shown, `null` if never. */
    fun getLastGreetingDay(): Flow<LocalDate?>

    suspend fun setLastGreetingDay(day: LocalDate)

    /** Whether the first start setup (#139) is done or was never needed; false by default. */
    fun getSetupDone(): Flow<Boolean>

    suspend fun setSetupDone()
}
