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
package com.shub39.grit.logic.fighter

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.shub39.grit.core.fighter.FighterStat
import com.shub39.grit.core.fighter.RoutineStatCodec
import com.shub39.grit.core.fighter.RoutineStatStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

/**
 * Stores the trained stat per routine in the app's shared preferences file, as a string set
 * `routine_stats` with one `"habitId|statKey"` entry per routine. Keeps the Room schema untouched.
 */
@Single(binds = [RoutineStatStore::class])
class RoutineStatStoreImpl(private val datastore: DataStore<Preferences>) : RoutineStatStore {

    companion object {
        private val statsKey = stringSetPreferencesKey("routine_stats")
    }

    override fun getStats(): Flow<Map<Long, FighterStat>> =
        datastore.data.map { prefs ->
            prefs[statsKey].orEmpty().mapNotNull(RoutineStatCodec::decode).toMap()
        }

    override suspend fun setStat(habitId: Long, stat: FighterStat) {
        datastore.edit {
            it[statsKey] =
                it[statsKey].orEmpty().without(habitId) + RoutineStatCodec.encode(habitId, stat)
        }
    }

    override suspend fun deleteStat(habitId: Long) {
        datastore.edit { it[statsKey] = it[statsKey].orEmpty().without(habitId) }
    }

    private fun Set<String>.without(habitId: Long): Set<String> =
        filterNot { RoutineStatCodec.belongsTo(it, habitId) }.toSet()
}
