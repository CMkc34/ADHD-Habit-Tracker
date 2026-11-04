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
package com.shub39.grit.shared.ui.habit.motivation

import com.shub39.grit.core.motivation.QuoteCatalog
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.allStringResources
import kotlin.test.Test
import kotlin.test.assertEquals

class QuoteTextTest {

    @Test
    fun `every catalog quote resolves to its own text`() {
        (QuoteCatalog.all + QuoteCatalog.fallback).forEach { quote ->
            assertEquals(quote.id, quote.textResource().key)
        }
    }

    @Test
    fun `every quote text belongs to a catalog quote`() {
        val catalogIds = (QuoteCatalog.all + QuoteCatalog.fallback).map { it.id }.toSet()
        val quoteTextIds = Res.allStringResources.keys.filter { it.startsWith("quote_") }.toSet()

        assertEquals(catalogIds, quoteTextIds)
    }
}
