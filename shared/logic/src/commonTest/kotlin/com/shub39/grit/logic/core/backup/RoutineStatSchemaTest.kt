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

import com.shub39.grit.core.fighter.FighterStat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.serialization.json.Json

class RoutineStatSchemaTest {

    // same settings as RestoreImpl
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `stats survive export and import`() {
        val stats =
            mapOf(2L to FighterStat.STRENGTH, 1L to FighterStat.FOCUS, 7L to FighterStat.SPEED)
        val schema =
            ExportSchema(
                habits = emptyList(),
                habitStatus = emptyList(),
                routineStats = stats.toRoutineStatSchemas(),
            )

        val imported = json.decodeFromString<ExportSchema>(Json.encodeToString(schema))

        assertEquals(stats, imported.routineStats!!.associate { it.habitId to it.toFighterStat() })
    }

    @Test
    fun `backup without stats can still be read`() {
        val old = """{"habits":[],"habitStatus":[],"routineStyles":[]}"""

        assertNull(json.decodeFromString<ExportSchema>(old).routineStats)
    }

    @Test
    fun `unknown stat key from a newer app falls back to stamina`() {
        val backup =
            """{"habits":[],"habitStatus":[],"routineStats":[{"habitId":5,"stat":"luck"}]}"""

        val stat = json.decodeFromString<ExportSchema>(backup).routineStats!!.single()

        assertEquals(FighterStat.STAMINA, stat.toFighterStat())
    }
}
