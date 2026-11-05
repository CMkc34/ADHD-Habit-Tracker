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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime

class StatRoutinesTest {

    private fun habit(id: Long, title: String, index: Int) =
        Habit(
            id = id,
            title = title,
            description = "",
            time = LocalDateTime(2026, 10, 1, 9, 0),
            days = DayOfWeek.entries.toSet(),
            index = index,
            reminder = false,
        )

    @Test
    fun `routines are grouped by the stat they train in list order`() {
        val habits =
            listOf(
                habit(1, "Meditation", index = 2),
                habit(2, "Push-ups", index = 0),
                habit(3, "Morning walk", index = 1),
            )
        val stats =
            mapOf(1L to FighterStat.FOCUS, 2L to FighterStat.STRENGTH, 3L to FighterStat.FOCUS)

        val routines = StatRoutines.of(habits, stats)

        assertEquals(listOf("Push-ups"), routines[FighterStat.STRENGTH])
        assertEquals(listOf("Morning walk", "Meditation"), routines[FighterStat.FOCUS])
    }

    @Test
    fun `every stat is listed and routines without a stat train stamina`() {
        val routines = StatRoutines.of(listOf(habit(7, "Water", index = 0)), emptyMap())

        assertEquals(FighterStat.entries.toSet(), routines.keys)
        assertEquals(listOf("Water"), routines[FighterStat.STAMINA])
        assertEquals(emptyList(), routines[FighterStat.SPEED])
    }
}
