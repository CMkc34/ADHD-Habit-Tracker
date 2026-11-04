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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.serialization.json.Json

class LegacyTasksBackupTest {

    // same settings as RestoreImpl
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `old backup with tasks is still read and keeps its habits`() {
        val old =
            """
            {"tasksSchemaVersion":6,"habitsSchemaVersion":6,
            "habits":[{"id":1,"title":"Read","description":"","index":0,"time":0,"days":"","reminder":false}],
            "habitStatus":[{"habitId":1,"date":0}],
            "tasks":[{"id":3,"categoryId":2,"title":"Buy milk","status":false,"index":0,"reminder":1700000000000}],
            "categories":[{"id":2,"name":"Home","index":0,"color":"#FF0000"}],
            "subTasks":[{"id":4,"taskId":3,"title":"Oat milk","index":0}]}
            """

        val imported = json.decodeFromString<ExportSchema>(old)

        assertEquals(listOf("Read"), imported.habits.map { it.title })
        assertEquals(1, imported.habitStatus.size)
    }

    @Test
    fun `new backup leaves out task data`() {
        val exported =
            Json.encodeToString(ExportSchema(habits = emptyList(), habitStatus = emptyList()))

        assertFalse("\"tasks\"" in exported)
        assertFalse("\"categories\"" in exported)
        assertFalse("\"subTasks\"" in exported)
    }

    @Test
    fun `backup without any task fields can be read`() {
        val imported = json.decodeFromString<ExportSchema>("""{"habits":[],"habitStatus":[]}""")

        assertEquals(emptyList(), imported.tasks)
        assertEquals(LEGACY_TASKS_SCHEMA_VERSION, imported.tasksSchemaVersion)
    }
}
