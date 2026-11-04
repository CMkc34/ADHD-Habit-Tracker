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
package com.shub39.grit.core.motivation

import com.shub39.grit.core.avatar.PlantMood
import com.shub39.grit.core.avatar.PlantStage
import com.shub39.grit.core.avatar.PlantState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate

class DailyGreetingTest {
    private val today = LocalDate(2026, 11, 1)

    private fun plant(activeDays: Int, mood: PlantMood = PlantMood.HAPPY) =
        PlantState(activeDays, PlantStage.forActiveDays(activeDays), mood)

    @Test
    fun `due when never shown`() {
        assertTrue(DailyGreeting.isDue(enabled = true, lastShown = null, today = today))
    }

    @Test
    fun `due once per day`() {
        assertTrue(DailyGreeting.isDue(true, LocalDate(2026, 10, 31), today))
        assertFalse(DailyGreeting.isDue(true, today, today))
    }

    @Test
    fun `never due when turned off`() {
        assertFalse(DailyGreeting.isDue(enabled = false, lastShown = null, today = today))
    }

    @Test
    fun `clock set back does not repeat the greeting`() {
        assertFalse(DailyGreeting.isDue(true, LocalDate(2026, 11, 2), today))
    }

    @Test
    fun `kind follows routines and plant`() {
        assertEquals(GreetingKind.WELCOME, DailyGreeting.kindOf(false, plant(40)))
        assertEquals(GreetingKind.FIRST_DAY, DailyGreeting.kindOf(true, plant(0, PlantMood.TIRED)))
        assertEquals(GreetingKind.TIRED, DailyGreeting.kindOf(true, plant(12, PlantMood.TIRED)))
        assertEquals(GreetingKind.GROWING, DailyGreeting.kindOf(true, plant(12)))
    }

    @Test
    fun `day time by hour`() {
        assertEquals(DayTime.DAY, DailyGreeting.dayTimeOf(3))
        assertEquals(DayTime.MORNING, DailyGreeting.dayTimeOf(4))
        assertEquals(DayTime.MORNING, DailyGreeting.dayTimeOf(10))
        assertEquals(DayTime.DAY, DailyGreeting.dayTimeOf(11))
        assertEquals(DayTime.DAY, DailyGreeting.dayTimeOf(17))
        assertEquals(DayTime.EVENING, DailyGreeting.dayTimeOf(18))
        assertEquals(DayTime.EVENING, DailyGreeting.dayTimeOf(23))
    }
}
