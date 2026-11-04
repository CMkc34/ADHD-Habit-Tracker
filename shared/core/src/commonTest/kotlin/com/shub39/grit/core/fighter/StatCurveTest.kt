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
 */ package com.shub39.grit.core.fighter

import kotlin.test.Test
import kotlin.test.assertEquals

class StatCurveTest {

    @Test
    fun `first levels`() {
        assertEquals(listOf(0, 0, 5, 15, 30, 50), (0..5).map(StatCurve::xpForLevel))
        assertEquals(1, StatCurve.levelFor(0))
        assertEquals(1, StatCurve.levelFor(4))
        assertEquals(2, StatCurve.levelFor(5))
        assertEquals(4, StatCurve.levelFor(49))
        assertEquals(5, StatCurve.levelFor(50))
        assertEquals(1, StatCurve.levelFor(-3))
    }

    @Test
    fun `levels round-trip`() {
        for (level in 2..50) {
            assertEquals(level, StatCurve.levelFor(StatCurve.xpForLevel(level)))
            assertEquals(level - 1, StatCurve.levelFor(StatCurve.xpForLevel(level) - 1))
        }
    }

    @Test
    fun `progress towards the next level`() {
        assertEquals(0f, StatCurve.progress(0))
        assertEquals(0.4f, StatCurve.progress(2))
        assertEquals(0f, StatCurve.progress(5))
        assertEquals(0.5f, StatCurve.progress(10))
        assertEquals(0f, StatCurve.progress(-1))
    }
}
