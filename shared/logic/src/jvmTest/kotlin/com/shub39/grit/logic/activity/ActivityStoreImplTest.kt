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
import com.shub39.grit.logic.core.datastore.createDataStore
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class ActivityStoreImplTest {

    private val day = LocalDate(2026, 10, 18)

    private fun store() =
        ActivityStoreImpl(
            createDataStore {
                createTempDirectory("activity").resolve("test.preferences_pb").toString()
            }
        )

    @Test
    fun `marking a day active stores it once`() = runTest {
        val store = store()

        store.markActive(day)
        store.markActive(day)

        assertEquals(setOf(day), store.getActivity().first().activeDays)
    }

    @Test
    fun `start date is set only once`() = runTest {
        val store = store()

        store.ensureStartDate(day)
        store.ensureStartDate(LocalDate(2026, 12, 24))

        assertEquals(day, store.getActivity().first().startDate)
    }

    @Test
    fun `first active day sets the start date when none exists`() = runTest {
        val store = store()

        store.markActive(day)

        assertEquals(day, store.getActivity().first().startDate)
    }

    @Test
    fun `seen freezes are added up`() = runTest {
        val store = store()
        val first = LocalDate(2026, 10, 20)
        val second = LocalDate(2026, 10, 25)

        store.markFreezesSeen(setOf(first))
        store.markFreezesSeen(setOf(second))

        assertEquals(setOf(first, second), store.getActivity().first().seenFreezes)
    }

    @Test
    fun `replacing all stores exactly the given data`() = runTest {
        val store = store()
        val restored =
            ActivityData(
                startDate = LocalDate(2026, 10, 1),
                activeDays = setOf(LocalDate(2026, 10, 1), LocalDate(2026, 10, 2)),
                seenFreezes = setOf(LocalDate(2026, 10, 3)),
            )
        store.markActive(day)

        store.replaceAll(restored)

        assertEquals(restored, store.getActivity().first())
    }

    @Test
    fun `replacing with empty data clears the start date`() = runTest {
        val store = store()
        store.markActive(day)

        store.replaceAll(ActivityData())

        assertEquals(ActivityData(), store.getActivity().first())
    }
}
