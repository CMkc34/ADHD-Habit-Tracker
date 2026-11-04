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
import kotlin.test.Test
import kotlin.test.assertEquals
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
    fun `catalog ids are unique and the default pot is free`() {
        assertEquals(ShopCatalog.items.size, ShopCatalog.items.map { it.id }.toSet().size)
        assertEquals(0, ShopCatalog.byId(ShopCatalog.DEFAULT_POT_COLOR)?.price)
        assertTrue(ShopCatalog.DEFAULT_POT_COLOR in Shop.owned(emptyList()))
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
        val bird = ShopCatalog.byId("acc_bird")!!
        val blue = ShopCatalog.byId("pot_blue")!!
        val bought = listOf(Purchase("pot_blue", day(0), 10))

        assertEquals(
            PurchaseCheck.ALREADY_OWNED,
            Shop.check(blue, PlantStage.TREE, energy = 100, purchases = bought, today = day(0)),
        )
        assertEquals(
            PurchaseCheck.STAGE_TOO_LOW,
            Shop.check(bird, PlantStage.SAPLING, energy = 100, purchases = bought, today = day(0)),
        )
        assertEquals(
            PurchaseCheck.NOT_ENOUGH_ENERGY,
            Shop.check(bird, PlantStage.YOUNG_TREE, energy = 39, purchases = bought, today = day(0)),
        )
        assertEquals(
            PurchaseCheck.OK,
            Shop.check(bird, PlantStage.YOUNG_TREE, energy = 40, purchases = bought, today = day(0)),
        )
    }

    private fun buy(price: Int, onDay: Int) = Purchase("pot_blue", day(onDay), price)
}
