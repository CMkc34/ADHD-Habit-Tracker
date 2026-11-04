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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate

class SaleWindowTest {

    private val autumn = SaleWindow(fromMonth = 10, fromDay = 1, toMonth = 11, toDay = 30)
    private val winter = SaleWindow(fromMonth = 12, fromDay = 20, toMonth = 1, toDay = 6)

    private val pumpkin =
        ShopItem("deco_pumpkin", ShopItemKind.DECORATION, 30, PlantStage.SEED, season = autumn)
    private val mushrooms = ShopCatalog.byId("deco_mushrooms")!!
    private val items = listOf(mushrooms, pumpkin)

    @Test
    fun `a window inside one year includes its first and last day`() {
        assertTrue(LocalDate(2026, 10, 1) in autumn)
        assertTrue(LocalDate(2026, 11, 30) in autumn)
        assertFalse(LocalDate(2026, 9, 30) in autumn)
        assertFalse(LocalDate(2026, 12, 1) in autumn)
    }

    @Test
    fun `a window over the new year covers both ends`() {
        assertTrue(LocalDate(2026, 12, 24) in winter)
        assertTrue(LocalDate(2027, 1, 6) in winter)
        assertFalse(LocalDate(2027, 1, 7) in winter)
        assertFalse(LocalDate(2026, 12, 19) in winter)
    }

    @Test
    fun `items without a season are always for sale`() {
        assertTrue(mushrooms.isForSale(LocalDate(2026, 3, 3)))
        assertTrue(
            ShopCatalog.items
                .filter { it.season == null }
                .all { it.isForSale(LocalDate(2026, 7, 1)) }
        )
    }

    @Test
    fun `the shop hides a seasonal item outside its window`() {
        assertEquals(
            listOf(mushrooms, pumpkin),
            Shop.offered(LocalDate(2026, 11, 3), emptyList(), items),
        )
        assertEquals(listOf(mushrooms), Shop.offered(LocalDate(2026, 12, 3), emptyList(), items))
    }

    @Test
    fun `a bought seasonal item stays after its season`() {
        val bought = listOf(Purchase("deco_pumpkin", LocalDate(2026, 11, 3), 30))

        assertEquals(listOf(mushrooms, pumpkin), Shop.offered(LocalDate(2027, 3, 1), bought, items))
        assertEquals(
            PurchaseCheck.ALREADY_OWNED,
            Shop.check(pumpkin, PlantStage.SEED, 100, bought, LocalDate(2027, 3, 1)),
        )
    }

    @Test
    fun `a seasonal item can only be bought in its window`() {
        assertEquals(
            PurchaseCheck.OUT_OF_SEASON,
            Shop.check(pumpkin, PlantStage.SEED, 100, emptyList(), LocalDate(2026, 12, 1)),
        )
        assertEquals(
            PurchaseCheck.OK,
            Shop.check(pumpkin, PlantStage.SEED, 100, emptyList(), LocalDate(2026, 10, 15)),
        )
    }
}
