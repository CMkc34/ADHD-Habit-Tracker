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

import com.shub39.grit.core.activity.ActivityData
import com.shub39.grit.core.activity.ActivityPlanner
import com.shub39.grit.core.activity.ActivityRepo
import com.shub39.grit.core.activity.ActivityReport
import com.shub39.grit.core.activity.ActivityStore
import com.shub39.grit.core.activity.BonusDaySource
import com.shub39.grit.core.activity.FreezeNotice
import com.shub39.grit.core.avatar.AvatarStore
import com.shub39.grit.core.avatar.Purchase
import com.shub39.grit.core.now
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.Single

@Single(binds = [ActivityRepo::class])
class ActivityRepository(
    private val store: ActivityStore,
    private val avatarStore: AvatarStore,
    private val bonusDaySource: BonusDaySource,
) : ActivityRepo {

    override fun getActivityReport(): Flow<ActivityReport> =
        combine(activity(), purchases(), bonusDaySource.getBonusDays()) { data, purchases, bonus ->
            plan(data, purchases, bonus)
        }

    override fun getFreezeNotice(): Flow<FreezeNotice?> =
        combine(activity(), purchases(), bonusDaySource.getBonusDays()) { data, purchases, bonus ->
            FreezeNotice.of(plan(data, purchases, bonus), data.seenFreezes)
        }

    override suspend fun markFreezesSeen(days: Set<LocalDate>) = store.markFreezesSeen(days)

    override suspend fun setAutoFreeze(enabled: Boolean) =
        store.setAutoFreeze(LocalDate.now(), enabled)

    private fun activity(): Flow<ActivityData> =
        store
            .getActivity()
            // the first look at the activity marks the start of the system ("from installation")
            .onStart { store.ensureStartDate(LocalDate.now()) }

    // energy spent in the plant's shop (phase 4)
    private fun purchases(): Flow<List<Purchase>> =
        avatarStore.getAvatar().map { it.purchases }.distinctUntilChanged()

    private fun plan(
        data: ActivityData,
        purchases: List<Purchase>,
        bonusDays: Set<LocalDate>,
    ): ActivityReport {
        val today = LocalDate.now()
        return ActivityPlanner.plan(
            activeDays = data.activeDays,
            startDate = data.startDate ?: today,
            today = today,
            purchases = purchases,
            autoFreeze = data.autoFreeze,
            bonusDays = bonusDays,
        )
    }
}
