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
import kotlinx.datetime.number

/**
 * The days of every year on which a seasonal shop item is for sale, from [fromMonth]/[fromDay] to
 * [toMonth]/[toDay], both included. A window may run over the new year (for example 20 December to
 * 6 January).
 */
data class SaleWindow(val fromMonth: Int, val fromDay: Int, val toMonth: Int, val toDay: Int) {
    init {
        require(fromMonth in 1..12 && toMonth in 1..12) { "month must be 1..12" }
        require(fromDay in 1..31 && toDay in 1..31) { "day must be 1..31" }
    }

    operator fun contains(date: LocalDate): Boolean {
        val day = date.month.number * 100 + date.day
        val from = fromMonth * 100 + fromDay
        val to = toMonth * 100 + toDay
        return if (from <= to) day in from..to else day >= from || day <= to
    }
}

/** A seasonal item outside its window cannot be bought; other items are always for sale. */
fun ShopItem.isForSale(today: LocalDate): Boolean = season?.contains(today) ?: true
