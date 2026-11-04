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

class ShopPreviewTest {

    private val bird = ShopCatalog.byId("acc_bird")!!
    private val blue = ShopCatalog.byId("pot_blue")!!

    private fun state(stage: PlantStage, energy: Int, data: AvatarData = AvatarData()) =
        AvatarState(PlantState(stage.minActiveDays, stage, PlantMood.HAPPY), energy, data)

    @Test
    fun `a buyable item is shown on the current plant`() {
        val preview = state(PlantStage.TREE, 50).preview(bird)

        assertTrue(preview.canBuy)
        assertFalse(preview.isFutureStage)
        assertEquals(PlantStage.TREE, preview.stage)
        assertEquals(emptySet(), preview.without.accessories)
        assertEquals(setOf("acc_bird"), preview.with.accessories)
        assertEquals(0, preview.missingEnergy)
    }

    @Test
    fun `missing water is counted and buying is not possible`() {
        val preview = state(PlantStage.SEED, 4).preview(blue)

        assertEquals(PurchaseCheck.NOT_ENOUGH_ENERGY, preview.check)
        assertFalse(preview.canBuy)
        assertEquals(6, preview.missingEnergy)
        assertEquals(ShopCatalog.DEFAULT_POT_COLOR, preview.without.potColor)
        assertEquals("pot_blue", preview.with.potColor)
    }

    @Test
    fun `a locked item is shown at the stage it needs`() {
        val preview = state(PlantStage.SEEDLING, 100).preview(bird)

        assertEquals(PurchaseCheck.STAGE_TOO_LOW, preview.check)
        assertTrue(preview.isFutureStage)
        assertFalse(preview.canBuy)
        assertEquals(PlantStage.YOUNG_TREE, preview.stage)
    }
}
