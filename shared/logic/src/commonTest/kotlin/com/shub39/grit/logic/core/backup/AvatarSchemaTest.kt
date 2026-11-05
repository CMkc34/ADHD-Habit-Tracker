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
import com.shub39.grit.core.avatar.Purchase
import com.shub39.grit.core.avatar.Shop
import com.shub39.grit.core.fighter.FighterCharacter
import com.shub39.grit.core.fighter.FighterColors
import com.shub39.grit.core.fighter.FighterLook
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
                        Purchase("gi_blue", LocalDate(2026, 10, 20), 10),
                        Purchase("gear_sweatband", LocalDate(2026, 10, 22), 10),
                        Purchase("prop_gong", LocalDate(2026, 10, 29), 60),
                    ),
                outfit = "gi_blue",
                accessories = setOf("gear_sweatband"),
                seenStage = 5,
                decorations = setOf("prop_gong"),
                userName = "Lena",
                look = FighterLook.MIRA,
                fighterCharacter = FighterCharacter.SHADOW_RUNNER,
                fighterColors = FighterColors(skin = 2, hair = 4, main = 1, accent = 3),
            )

        val exported = Json.encodeToString(schema(data.toAvatarSchema()))
        val imported = json.decodeFromString<ExportSchema>(exported)

        assertEquals(data, imported.avatar?.toAvatarData())
    }

    @Test
    fun `no outfit is left out of the backup and comes back as none`() {
        val exported = Json.encodeToString(schema(AvatarData(name = "Fern").toAvatarSchema()))

        assertFalse("potColor" in exported)
        assertNull(json.decodeFromString<ExportSchema>(exported).avatar?.toAvatarData()?.outfit)
    }

    @Test
    fun `a plant pot colour in an old backup is no outfit`() {
        val old =
            """{"habits":[],"habitStatus":[],"avatar":{"name":"Fern","potColor":"pot_blue"}}"""

        assertNull(json.decodeFromString<ExportSchema>(old).avatar?.toAvatarData()?.outfit)
    }

    @Test
    fun `backup from before decorations can still be read`() {
        val old =
            """{"habits":[],"habitStatus":[],"avatar":{"name":"Fern","accessories":["acc_bird"]}}"""

        val avatar = json.decodeFromString<ExportSchema>(old).avatar?.toAvatarData()

        assertEquals(setOf("acc_bird"), avatar?.accessories)
        assertEquals(emptySet(), avatar?.decorations)
        assertEquals(FighterLook.KAI, avatar?.look)
    }

    @Test
    fun `plant backup imports with its look and keeps the plant purchases`() {
        val old =
            """{"habits":[],"habitStatus":[],"avatar":{"name":"","species":"cherry","seenStage":4,""" +
                """"purchases":[{"itemId":"plant_watering_can","date":"2026-10-20","price":25}],""" +
                """"accessories":["plant_watering_can"]}}"""

        val avatar = json.decodeFromString<ExportSchema>(old).avatar!!.toAvatarData()

        assertEquals(FighterLook.MIRA, avatar.look)
        assertEquals(4, avatar.seenStage)
        // kept for older versions, but not owned while the item is not in the catalog
        assertEquals(listOf("plant_watering_can"), avatar.purchases.map { it.itemId })
        assertFalse("plant_watering_can" in Shop.owned(avatar.purchases))
        // exported again under the old field name and id
        assertEquals("cherry", avatar.toAvatarSchema().species)
    }

    @Test
    fun `backup without avatar can still be read`() {
        val old = """{"habits":[],"habitStatus":[],"tasks":[],"categories":[],"subTasks":[]}"""

        assertNull(json.decodeFromString<ExportSchema>(old).avatar)
    }

    @Test
    fun `the fighter choice is stored with its colours`() {
        val data =
            AvatarData(
                fighterCharacter = FighterCharacter.KARATEKA,
                fighterColors = FighterColors(skin = 0, hair = 1, main = 2, accent = 3),
            )

        val schema = data.toAvatarSchema()

        assertEquals("karateka", schema.fighterCharacter)
        assertEquals("0|1|2|3", schema.fighterColors)
    }

    @Test
    fun `a backup from before the fighter choice keeps the old look`() {
        val old = """{"habits":[],"habitStatus":[],"avatar":{"name":"","species":"cherry"}}"""

        val avatar = json.decodeFromString<ExportSchema>(old).avatar!!.toAvatarData()

        assertNull(avatar.fighterCharacter)
        assertNull(avatar.fighterColors)
        assertEquals(FighterCharacter.SPARK_PRODIGY, avatar.character)
        assertFalse("fighterCharacter" in Json.encodeToString(schema(avatar.toAvatarSchema())))
    }

    @Test
    fun `broken fighter colours in a backup fall back to the defaults`() {
        val old =
            """{"habits":[],"habitStatus":[],"avatar":{"fighterCharacter":"street_brawler",""" +
                """"fighterColors":"9|x"}}"""

        val avatar = json.decodeFromString<ExportSchema>(old).avatar!!.toAvatarData()

        assertEquals(FighterCharacter.STREET_BRAWLER, avatar.character)
        assertEquals(FighterCharacter.STREET_BRAWLER.defaultColors, avatar.colors)
    }
}
