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

import com.shub39.grit.core.fighter.FighterLook
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.core.fighter.FighterStat
import com.shub39.grit.core.fighter.FighterState
import com.shub39.grit.core.now
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/** The fighter as the UI shows it; [today] decides which seasonal items are for sale. */
data class AvatarState(
    val fighter: FighterState,
    val energy: Int,
    val data: AvatarData,
    val today: LocalDate = LocalDate.now(),
) {
    val owned: Set<String>
        get() = Shop.owned(data.purchases)

    /** The fighter's name: the one the user gave, otherwise the name of its look. */
    val name: String
        get() = data.name.ifBlank { data.look.defaultName }

    /**
     * A rank reached since the user last looked, `null` when there is nothing new. The rank number
     * is stored where the plant stage was, so ranks the plant already had are not announced again.
     */
    val newRank: FighterRank?
        get() = fighter.rank.takeIf { it.number > data.seenStage && data.seenStage > 0 }

    /**
     * Stat levels reached since the user last looked, empty when there is nothing new. Nothing is
     * announced before the first levels were stored, see [AvatarData.seenStatLevels].
     */
    val newStatLevels: Map<FighterStat, Int>
        get() =
            if (data.seenStatLevels.isEmpty()) emptyMap()
            else
                FighterStat.entries
                    .associateWith { fighter.level(it) }
                    .filter { (stat, level) -> level > (data.seenStatLevels[stat] ?: 1) }

    /** The current level of every stat, to store as seen. */
    val statLevels: Map<FighterStat, Int>
        get() = FighterStat.entries.associateWith { fighter.level(it) }

    fun check(item: ShopItem): PurchaseCheck =
        Shop.check(item, fighter.rank, energy, data.purchases, today)

    /** What the shop shows today, see [Shop.offered]. */
    val offered: List<ShopItem>
        get() = Shop.offered(today, data.purchases)

    fun isWorn(item: ShopItem): Boolean =
        when (item.kind) {
            ShopItemKind.POT_COLOR -> data.potColor == item.id
            ShopItemKind.ACCESSORY -> item.id in data.accessories
            ShopItemKind.DECORATION -> item.id in data.decorations
        }

    /** What the fighter would look like with [item] on, for the preview before buying. */
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

    /** Stores how the fighter calls the user, see [UserName.clean]. */
    suspend fun setUserName(name: String)

    /** Switches the fighter's look; rank, stats, water and purchases stay. */
    suspend fun setLook(look: FighterLook)

    /**
     * Puts an owned item on or takes it off (a decoration is shown or hidden in the scene); an
     * outfit can only be replaced, not removed.
     */
    suspend fun wear(itemId: String, worn: Boolean)

    suspend fun markRankSeen(rank: FighterRank)

    /** Remembers [levels] as announced; a stored level never goes down. */
    suspend fun markStatLevelsSeen(levels: Map<FighterStat, Int>)
}
