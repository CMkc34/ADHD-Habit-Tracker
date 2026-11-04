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
package com.shub39.grit.core.avatar

import com.shub39.grit.core.activity.ActivityReport
import com.shub39.grit.core.activity.DayStatus

/** Growth stages of the plant avatar, from a seed to a mighty tree after a year of active days. */
enum class PlantStage(val minActiveDays: Int) {
    SEED(0),
    GERM(3),
    SEEDLING(7),
    SPROUT(14),
    PLANTLET(21),
    YOUNG_PLANT(30),
    SAPLING(45),
    YOUNG_TREE(60),
    TREE(90),
    BIG_TREE(150),
    BLOSSOMING_TREE(240),
    MIGHTY_TREE(365);

    /** 1-based number shown to the user. */
    val number: Int
        get() = ordinal + 1

    val next: PlantStage?
        get() = entries.getOrNull(ordinal + 1)

    companion object {
        fun forActiveDays(activeDays: Int): PlantStage =
            entries.last { activeDays >= it.minActiveDays }
    }
}

enum class PlantMood {
    HAPPY,
    /** The activity streak is broken. The plant never shrinks, it only looks tired. */
    TIRED,
}

/** Everything the avatar needs from the activity report. */
data class PlantState(
    val activeDays: Int,
    val stage: PlantStage,
    val mood: PlantMood,
    val weather: Weather = Weather.FAIR,
) {
    /** Active days still missing for the next stage, `null` at the last stage. */
    val daysToNextStage: Int?
        get() = stage.next?.let { it.minActiveDays - activeDays }

    /** How far the plant is on its way to the next stage, from 0 to 1; 1 at the last stage. */
    val stageProgress: Float
        get() {
            val next = stage.next ?: return 1f
            val done = activeDays - stage.minActiveDays
            return (done.toFloat() / (next.minActiveDays - stage.minActiveDays)).coerceIn(0f, 1f)
        }

    companion object {
        fun of(report: ActivityReport): PlantState {
            val activeDays = report.days.values.count { it == DayStatus.ACTIVE }
            return PlantState(
                activeDays = activeDays,
                stage = PlantStage.forActiveDays(activeDays),
                mood = if (report.currentStreak > 0) PlantMood.HAPPY else PlantMood.TIRED,
                weather = Weather.of(report),
            )
        }
    }
}
