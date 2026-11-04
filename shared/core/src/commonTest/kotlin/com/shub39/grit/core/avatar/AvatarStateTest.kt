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

class AvatarStateTest {

    private val bird = ShopCatalog.byId("acc_bird")!!
    private val blue = ShopCatalog.byId("pot_blue")!!

    private fun state(stage: PlantStage, energy: Int, data: AvatarData = AvatarData()) =
        AvatarState(PlantState(stage.minActiveDays, stage, PlantMood.HAPPY), energy, data)

    @Test
    fun `check uses stage, energy and purchases`() {
        assertEquals(
            PurchaseCheck.STAGE_TOO_LOW,
            state(PlantStage.YOUNG_TREE.prev(), 100).check(bird),
        )
        assertEquals(PurchaseCheck.NOT_ENOUGH_ENERGY, state(PlantStage.YOUNG_TREE, 39).check(bird))
        assertEquals(PurchaseCheck.OK, state(PlantStage.YOUNG_TREE, 40).check(bird))

        val owned = AvatarData(purchases = listOf(Purchase(bird.id, LocalDate(2026, 1, 1), 40)))
        assertEquals(PurchaseCheck.ALREADY_OWNED, state(PlantStage.TREE, 100, owned).check(bird))
    }

    @Test
    fun `worn items and the preview`() {
        val plain = state(PlantStage.TREE, 0)
        assertFalse(plain.isWorn(blue))
        assertTrue(plain.isWorn(ShopCatalog.byId(ShopCatalog.DEFAULT_POT_COLOR)!!))

        assertEquals("pot_blue", plain.tryOn(blue).potColor)
        assertEquals(setOf("acc_bird"), plain.tryOn(bird).accessories)
        assertTrue(state(PlantStage.TREE, 0, plain.tryOn(bird)).isWorn(bird))
    }

    @Test
    fun `decorations are shown on the meadow, not worn by the plant`() {
        val pond = ShopCatalog.byId("deco_pond")!!
        val plain = state(PlantStage.TREE, 0)

        val preview = plain.tryOn(pond)
        assertEquals(setOf("deco_pond"), preview.decorations)
        assertEquals(emptySet(), preview.accessories)
        assertFalse(plain.isWorn(pond))
        assertTrue(state(PlantStage.TREE, 0, preview).isWorn(pond))
    }

    private fun PlantStage.prev(): PlantStage = PlantStage.entries[ordinal - 1]
}
