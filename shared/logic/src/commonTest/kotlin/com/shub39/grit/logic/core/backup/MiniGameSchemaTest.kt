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

import com.shub39.grit.core.minigame.MiniGame
import com.shub39.grit.core.minigame.RunLog
import com.shub39.grit.core.minigame.RunRecord
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json

class MiniGameSchemaTest {

    // same settings as RestoreImpl
    private val json = Json { ignoreUnknownKeys = true }

    private val day = LocalDate(2026, 11, 15)

    private val runs =
        listOf(
            RunRecord(day, MiniGame.KI_BARRAGE.id, 750, startedAt = 1),
            RunRecord(day, MiniGame.CLUTTER_SMASH.id, 0, startedAt = 2),
            RunRecord(day, MiniGame.BOARD_BREAK.id, 14, startedAt = 3),
        )

    private fun roundTrip(schema: ExportSchema): ExportSchema =
        json.decodeFromString(Json.encodeToString(schema))

    @Test
    fun `backup round-trip keeps the runs and high scores`() {
        val schema =
            ExportSchema(
                habits = emptyList(),
                habitStatus = emptyList(),
                minigames = runs.toMiniGameSchema(),
            )

        val imported = roundTrip(schema).minigames!!.toRunRecords()

        assertEquals(runs, imported)
        assertEquals(750, RunLog.best(imported, MiniGame.KI_BARRAGE))
    }

    @Test
    fun `older backups have no mini-games`() {
        val schema = ExportSchema(habits = emptyList(), habitStatus = emptyList())
        assertNull(roundTrip(schema).minigames)
    }

    @Test
    fun `broken runs are dropped`() {
        val schema =
            MiniGameSchema(
                runs =
                    listOf(
                        RunSchema("broken", "ki_barrage", 1, 1),
                        RunSchema("2026-11-15", "", 1, 2),
                        RunSchema("2026-11-15", "board_break", -3, 3),
                    )
            )
        assertEquals(listOf(RunRecord(day, "board_break", 0, 3)), schema.toRunRecords())
    }
}
