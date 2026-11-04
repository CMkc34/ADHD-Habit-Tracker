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
package com.shub39.grit.core.habits.style

import kotlinx.coroutines.flow.Flow

/** Colour and icon of a routine, shown on its card. */
data class RoutineStyle(
    val color: RoutineColor = RoutineColor.GREEN,
    val icon: RoutineIcon = RoutineIcon.BOLT,
) {
    companion object {
        /** Used for routines without a stored style. */
        val DEFAULT = RoutineStyle()

        /** Builds a style from stored keys; an unknown key falls back to the default part. */
        fun fromKeys(colorKey: String, iconKey: String): RoutineStyle =
            RoutineStyle(
                color = RoutineColor.fromKey(colorKey) ?: DEFAULT.color,
                icon = RoutineIcon.fromKey(iconKey) ?: DEFAULT.icon,
            )
    }
}

/** The style for [habitId], or [RoutineStyle.DEFAULT] when none is stored. */
fun Map<Long, RoutineStyle>.styleOf(habitId: Long): RoutineStyle =
    this[habitId] ?: RoutineStyle.DEFAULT

/** Store entries in the form `"habitId|colorKey|iconKey"`. */
object RoutineStyleCodec {
    private const val SEPARATOR = "|"

    fun encode(habitId: Long, style: RoutineStyle): String =
        listOf(habitId, style.color.key, style.icon.key).joinToString(SEPARATOR)

    /** `null` for a broken entry; unknown colour or icon keys fall back to the default. */
    fun decode(value: String): Pair<Long, RoutineStyle>? {
        val parts = value.split(SEPARATOR)
        if (parts.size != 3) return null
        val habitId = parts[0].toLongOrNull() ?: return null
        return habitId to RoutineStyle.fromKeys(parts[1], parts[2])
    }

    /** Whether [value] is the entry of [habitId], also when the rest of it is broken. */
    fun belongsTo(value: String, habitId: Long): Boolean =
        value.substringBefore(SEPARATOR) == habitId.toString()
}

interface RoutineStyleStore {
    /** Stored styles by habit id; routines without an entry use [RoutineStyle.DEFAULT]. */
    fun getStyles(): Flow<Map<Long, RoutineStyle>>

    suspend fun setStyle(habitId: Long, style: RoutineStyle)

    suspend fun deleteStyle(habitId: Long)
}
