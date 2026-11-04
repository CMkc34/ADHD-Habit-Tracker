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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class GritQuoteEngineTest {

    private val starter = MotivationQuote("starter", QuoteTone.MOTIVATING, StreakTier.STARTER)
    private val building = MotivationQuote("building", QuoteTone.SARCASTIC, StreakTier.BUILDING)
    private val legend = MotivationQuote("legend", QuoteTone.MOTIVATING, StreakTier.LEGEND)
    private val oneQuotePerTier = listOf(starter, building, legend)

    @Test
    fun `returns the quote of the tier that matches the streak`() {
        assertEquals(starter, GritQuoteEngine.getQuote(1, quotes = oneQuotePerTier))
        assertEquals(building, GritQuoteEngine.getQuote(3, quotes = oneQuotePerTier))
        assertEquals(legend, GritQuoteEngine.getQuote(40, quotes = oneQuotePerTier))
    }

    private val buildingQuotes =
        listOf(
            MotivationQuote("b_motivating_1", QuoteTone.MOTIVATING, StreakTier.BUILDING),
            MotivationQuote("b_motivating_2", QuoteTone.MOTIVATING, StreakTier.BUILDING),
            MotivationQuote("b_sarcastic_1", QuoteTone.SARCASTIC, StreakTier.BUILDING),
            MotivationQuote("b_sarcastic_2", QuoteTone.SARCASTIC, StreakTier.BUILDING),
        )

    @Test
    fun `same seed picks the same quotes`() {
        val first = List(20) { GritQuoteEngine.getQuote(3, Random(it), quotes = buildingQuotes) }
        val second = List(20) { GritQuoteEngine.getQuote(3, Random(it), quotes = buildingQuotes) }

        assertEquals(first, second)
    }

    @Test
    fun `both tones show up over many check-ins`() {
        val random = Random(42)
        val tones =
            List(100) { GritQuoteEngine.getQuote(3, random, quotes = buildingQuotes).tone }.toSet()

        assertEquals(setOf(QuoteTone.MOTIVATING, QuoteTone.SARCASTIC), tones)
    }

    @Test
    fun `never repeats the previous quote when alternatives exist`() {
        repeat(50) { seed ->
            val quote =
                GritQuoteEngine.getQuote(
                    3,
                    Random(seed),
                    previous = "b_sarcastic_1",
                    quotes = buildingQuotes,
                )
            assertNotEquals("b_sarcastic_1", quote.id, "seed $seed")
        }
    }

    @Test
    fun `repeats the previous quote when it is the only one in the tier`() {
        val quote = GritQuoteEngine.getQuote(1, previous = "starter", quotes = oneQuotePerTier)

        assertEquals(starter, quote)
    }

    @Test
    fun `falls back to the default quote when no quote fits`() {
        assertEquals(QuoteCatalog.fallback, GritQuoteEngine.getQuote(3, quotes = emptyList()))
        assertEquals(QuoteCatalog.fallback, GritQuoteEngine.getQuote(7, quotes = oneQuotePerTier))
    }
}
