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
package com.shub39.grit.core.fighter

import kotlinx.coroutines.flow.Flow

/** The stat [habitId] trains, or [FighterStat.STAMINA] when none is stored. */
fun Map<Long, FighterStat>.statOf(habitId: Long): FighterStat = this[habitId] ?: FighterStat.DEFAULT

/** Store entries in the form `"habitId|statKey"`. */
object RoutineStatCodec {
    private const val SEPARATOR = "|"

    fun encode(habitId: Long, stat: FighterStat): String =
        listOf(habitId, stat.key).joinToString(SEPARATOR)

    /** `null` for a broken entry; an unknown stat key falls back to [FighterStat.STAMINA]. */
    fun decode(value: String): Pair<Long, FighterStat>? {
        val parts = value.split(SEPARATOR)
        if (parts.size != 2) return null
        val habitId = parts[0].toLongOrNull() ?: return null
        return habitId to FighterStat.fromKey(parts[1])
    }

    /** Whether [value] is the entry of [habitId], also when the rest of it is broken. */
    fun belongsTo(value: String, habitId: Long): Boolean =
        value.substringBefore(SEPARATOR) == habitId.toString()
}

/** Which [FighterStat] each routine trains. Kept outside the Room schema (SPEC 9.8). */
interface RoutineStatStore {
    /** Stored stats by habit id; routines without an entry train [FighterStat.STAMINA]. */
    fun getStats(): Flow<Map<Long, FighterStat>>

    suspend fun setStat(habitId: Long, stat: FighterStat)

    suspend fun deleteStat(habitId: Long)
}
