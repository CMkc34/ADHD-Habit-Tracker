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
package com.shub39.grit.logic.core.backup

import com.shub39.grit.core.avatar.AvatarData
import com.shub39.grit.core.avatar.AvatarSpecies
import com.shub39.grit.core.avatar.Purchase
import com.shub39.grit.core.avatar.ShopCatalog
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * The plant avatar in a backup (phase 4, decorations since #82); dates are ISO strings
 * (`2026-10-22`).
 */
@Serializable
data class AvatarSchema(
    val name: String = "",
    val purchases: List<PurchaseSchema> = emptyList(),
    val potColor: String = ShopCatalog.DEFAULT_POT_COLOR,
    val accessories: List<String> = emptyList(),
    val seenStage: Int = 0,
    val decorations: List<String> = emptyList(),
    // #139: how the plant calls the user
    val userName: String = "",
    // #140: which plant grows, see AvatarSpecies.id
    val species: String = AvatarSpecies.SPROUT.id,
)

@Serializable data class PurchaseSchema(val itemId: String, val date: String, val price: Int)

fun AvatarData.toAvatarSchema(): AvatarSchema {
    return AvatarSchema(
        name = name,
        purchases = purchases.map { PurchaseSchema(it.itemId, it.date.toString(), it.price) },
        potColor = potColor,
        accessories = accessories.sorted(),
        seenStage = seenStage,
        decorations = decorations.sorted(),
        userName = userName,
        species = species.id,
    )
}

fun AvatarSchema.toAvatarData(): AvatarData {
    return AvatarData(
        name = name,
        purchases = purchases.map { Purchase(it.itemId, LocalDate.parse(it.date), it.price) },
        potColor = potColor,
        accessories = accessories.toSet(),
        seenStage = seenStage,
        decorations = decorations.toSet(),
        userName = userName,
        species = AvatarSpecies.fromId(species),
    )
}
