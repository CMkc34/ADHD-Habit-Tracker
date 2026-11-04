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
package com.shub39.grit.logic.avatar

import com.shub39.grit.core.fighter.FighterStat
import com.shub39.grit.core.fighter.RoutineStatStore
import com.shub39.grit.core.habits.Habit
import com.shub39.grit.core.habits.HabitRepo
import com.shub39.grit.core.habits.HabitStatus
import com.shub39.grit.core.habits.HabitWithAnalytics
import com.shub39.grit.core.habits.MeasurableTargets
import com.shub39.grit.core.habits.OverallAnalytics
import com.shub39.grit.core.habits.Target
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

fun routine(id: Long) =
    Habit(
        id = id,
        title = "Routine $id",
        description = "",
        time = LocalDateTime(2026, 10, 1, 9, 0),
        days = DayOfWeek.entries.toSet(),
        index = id.toInt(),
        reminder = false,
    )

/** Only the habits with their check-ins, which is all the avatar needs for the XP. */
class FakeHabitRepo(habits: List<Habit> = emptyList()) : HabitRepo {
    val habits = MutableStateFlow(habits)
    val statuses = MutableStateFlow(emptyList<HabitStatus>())

    fun checkIn(habitId: Long, date: LocalDate) =
        statuses.update { it + HabitStatus(habitId = habitId, date = date) }

    override suspend fun upsertHabit(habit: Habit): Long = habit.id

    override suspend fun deleteHabit(habitId: Long) = Unit

    override suspend fun getHabits(): List<Habit> = habits.value

    override suspend fun getHabitById(id: Long): Habit? = habits.value.find { it.id == id }

    override suspend fun getHabitStatuses(): List<HabitStatus> = statuses.value

    override fun getHabitsWithAnalytics(): Flow<List<HabitWithAnalytics>> =
        statuses.map { all ->
            val noTarget = Target(progress = 0.0, target = 0.0)
            habits.value.map { habit ->
                HabitWithAnalytics(
                    habit = habit,
                    consistency = 0f,
                    statuses = all.filter { it.habitId == habit.id },
                    weeklyComparisonData = emptyList(),
                    weekDayFrequencyData = emptyMap(),
                    measurableTargets =
                        MeasurableTargets(noTarget, noTarget, noTarget, noTarget, noTarget),
                    currentStreak = 0,
                    bestStreak = 0,
                    startedDaysAgo = 0,
                )
            }
        }

    override fun getCompletedHabitIds(): Flow<List<Long>> = flowOf(emptyList())

    override fun getOverallAnalytics(): Flow<OverallAnalytics> = flowOf(OverallAnalytics())

    override fun getHabitsWithStatus(): Flow<List<Pair<Habit, Boolean>>> = flowOf(emptyList())

    override suspend fun getStatusForHabit(id: Long): List<HabitStatus> =
        statuses.value.filter { it.habitId == id }

    override suspend fun insertHabitStatus(habitStatus: HabitStatus) =
        statuses.update { it + habitStatus }

    override suspend fun deleteHabitStatus(habitId: Long, date: LocalDate) =
        statuses.update { list -> list.filterNot { it.habitId == habitId && it.date == date } }

    override suspend fun getCompletedHabitsForDate(date: LocalDate): Map<Habit, Double?> =
        emptyMap()
}

class FakeRoutineStatStore(stats: Map<Long, FighterStat> = emptyMap()) : RoutineStatStore {
    val stats = MutableStateFlow(stats)

    override fun getStats(): Flow<Map<Long, FighterStat>> = stats

    override suspend fun setStat(habitId: Long, stat: FighterStat) =
        stats.update { it + (habitId to stat) }

    override suspend fun deleteStat(habitId: Long) = stats.update { it - habitId }
}
