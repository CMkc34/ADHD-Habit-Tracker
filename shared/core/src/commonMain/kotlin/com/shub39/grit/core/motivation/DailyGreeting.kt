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
import com.shub39.grit.core.avatar.PlantState
import kotlinx.datetime.LocalDate

/** What the plant says when it greets the user on the first app start of a day. */
enum class GreetingKind {
    /** No routines yet: the plant introduces itself and the add sheet opens afterwards. */
    WELCOME,

    /** Routines exist, but no day has been active yet. */
    FIRST_DAY,

    /** The activity streak is broken, the plant is tired. */
    TIRED,

    /** The activity streak is running, the plant is growing. */
    GROWING,
}

/** Rough time of day for the greeting's header. */
enum class DayTime {
    MORNING,
    DAY,
    EVENING,
}

object DailyGreeting {
    /** Whether the greeting is due: it is on and has not been shown on [today] yet. */
    fun isDue(enabled: Boolean, lastShown: LocalDate?, today: LocalDate): Boolean =
        enabled && (lastShown == null || lastShown < today)

    fun kindOf(hasRoutines: Boolean, plant: PlantState): GreetingKind =
        when {
            !hasRoutines -> GreetingKind.WELCOME
            plant.activeDays == 0 -> GreetingKind.FIRST_DAY
            plant.mood == PlantMood.TIRED -> GreetingKind.TIRED
            else -> GreetingKind.GROWING
        }

    /** Morning until 11:00, evening from 18:00, day in between and at night. */
    fun dayTimeOf(hour: Int): DayTime =
        when (hour) {
            in 4..10 -> DayTime.MORNING
            in 18..23 -> DayTime.EVENING
            else -> DayTime.DAY
        }
}
