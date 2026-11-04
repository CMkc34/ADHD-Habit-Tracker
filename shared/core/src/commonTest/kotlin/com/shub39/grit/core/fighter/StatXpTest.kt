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
import com.shub39.grit.core.habits.HabitStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

class StatXpTest {

    private val day = LocalDate(2026, 10, 30)
    private val next = LocalDate(2026, 10, 31)

    private val pushups = habit(1)
    private val walk = habit(2)
    private val water = habit(3, target = 8.0)

    private val stats =
        mapOf(1L to FighterStat.STRENGTH, 2L to FighterStat.SPEED, 3L to FighterStat.STAMINA)

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

    private fun xp(habits: List<Habit>, statuses: List<HabitStatus>) =
        StatXp.of(habits, statuses) { stats[it] ?: FighterStat.DEFAULT }

    @Test
    fun `one xp per routine and date`() {
        val statuses =
            listOf(
                HabitStatus(habitId = 1, date = day),
                HabitStatus(habitId = 1, date = next),
                HabitStatus(habitId = 2, date = day),
            )

        val xp = xp(listOf(pushups, walk), statuses)

        assertEquals(2, xp[FighterStat.STRENGTH])
        assertEquals(1, xp[FighterStat.SPEED])
    }

    @Test
    fun `a routine-date pair counts only once`() {
        val statuses =
            listOf(
                HabitStatus(id = 1, habitId = 1, date = day),
                HabitStatus(id = 2, habitId = 1, date = day),
            )

        assertEquals(1, xp(listOf(pushups), statuses)[FighterStat.STRENGTH])
    }

    @Test
    fun `measurable routines count from their target`() {
        val statuses =
            listOf(
                HabitStatus(habitId = 3, date = day, value = 7.5),
                HabitStatus(habitId = 3, date = next, value = 8.0),
            )

        assertEquals(1, xp(listOf(water), statuses)[FighterStat.STAMINA])
    }

    @Test
    fun `statuses of deleted routines do not count`() {
        val statuses = listOf(HabitStatus(habitId = 99, date = day))

        assertEquals(0, xp(listOf(pushups), statuses).values.sum())
    }

    @Test
    fun `stats without routines have zero xp`() {
        val xp = xp(listOf(pushups), listOf(HabitStatus(habitId = 1, date = day)))

        assertEquals(FighterStat.entries.toSet(), xp.keys)
        assertEquals(0, xp[FighterStat.FOCUS])
        assertEquals(0, xp[FighterStat.SPEED])
        assertEquals(0, xp[FighterStat.STAMINA])
    }

    @Test
    fun `xp moves with the routine's stat`() {
        val statuses = listOf(HabitStatus(habitId = 1, date = day))

        val xp = StatXp.of(listOf(pushups), statuses) { FighterStat.FOCUS }

        assertEquals(1, xp[FighterStat.FOCUS])
        assertEquals(0, xp[FighterStat.STRENGTH])
    }
}
