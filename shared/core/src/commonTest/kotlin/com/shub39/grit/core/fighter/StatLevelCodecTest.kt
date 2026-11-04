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
import kotlin.test.assertNull

class StatLevelCodecTest {

    @Test
    fun `entries have the documented form`() {
        assertEquals("strength|4", StatLevelCodec.encode(FighterStat.STRENGTH, 4))
    }

    @Test
    fun `levels round-trip`() {
        val levels = mapOf(FighterStat.STRENGTH to 4, FighterStat.FOCUS to 1)

        assertEquals(levels, StatLevelCodec.decodeAll(StatLevelCodec.encodeAll(levels)))
    }

    @Test
    fun `broken entries and unknown stats are dropped`() {
        assertNull(StatLevelCodec.decode("strength"))
        assertNull(StatLevelCodec.decode("strength|x"))
        assertNull(StatLevelCodec.decode("luck|3"))
        assertEquals(
            mapOf(FighterStat.SPEED to 3),
            StatLevelCodec.decodeAll(setOf("speed|2", "speed|3", "luck|9", "")),
        )
    }
}
