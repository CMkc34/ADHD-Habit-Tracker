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
package com.shub39.grit.core.arena

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate

class FightRecordCodecTest {

    private val record =
        FightRecord(LocalDate(2026, 11, 5), "snooze_button", Outcome.WIN, startedAt = 1234L)

    @Test
    fun `entry round-trips`() {
        val encoded = FightRecordCodec.encode(record)

        assertEquals("2026-11-05|snooze_button|WIN|1234", encoded)
        assertEquals(record, FightRecordCodec.decode(encoded))
    }

    @Test
    fun `entry without start time reads it as zero`() {
        assertEquals(
            record.copy(startedAt = 0L),
            FightRecordCodec.decode("2026-11-05|snooze_button|WIN"),
        )
    }

    @Test
    fun `unknown outcome counts as a loss`() {
        assertEquals(
            Outcome.LOSS,
            FightRecordCodec.decode("2026-11-05|snooze_button|FLEE|1")?.outcome,
        )
    }

    @Test
    fun `unknown opponent is kept but has no opponent`() {
        val decoded = FightRecordCodec.decode("2026-11-05|future_boss|WIN|1")

        assertEquals("future_boss", decoded?.opponentId)
        assertNull(decoded?.opponent)
    }

    @Test
    fun `broken entries are dropped`() {
        listOf(
                "",
                "garbage",
                "2026-13-40|snooze_button|WIN|1",
                "2026-11-05||WIN|1",
                "2026-11-05|snooze_button|WIN|x",
                "a|b|c|d|e",
            )
            .forEach { assertNull(FightRecordCodec.decode(it), it) }
    }
}
