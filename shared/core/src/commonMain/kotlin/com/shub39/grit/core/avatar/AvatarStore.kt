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
import com.shub39.grit.core.fighter.FighterStat
import kotlinx.coroutines.flow.Flow

/** What is stored about the fighter; its rank, stats and the energy balance are derived. */
data class AvatarData(
    val name: String = "",
    /**
     * Everything ever bought. Purchases of items that are no longer in the [ShopCatalog] stay here,
     * but are neither owned nor charged (SPEC 9.8).
     */
    val purchases: List<Purchase> = emptyList(),
    val potColor: String = ShopCatalog.DEFAULT_POT_COLOR,
    val accessories: Set<String> = emptySet(),
    /** Bought decorations that are shown in the scene. */
    val decorations: Set<String> = emptySet(),
    /**
     * Highest rank number the user has been told about, so a new rank is announced once. It is the
     * former plant stage, which has the same thresholds.
     */
    val seenStage: Int = 0,
    /** How the fighter calls the user (#139); empty while the user has not given a name. */
    val userName: String = "",
    /** Who trains with the user, stored where the plant species was. */
    val look: FighterLook = FighterLook.DEFAULT,
    /**
     * Highest level of each stat the user has been told about. Empty until the levels are first
     * stored, which happens without announcing them (first run after the update, SPEC 9.8).
     */
    val seenStatLevels: Map<FighterStat, Int> = emptyMap(),
) {
    /**
     * Combines the current record with a restored backup: purchases are joined (the earlier one
     * wins), and the backup fills in what is still at its default here.
     */
    fun mergedWith(backup: AvatarData): AvatarData =
        AvatarData(
            name = name.ifBlank { backup.name },
            purchases =
                (purchases + backup.purchases)
                    .groupBy { it.itemId }
                    .map { (_, sameItem) -> sameItem.minBy { it.date } }
                    .sortedBy { it.date },
            potColor = if (potColor == ShopCatalog.DEFAULT_POT_COLOR) backup.potColor else potColor,
            accessories = accessories.ifEmpty { backup.accessories },
            decorations = decorations.ifEmpty { backup.decorations },
            seenStage = maxOf(seenStage, backup.seenStage),
            userName = userName.ifBlank { backup.userName },
            look = if (look == FighterLook.DEFAULT) backup.look else look,
            seenStatLevels =
                (seenStatLevels.keys + backup.seenStatLevels.keys).associateWith {
                    maxOf(seenStatLevels[it] ?: 0, backup.seenStatLevels[it] ?: 0)
                },
        )
}

interface AvatarStore {
    fun getAvatar(): Flow<AvatarData>

    suspend fun setName(name: String)

    suspend fun setUserName(name: String)

    suspend fun setLook(look: FighterLook)

    suspend fun addPurchase(purchase: Purchase)

    suspend fun setPotColor(itemId: String)

    suspend fun setAccessory(itemId: String, worn: Boolean)

    suspend fun setDecoration(itemId: String, shown: Boolean)

    suspend fun markStageSeen(stage: Int)

    /** Stores [levels] as seen; per stat, the higher of the stored and the given level is kept. */
    suspend fun markStatLevelsSeen(levels: Map<FighterStat, Int>)

    /** Replaces everything stored with [data], used when restoring a backup. */
    suspend fun replaceAll(data: AvatarData)
}
