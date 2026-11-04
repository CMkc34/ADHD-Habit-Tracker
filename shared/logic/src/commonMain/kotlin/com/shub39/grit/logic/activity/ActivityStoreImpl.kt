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
package com.shub39.grit.logic.activity

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.shub39.grit.core.activity.ActivityData
import com.shub39.grit.core.activity.ActivityStore
import com.shub39.grit.core.activity.AutoFreezeSwitches
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.Single

/** Stores activity in the app's shared preferences file, under keys prefixed with `activity_`. */
@Single(binds = [ActivityStore::class])
class ActivityStoreImpl(private val datastore: DataStore<Preferences>) : ActivityStore {

    companion object {
        private val startDateKey = stringPreferencesKey("activity_start_date")
        private val activeDaysKey = stringSetPreferencesKey("activity_days")
        private val seenFreezesKey = stringSetPreferencesKey("activity_seen_freezes")
        // "2026-10-22|false": automatic freezes switched off on that day
        private val autoFreezeKey = stringSetPreferencesKey("activity_auto_freeze")
    }

    override fun getActivity(): Flow<ActivityData> =
        datastore.data.map { prefs ->
            ActivityData(
                startDate = prefs[startDateKey]?.let(LocalDate::parse),
                activeDays = prefs[activeDaysKey].orEmpty().toDates(),
                seenFreezes = prefs[seenFreezesKey].orEmpty().toDates(),
                autoFreeze = prefs[autoFreezeKey].orEmpty().toSwitches(),
            )
        }

    override suspend fun ensureStartDate(day: LocalDate) {
        datastore.edit { if (it[startDateKey] == null) it[startDateKey] = day.toString() }
    }

    override suspend fun markActive(day: LocalDate) {
        datastore.edit {
            if (it[startDateKey] == null) it[startDateKey] = day.toString()
            it[activeDaysKey] = it[activeDaysKey].orEmpty() + day.toString()
        }
    }

    override suspend fun markFreezesSeen(days: Set<LocalDate>) {
        datastore.edit {
            it[seenFreezesKey] = it[seenFreezesKey].orEmpty() + days.map(LocalDate::toString)
        }
    }

    override suspend fun setAutoFreeze(day: LocalDate, enabled: Boolean) {
        datastore.edit {
            val switches = it[autoFreezeKey].orEmpty().toSwitches().switched(day, enabled)
            it[autoFreezeKey] = switches.toStrings()
        }
    }

    override suspend fun replaceAll(data: ActivityData) {
        datastore.edit {
            val startDate = data.startDate
            if (startDate == null) it.remove(startDateKey)
            else it[startDateKey] = startDate.toString()
            it[activeDaysKey] = data.activeDays.mapTo(mutableSetOf(), LocalDate::toString)
            it[seenFreezesKey] = data.seenFreezes.mapTo(mutableSetOf(), LocalDate::toString)
            it[autoFreezeKey] = data.autoFreeze.toStrings()
        }
    }

    private fun Set<String>.toDates(): Set<LocalDate> =
        mapTo(mutableSetOf()) { LocalDate.parse(it) }

    private fun Set<String>.toSwitches(): AutoFreezeSwitches =
        AutoFreezeSwitches(
            associate { entry ->
                val (day, enabled) = entry.split("|")
                LocalDate.parse(day) to enabled.toBoolean()
            }
        )

    private fun AutoFreezeSwitches.toStrings(): Set<String> =
        changes.mapTo(mutableSetOf()) { (day, enabled) -> "$day|$enabled" }
}
