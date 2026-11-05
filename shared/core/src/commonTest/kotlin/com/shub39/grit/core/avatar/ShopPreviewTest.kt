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

import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.core.fighter.FighterState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ShopPreviewTest {

    private val cape = ShopCatalog.byId("gear_cape")!!
    private val blue = ShopCatalog.byId("gi_blue")!!

    private fun state(rank: FighterRank, energy: Int, data: AvatarData = AvatarData()) =
        AvatarState(FighterState(rank.minActiveDays, rank, FighterMood.READY), energy, data)

    @Test
    fun `a buyable item is shown on the current fighter`() {
        val preview = state(FighterRank.CHAMPION, 50).preview(cape)

        assertTrue(preview.canBuy)
        assertFalse(preview.isFutureRank)
        assertEquals(FighterRank.CHAMPION, preview.rank)
        assertEquals(emptySet(), preview.without.accessories)
        assertEquals(setOf("gear_cape"), preview.with.accessories)
        assertEquals(0, preview.missingEnergy)
    }

    @Test
    fun `missing water is counted and buying is not possible`() {
        val preview = state(FighterRank.ROOKIE, 4).preview(blue)

        assertEquals(PurchaseCheck.NOT_ENOUGH_ENERGY, preview.check)
        assertFalse(preview.canBuy)
        assertEquals(6, preview.missingEnergy)
        assertNull(preview.without.outfit)
        assertEquals("gi_blue", preview.with.outfit)
    }

    @Test
    fun `a locked item is shown at the rank it needs`() {
        val preview = state(FighterRank.SPARRING_PARTNER, 100).preview(cape)

        assertEquals(PurchaseCheck.RANK_TOO_LOW, preview.check)
        assertTrue(preview.isFutureRank)
        assertFalse(preview.canBuy)
        assertEquals(FighterRank.FIGHTER, preview.rank)
    }
}
