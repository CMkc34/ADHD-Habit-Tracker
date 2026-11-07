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
package com.shub39.grit.shared.ui.habit.arena

import com.shub39.grit.core.arena.Opponent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OpponentAttackTest {

    @Test
    fun `every opponent but the swarm throws something of its own`() {
        val looks =
            Opponent.entries
                .filter { OpponentAttack.path(it) != AttackPath.RUSH }
                .map { assertNotNull(OpponentAttack.render(it), "$it").argb.toList() }
        assertEquals(looks.size, looks.toSet().size)
        assertEquals(AttackPath.RUSH, OpponentAttack.path(Opponent.DISTRACTION_SWARM))
        assertNull(OpponentAttack.render(Opponent.DISTRACTION_SWARM))
    }

    @Test
    fun `attacks flicker and are not blank`() {
        for (o in Opponent.entries.filter { OpponentAttack.path(it) != AttackPath.RUSH }) {
            val frames = (0 until OpponentAttack.FRAMES).map { OpponentAttack.render(o, it)!! }
            frames.forEach { assertFalse(it.isBlank, "$o") }
            assertTrue(frames.map { it.argb.toList() }.toSet().size > 1, "$o does not flicker")
        }
    }

    @Test
    fun `the special is the same attack twice as big`() {
        for (o in Opponent.entries.filter { OpponentAttack.path(it) != AttackPath.RUSH }) {
            val normal = OpponentAttack.render(o)!!
            val special = OpponentAttack.render(o, special = true)!!
            assertEquals(normal.width * 2, special.width, "$o")
            assertEquals(normal.height * 2, special.height, "$o")
            assertEquals(normal[0, 0], special[1, 1], "$o")
        }
    }
}
