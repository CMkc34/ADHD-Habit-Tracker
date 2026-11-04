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

import com.shub39.grit.core.fighter.FighterItems
import com.shub39.grit.core.fighter.FighterRank
import kotlinx.datetime.LocalDate

enum class ShopItemKind {
    /** A gi colour; replaces the main colour chosen at setup. */
    OUTFIT,
    /** Worn by the fighter; any number at the same time. */
    GEAR,
    /** Stands on its own fixed spot in the training ground; any number can be shown. */
    PROP,
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

/**
 * Everything the shop sells (SPEC 9.7, #7). Purely cosmetic: water never buys stats. The ids are
 * stored with each purchase, so they never change; prices and ranks live only here.
 */
object ShopCatalog {
    const val DEFAULT_POT_COLOR = FighterItems.GI_WHITE

    val items: List<ShopItem> =
        listOf(
            ShopItem(FighterItems.GI_WHITE, ShopItemKind.OUTFIT, 0, FighterRank.ROOKIE),
            ShopItem(FighterItems.GI_BLUE, ShopItemKind.OUTFIT, 10, FighterRank.ROOKIE),
            ShopItem(FighterItems.GI_RED, ShopItemKind.OUTFIT, 10, FighterRank.ROOKIE),
            ShopItem(FighterItems.GI_BLACK, ShopItemKind.OUTFIT, 10, FighterRank.ROOKIE),
            ShopItem(FighterItems.GI_PURPLE, ShopItemKind.OUTFIT, 15, FighterRank.SPARRING_PARTNER),
            ShopItem(FighterItems.GI_GOLD, ShopItemKind.OUTFIT, 40, FighterRank.FIGHTER),
            ShopItem(FighterItems.SWEATBAND, ShopItemKind.GEAR, 10, FighterRank.ROOKIE),
            ShopItem(FighterItems.GLOVES, ShopItemKind.GEAR, 20, FighterRank.TRAINEE),
            ShopItem(FighterItems.SNEAKERS, ShopItemKind.GEAR, 25, FighterRank.BRAWLER),
            ShopItem(FighterItems.SUNGLASSES, ShopItemKind.GEAR, 30, FighterRank.CONTENDER),
            ShopItem(FighterItems.CAPE, ShopItemKind.GEAR, 40, FighterRank.FIGHTER),
            ShopItem(FighterItems.CHAMPION_BELT, ShopItemKind.GEAR, 70, FighterRank.CHAMPION),
            ShopItem(FighterItems.CROWN, ShopItemKind.GEAR, 80, FighterRank.GRANDMASTER),
            ShopItem(FighterItems.MAT, ShopItemKind.PROP, 10, FighterRank.ROOKIE),
            ShopItem(FighterItems.WATER_BOTTLE, ShopItemKind.PROP, 10, FighterRank.ROOKIE),
            ShopItem(FighterItems.PUNCHING_BAG, ShopItemKind.PROP, 25, FighterRank.TRAINEE),
            ShopItem(FighterItems.DUMBBELLS, ShopItemKind.PROP, 25, FighterRank.SPARRING_PARTNER),
            ShopItem(FighterItems.WOODEN_DUMMY, ShopItemKind.PROP, 35, FighterRank.CONTENDER),
            ShopItem(FighterItems.BANNER, ShopItemKind.PROP, 40, FighterRank.CHALLENGER),
            ShopItem(FighterItems.TROPHY_SHELF, ShopItemKind.PROP, 50, FighterRank.CHAMPION),
            ShopItem(FighterItems.GONG, ShopItemKind.PROP, 60, FighterRank.MASTER),
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
