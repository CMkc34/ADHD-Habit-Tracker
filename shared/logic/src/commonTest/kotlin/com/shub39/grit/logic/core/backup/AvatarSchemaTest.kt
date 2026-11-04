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

import com.shub39.grit.core.avatar.AvatarData
import com.shub39.grit.core.avatar.AvatarSpecies
import com.shub39.grit.core.avatar.Purchase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json

class AvatarSchemaTest {

    // same settings as RestoreImpl
    private val json = Json { ignoreUnknownKeys = true }

    private fun schema(avatar: AvatarSchema?) =
        ExportSchema(habits = emptyList(), habitStatus = emptyList(), avatar = avatar)

    @Test
    fun `avatar survives export and import`() {
        val data =
            AvatarData(
                name = "Fern",
                purchases =
                    listOf(
                        Purchase("pot_blue", LocalDate(2026, 10, 20), 10),
                        Purchase("acc_ladybug", LocalDate(2026, 10, 22), 15),
                        Purchase("deco_pond", LocalDate(2026, 10, 29), 50),
                    ),
                potColor = "pot_blue",
                accessories = setOf("acc_ladybug"),
                seenStage = 5,
                decorations = setOf("deco_pond"),
                userName = "Lena",
                species = AvatarSpecies.CHERRY,
            )

        val exported = Json.encodeToString(schema(data.toAvatarSchema()))
        val imported = json.decodeFromString<ExportSchema>(exported)

        assertEquals(data, imported.avatar?.toAvatarData())
    }

    @Test
    fun `backup from before decorations can still be read`() {
        val old =
            """{"habits":[],"habitStatus":[],"avatar":{"name":"Fern","accessories":["acc_bird"]}}"""

        val avatar = json.decodeFromString<ExportSchema>(old).avatar?.toAvatarData()

        assertEquals(setOf("acc_bird"), avatar?.accessories)
        assertEquals(emptySet(), avatar?.decorations)
        assertEquals(AvatarSpecies.SPROUT, avatar?.species)
    }

    @Test
    fun `backup without avatar can still be read`() {
        val old = """{"habits":[],"habitStatus":[],"tasks":[],"categories":[],"subTasks":[]}"""

        assertNull(json.decodeFromString<ExportSchema>(old).avatar)
    }
}
