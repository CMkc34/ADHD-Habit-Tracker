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

import com.shub39.grit.core.now
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/** The plant as the UI shows it; [today] decides which seasonal items are for sale. */
data class AvatarState(
    val plant: PlantState,
    val energy: Int,
    val data: AvatarData,
    val today: LocalDate = LocalDate.now(),
) {
    val owned: Set<String>
        get() = Shop.owned(data.purchases)

    /** A stage reached since the user last looked, `null` when there is nothing new. */
    val newStage: PlantStage?
        get() = plant.stage.takeIf { it.number > data.seenStage && data.seenStage > 0 }

    fun check(item: ShopItem): PurchaseCheck =
        Shop.check(item, plant.stage, energy, data.purchases, today)

    /** What the shop shows today, see [Shop.offered]. */
    val offered: List<ShopItem>
        get() = Shop.offered(today, data.purchases)

    fun isWorn(item: ShopItem): Boolean =
        when (item.kind) {
            ShopItemKind.POT_COLOR -> data.potColor == item.id
            ShopItemKind.ACCESSORY -> item.id in data.accessories
            ShopItemKind.DECORATION -> item.id in data.decorations
        }

    /** What the plant would look like with [item] on, for the preview before buying. */
    fun tryOn(item: ShopItem): AvatarData =
        when (item.kind) {
            ShopItemKind.POT_COLOR -> data.copy(potColor = item.id)
            ShopItemKind.ACCESSORY -> data.copy(accessories = data.accessories + item.id)
            ShopItemKind.DECORATION -> data.copy(decorations = data.decorations + item.id)
        }
}

interface AvatarRepo {
    fun getAvatarState(): Flow<AvatarState>

    /** Buys [itemId] and puts it on; returns why not when it cannot be bought. */
    suspend fun buy(itemId: String): PurchaseCheck

    suspend fun rename(name: String)

    /** Stores how the plant calls the user, see [UserName.clean]. */
    suspend fun setUserName(name: String)

    /** Switches the plant; stage, water and purchases stay. */
    suspend fun setSpecies(species: AvatarSpecies)

    /**
     * Puts an owned item on or takes it off (a decoration is shown or hidden on the meadow); a pot
     * color can only be replaced, not removed.
     */
    suspend fun wear(itemId: String, worn: Boolean)

    suspend fun markStageSeen(stage: PlantStage)
}
