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
import com.shub39.grit.core.activity.ActivityStore
import com.shub39.grit.core.activity.BonusDaySource
import com.shub39.grit.core.avatar.AvatarData
import com.shub39.grit.core.avatar.Purchase
import com.shub39.grit.core.now
import com.shub39.grit.logic.avatar.FakeAvatarStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

private class FakeActivityStore(initial: ActivityData = ActivityData()) : ActivityStore {
    val data = MutableStateFlow(initial)

    override fun getActivity(): Flow<ActivityData> = data

    override suspend fun ensureStartDate(day: LocalDate) {
        data.update { if (it.startDate == null) it.copy(startDate = day) else it }
    }

    override suspend fun markActive(day: LocalDate) {
        data.update { it.copy(activeDays = it.activeDays + day) }
    }

    override suspend fun markFreezesSeen(days: Set<LocalDate>) {
        data.update { it.copy(seenFreezes = it.seenFreezes + days) }
    }

    override suspend fun setAutoFreeze(day: LocalDate, enabled: Boolean) {
        data.update { it.copy(autoFreeze = it.autoFreeze.switched(day, enabled)) }
    }

    override suspend fun replaceAll(data: ActivityData) {
        this.data.value = data
    }
}

class ActivityRepositoryTest {

    // relative to the real date, like the repository's "today"
    private val today = LocalDate.now()

    private fun daysAgo(n: Int) = today.minus(n, DateTimeUnit.DAY)

    private val noBonus = BonusDaySource { flowOf(emptySet()) }

    @Test
    fun `report evaluates the stored active days`() = runTest {
        val store =
            FakeActivityStore(
                ActivityData(
                    startDate = daysAgo(2),
                    activeDays = setOf(daysAgo(2), daysAgo(1), today),
                )
            )

        val report =
            ActivityRepository(store, FakeAvatarStore(), noBonus).getActivityReport().first()

        assertEquals(3, report.currentStreak)
        assertEquals(3, report.energy)
    }

    @Test
    fun `reading the report sets the start date on first use`() = runTest {
        val store = FakeActivityStore()

        ActivityRepository(store, FakeAvatarStore(), noBonus).getActivityReport().first()

        assertEquals(today, store.data.value.startDate)
    }

    @Test
    fun `freeze notice disappears once its freezes are seen`() = runTest {
        val store =
            FakeActivityStore(
                ActivityData(
                    startDate = daysAgo(8),
                    activeDays = (2..8).map(::daysAgo).toSet() + today,
                )
            )
        val repository = ActivityRepository(store, FakeAvatarStore(), noBonus)

        val notice = assertNotNull(repository.getFreezeNotice().first())
        assertEquals(setOf(daysAgo(1)), notice.frozenDays)

        repository.markFreezesSeen(notice.frozenDays)

        assertNull(repository.getFreezeNotice().first())
    }

    @Test
    fun `purchases from the plant shop lower the energy`() = runTest {
        val store =
            FakeActivityStore(
                ActivityData(startDate = daysAgo(4), activeDays = (0..4).map(::daysAgo).toSet())
            )
        val avatarStore =
            FakeAvatarStore(AvatarData(purchases = listOf(Purchase("pot_blue", daysAgo(1), 3))))

        val report = ActivityRepository(store, avatarStore, noBonus).getActivityReport().first()

        assertEquals(2, report.energy)
    }

    @Test
    fun `switching auto freeze off keeps earlier freezes`() = runTest {
        val store =
            FakeActivityStore(
                ActivityData(
                    startDate = daysAgo(8),
                    activeDays = (2..8).map(::daysAgo).toSet() + today,
                )
            )
        val repository = ActivityRepository(store, FakeAvatarStore(), noBonus)

        repository.setAutoFreeze(false)
        val report = repository.getActivityReport().first()

        assertFalse(report.autoFreeze)
        assertEquals(setOf(daysAgo(1)), report.frozenDays)
    }

    @Test
    fun `bonus days add energy only while they are there`() = runTest {
        val store =
            FakeActivityStore(
                ActivityData(startDate = daysAgo(2), activeDays = (0..2).map(::daysAgo).toSet())
            )
        val bonus = MutableStateFlow(setOf(daysAgo(1), today))
        val repository = ActivityRepository(store, FakeAvatarStore(), BonusDaySource { bonus })

        assertEquals(5, repository.getActivityReport().first().energy)

        // taking a check-in back drops today below two routines
        bonus.value = setOf(daysAgo(1))

        assertEquals(4, repository.getActivityReport().first().energy)
    }
}
