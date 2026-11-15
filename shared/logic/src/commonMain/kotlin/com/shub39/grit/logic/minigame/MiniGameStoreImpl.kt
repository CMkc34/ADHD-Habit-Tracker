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
package com.shub39.grit.logic.minigame

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.shub39.grit.core.minigame.MiniGameStore
import com.shub39.grit.core.minigame.RunRecord
import com.shub39.grit.core.minigame.RunRecordCodec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

/** The mini-game run log in the Preferences DataStore, key `minigame_runs` (SPEC 10). */
@Single(binds = [MiniGameStore::class])
class MiniGameStoreImpl(private val datastore: DataStore<Preferences>) : MiniGameStore {

    companion object {
        private val runsKey = stringSetPreferencesKey("minigame_runs")
    }

    override fun getRuns(): Flow<List<RunRecord>> = datastore.data.map { prefs -> prefs.runs() }

    override suspend fun addRun(record: RunRecord) {
        datastore.edit { it[runsKey] = it[runsKey].orEmpty() + RunRecordCodec.encode(record) }
    }

    override suspend fun updateRun(record: RunRecord) {
        datastore.edit { prefs ->
            // entries this app can't read (e.g. from a newer version) are kept as they are
            prefs[runsKey] =
                prefs[runsKey]
                    .orEmpty()
                    .filterNot { RunRecordCodec.decode(it)?.isSameRun(record) == true }
                    .toSet() + RunRecordCodec.encode(record)
        }
    }

    override suspend fun replaceAll(records: List<RunRecord>) {
        datastore.edit { it[runsKey] = records.map(RunRecordCodec::encode).toSet() }
    }

    private fun Preferences.runs(): List<RunRecord> =
        this[runsKey].orEmpty().mapNotNull(RunRecordCodec::decode).sortedBy { it.startedAt }
}
