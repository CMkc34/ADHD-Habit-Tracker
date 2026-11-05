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

import com.shub39.grit.core.arena.ArenaStore
import com.shub39.grit.core.arena.FightRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeArenaStore(fights: List<FightRecord> = emptyList()) : ArenaStore {
    val fights = MutableStateFlow(fights)

    override fun getFights(): Flow<List<FightRecord>> = fights

    override suspend fun addFight(record: FightRecord) = fights.update { it + record }

    override suspend fun updateFight(record: FightRecord) =
        fights.update { list -> list.map { if (it.isSameFight(record)) record else it } }

    override suspend fun replaceAll(records: List<FightRecord>) = fights.update { records }
}
