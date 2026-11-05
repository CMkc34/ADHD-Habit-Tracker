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
package com.shub39.grit.logic.core.backup

import com.shub39.grit.core.arena.FightLog
import com.shub39.grit.core.arena.FightRecord
import com.shub39.grit.core.arena.Opponent
import com.shub39.grit.core.arena.Outcome
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json

class ArenaSchemaTest {

    // same settings as RestoreImpl
    private val json = Json { ignoreUnknownKeys = true }

    private val day = LocalDate(2026, 11, 5)

    private val fights =
        listOf(
            FightRecord(day, Opponent.SNOOZE_BUTTON.id, Outcome.WIN, startedAt = 1),
            FightRecord(day, Opponent.COUCH_GREMLIN.id, Outcome.DRAW, startedAt = 2),
            FightRecord(day, Opponent.COUCH_GREMLIN.id, Outcome.WIN, startedAt = 3),
            FightRecord(day, Opponent.DOOMSCROLL_IMP.id, Outcome.LOSS, startedAt = 4),
        )

    private fun roundTrip(schema: ExportSchema): ExportSchema =
        json.decodeFromString(Json.encodeToString(schema))

    @Test
    fun `backup round-trip keeps the ladder progress`() {
        val schema =
            ExportSchema(
                habits = emptyList(),
                habitStatus = emptyList(),
                arena = fights.toArenaSchema(),
            )

        val imported = roundTrip(schema).arena!!.toFightRecords()

        assertEquals(fights, imported)
        assertEquals(
            setOf(Opponent.SNOOZE_BUTTON, Opponent.COUCH_GREMLIN),
            FightLog.beaten(imported),
        )
    }

    @Test
    fun `backup without arena can still be read`() {
        val old = """{"habits":[],"habitStatus":[],"routineStats":[]}"""

        assertNull(json.decodeFromString<ExportSchema>(old).arena)
    }

    @Test
    fun `importing merges with the fights on the device`() {
        val device =
            listOf(FightRecord(day, Opponent.SNOOZE_BUTTON.id, Outcome.LOSS, startedAt = 9))
        val backup =
            roundTrip(
                ExportSchema(
                    habits = emptyList(),
                    habitStatus = emptyList(),
                    arena = fights.toArenaSchema(),
                )
            )

        val merged = FightLog.merge(device, backup.arena!!.toFightRecords())

        assertEquals(fights + device, merged)
    }

    @Test
    fun `broken dates are dropped and unknown outcomes count as losses`() {
        val backup =
            """{"habits":[],"habitStatus":[],"arena":{"fights":[
                {"date":"someday","opponentId":"snooze_button","outcome":"WIN","startedAt":1},
                {"date":"2026-11-05","opponentId":"snooze_button","outcome":"FLEE","startedAt":2}
            ]}}"""

        val records = json.decodeFromString<ExportSchema>(backup).arena!!.toFightRecords()

        assertEquals(
            listOf(FightRecord(day, Opponent.SNOOZE_BUTTON.id, Outcome.LOSS, startedAt = 2)),
            records,
        )
    }
}
