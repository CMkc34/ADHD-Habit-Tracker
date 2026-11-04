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

import com.shub39.grit.core.habits.style.RoutineStyle
import kotlinx.serialization.Serializable

/**
 * Colour and icon of a routine in a backup (phase 5), by the keys of `RoutineColor`/`RoutineIcon`.
 */
@Serializable data class RoutineStyleSchema(val habitId: Long, val color: String, val icon: String)

fun Map<Long, RoutineStyle>.toRoutineStyleSchemas(): List<RoutineStyleSchema> =
    entries
        .sortedBy { it.key }
        .map { (habitId, style) -> RoutineStyleSchema(habitId, style.color.key, style.icon.key) }

/** Unknown keys (e.g. from a newer app) fall back to the default colour or icon. */
fun RoutineStyleSchema.toRoutineStyle(): RoutineStyle = RoutineStyle.fromKeys(color, icon)
