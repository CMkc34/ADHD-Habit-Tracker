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

/**
 * Store entries of the stat levels the user has been told about, in the form `"statKey|level"`
 * (`avatar_seen_stat_levels`, SPEC 9.8).
 */
object StatLevelCodec {
    private const val SEPARATOR = "|"

    fun encode(stat: FighterStat, level: Int): String =
        listOf(stat.key, level).joinToString(SEPARATOR)

    /** `null` for a broken entry or an unknown stat key. */
    fun decode(value: String): Pair<FighterStat, Int>? {
        val parts = value.split(SEPARATOR)
        if (parts.size != 2) return null
        val stat = FighterStat.entries.firstOrNull { it.key == parts[0] } ?: return null
        val level = parts[1].toIntOrNull() ?: return null
        return stat to level
    }

    fun encodeAll(levels: Map<FighterStat, Int>): Set<String> =
        levels.mapTo(mutableSetOf()) { (stat, level) -> encode(stat, level) }

    /** Broken entries are dropped; for a stat stored twice the higher level wins. */
    fun decodeAll(values: Set<String>): Map<FighterStat, Int> =
        values.mapNotNull(::decode).groupBy({ it.first }, { it.second }).mapValues { (_, levels) ->
            levels.max()
        }
}
