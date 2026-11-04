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
package com.shub39.grit.logic.motivation

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.shub39.grit.core.motivation.MotivationSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.Single

@Single(binds = [MotivationSettings::class])
class MotivationSettingsImpl(private val datastore: DataStore<Preferences>) : MotivationSettings {

    companion object {
        // a new key, so the popup is on once for everyone who had switched it off before (#109)
        private val popupEnabledKey = booleanPreferencesKey("motivation_popup_on")
        private val greetingEnabledKey = booleanPreferencesKey("daily_greeting_on")
        private val lastGreetingKey = stringPreferencesKey("daily_greeting_last_day")
        private val setupDoneKey = booleanPreferencesKey("user_setup_done")
    }

    override fun getPopupEnabled(): Flow<Boolean> =
        datastore.data.map { prefs -> prefs[popupEnabledKey] ?: true }

    override suspend fun setPopupEnabled(enabled: Boolean) {
        datastore.edit { prefs -> prefs[popupEnabledKey] = enabled }
    }

    override fun getGreetingEnabled(): Flow<Boolean> =
        datastore.data.map { prefs -> prefs[greetingEnabledKey] ?: true }

    override suspend fun setGreetingEnabled(enabled: Boolean) {
        datastore.edit { prefs -> prefs[greetingEnabledKey] = enabled }
    }

    override fun getLastGreetingDay(): Flow<LocalDate?> =
        datastore.data.map { prefs ->
            prefs[lastGreetingKey]?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        }

    override suspend fun setLastGreetingDay(day: LocalDate) {
        datastore.edit { prefs -> prefs[lastGreetingKey] = day.toString() }
    }

    override fun getSetupDone(): Flow<Boolean> =
        datastore.data.map { prefs -> prefs[setupDoneKey] ?: false }

    override suspend fun setSetupDone() {
        datastore.edit { prefs -> prefs[setupDoneKey] = true }
    }
}
