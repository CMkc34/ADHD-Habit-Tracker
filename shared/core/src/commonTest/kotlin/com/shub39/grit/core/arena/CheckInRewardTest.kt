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
package com.shub39.grit.core.arena

import com.shub39.grit.core.fighter.FighterStat
import com.shub39.grit.core.habits.Habit
import com.shub39.grit.core.habits.HabitStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

class CheckInRewardTest {

    private val today = LocalDate(2026, 11, 5)
    private val yesterday = LocalDate(2026, 11, 4)

    private val routines = (1L..4L).map { habit(it) }
    private val water = habit(9, target = 8.0)
    private val habits = routines + water

    private val stats = mapOf(1L to FighterStat.STRENGTH, 9L to FighterStat.FOCUS)

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

    private fun status(id: Long, date: LocalDate = today, value: Double = 0.0) =
        HabitStatus(habitId = id, date = date, value = value)

    private fun snapshot(statuses: List<HabitStatus>, stat: FighterStat) =
        TrainingSnapshot.of(
            habits = habits,
            statuses = statuses,
            stat = stat,
            statOf = { stats[it] ?: FighterStat.DEFAULT },
            today = today,
        )

    /** The reward for adding [added] to [existing], for the stat of [added]'s routine. */
    private fun reward(
        added: HabitStatus,
        existing: List<HabitStatus> = emptyList(),
    ): CheckInReward {
        val stat = stats[added.habitId] ?: FighterStat.DEFAULT
        return CheckInReward.between(snapshot(existing, stat), snapshot(existing + added, stat))
    }

    @Test
    fun `check-in for today adds XP and a ticket`() {
        assertEquals(
            CheckInReward(statGain = FighterStat.STRENGTH, ticketEarned = true),
            reward(status(1)),
        )
    }

    @Test
    fun `check-in for yesterday adds XP but no ticket`() {
        assertEquals(CheckInReward(statGain = FighterStat.STRENGTH), reward(status(1, yesterday)))
    }

    @Test
    fun `measurable routine below its target earns nothing`() {
        assertEquals(CheckInReward.NONE, reward(status(9, value = 3.0)))
        assertEquals(
            CheckInReward(statGain = FighterStat.FOCUS, ticketEarned = true),
            reward(status(9, value = 8.0)),
        )
    }

    @Test
    fun `a second status for the same date earns nothing`() {
        assertEquals(CheckInReward.NONE, reward(status(1), existing = listOf(status(1))))
    }

    @Test
    fun `level-up at the threshold`() {
        // 4 earlier days give 4 XP, the fifth reaches level 2 at 5 XP
        val earlier = (1..4).map { status(1, LocalDate(2026, 10, it)) }
        val reward = reward(status(1), existing = earlier)
        assertEquals(FighterStat.STRENGTH, reward.statGain)
        assertEquals(2, reward.newStatLevel)

        val noLevel = reward(status(1), existing = earlier.take(3))
        assertNull(noLevel.newStatLevel)
    }

    @Test
    fun `only the first three routines of today earn a ticket`() {
        val earned = mutableListOf<HabitStatus>()
        val tickets =
            routines.map {
                val added = status(it.id)
                reward(added, existing = earned.toList()).ticketEarned.also { earned += added }
            }
        assertEquals(listOf(true, true, true, false), tickets)
    }

    @Test
    fun `the fourth routine of today still adds XP`() {
        val existing = listOf(status(2), status(3), status(4))
        val reward = reward(status(1), existing = existing)
        assertEquals(FighterStat.STRENGTH, reward.statGain)
        assertFalse(reward.ticketEarned)
    }

    @Test
    fun `stamina is trained by routines without a stored stat`() {
        val reward = reward(status(2))
        assertEquals(FighterStat.STAMINA, reward.statGain)
        assertTrue(reward.ticketEarned)
    }
}
