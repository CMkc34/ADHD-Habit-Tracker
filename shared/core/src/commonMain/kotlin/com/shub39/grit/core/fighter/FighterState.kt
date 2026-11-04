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

import com.shub39.grit.core.activity.ActivityReport
import com.shub39.grit.core.activity.DayStatus
import com.shub39.grit.core.avatar.Weather

enum class FighterMood {
    READY,
    /** The activity streak is broken. The fighter never loses a rank, it only looks tired. */
    TIRED,
}

/** Everything the fighter avatar needs: rank from the activity report, stats from the XP. */
data class FighterState(
    val activeDays: Int,
    val rank: FighterRank,
    val mood: FighterMood,
    val weather: Weather = Weather.FAIR,
    val xp: Map<FighterStat, Int> = emptyMap(),
) {
    fun xp(stat: FighterStat): Int = xp[stat] ?: 0

    fun level(stat: FighterStat): Int = StatCurve.levelFor(xp(stat))

    /** Active days still missing for the next rank, `null` at the last rank. */
    val daysToNextRank: Int?
        get() = rank.next?.let { it.minActiveDays - activeDays }

    /** How far the fighter is on its way to the next rank, from 0 to 1; 1 at the last rank. */
    val rankProgress: Float
        get() {
            val next = rank.next ?: return 1f
            val done = activeDays - rank.minActiveDays
            return (done.toFloat() / (next.minActiveDays - rank.minActiveDays)).coerceIn(0f, 1f)
        }

    companion object {
        fun of(report: ActivityReport, xp: Map<FighterStat, Int>): FighterState {
            val activeDays = report.days.values.count { it == DayStatus.ACTIVE }
            return FighterState(
                activeDays = activeDays,
                rank = FighterRank.forActiveDays(activeDays),
                mood = if (report.currentStreak > 0) FighterMood.READY else FighterMood.TIRED,
                weather = Weather.of(report),
                xp = xp,
            )
        }
    }
}
