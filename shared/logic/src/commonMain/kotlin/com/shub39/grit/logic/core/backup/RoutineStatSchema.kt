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
package com.shub39.grit.logic.core.backup

import com.shub39.grit.core.fighter.FighterStat
import kotlinx.serialization.Serializable

/** The stat a routine trains in a backup (phase 6), by the key of `FighterStat`. */
@Serializable data class RoutineStatSchema(val habitId: Long, val stat: String)

fun Map<Long, FighterStat>.toRoutineStatSchemas(): List<RoutineStatSchema> =
    entries.sortedBy { it.key }.map { (habitId, stat) -> RoutineStatSchema(habitId, stat.key) }

/** An unknown key (e.g. from a newer app) falls back to [FighterStat.DEFAULT]. */
fun RoutineStatSchema.toFighterStat(): FighterStat = FighterStat.fromKey(stat)
