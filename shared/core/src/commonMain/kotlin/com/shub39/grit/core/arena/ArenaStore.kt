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
package com.shub39.grit.core.arena

import kotlinx.coroutines.flow.Flow

/** The arena's fight log. Kept outside the Room schema (SPEC 9.8). */
interface ArenaStore {
    fun getFights(): Flow<List<FightRecord>>

    suspend fun addFight(record: FightRecord)

    /** Replaces the stored fight with the same day, opponent and start as [record]. */
    suspend fun updateFight(record: FightRecord)

    /** Replaces the whole log with [records], used when restoring a backup. */
    suspend fun replaceAll(records: List<FightRecord>)
}
