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
package com.shub39.grit.core.motivation

import kotlin.random.Random

/** Picks a quote that fits the user's current streak. Stateless; knows nothing about storage. */
object GritQuoteEngine {

    /**
     * Returns a random quote for the tier of [activeDays]. The quote with id [previous] is skipped
     * as long as the tier offers another one. Without any fitting quote, [QuoteCatalog.fallback] is
     * returned.
     */
    fun getQuote(
        activeDays: Int,
        random: Random = Random.Default,
        previous: String? = null,
        quotes: List<MotivationQuote> = QuoteCatalog.all,
    ): MotivationQuote {
        val tier = StreakTier.forStreak(activeDays)
        val candidates = quotes.filter { it.tier == tier }
        if (candidates.isEmpty()) return QuoteCatalog.fallback
        val fresh = candidates.filter { it.id != previous }
        return fresh.ifEmpty { candidates }.random(random)
    }
}
