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
package com.shub39.grit.shared.ui.habit.fighter

import com.shub39.grit.core.avatar.ShopCatalog
import com.shub39.grit.core.avatar.ShopItemKind
import com.shub39.grit.core.fighter.FighterItems
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PropPixelsTest {

    @Test
    fun `every prop of the shop has an icon`() {
        val props = ShopCatalog.items.filter { it.kind == ShopItemKind.PROP }.map { it.id }
        assertEquals(FighterItems.props.toSet(), props.toSet())
        for (id in props) {
            val image = assertNotNull(PropPixels.render(id), id)
            assertEquals(PropPixels.SIZE, image.width)
            assertFalse(image.isBlank, id)
        }
    }

    @Test
    fun `the icons differ from each other`() {
        val icons = FighterItems.props.map { PropPixels.render(it)!!.argb.toList() }
        assertEquals(icons.size, icons.toSet().size)
    }

    @Test
    fun `items that are no props have no icon`() {
        assertNull(PropPixels.render(FighterItems.GI_BLUE))
        assertNull(PropPixels.render("deco_pond"))
    }
}
