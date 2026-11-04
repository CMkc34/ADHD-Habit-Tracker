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

import com.shub39.grit.core.habits.style.RoutineColor
import com.shub39.grit.core.habits.style.RoutineIcon
import com.shub39.grit.core.habits.style.RoutineStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.serialization.json.Json

class RoutineStyleSchemaTest {

    // same settings as RestoreImpl
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `styles survive export and import`() {
        val styles =
            mapOf(
                2L to RoutineStyle(RoutineColor.CORAL, RoutineIcon.RUN),
                1L to RoutineStyle(RoutineColor.INDIGO, RoutineIcon.BOOK),
            )
        val schema =
            ExportSchema(
                habits = emptyList(),
                habitStatus = emptyList(),
                routineStyles = styles.toRoutineStyleSchemas(),
            )

        val imported = json.decodeFromString<ExportSchema>(Json.encodeToString(schema))

        assertEquals(
            styles,
            imported.routineStyles!!.associate { it.habitId to it.toRoutineStyle() },
        )
    }

    @Test
    fun `backup without styles can still be read`() {
        val old = """{"habits":[],"habitStatus":[]}"""

        assertNull(json.decodeFromString<ExportSchema>(old).routineStyles)
    }

    @Test
    fun `unknown keys from a newer app fall back to the default`() {
        val backup =
            """{"habits":[],"habitStatus":[],"routineStyles":[{"habitId":5,"color":"neon","icon":"rocket"}]}"""

        val style = json.decodeFromString<ExportSchema>(backup).routineStyles!!.single()

        assertEquals(RoutineStyle.DEFAULT, style.toRoutineStyle())
    }
}
