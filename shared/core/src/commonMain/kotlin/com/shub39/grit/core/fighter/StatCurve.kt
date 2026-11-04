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
 * Level curve of a stat: level L is reached at `5 * (L - 1) * L / 2` XP, so 0, 5, 15, 30, 50 …
 * Every level needs 5 XP more than the one before. This is the only place the curve is defined.
 */
object StatCurve {
    const val STEP = 5

    /** XP needed to reach [level]; level 1 and below need nothing. */
    fun xpForLevel(level: Int): Int = if (level <= 1) 0 else STEP * (level - 1) * level / 2

    /** The level reached with [xp], at least 1. */
    fun levelFor(xp: Int): Int {
        var level = 1
        while (xp >= xpForLevel(level + 1)) level++
        return level
    }

    /** How far [xp] is on its way from the current level to the next one, from 0 to 1. */
    fun progress(xp: Int): Float {
        val level = levelFor(xp)
        val start = xpForLevel(level)
        val end = xpForLevel(level + 1)
        return ((xp.coerceAtLeast(0) - start).toFloat() / (end - start)).coerceIn(0f, 1f)
    }
}
