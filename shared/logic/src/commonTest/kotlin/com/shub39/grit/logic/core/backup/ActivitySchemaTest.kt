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

import com.shub39.grit.core.activity.ActivityData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json

class ActivitySchemaTest {

    // same settings as RestoreImpl
    private val json = Json { ignoreUnknownKeys = true }

    private fun day(n: Int) = LocalDate(2026, 10, n)

    private fun schema(activity: ActivitySchema?) =
        ExportSchema(habits = emptyList(), habitStatus = emptyList(), activity = activity)

    @Test
    fun `activity survives export and import`() {
        val data = ActivityData(day(1), setOf(day(1), day(2), day(4)), setOf(day(3)))

        val exported = Json.encodeToString(schema(data.toActivitySchema()))
        val imported = json.decodeFromString<ExportSchema>(exported)

        assertEquals(data, imported.activity?.toActivityData())
    }

    @Test
    fun `empty activity survives export and import`() {
        val exported = Json.encodeToString(schema(ActivityData().toActivitySchema()))
        val imported = json.decodeFromString<ExportSchema>(exported)

        assertEquals(ActivityData(), imported.activity?.toActivityData())
    }

    @Test
    fun `old backup without activity can still be read`() {
        val old =
            """
            {"habits":[{"id":1,"title":"Read","description":"","index":0,"time":0,"days":"","reminder":false}],
            "habitStatus":[{"habitId":1,"date":0}],"tasks":[],"categories":[],"subTasks":[]}
            """

        val imported = json.decodeFromString<ExportSchema>(old)

        assertEquals(1, imported.habits.size)
        assertNull(imported.activity)
    }
}
