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
import com.shub39.grit.core.avatar.Purchase
import com.shub39.grit.core.avatar.PurchaseCheck
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.core.fighter.FighterStat
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

    private fun repository(
        activeDays: Int,
        store: FakeAvatarStore = FakeAvatarStore(),
        habits: FakeHabitRepo = FakeHabitRepo(),
        stats: FakeRoutineStatStore = FakeRoutineStatStore(),
    ) = AvatarRepository(FakeActivityRepo(activeDays, store), store, habits, stats)

    @Test
    fun `buying takes the energy and puts the item on`() = runTest {
        val store = FakeAvatarStore()
        val repository = repository(activeDays = 30, store = store)

        assertEquals(PurchaseCheck.OK, repository.buy("gear_gloves"))

        val state = repository.getAvatarState().first()
        assertEquals(30 - 20, state.energy)
        assertEquals(setOf("gear_gloves"), state.data.accessories)
        assertEquals(PurchaseCheck.ALREADY_OWNED, repository.buy("gear_gloves"))
    }

    @Test
    fun `items above the rank or the balance cannot be bought`() = runTest {
        val repository = repository(activeDays = 10)

        assertEquals(PurchaseCheck.RANK_TOO_LOW, repository.buy("gear_cape"))
        assertEquals(PurchaseCheck.NOT_ENOUGH_ENERGY, repository.buy("gear_gloves"))
        assertEquals(emptyList(), repository.getAvatarState().first().data.purchases)
    }

    @Test
    fun `one outfit is worn at a time and taking it off brings back the setup colours`() = runTest {
        val store = FakeAvatarStore()
        val repository = repository(activeDays = 20, store = store)
        assertNull(store.data.value.outfit)

        repository.buy("gi_blue")
        assertEquals("gi_blue", store.data.value.outfit)
        repository.buy("gi_red")
        assertEquals("gi_red", store.data.value.outfit)

        // taking off an outfit that is not worn changes nothing
        repository.wear("gi_blue", worn = false)
        assertEquals("gi_red", store.data.value.outfit)

        repository.wear("gi_red", worn = false)
        assertNull(store.data.value.outfit)

        repository.wear("gi_blue", worn = true)
        assertEquals("gi_blue", store.data.value.outfit)
    }

    @Test
    fun `outfits that are not owned cannot be worn`() = runTest {
        val store = FakeAvatarStore()

        repository(activeDays = 20, store = store).wear("gi_white", worn = true)

        assertNull(store.data.value.outfit)
    }

    @Test
    fun `items that are not owned cannot be worn`() = runTest {
        val store = FakeAvatarStore()

        repository(activeDays = 5, store = store).wear("gear_crown", worn = true)

        assertEquals(emptySet(), store.data.value.accessories)
    }

    @Test
    fun `a bought prop is shown and can be hidden again`() = runTest {
        val store = FakeAvatarStore()
        val repository = repository(activeDays = 20, store = store)

        assertEquals(PurchaseCheck.OK, repository.buy("prop_mat"))
        assertEquals(setOf("prop_mat"), store.data.value.decorations)
        assertEquals(emptySet(), store.data.value.accessories)

        repository.wear("prop_mat", worn = false)
        assertEquals(emptySet(), store.data.value.decorations)
        assertEquals(listOf("prop_mat"), store.data.value.purchases.map { it.itemId })

        repository.wear("prop_mat", worn = true)
        assertEquals(setOf("prop_mat"), store.data.value.decorations)
    }

    @Test
    fun `props that are not owned cannot be shown`() = runTest {
        val store = FakeAvatarStore()

        repository(activeDays = 400, store = store).wear("prop_gong", worn = true)

        assertEquals(emptySet(), store.data.value.decorations)
    }

    @Test
    fun `the first look remembers the rank, later ranks are new`() = runTest {
        val store = FakeAvatarStore()
        val repository = repository(activeDays = 8, store = store)

        assertNull(repository.getAvatarState().first().newRank)
        assertEquals(FighterRank.SPARRING_PARTNER.number, store.data.value.seenStage)

        store.data.value = store.data.value.copy(seenStage = FighterRank.TRAINEE.number)
        assertEquals(FighterRank.SPARRING_PARTNER, repository.getAvatarState().first().newRank)

        repository.markRankSeen(FighterRank.SPARRING_PARTNER)
        assertNull(repository.getAvatarState().first().newRank)
    }

    @Test
    fun `the plant stage the user saw becomes the rank, without a notice`() = runTest {
        // 25 active days: plant stage 5 (plantlet), now rank 5 (contender)
        val store = FakeAvatarStore(AvatarData(seenStage = 5))
        val state = repository(activeDays = 25, store = store).getAvatarState().first()

        assertEquals(FighterRank.CONTENDER, state.fighter.rank)
        assertEquals(5, state.fighter.rank.number)
        assertNull(state.newRank)
    }

    @Test
    fun `xp comes from the check-ins of each routine's stat`() = runTest {
        val today = LocalDate.now()
        val habits = FakeHabitRepo(listOf(routine(1), routine(2)))
        val stats = FakeRoutineStatStore(mapOf(1L to FighterStat.STRENGTH))
        (0 until 6).forEach { habits.checkIn(1, today.minus(it, DateTimeUnit.DAY)) }
        habits.checkIn(2, today)

        val fighter =
            repository(activeDays = 6, habits = habits, stats = stats)
                .getAvatarState()
                .first()
                .fighter

        assertEquals(6, fighter.xp(FighterStat.STRENGTH))
        assertEquals(2, fighter.level(FighterStat.STRENGTH))
        assertEquals(1, fighter.xp(FighterStat.STAMINA))
    }

    @Test
    fun `no level-up notices on the first run after the update`() = runTest {
        val today = LocalDate.now()
        val store = FakeAvatarStore(AvatarData(seenStage = 3))
        val habits = FakeHabitRepo(listOf(routine(1)))
        (0 until 20).forEach { habits.checkIn(1, today.minus(it, DateTimeUnit.DAY)) }
        val repository = repository(activeDays = 20, store = store, habits = habits)

        // 20 stamina XP from before the update: level 3, stored without a notice
        assertEquals(emptyMap(), repository.getAvatarState().first().newStatLevels)
        assertEquals(3, store.data.value.seenStatLevels[FighterStat.STAMINA])
        assertEquals(1, store.data.value.seenStatLevels[FighterStat.FOCUS])

        // 10 more check-ins reach level 4, which is announced until it is marked seen
        (20 until 30).forEach { habits.checkIn(1, today.minus(it, DateTimeUnit.DAY)) }
        val state = repository.getAvatarState().first()
        assertEquals(mapOf(FighterStat.STAMINA to 4), state.newStatLevels)

        repository.markStatLevelsSeen(state.statLevels)
        assertEquals(emptyMap(), repository.getAvatarState().first().newStatLevels)
    }

    @Test
    fun `water spent on items that left the catalog comes back`() = runTest {
        val today = LocalDate.now()
        val store =
            FakeAvatarStore(
                AvatarData(
                    purchases =
                        listOf(
                            Purchase("gi_blue", today, 10),
                            Purchase("plant_only_item", today, 15),
                        )
                )
            )

        val state = repository(activeDays = 30, store = store).getAvatarState().first()

        assertEquals(30 - 10, state.energy)
        assertEquals(setOf("gi_blue"), state.owned)
        // still stored, so nothing is lost when a backup goes back to an older version
        assertEquals(2, store.data.value.purchases.size)
    }

    @Test
    fun `restoring merges purchases and keeps local choices`() {
        val today = LocalDate.now()
        val local = AvatarData(name = "Fern", purchases = listOf(Purchase("gi_blue", today, 10)))
        val backup =
            AvatarData(
                name = "Grit",
                purchases =
                    listOf(
                        Purchase("gi_blue", today.minus(3, DateTimeUnit.DAY), 10),
                        Purchase("gear_sweatband", today, 15),
                    ),
                outfit = "gi_blue",
                accessories = setOf("gear_sweatband"),
                seenStage = 4,
                decorations = setOf("prop_gong"),
            )

        val merged = local.mergedWith(backup)

        assertEquals("Fern", merged.name)
        assertEquals(
            listOf(today.minus(3, DateTimeUnit.DAY), today),
            merged.purchases.map { it.date },
        )
        assertEquals("gi_blue", merged.outfit)
        assertEquals(setOf("gear_sweatband"), merged.accessories)
        assertEquals(4, merged.seenStage)
        assertEquals(setOf("prop_gong"), merged.decorations)
        assertEquals(
            setOf("prop_mat"),
            local.copy(decorations = setOf("prop_mat")).mergedWith(backup).decorations,
        )
    }
}
