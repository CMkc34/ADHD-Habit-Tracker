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
import com.shub39.grit.core.avatar.AvatarSpecies
import com.shub39.grit.core.avatar.AvatarState
import com.shub39.grit.core.avatar.AvatarStore
import com.shub39.grit.core.avatar.PlantStage
import com.shub39.grit.core.avatar.PlantState
import com.shub39.grit.core.avatar.Purchase
import com.shub39.grit.core.avatar.PurchaseCheck
import com.shub39.grit.core.avatar.Shop
import com.shub39.grit.core.avatar.ShopCatalog
import com.shub39.grit.core.avatar.ShopItemKind
import com.shub39.grit.core.avatar.UserName
import com.shub39.grit.core.now
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.Single

@Single(binds = [AvatarRepo::class])
class AvatarRepository(private val activityRepo: ActivityRepo, private val store: AvatarStore) :
    AvatarRepo {

    override fun getAvatarState(): Flow<AvatarState> =
        combine(activityRepo.getActivityReport(), store.getAvatar()) { report, data ->
                AvatarState(
                    plant = PlantState.of(report),
                    energy = report.energy,
                    data = data,
                    today = LocalDate.now(),
                )
            }
            // the first look sets the stage the user knows, later stages are announced
            .onEach { if (it.data.seenStage == 0) store.markStageSeen(it.plant.stage.number) }

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

    override suspend fun setSpecies(species: AvatarSpecies) = store.setSpecies(species)

    override suspend fun wear(itemId: String, worn: Boolean) {
        val item = ShopCatalog.byId(itemId) ?: return
        val owned = Shop.owned(store.getAvatar().first().purchases)
        if (item.id !in owned) return

        when (item.kind) {
            ShopItemKind.POT_COLOR -> if (worn) store.setPotColor(item.id)
            ShopItemKind.ACCESSORY -> store.setAccessory(item.id, worn)
            ShopItemKind.DECORATION -> store.setDecoration(item.id, worn)
        }
    }

    override suspend fun markStageSeen(stage: PlantStage) = store.markStageSeen(stage.number)
}
