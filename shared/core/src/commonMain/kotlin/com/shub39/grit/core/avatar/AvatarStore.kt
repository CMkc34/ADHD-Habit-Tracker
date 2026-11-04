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

import kotlinx.coroutines.flow.Flow

/** What is stored about the plant; its stage and the energy balance are derived. */
data class AvatarData(
    val name: String = "",
    val purchases: List<Purchase> = emptyList(),
    val potColor: String = ShopCatalog.DEFAULT_POT_COLOR,
    val accessories: Set<String> = emptySet(),
    /** Bought decorations that are shown on the meadow. */
    val decorations: Set<String> = emptySet(),
    /** Highest stage the user has been told about, so a new stage is announced once. */
    val seenStage: Int = 0,
    /** How the plant calls the user (#139); empty while the user has not given a name. */
    val userName: String = "",
    /** Which plant the user chose (#140). */
    val species: AvatarSpecies = AvatarSpecies.SPROUT,
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
            species = if (species == AvatarSpecies.SPROUT) backup.species else species,
        )
}

interface AvatarStore {
    fun getAvatar(): Flow<AvatarData>

    suspend fun setName(name: String)

    suspend fun setUserName(name: String)

    suspend fun setSpecies(species: AvatarSpecies)

    suspend fun addPurchase(purchase: Purchase)

    suspend fun setPotColor(itemId: String)

    suspend fun setAccessory(itemId: String, worn: Boolean)

    suspend fun setDecoration(itemId: String, shown: Boolean)

    suspend fun markStageSeen(stage: Int)

    /** Replaces everything stored with [data], used when restoring a backup. */
    suspend fun replaceAll(data: AvatarData)
}
