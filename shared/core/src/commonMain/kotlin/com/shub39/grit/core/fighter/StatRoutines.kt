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

import com.shub39.grit.core.habits.Habit

/** Which routines train which stat, for the stat panel of the Fighter tab (#9). */
object StatRoutines {
    /**
     * The titles of the routines that train each stat, in the order of the routine list. Every stat
     * is in the result, with an empty list when no routine trains it; routines without a stored
     * stat train [FighterStat.DEFAULT].
     */
    fun of(habits: List<Habit>, stats: Map<Long, FighterStat>): Map<FighterStat, List<String>> {
        val byStat = habits.sortedBy { it.index }.groupBy({ stats.statOf(it.id) }, { it.title })
        return FighterStat.entries.associateWith { byStat[it].orEmpty() }
    }
}
