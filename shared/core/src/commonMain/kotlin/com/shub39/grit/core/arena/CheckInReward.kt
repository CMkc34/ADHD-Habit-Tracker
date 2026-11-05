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
import com.shub39.grit.core.fighter.StatCurve
import com.shub39.grit.core.fighter.StatXp
import com.shub39.grit.core.habits.Habit
import com.shub39.grit.core.habits.HabitStatus
import kotlinx.datetime.LocalDate

/**
 * The training of one stat and today's earned tickets at one moment. Read before and after saving a
 * check-in, then compared with [CheckInReward.between].
 */
data class TrainingSnapshot(val stat: FighterStat, val xp: Int, val ticketsToday: Int) {
    val level: Int
        get() = StatCurve.levelFor(xp)

    companion object {
        /** XP of [stat] (SPEC 9.3) and the tickets earned [today] (SPEC 9.6). */
        fun of(
            habits: List<Habit>,
            statuses: List<HabitStatus>,
            stat: FighterStat,
            statOf: (habitId: Long) -> FighterStat,
            today: LocalDate,
        ): TrainingSnapshot =
            TrainingSnapshot(
                stat = stat,
                xp = StatXp.of(habits, statuses, statOf).getValue(stat),
                ticketsToday = FightTickets.earned(habits, statuses, today),
            )
    }
}

/** What a check-in earned, shown in the motivation popup (#15). */
data class CheckInReward(
    /** The stat that got XP, `null` when the check-in added none. */
    val statGain: FighterStat? = null,
    /** The new level of [statGain] when it went up, otherwise `null`. */
    val newStatLevel: Int? = null,
    /** Whether the check-in earned one of today's fight tickets. */
    val ticketEarned: Boolean = false,
) {
    companion object {
        val NONE = CheckInReward()

        /**
         * Compares the snapshots around a check-in. A routine below its target, a second check-in
         * for the same date or a check-in after the third ticket of the day earn nothing new.
         */
        fun between(before: TrainingSnapshot, after: TrainingSnapshot): CheckInReward {
            val gained = after.stat == before.stat && after.xp > before.xp
            return CheckInReward(
                statGain = after.stat.takeIf { gained },
                newStatLevel = after.level.takeIf { gained && it > before.level },
                ticketEarned = after.ticketsToday > before.ticketsToday,
            )
        }
    }
}
