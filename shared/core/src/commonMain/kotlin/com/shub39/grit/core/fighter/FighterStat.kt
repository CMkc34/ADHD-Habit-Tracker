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

/** The four stats a routine can train. Each routine trains exactly one of them. */
enum class FighterStat(val key: String) {
    /** Damage. */
    STRENGTH("strength"),
    /** Turn order and dodging. */
    SPEED("speed"),
    /** Hit points. The default for routines without a stored stat. */
    STAMINA("stamina"),
    /** Critical hits and the special attack. */
    FOCUS("focus");

    companion object {
        val DEFAULT = STAMINA

        /** The stat stored under [key]; unknown or missing keys fall back to [DEFAULT]. */
        fun fromKey(key: String?): FighterStat = entries.firstOrNull { it.key == key } ?: DEFAULT
    }
}
