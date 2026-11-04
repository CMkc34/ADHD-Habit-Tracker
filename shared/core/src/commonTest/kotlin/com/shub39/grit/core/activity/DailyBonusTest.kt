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
package com.shub39.grit.core.activity

import com.shub39.grit.core.habits.Habit
import com.shub39.grit.core.habits.HabitStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

class DailyBonusTest {

    private val day = LocalDate(2026, 10, 30)
    private val next = LocalDate(2026, 10, 31)

    private val read = habit(1)
    private val walk = habit(2)
    private val water = habit(3, target = 8.0)

    private fun habit(id: Long, target: Double = 0.0) =
        Habit(
            id = id,
            title = "Routine $id",
            description = "",
            time = LocalDateTime(2026, 10, 1, 9, 0),
            days = DayOfWeek.entries.toSet(),
            index = id.toInt(),
            reminder = false,
            targetValue = target,
        )

    @Test
    fun `two routines on one day earn the bonus`() {
        val statuses =
            listOf(HabitStatus(habitId = 1, date = day), HabitStatus(habitId = 2, date = day))

        assertEquals(setOf(day), DailyBonus.days(listOf(read, walk), statuses))
    }

    @Test
    fun `one routine or two on different days earn nothing`() {
        val statuses =
            listOf(HabitStatus(habitId = 1, date = day), HabitStatus(habitId = 2, date = next))

        assertEquals(emptySet(), DailyBonus.days(listOf(read, walk), statuses))
    }

    @Test
    fun `three routines still earn one bonus`() {
        val statuses = (1L..3L).map { HabitStatus(habitId = it, date = day, value = 8.0) }

        assertEquals(setOf(day), DailyBonus.days(listOf(read, walk, water), statuses))
    }

    @Test
    fun `a measurable routine counts only once its target is reached`() {
        val below =
            listOf(
                HabitStatus(habitId = 1, date = day),
                HabitStatus(habitId = 3, date = day, value = 5.0),
            )
        val reached =
            listOf(
                HabitStatus(habitId = 1, date = day),
                HabitStatus(habitId = 3, date = day, value = 8.0),
            )

        assertEquals(emptySet(), DailyBonus.days(listOf(read, water), below))
        assertEquals(setOf(day), DailyBonus.days(listOf(read, water), reached))
    }

    @Test
    fun `check-ins of deleted routines do not count`() {
        val statuses =
            listOf(HabitStatus(habitId = 1, date = day), HabitStatus(habitId = 9, date = day))

        assertEquals(emptySet(), DailyBonus.days(listOf(read), statuses))
    }
}
