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

import com.shub39.grit.core.avatar.Purchase
import kotlinx.datetime.LocalDate

enum class DayStatus {
    /** At least one check-in was made on this day. */
    ACTIVE,
    /** Missed, but bridged by a freeze. */
    FROZEN,
    MISSED,
    /** Today without a check-in yet. */
    OPEN,
}

/** Result of replaying the user's active days. */
data class ActivityReport(
    val currentStreak: Int,
    val longestStreak: Int,
    val energy: Int,
    /** Status of every day from the start date up to today, in order. */
    val days: Map<LocalDate, DayStatus>,
    /** Whether missed days are frozen automatically from today on. */
    val autoFreeze: Boolean = true,
    /** Active days that earned the [DailyBonus] on top of the daily energy. */
    val bonusDays: Set<LocalDate> = emptySet(),
) {
    val frozenDays: Set<LocalDate>
        get() = days.filterValues { it == DayStatus.FROZEN }.keys
}

/** Derives the activity streak, energy and freezes from the days the user used the app. */
object ActivityPlanner {
    const val ENERGY_PER_ACTIVE_DAY = 1
    const val FREEZE_COST = 7
    const val MAX_FROZEN_DAYS_IN_A_ROW = 2

    /**
     * Replays every day from [startDate] to [today]. A gap of missed days is frozen only as a
     * whole: when it is at most [MAX_FROZEN_DAYS_IN_A_ROW] long and the energy covers all of it.
     * Otherwise the streak breaks and no energy is spent. [today] without activity does not break
     * the streak. Every active day in [bonusDays] earns the [DailyBonus] as well; a bonus day the
     * user was not active on earns nothing.
     *
     * Energy spent on [purchases] is taken on the day of the purchase, but only after a gap that is
     * open on that day has been decided, so a purchase never undoes a freeze that was shown before.
     * A purchase never takes the energy below 0: when a merged backup brings in a purchase that was
     * not affordable here, the difference is dropped.
     *
     * A gap is not frozen when [autoFreeze] was off on any of its days. Switching freezes off keeps
     * the freezes of earlier days.
     */
    fun plan(
        activeDays: Set<LocalDate>,
        startDate: LocalDate,
        today: LocalDate,
        purchases: List<Purchase> = emptyList(),
        autoFreeze: AutoFreezeSwitches = AutoFreezeSwitches(),
        bonusDays: Set<LocalDate> = emptySet(),
    ): ActivityReport {
        var streak = 0
        var longest = 0
        var energy = 0
        var pendingSpend = 0
        val days = linkedMapOf<LocalDate, DayStatus>()
        val earnedBonus = mutableSetOf<LocalDate>()
        val gap = mutableListOf<LocalDate>()
        val spendByDay = purchases.groupBy({ it.date }, { it.price })

        fun spendPending() {
            energy = maxOf(0, energy - pendingSpend)
            pendingSpend = 0
        }

        fun closeGap() {
            if (gap.isEmpty()) return
            val cost = gap.size * FREEZE_COST
            val canFreeze = streak > 0 && gap.size <= MAX_FROZEN_DAYS_IN_A_ROW && energy >= cost
            if (canFreeze && gap.all(autoFreeze::isOn)) {
                gap.forEach { days[it] = DayStatus.FROZEN }
                energy -= cost
            } else {
                streak = 0
            }
            gap.clear()
            spendPending()
        }

        for (epochDay in startDate.toEpochDays()..today.toEpochDays()) {
            val day = LocalDate.fromEpochDays(epochDay)
            when {
                day in activeDays -> {
                    closeGap()
                    streak++
                    longest = maxOf(longest, streak)
                    energy += ENERGY_PER_ACTIVE_DAY
                    if (day in bonusDays) {
                        energy += DailyBonus.ENERGY
                        earnedBonus += day
                    }
                    days[day] = DayStatus.ACTIVE
                }
                day < today -> {
                    gap += day
                    days[day] = DayStatus.MISSED
                }
                else -> days[day] = DayStatus.OPEN
            }
            // purchases made before the start date count on the start date
            val spentToday =
                if (epochDay == startDate.toEpochDays()) {
                    spendByDay.filterKeys { it <= startDate }.values.flatten().sum()
                } else {
                    spendByDay[day].orEmpty().sum()
                }
            pendingSpend += spentToday
            if (gap.isEmpty()) spendPending()
        }
        closeGap()
        spendPending()

        return ActivityReport(
            currentStreak = streak,
            longestStreak = longest,
            energy = energy,
            days = days,
            autoFreeze = autoFreeze.isOn(today),
            bonusDays = earnedBonus,
        )
    }
}
