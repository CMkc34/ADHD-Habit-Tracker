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
package com.shub39.grit.logic.avatar

import com.shub39.grit.core.avatar.AvatarData
import com.shub39.grit.core.fighter.FighterCharacter
import com.shub39.grit.core.fighter.FighterColors
import com.shub39.grit.core.fighter.FighterLook
import com.shub39.grit.logic.core.datastore.createDataStore
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class AvatarStoreImplTest {

    private val path = createTempDirectory("avatar").resolve("test.preferences_pb").toString()

    private fun store() = AvatarStoreImpl(createDataStore { path })

    private val colors = FighterColors(skin = 1, hair = 2, main = 3, accent = 0)

    @Test
    fun `the picked fighter and colours are stored`() = runTest {
        val store = store()

        store.setFighter(FighterCharacter.SHADOW_RUNNER, colors)

        val data = store.getAvatar().first()
        assertEquals(FighterCharacter.SHADOW_RUNNER, data.fighterCharacter)
        assertEquals(colors, data.fighterColors)
    }

    @Test
    fun `an install from before the choice keeps its look in default colours`() = runTest {
        val store = store()

        store.replaceAll(AvatarData(look = FighterLook.MIRA))

        val data = store.getAvatar().first()
        assertNull(data.fighterCharacter)
        assertEquals(FighterCharacter.SPARK_PRODIGY, data.character)
        assertEquals(FighterCharacter.SPARK_PRODIGY.defaultColors, data.colors)
    }

    @Test
    fun `restoring a backup replaces or clears the choice`() = runTest {
        val store = store()
        store.setFighter(FighterCharacter.KARATEKA, colors)

        store.replaceAll(AvatarData(fighterCharacter = FighterCharacter.STREET_BRAWLER))
        assertEquals(FighterCharacter.STREET_BRAWLER, store.getAvatar().first().fighterCharacter)
        assertEquals(
            FighterCharacter.STREET_BRAWLER.defaultColors,
            store.getAvatar().first().fighterColors,
        )

        store.replaceAll(AvatarData())
        assertNull(store.getAvatar().first().fighterCharacter)
        assertNull(store.getAvatar().first().fighterColors)
    }
}
