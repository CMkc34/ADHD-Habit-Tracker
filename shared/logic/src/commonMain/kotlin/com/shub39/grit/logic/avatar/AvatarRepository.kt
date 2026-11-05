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

import com.shub39.grit.core.activity.ActivityRepo
import com.shub39.grit.core.avatar.AvatarRepo
import com.shub39.grit.core.avatar.AvatarState
import com.shub39.grit.core.avatar.AvatarStore
import com.shub39.grit.core.avatar.Purchase
import com.shub39.grit.core.avatar.PurchaseCheck
import com.shub39.grit.core.avatar.Shop
import com.shub39.grit.core.avatar.ShopCatalog
import com.shub39.grit.core.avatar.ShopItemKind
import com.shub39.grit.core.avatar.UserName
import com.shub39.grit.core.fighter.FighterCharacter
import com.shub39.grit.core.fighter.FighterColors
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.core.fighter.FighterStat
import com.shub39.grit.core.fighter.FighterState
import com.shub39.grit.core.fighter.RoutineStatStore
import com.shub39.grit.core.fighter.StatXp
import com.shub39.grit.core.fighter.statOf
import com.shub39.grit.core.habits.HabitRepo
import com.shub39.grit.core.now
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.Single

@Single(binds = [AvatarRepo::class])
class AvatarRepository(
    private val activityRepo: ActivityRepo,
    private val store: AvatarStore,
    private val habitRepo: HabitRepo,
    private val routineStatStore: RoutineStatStore,
) : AvatarRepo {

    override fun getAvatarState(): Flow<AvatarState> =
        combine(activityRepo.getActivityReport(), store.getAvatar(), xp()) { report, data, xp ->
                AvatarState(
                    fighter = FighterState.of(report, xp),
                    energy = report.energy,
                    data = data,
                    today = LocalDate.now(),
                )
            }
            // the first look sets what the user knows, later ranks and levels are announced;
            // users of the plant keep their stage as rank, and their XP is not announced at once
            .onEach {
                if (it.data.seenStage == 0) store.markStageSeen(it.fighter.rank.number)
                if (it.data.seenStatLevels.isEmpty()) store.markStatLevelsSeen(it.statLevels)
            }

    /** XP per stat from all routines and their check-ins (SPEC 9.3). */
    private fun xp(): Flow<Map<FighterStat, Int>> =
        combine(habitRepo.getHabitsWithAnalytics(), routineStatStore.getStats()) { habits, stats ->
                StatXp.of(
                    habits = habits.map { it.habit },
                    statuses = habits.flatMap { it.statuses },
                    statOf = stats::statOf,
                )
            }
            .distinctUntilChanged()

    override suspend fun buy(itemId: String): PurchaseCheck {
        val item = ShopCatalog.byId(itemId) ?: return PurchaseCheck.ALREADY_OWNED
        val state = getAvatarState().first()
        val check = state.check(item)
        if (check != PurchaseCheck.OK) return check

        store.addPurchase(Purchase(itemId = item.id, date = LocalDate.now(), price = item.price))
        wear(item.id, worn = true)
        return check
    }

    override suspend fun rename(name: String) = store.setName(name)

    override suspend fun setUserName(name: String) = store.setUserName(UserName.clean(name))

    override suspend fun chooseFighter(character: FighterCharacter, colors: FighterColors) =
        store.setFighter(character, colors)

    override suspend fun wear(itemId: String, worn: Boolean) {
        val item = ShopCatalog.byId(itemId) ?: return
        val owned = Shop.owned(store.getAvatar().first().purchases)
        if (item.id !in owned) return

        when (item.kind) {
            ShopItemKind.OUTFIT ->
                when {
                    worn -> store.setOutfit(item.id)
                    store.getAvatar().first().outfit == item.id -> store.setOutfit(null)
                }
            ShopItemKind.GEAR -> store.setAccessory(item.id, worn)
            ShopItemKind.PROP -> store.setDecoration(item.id, worn)
        }
    }

    override suspend fun markRankSeen(rank: FighterRank) = store.markStageSeen(rank.number)

    override suspend fun markStatLevelsSeen(levels: Map<FighterStat, Int>) =
        store.markStatLevelsSeen(levels)
}
