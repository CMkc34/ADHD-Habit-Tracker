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
package com.shub39.grit.core.fighter

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RoutineStatTest {

    @Test
    fun `every stat survives encode and decode`() {
        FighterStat.entries.forEach { stat ->
            assertEquals(42L to stat, RoutineStatCodec.decode(RoutineStatCodec.encode(42L, stat)))
        }
    }

    @Test
    fun `entry has the documented form`() {
        assertEquals("7|focus", RoutineStatCodec.encode(7L, FighterStat.FOCUS))
    }

    @Test
    fun `broken entries decode to null`() {
        listOf("", "7", "x|focus", "7|focus|extra", "|focus").forEach {
            assertNull(RoutineStatCodec.decode(it), "entry '$it'")
        }
    }

    @Test
    fun `unknown stat key falls back to stamina`() {
        assertEquals(3L to FighterStat.STAMINA, RoutineStatCodec.decode("3|luck"))
    }

    @Test
    fun `belongsTo matches the habit id only`() {
        assertTrue(RoutineStatCodec.belongsTo("12|speed", 12L))
        assertTrue(RoutineStatCodec.belongsTo("12|broken|entry", 12L))
        assertFalse(RoutineStatCodec.belongsTo("12|speed", 1L))
        assertFalse(RoutineStatCodec.belongsTo("120|speed", 12L))
    }

    @Test
    fun `routines without an entry train stamina`() {
        val stats = mapOf(1L to FighterStat.STRENGTH)

        assertEquals(FighterStat.STRENGTH, stats.statOf(1L))
        assertEquals(FighterStat.STAMINA, stats.statOf(2L))
    }

    @Test
    fun `stat keys never change`() {
        assertEquals(
            listOf("strength", "speed", "stamina", "focus"),
            FighterStat.entries.map { it.key },
        )
    }
}
