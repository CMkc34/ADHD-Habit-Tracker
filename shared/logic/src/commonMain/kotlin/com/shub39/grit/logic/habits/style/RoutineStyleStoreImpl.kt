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
package com.shub39.grit.logic.habits.style

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.shub39.grit.core.habits.style.RoutineStyle
import com.shub39.grit.core.habits.style.RoutineStyleCodec
import com.shub39.grit.core.habits.style.RoutineStyleStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

/**
 * Stores colour and icon per routine in the app's shared preferences file, as a string set
 * `routine_styles` with one `"habitId|colorKey|iconKey"` entry per routine. Keeps the Room schema
 * untouched.
 */
@Single(binds = [RoutineStyleStore::class])
class RoutineStyleStoreImpl(private val datastore: DataStore<Preferences>) : RoutineStyleStore {

    companion object {
        private val stylesKey = stringSetPreferencesKey("routine_styles")
    }

    override fun getStyles(): Flow<Map<Long, RoutineStyle>> =
        datastore.data.map { prefs ->
            prefs[stylesKey].orEmpty().mapNotNull(RoutineStyleCodec::decode).toMap()
        }

    override suspend fun setStyle(habitId: Long, style: RoutineStyle) {
        datastore.edit {
            it[stylesKey] =
                it[stylesKey]
                    .orEmpty()
                    .filterNot { entry -> RoutineStyleCodec.belongsTo(entry, habitId) }
                    .toSet() + RoutineStyleCodec.encode(habitId, style)
        }
    }

    override suspend fun deleteStyle(habitId: Long) {
        datastore.edit {
            it[stylesKey] =
                it[stylesKey]
                    .orEmpty()
                    .filterNot { entry -> RoutineStyleCodec.belongsTo(entry, habitId) }
                    .toSet()
        }
    }
}
