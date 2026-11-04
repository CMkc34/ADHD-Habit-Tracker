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

import com.shub39.grit.core.activity.ActivityPlanner
import com.shub39.grit.core.avatar.Weather
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate

class FighterStateTest {

    private val start = LocalDate(2026, 1, 1)

    private fun day(n: Int) = LocalDate.fromEpochDays(start.toEpochDays() + n)

    private fun state(activeDays: Int) =
        FighterState(activeDays, FighterRank.forActiveDays(activeDays), FighterMood.READY)

    @Test
    fun `days and progress to the next rank`() {
        assertEquals(3, state(0).daysToNextRank)
        assertEquals(1, state(29).daysToNextRank)
        assertNull(state(400).daysToNextRank)

        assertEquals(0f, state(0).rankProgress)
        assertEquals(0.5f, state(120).rankProgress)
        assertEquals(1f, state(400).rankProgress)
    }

    @Test
    fun `levels come from xp`() {
        val fighter = state(0).copy(xp = mapOf(FighterStat.STRENGTH to 15, FighterStat.FOCUS to 4))

        assertEquals(3, fighter.level(FighterStat.STRENGTH))
        assertEquals(1, fighter.level(FighterStat.FOCUS))
        assertEquals(1, fighter.level(FighterStat.SPEED))
        assertEquals(0, fighter.xp(FighterStat.SPEED))
    }

    @Test
    fun `a running streak makes the fighter ready`() {
        val report =
            ActivityPlanner.plan(
                activeDays = (0..7).map(::day).toSet(),
                startDate = start,
                today = day(7),
            )
        val xp = mapOf(FighterStat.SPEED to 5)

        val fighter = FighterState.of(report, xp)

        assertEquals(8, fighter.activeDays)
        assertEquals(FighterRank.SPARRING_PARTNER, fighter.rank)
        assertEquals(FighterMood.READY, fighter.mood)
        assertEquals(Weather.of(report), fighter.weather)
        assertEquals(2, fighter.level(FighterStat.SPEED))
    }

    @Test
    fun `a broken streak makes the fighter tired but never lowers the rank`() {
        val activeDays = (0..7).map(::day).toSet()
        val running = ActivityPlanner.plan(activeDays, startDate = start, today = day(7))
        // long enough away that the energy cannot freeze the gap
        val broken = ActivityPlanner.plan(activeDays, startDate = start, today = day(40))

        assertEquals(0, broken.currentStreak)
        val before = FighterState.of(running, emptyMap())
        val after = FighterState.of(broken, emptyMap())

        assertEquals(FighterMood.TIRED, after.mood)
        assertEquals(before.rank, after.rank)
        assertEquals(before.activeDays, after.activeDays)
    }
}
