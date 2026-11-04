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

import com.shub39.grit.core.activity.ActivityPlanner
import com.shub39.grit.core.activity.ActivityRepo
import com.shub39.grit.core.activity.ActivityReport
import com.shub39.grit.core.activity.FreezeNotice
import com.shub39.grit.core.avatar.AvatarData
import com.shub39.grit.core.avatar.PlantStage
import com.shub39.grit.core.avatar.Purchase
import com.shub39.grit.core.avatar.PurchaseCheck
import com.shub39.grit.core.avatar.ShopCatalog
import com.shub39.grit.core.now
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/** Plans [activeDays] days in a row up to today, with the purchases from [avatarStore]. */
private class FakeActivityRepo(
    private val activeDays: Int,
    private val avatarStore: FakeAvatarStore,
) : ActivityRepo {
    private val today = LocalDate.now()

    override fun getActivityReport(): Flow<ActivityReport> =
        combine(flowOf(activeDays), avatarStore.data) { days, avatar ->
            ActivityPlanner.plan(
                activeDays = (0 until days).map { today.minus(it, DateTimeUnit.DAY) }.toSet(),
                startDate = today.minus(days, DateTimeUnit.DAY),
                today = today,
                purchases = avatar.purchases,
            )
        }

    override fun getFreezeNotice(): Flow<FreezeNotice?> = flowOf(null)

    override suspend fun markFreezesSeen(days: Set<LocalDate>) = Unit

    override suspend fun setAutoFreeze(enabled: Boolean) = Unit
}

class AvatarRepositoryTest {

    private fun repository(activeDays: Int, store: FakeAvatarStore = FakeAvatarStore()) =
        AvatarRepository(FakeActivityRepo(activeDays, store), store)

    @Test
    fun `buying takes the energy and puts the item on`() = runTest {
        val store = FakeAvatarStore()
        val repository = repository(activeDays = 30, store = store)

        assertEquals(PurchaseCheck.OK, repository.buy("acc_watering_can"))

        val state = repository.getAvatarState().first()
        assertEquals(30 - 25, state.energy)
        assertEquals(setOf("acc_watering_can"), state.data.accessories)
        assertEquals(PurchaseCheck.ALREADY_OWNED, repository.buy("acc_watering_can"))
    }

    @Test
    fun `items above the stage or the balance cannot be bought`() = runTest {
        val repository = repository(activeDays = 10)

        assertEquals(PurchaseCheck.STAGE_TOO_LOW, repository.buy("acc_bird"))
        assertEquals(PurchaseCheck.NOT_ENOUGH_ENERGY, repository.buy("acc_watering_can"))
        assertEquals(emptyList(), repository.getAvatarState().first().data.purchases)
    }

    @Test
    fun `a bought pot color replaces the current one and cannot be taken off`() = runTest {
        val store = FakeAvatarStore()
        val repository = repository(activeDays = 20, store = store)

        repository.buy("pot_blue")
        repository.wear("pot_blue", worn = false)
        assertEquals("pot_blue", store.data.value.potColor)

        repository.wear(ShopCatalog.DEFAULT_POT_COLOR, worn = true)
        assertEquals(ShopCatalog.DEFAULT_POT_COLOR, store.data.value.potColor)
    }

    @Test
    fun `items that are not owned cannot be worn`() = runTest {
        val store = FakeAvatarStore()

        repository(activeDays = 5, store = store).wear("acc_swing", worn = true)

        assertEquals(emptySet(), store.data.value.accessories)
    }

    @Test
    fun `a bought decoration is shown and can be hidden again`() = runTest {
        val store = FakeAvatarStore()
        val repository = repository(activeDays = 20, store = store)

        assertEquals(PurchaseCheck.OK, repository.buy("deco_mushrooms"))
        assertEquals(setOf("deco_mushrooms"), store.data.value.decorations)
        assertEquals(emptySet(), store.data.value.accessories)

        repository.wear("deco_mushrooms", worn = false)
        assertEquals(emptySet(), store.data.value.decorations)
        assertEquals(listOf("deco_mushrooms"), store.data.value.purchases.map { it.itemId })

        repository.wear("deco_mushrooms", worn = true)
        assertEquals(setOf("deco_mushrooms"), store.data.value.decorations)
    }

    @Test
    fun `decorations that are not owned cannot be shown`() = runTest {
        val store = FakeAvatarStore()

        repository(activeDays = 400, store = store).wear("deco_pond", worn = true)

        assertEquals(emptySet(), store.data.value.decorations)
    }

    @Test
    fun `the first look remembers the stage, later stages are new`() = runTest {
        val store = FakeAvatarStore()
        val repository = repository(activeDays = 8, store = store)

        assertNull(repository.getAvatarState().first().newStage)
        assertEquals(PlantStage.SEEDLING.number, store.data.value.seenStage)

        store.data.value = store.data.value.copy(seenStage = PlantStage.GERM.number)
        assertEquals(PlantStage.SEEDLING, repository.getAvatarState().first().newStage)

        repository.markStageSeen(PlantStage.SEEDLING)
        assertNull(repository.getAvatarState().first().newStage)
    }

    @Test
    fun `restoring merges purchases and keeps local choices`() {
        val today = LocalDate.now()
        val local = AvatarData(name = "Fern", purchases = listOf(Purchase("pot_blue", today, 10)))
        val backup =
            AvatarData(
                name = "Grit",
                purchases =
                    listOf(
                        Purchase("pot_blue", today.minus(3, DateTimeUnit.DAY), 10),
                        Purchase("acc_ladybug", today, 15),
                    ),
                potColor = "pot_blue",
                accessories = setOf("acc_ladybug"),
                seenStage = 4,
                decorations = setOf("deco_pond"),
            )

        val merged = local.mergedWith(backup)

        assertEquals("Fern", merged.name)
        assertEquals(
            listOf(today.minus(3, DateTimeUnit.DAY), today),
            merged.purchases.map { it.date },
        )
        assertEquals("pot_blue", merged.potColor)
        assertEquals(setOf("acc_ladybug"), merged.accessories)
        assertEquals(4, merged.seenStage)
        assertEquals(setOf("deco_pond"), merged.decorations)
        assertEquals(
            setOf("deco_fence"),
            local.copy(decorations = setOf("deco_fence")).mergedWith(backup).decorations,
        )
    }
}
