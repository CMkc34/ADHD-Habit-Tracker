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
package com.shub39.grit.core.habits.style

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RoutineStyleTest {

    @Test
    fun `there are 24 colours and 36 icons with unique keys`() {
        assertEquals(24, RoutineColor.entries.size)
        assertEquals(36, RoutineIcon.entries.size)
        assertEquals(24, RoutineColor.entries.map { it.key }.toSet().size)
        assertEquals(36, RoutineIcon.entries.map { it.key }.toSet().size)
        assertEquals(24, RoutineColor.entries.map { it.argb }.toSet().size)
    }

    @Test
    fun `keys contain no separator and stay stable`() {
        (RoutineColor.entries.map { it.key } + RoutineIcon.entries.map { it.key }).forEach {
            assertFalse("|" in it, it)
        }
        // stored in preferences and backups, so these must never change
        assertEquals("green", RoutineColor.GREEN.key)
        assertEquals("anthracite", RoutineColor.ANTHRACITE.key)
        assertEquals("bolt", RoutineIcon.BOLT.key)
        assertEquals("check", RoutineIcon.CHECK.key)
    }

    @Test
    fun `default is green with a bolt`() {
        assertEquals(RoutineStyle(RoutineColor.GREEN, RoutineIcon.BOLT), RoutineStyle.DEFAULT)
        assertEquals(0xFF43A047, RoutineColor.GREEN.argb)
        assertEquals(RoutineStyle.DEFAULT, emptyMap<Long, RoutineStyle>().styleOf(3))
    }

    @Test
    fun `encode and decode round trip`() {
        val style = RoutineStyle(RoutineColor.SKY_BLUE, RoutineIcon.PHONE_OFF)
        val encoded = RoutineStyleCodec.encode(42, style)

        assertEquals("42|sky_blue|phone_off", encoded)
        assertEquals(42L to style, RoutineStyleCodec.decode(encoded))
    }

    @Test
    fun `unknown keys fall back to the default part`() {
        assertEquals(
            7L to RoutineStyle(RoutineColor.GREEN, RoutineIcon.BOOK),
            RoutineStyleCodec.decode("7|neon|book"),
        )
        assertEquals(
            7L to RoutineStyle(RoutineColor.RED, RoutineIcon.BOLT),
            RoutineStyleCodec.decode("7|red|rocket"),
        )
    }

    @Test
    fun `broken entries are skipped`() {
        assertNull(RoutineStyleCodec.decode(""))
        assertNull(RoutineStyleCodec.decode("7|red"))
        assertNull(RoutineStyleCodec.decode("x|red|bolt"))
        assertNull(RoutineStyleCodec.decode("7|red|bolt|extra"))
    }

    @Test
    fun `entries are matched to their habit by id`() {
        assertTrue(RoutineStyleCodec.belongsTo("7|red|bolt", 7))
        assertTrue(RoutineStyleCodec.belongsTo("7|broken", 7))
        assertFalse(RoutineStyleCodec.belongsTo("17|red|bolt", 7))
        assertFalse(RoutineStyleCodec.belongsTo("71|red|bolt", 7))
    }

    @Test
    fun `content is white on dark and black on light colours`() {
        assertEquals(0xFFFFFFFF, RoutineColor.GREEN.contentArgb)
        assertEquals(0xFFFFFFFF, RoutineColor.BLUE.contentArgb)
        assertEquals(0xFFFFFFFF, RoutineColor.ANTHRACITE.contentArgb)
        assertEquals(0xFF000000, RoutineColor.YELLOW.contentArgb)
        assertEquals(0xFF000000, RoutineColor.LIME.contentArgb)
        assertEquals(0xFF000000, RoutineColor.SAND.contentArgb)
    }
}
