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

import kotlinx.datetime.LocalDate

enum class ShopItemKind {
    /** Exactly one is worn at a time. */
    POT_COLOR,
    /** Any number can be worn. */
    ACCESSORY,
    /** Stands on its own fixed spot on the meadow; any number can be shown. */
    DECORATION,
}

data class ShopItem(
    val id: String,
    val kind: ShopItemKind,
    val price: Int,
    val minStage: PlantStage,
    /**
     * Only for seasonal items: when they are for sale. Outside of it they disappear from the shop
     * unless they were bought; bought ones stay forever.
     */
    val season: SaleWindow? = null,
)

/** A bought item. The price is kept, so later price changes do not rewrite the past. */
data class Purchase(val itemId: String, val date: LocalDate, val price: Int)

/** Everything the plant can wear. The ids are stored with each purchase, so they never change. */
object ShopCatalog {
    const val DEFAULT_POT_COLOR = "pot_terracotta"

    val items: List<ShopItem> =
        listOf(
            ShopItem(DEFAULT_POT_COLOR, ShopItemKind.POT_COLOR, 0, PlantStage.SEED),
            ShopItem("pot_blue", ShopItemKind.POT_COLOR, 10, PlantStage.SEED),
            ShopItem("pot_yellow", ShopItemKind.POT_COLOR, 10, PlantStage.SEED),
            ShopItem("pot_green", ShopItemKind.POT_COLOR, 10, PlantStage.SEED),
            ShopItem("pot_purple", ShopItemKind.POT_COLOR, 15, PlantStage.SEEDLING),
            ShopItem("pot_gold", ShopItemKind.POT_COLOR, 40, PlantStage.YOUNG_PLANT),
            ShopItem("acc_watering_can", ShopItemKind.ACCESSORY, 25, PlantStage.SEED),
            ShopItem("acc_ladybug", ShopItemKind.ACCESSORY, 15, PlantStage.GERM),
            ShopItem("acc_butterfly", ShopItemKind.ACCESSORY, 20, PlantStage.SPROUT),
            ShopItem("acc_blossoms", ShopItemKind.ACCESSORY, 30, PlantStage.PLANTLET),
            ShopItem("acc_bird", ShopItemKind.ACCESSORY, 40, PlantStage.YOUNG_TREE),
            ShopItem("acc_birdhouse", ShopItemKind.ACCESSORY, 50, PlantStage.TREE),
            ShopItem("acc_fruits", ShopItemKind.ACCESSORY, 60, PlantStage.BIG_TREE),
            ShopItem("acc_fairy_lights", ShopItemKind.ACCESSORY, 70, PlantStage.BIG_TREE),
            ShopItem("acc_swing", ShopItemKind.ACCESSORY, 80, PlantStage.BLOSSOMING_TREE),
            ShopItem("deco_mushrooms", ShopItemKind.DECORATION, 10, PlantStage.SEED),
            ShopItem("deco_fence", ShopItemKind.DECORATION, 15, PlantStage.GERM),
            ShopItem("deco_snail", ShopItemKind.DECORATION, 15, PlantStage.SEEDLING),
            ShopItem("deco_hedgehog", ShopItemKind.DECORATION, 25, PlantStage.PLANTLET),
            ShopItem("deco_rabbit", ShopItemKind.DECORATION, 30, PlantStage.YOUNG_PLANT),
            ShopItem("deco_bench", ShopItemKind.DECORATION, 40, PlantStage.YOUNG_TREE),
            ShopItem("deco_pond", ShopItemKind.DECORATION, 50, PlantStage.TREE),
            ShopItem("deco_lantern", ShopItemKind.DECORATION, 45, PlantStage.BIG_TREE),
        )

    fun byId(id: String): ShopItem? = items.firstOrNull { it.id == id }
}

enum class PurchaseCheck {
    OK,
    ALREADY_OWNED,
    STAGE_TOO_LOW,
    NOT_ENOUGH_ENERGY,
    /** A seasonal item outside of its sale window. */
    OUT_OF_SEASON,
}

object Shop {
    /** Free items are owned from the start. */
    fun owned(purchases: List<Purchase>): Set<String> =
        ShopCatalog.items.filter { it.price == 0 }.map { it.id }.toSet() +
            purchases.map { it.itemId }

    /**
     * The items the shop shows on [today]: everything except seasonal items outside of their window
     * that were not bought.
     */
    fun offered(
        today: LocalDate,
        purchases: List<Purchase>,
        items: List<ShopItem> = ShopCatalog.items,
    ): List<ShopItem> {
        val owned = owned(purchases)
        return items.filter { it.id in owned || it.isForSale(today) }
    }

    fun check(
        item: ShopItem,
        stage: PlantStage,
        energy: Int,
        purchases: List<Purchase>,
        today: LocalDate,
    ): PurchaseCheck =
        when {
            item.id in owned(purchases) -> PurchaseCheck.ALREADY_OWNED
            !item.isForSale(today) -> PurchaseCheck.OUT_OF_SEASON
            stage < item.minStage -> PurchaseCheck.STAGE_TOO_LOW
            energy < item.price -> PurchaseCheck.NOT_ENOUGH_ENERGY
            else -> PurchaseCheck.OK
        }
}
