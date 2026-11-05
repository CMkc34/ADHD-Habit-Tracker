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

import com.shub39.grit.core.activity.ActivityPlanner
import com.shub39.grit.core.activity.DayStatus
import com.shub39.grit.core.fighter.FighterItems
import com.shub39.grit.core.fighter.FighterRank
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate

class ShopTest {

    private val start = LocalDate(2026, 1, 1)

    private fun day(n: Int) = LocalDate.fromEpochDays(start.toEpochDays() + n)

    private fun plan(active: List<Int>, today: Int, purchases: List<Purchase> = emptyList()) =
        ActivityPlanner.plan(
            activeDays = active.map(::day).toSet(),
            startDate = start,
            today = day(today),
            purchases = purchases,
        )

    @Test
    fun `catalog ids are unique and nothing is free`() {
        assertEquals(ShopCatalog.items.size, ShopCatalog.items.map { it.id }.toSet().size)
        assertTrue(ShopCatalog.items.all { it.price > 0 })
        assertEquals(emptySet(), Shop.owned(emptyList()))
    }

    @Test
    fun `the catalog sells the fighter's items`() {
        fun ids(kind: ShopItemKind) = ShopCatalog.items.filter { it.kind == kind }.map { it.id }

        assertEquals(FighterItems.outfits, ids(ShopItemKind.OUTFIT))
        assertEquals(FighterItems.gear.toSet(), ids(ShopItemKind.GEAR).toSet())
        assertEquals(FighterItems.props, ids(ShopItemKind.PROP))
        assertTrue(ShopCatalog.isOutfit(FighterItems.GI_GOLD))
        assertFalse(ShopCatalog.isOutfit(FighterItems.CAPE))
        assertFalse(ShopCatalog.isOutfit("pot_terracotta"))
        assertFalse(ShopCatalog.isOutfit(null))
    }

    @Test
    fun `ranks and prices follow the issue table`() {
        fun item(id: String) = ShopCatalog.byId(id)!!

        assertEquals(10, item(FighterItems.GI_WHITE).price)
        assertEquals(FighterRank.SPARRING_PARTNER, item(FighterItems.GI_PURPLE).minRank)
        assertEquals(FighterRank.FIGHTER, item(FighterItems.GI_GOLD).minRank)
        assertEquals(FighterRank.GRANDMASTER, item(FighterItems.CROWN).minRank)
        assertEquals(80, item(FighterItems.CROWN).price)
        assertEquals(FighterRank.MASTER, item(FighterItems.GONG).minRank)
        assertEquals(60, item(FighterItems.GONG).price)
    }

    @Test
    fun `a purchase lowers the energy`() {
        val report = plan(active = (0..19).toList(), today = 19, purchases = listOf(buy(10, 15)))

        assertEquals(10, report.energy)
    }

    @Test
    fun `without purchases the plan is unchanged`() {
        val active = (0..9).toList() + (11..15)

        assertEquals(plan(active, today = 15), plan(active, today = 15, purchases = emptyList()))
    }

    @Test
    fun `a purchase before a gap can prevent the freeze`() {
        // 10 active days = 10 energy, buy for 5 on day 9, day 10 missed, day 11 active
        val report =
            plan(
                active = (0..9).toList() + 11,
                today = 11,
                purchases = listOf(buy(price = 5, onDay = 9)),
            )

        assertEquals(DayStatus.MISSED, report.days[day(10)])
        assertEquals(1, report.currentStreak)
        assertEquals(6, report.energy)
    }

    @Test
    fun `a purchase during an open gap does not undo its freeze`() {
        // 10 energy, yesterday missed and frozen when the app opens; buying today keeps it
        val report =
            plan(active = (0..9).toList(), today = 11, purchases = listOf(buy(3, onDay = 11)))

        assertEquals(DayStatus.FROZEN, report.days[day(10)])
        assertEquals(10, report.currentStreak)
        assertEquals(0, report.energy)
    }

    @Test
    fun `a purchase in a gap is paid after the gap is decided`() {
        // purchase on the missed day 10; the gap is frozen first, then the purchase is paid
        val report =
            plan(
                active = (0..9).toList() + 11,
                today = 11,
                purchases = listOf(buy(price = 3, onDay = 10)),
            )

        assertEquals(DayStatus.FROZEN, report.days[day(10)])
        assertEquals(10 - ActivityPlanner.FREEZE_COST - 3 + 1, report.energy)
    }

    @Test
    fun `a purchase never takes the energy below zero`() {
        // 3 energy left, a merged backup brings a 15 energy purchase from another device
        val report =
            plan(
                active = (0..9).toList(),
                today = 9,
                purchases = listOf(buy(price = 7, onDay = 9), buy(price = 15, onDay = 9)),
            )

        assertEquals(0, report.energy)
    }

    @Test
    fun `energy earned after an unaffordable purchase counts in full`() {
        val report =
            plan(
                active = (0..9).toList(),
                today = 9,
                purchases = listOf(buy(price = 20, onDay = 2)),
            )

        // 3 energy on day 2, purchase drops it to 0, days 3 to 9 earn 7
        assertEquals(7, report.energy)
    }

    @Test
    fun `check refuses owned, locked and too expensive items`() {
        val cape = ShopCatalog.byId("gear_cape")!!
        val blue = ShopCatalog.byId("gi_blue")!!
        val bought = listOf(Purchase("gi_blue", day(0), 10))

        assertEquals(
            PurchaseCheck.ALREADY_OWNED,
            Shop.check(blue, FighterRank.CHAMPION, energy = 100, purchases = bought, today = day(0)),
        )
        assertEquals(
            PurchaseCheck.RANK_TOO_LOW,
            Shop.check(
                cape,
                FighterRank.CONTENDER,
                energy = 100,
                purchases = bought,
                today = day(0),
            ),
        )
        assertEquals(
            PurchaseCheck.NOT_ENOUGH_ENERGY,
            Shop.check(cape, FighterRank.FIGHTER, energy = 39, purchases = bought, today = day(0)),
        )
        assertEquals(
            PurchaseCheck.OK,
            Shop.check(cape, FighterRank.FIGHTER, energy = 40, purchases = bought, today = day(0)),
        )
    }

    @Test
    fun `purchases of items no longer in the catalog are not owned`() {
        val bought = listOf(Purchase("gi_blue", day(0), 10), Purchase("acc_gone", day(0), 25))

        val owned = Shop.owned(bought)
        assertTrue("gi_blue" in owned)
        assertFalse("acc_gone" in owned)
        assertEquals(listOf(bought[0]), Shop.charged(bought))
    }

    @Test
    fun `water spent on items no longer in the catalog comes back`() {
        // 20 active days = 20 energy; 10 spent on a known item, 25 on an item that left the shop
        val purchases = listOf(buy(price = 10, onDay = 15), Purchase("acc_gone", day(16), 25))

        assertEquals(10, plan(active = (0..19).toList(), today = 19, purchases).energy)
        assertEquals(
            plan(active = (0..19).toList(), today = 19, listOf(buy(price = 10, onDay = 15))),
            plan(active = (0..19).toList(), today = 19, purchases),
        )
    }

    private fun buy(price: Int, onDay: Int) = Purchase("gi_blue", day(onDay), price)
}
