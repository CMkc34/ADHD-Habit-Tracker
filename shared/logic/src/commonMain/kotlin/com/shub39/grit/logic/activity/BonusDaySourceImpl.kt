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
package com.shub39.grit.logic.activity

import com.shub39.grit.core.activity.BonusDaySource
import com.shub39.grit.core.activity.DailyBonus
import com.shub39.grit.logic.habits.database.HabitStatusDao
import com.shub39.grit.logic.habits.database.HabitsDao
import com.shub39.grit.logic.habits.toHabit
import com.shub39.grit.logic.habits.toHabitStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.Single

@Single(binds = [BonusDaySource::class])
class BonusDaySourceImpl(
    private val habitDao: HabitsDao,
    private val habitStatusDao: HabitStatusDao,
) : BonusDaySource {

    override fun getBonusDays(): Flow<Set<LocalDate>> =
        combine(habitDao.getAllHabitsFlow(), habitStatusDao.getAllHabitStatuses()) {
                habits,
                statuses ->
                DailyBonus.days(habits.map { it.toHabit() }, statuses.map { it.toHabitStatus() })
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
}
