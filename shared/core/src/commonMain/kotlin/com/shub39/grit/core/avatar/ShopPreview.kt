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

/**
 * What the buy dialog shows for [item]: the plant without and with it. When the item needs a higher
 * stage, the plant is shown grown to that stage, so the preview never draws an accessory on a plant
 * that cannot hold it yet.
 */
data class ShopPreview(
    val item: ShopItem,
    val check: PurchaseCheck,
    val stage: PlantStage,
    val without: AvatarData,
    val with: AvatarData,
    /** Water still needed to buy it, 0 when there is enough. */
    val missingEnergy: Int,
) {
    val canBuy: Boolean
        get() = check == PurchaseCheck.OK

    /** The preview shows a later stage than the plant has reached. */
    val isFutureStage: Boolean
        get() = check == PurchaseCheck.STAGE_TOO_LOW
}

fun AvatarState.preview(item: ShopItem): ShopPreview =
    ShopPreview(
        item = item,
        check = check(item),
        stage = maxOf(plant.stage, item.minStage),
        without = data,
        with = tryOn(item),
        missingEnergy = (item.price - energy).coerceAtLeast(0),
    )
