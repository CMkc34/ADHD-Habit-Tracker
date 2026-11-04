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

import com.shub39.grit.core.fighter.FighterRank
import kotlinx.datetime.LocalDate

enum class ShopItemKind {
    /** Exactly one is worn at a time. */
    POT_COLOR,
    /** Any number can be worn. */
    ACCESSORY,
    /** Stands on its own fixed spot in the scene; any number can be shown. */
    DECORATION,
}

data class ShopItem(
    val id: String,
    val kind: ShopItemKind,
    val price: Int,
    val minRank: FighterRank,
    /**
     * Only for seasonal items: when they are for sale. Outside of it they disappear from the shop
     * unless they were bought; bought ones stay forever.
     */
    val season: SaleWindow? = null,
)

/**
 * A bought item. The price is kept, so later price changes do not rewrite the past. A purchase of
 * an item that is no longer in the [ShopCatalog] is kept in storage and backups, but ignored (SPEC
 * 9.8).
 */
data class Purchase(val itemId: String, val date: LocalDate, val price: Int)

/** Everything the avatar can wear. The ids are stored with each purchase, so they never change. */
object ShopCatalog {
    const val DEFAULT_POT_COLOR = "pot_terracotta"

    val items: List<ShopItem> =
        listOf(
            ShopItem(DEFAULT_POT_COLOR, ShopItemKind.POT_COLOR, 0, FighterRank.ROOKIE),
            ShopItem("pot_blue", ShopItemKind.POT_COLOR, 10, FighterRank.ROOKIE),
            ShopItem("pot_yellow", ShopItemKind.POT_COLOR, 10, FighterRank.ROOKIE),
            ShopItem("pot_green", ShopItemKind.POT_COLOR, 10, FighterRank.ROOKIE),
            ShopItem("pot_purple", ShopItemKind.POT_COLOR, 15, FighterRank.SPARRING_PARTNER),
            ShopItem("pot_gold", ShopItemKind.POT_COLOR, 40, FighterRank.FIGHTER),
            ShopItem("acc_watering_can", ShopItemKind.ACCESSORY, 25, FighterRank.ROOKIE),
            ShopItem("acc_ladybug", ShopItemKind.ACCESSORY, 15, FighterRank.TRAINEE),
            ShopItem("acc_butterfly", ShopItemKind.ACCESSORY, 20, FighterRank.BRAWLER),
            ShopItem("acc_blossoms", ShopItemKind.ACCESSORY, 30, FighterRank.CONTENDER),
            ShopItem("acc_bird", ShopItemKind.ACCESSORY, 40, FighterRank.VETERAN),
            ShopItem("acc_birdhouse", ShopItemKind.ACCESSORY, 50, FighterRank.CHAMPION),
            ShopItem("acc_fruits", ShopItemKind.ACCESSORY, 60, FighterRank.MASTER),
            ShopItem("acc_fairy_lights", ShopItemKind.ACCESSORY, 70, FighterRank.MASTER),
            ShopItem("acc_swing", ShopItemKind.ACCESSORY, 80, FighterRank.GRANDMASTER),
            ShopItem("deco_mushrooms", ShopItemKind.DECORATION, 10, FighterRank.ROOKIE),
            ShopItem("deco_fence", ShopItemKind.DECORATION, 15, FighterRank.TRAINEE),
            ShopItem("deco_snail", ShopItemKind.DECORATION, 15, FighterRank.SPARRING_PARTNER),
            ShopItem("deco_hedgehog", ShopItemKind.DECORATION, 25, FighterRank.CONTENDER),
            ShopItem("deco_rabbit", ShopItemKind.DECORATION, 30, FighterRank.FIGHTER),
            ShopItem("deco_bench", ShopItemKind.DECORATION, 40, FighterRank.VETERAN),
            ShopItem("deco_pond", ShopItemKind.DECORATION, 50, FighterRank.CHAMPION),
            ShopItem("deco_lantern", ShopItemKind.DECORATION, 45, FighterRank.MASTER),
        )

    fun byId(id: String): ShopItem? = items.firstOrNull { it.id == id }
}

enum class PurchaseCheck {
    OK,
    ALREADY_OWNED,
    RANK_TOO_LOW,
    NOT_ENOUGH_ENERGY,
    /** A seasonal item outside of its sale window. */
    OUT_OF_SEASON,
}

object Shop {
    /** Whether [itemId] is sold in [items]; purchases of anything else are ignored. */
    fun isKnown(itemId: String, items: List<ShopItem> = ShopCatalog.items): Boolean =
        items.any { it.id == itemId }

    /**
     * Free items are owned from the start. Purchases of items that are not in [items] (the plant's
     * items after the switch to the fighter) don't count.
     */
    fun owned(purchases: List<Purchase>, items: List<ShopItem> = ShopCatalog.items): Set<String> =
        items.filter { it.price == 0 }.map { it.id }.toSet() +
            purchases.map { it.itemId }.filter { isKnown(it, items) }

    /**
     * The purchases that cost energy: only those of items in [items], so the water spent on items
     * that left the catalog comes back.
     */
    fun charged(
        purchases: List<Purchase>,
        items: List<ShopItem> = ShopCatalog.items,
    ): List<Purchase> = purchases.filter { isKnown(it.itemId, items) }

    /**
     * The items the shop shows on [today]: everything except seasonal items outside of their window
     * that were not bought.
     */
    fun offered(
        today: LocalDate,
        purchases: List<Purchase>,
        items: List<ShopItem> = ShopCatalog.items,
    ): List<ShopItem> {
        val owned = owned(purchases, items)
        return items.filter { it.id in owned || it.isForSale(today) }
    }

    fun check(
        item: ShopItem,
        rank: FighterRank,
        energy: Int,
        purchases: List<Purchase>,
        today: LocalDate,
    ): PurchaseCheck =
        when {
            item.price == 0 || purchases.any { it.itemId == item.id } -> PurchaseCheck.ALREADY_OWNED
            !item.isForSale(today) -> PurchaseCheck.OUT_OF_SEASON
            rank < item.minRank -> PurchaseCheck.RANK_TOO_LOW
            energy < item.price -> PurchaseCheck.NOT_ENOUGH_ENERGY
            else -> PurchaseCheck.OK
        }
}
