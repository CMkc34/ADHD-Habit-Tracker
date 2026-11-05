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

import com.shub39.grit.core.fighter.FighterCharacter
import com.shub39.grit.core.fighter.FighterColors
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
    /**
     * The worn outfit ([ShopItemKind.OUTFIT]), `null` while the fighter wears the colours chosen at
     * setup. Stored where the plant's pot colour was.
     */
    val outfit: String? = null,
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
    /**
     * The look from before the character choice, stored where the plant species was. Only used for
     * users who never picked a [fighterCharacter] (SPEC 9.8).
     */
    val look: FighterLook = FighterLook.DEFAULT,
    /**
     * Highest level of each stat the user has been told about. Empty until the levels are first
     * stored, which happens without announcing them (first run after the update, SPEC 9.8).
     */
    val seenStatLevels: Map<FighterStat, Int> = emptyMap(),
    /** The fighter picked at setup (#26), `null` for users who set up the app before the choice. */
    val fighterCharacter: FighterCharacter? = null,
    /** The colours picked at setup (#26), `null` for the [character]'s default colours. */
    val fighterColors: FighterColors? = null,
) {
    /** Who trains with the user: the picked fighter, or the one that replaces the old [look]. */
    val character: FighterCharacter
        get() = fighterCharacter ?: FighterCharacter.of(look)

    /** The colours the fighter wears without a bought outfit. */
    val colors: FighterColors
        get() = fighterColors ?: character.defaultColors

    /** The fighter's name: the one the user gave, or the [character]'s default name. */
    val displayName: String
        get() = name.ifBlank { character.defaultName }

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
            outfit = outfit ?: backup.outfit,
            accessories = accessories.ifEmpty { backup.accessories },
            decorations = decorations.ifEmpty { backup.decorations },
            seenStage = maxOf(seenStage, backup.seenStage),
            userName = userName.ifBlank { backup.userName },
            look = if (look == FighterLook.DEFAULT) backup.look else look,
            seenStatLevels =
                (seenStatLevels.keys + backup.seenStatLevels.keys).associateWith {
                    maxOf(seenStatLevels[it] ?: 0, backup.seenStatLevels[it] ?: 0)
                },
            fighterCharacter = fighterCharacter ?: backup.fighterCharacter,
            // the colours belong to the fighter they were picked for
            fighterColors = if (fighterCharacter != null) fighterColors else backup.fighterColors,
        )
}

interface AvatarStore {
    fun getAvatar(): Flow<AvatarData>

    suspend fun setName(name: String)

    suspend fun setUserName(name: String)

    /** Stores the fighter and colours picked at setup (#26). */
    suspend fun setFighter(character: FighterCharacter, colors: FighterColors)

    suspend fun addPurchase(purchase: Purchase)

    /** Puts on the outfit [itemId], or takes the outfit off with `null`. */
    suspend fun setOutfit(itemId: String?)

    suspend fun setAccessory(itemId: String, worn: Boolean)

    suspend fun setDecoration(itemId: String, shown: Boolean)

    suspend fun markStageSeen(stage: Int)

    /** Stores [levels] as seen; per stat, the higher of the stored and the given level is kept. */
    suspend fun markStatLevelsSeen(levels: Map<FighterStat, Int>)

    /** Replaces everything stored with [data], used when restoring a backup. */
    suspend fun replaceAll(data: AvatarData)
}
