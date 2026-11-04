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

import com.shub39.grit.core.activity.ActivityPlanner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate

class PlantStageTest {

    private val start = LocalDate(2026, 1, 1)

    private fun day(n: Int) = LocalDate.fromEpochDays(start.toEpochDays() + n)

    @Test
    fun `there are twelve stages ending at a year of active days`() {
        assertEquals(12, PlantStage.entries.size)
        assertEquals(
            listOf(0, 3, 7, 14, 21, 30, 45, 60, 90, 150, 240, 365),
            PlantStage.entries.map { it.minActiveDays },
        )
        assertEquals(12, PlantStage.MIGHTY_TREE.number)
    }

    @Test
    fun `every threshold starts its stage`() {
        PlantStage.entries.forEach { stage ->
            assertEquals(stage, PlantStage.forActiveDays(stage.minActiveDays))
            if (stage.minActiveDays > 0) {
                assertEquals(
                    PlantStage.entries[stage.ordinal - 1],
                    PlantStage.forActiveDays(stage.minActiveDays - 1),
                )
            }
        }
        assertEquals(PlantStage.MIGHTY_TREE, PlantStage.forActiveDays(1000))
    }

    @Test
    fun `days to the next stage`() {
        assertEquals(3, PlantState(0, PlantStage.SEED, PlantMood.HAPPY).daysToNextStage)
        assertEquals(1, PlantState(29, PlantStage.PLANTLET, PlantMood.HAPPY).daysToNextStage)
        assertNull(PlantState(400, PlantStage.MIGHTY_TREE, PlantMood.HAPPY).daysToNextStage)
    }

    @Test
    fun `progress towards the next stage`() {
        assertEquals(0f, PlantState(0, PlantStage.SEED, PlantMood.HAPPY).stageProgress)
        assertEquals(0.5f, PlantState(120, PlantStage.TREE, PlantMood.HAPPY).stageProgress)
        assertEquals(1f, PlantState(400, PlantStage.MIGHTY_TREE, PlantMood.HAPPY).stageProgress)
    }

    @Test
    fun `active days come from the report and frozen days do not count`() {
        // seven active days, one frozen day, one active day
        val report =
            ActivityPlanner.plan(
                activeDays = ((0..6) + 8).map(::day).toSet(),
                startDate = start,
                today = day(8),
            )

        val plant = PlantState.of(report)

        assertEquals(8, plant.activeDays)
        assertEquals(PlantStage.SEEDLING, plant.stage)
        assertEquals(PlantMood.HAPPY, plant.mood)
    }

    @Test
    fun `a broken streak makes the plant tired but never smaller`() {
        val report =
            ActivityPlanner.plan(
                activeDays = (0..14).map(::day).toSet(),
                startDate = start,
                today = day(20),
            )

        val plant = PlantState.of(report)

        assertEquals(0, report.currentStreak)
        assertEquals(PlantStage.SPROUT, plant.stage)
        assertEquals(PlantMood.TIRED, plant.mood)
    }

    @Test
    fun `today without activity keeps the plant happy`() {
        val report =
            ActivityPlanner.plan(
                activeDays = (0..3).map(::day).toSet(),
                startDate = start,
                today = day(4),
            )

        assertEquals(PlantMood.HAPPY, PlantState.of(report).mood)
    }
}
