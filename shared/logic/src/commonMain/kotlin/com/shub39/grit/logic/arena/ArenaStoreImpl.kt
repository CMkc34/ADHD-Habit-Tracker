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
package com.shub39.grit.logic.arena

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.shub39.grit.core.arena.ArenaStore
import com.shub39.grit.core.arena.FightRecord
import com.shub39.grit.core.arena.FightRecordCodec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

/**
 * Stores the fight log in the app's shared preferences file, as a string set `arena_fights` with
 * one `"yyyy-MM-dd|opponentId|OUTCOME|startedAt"` entry per fight. Keeps the Room schema untouched.
 */
@Single(binds = [ArenaStore::class])
class ArenaStoreImpl(private val datastore: DataStore<Preferences>) : ArenaStore {

    companion object {
        private val fightsKey = stringSetPreferencesKey("arena_fights")
    }

    override fun getFights(): Flow<List<FightRecord>> =
        datastore.data.map { prefs -> prefs.fights() }

    override suspend fun addFight(record: FightRecord) {
        datastore.edit { it[fightsKey] = it[fightsKey].orEmpty() + FightRecordCodec.encode(record) }
    }

    override suspend fun updateFight(record: FightRecord) {
        datastore.edit { prefs ->
            // entries this app can't read (e.g. from a newer version) are kept as they are
            prefs[fightsKey] =
                prefs[fightsKey]
                    .orEmpty()
                    .filterNot { FightRecordCodec.decode(it)?.isSameFight(record) == true }
                    .toSet() + FightRecordCodec.encode(record)
        }
    }

    override suspend fun replaceAll(records: List<FightRecord>) {
        datastore.edit { it[fightsKey] = records.map(FightRecordCodec::encode).toSet() }
    }

    private fun Preferences.fights(): List<FightRecord> =
        this[fightsKey].orEmpty().mapNotNull(FightRecordCodec::decode).sortedBy { it.startedAt }
}
