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
package com.shub39.grit.core.avatar

import com.shub39.grit.core.fighter.FighterLook
import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.core.fighter.FighterStat
import com.shub39.grit.core.fighter.FighterState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate

class AvatarStateTest {

    private val cape = ShopCatalog.byId("gear_cape")!!
    private val blue = ShopCatalog.byId("gi_blue")!!

    private fun state(
        rank: FighterRank,
        energy: Int,
        data: AvatarData = AvatarData(),
        xp: Map<FighterStat, Int> = emptyMap(),
    ) =
        AvatarState(
            FighterState(rank.minActiveDays, rank, FighterMood.READY, xp = xp),
            energy,
            data,
        )

    @Test
    fun `check uses rank, energy and purchases`() {
        assertEquals(PurchaseCheck.RANK_TOO_LOW, state(FighterRank.FIGHTER.prev(), 100).check(cape))
        assertEquals(PurchaseCheck.NOT_ENOUGH_ENERGY, state(FighterRank.FIGHTER, 39).check(cape))
        assertEquals(PurchaseCheck.OK, state(FighterRank.FIGHTER, 40).check(cape))

        val owned = AvatarData(purchases = listOf(Purchase(cape.id, LocalDate(2026, 1, 1), 40)))
        assertEquals(
            PurchaseCheck.ALREADY_OWNED,
            state(FighterRank.CHAMPION, 100, owned).check(cape),
        )
    }

    @Test
    fun `worn items and the preview`() {
        val plain = state(FighterRank.CHAMPION, 0)
        assertFalse(plain.isWorn(blue))
        assertNull(plain.data.outfit)
        assertFalse(plain.isWorn(ShopCatalog.byId("gi_white")!!))

        assertEquals("gi_blue", plain.tryOn(blue).outfit)
        assertEquals(setOf("gear_cape"), plain.tryOn(cape).accessories)
        assertTrue(state(FighterRank.CHAMPION, 0, plain.tryOn(cape)).isWorn(cape))
    }

    @Test
    fun `props are shown in the training ground, not worn`() {
        val gong = ShopCatalog.byId("prop_gong")!!
        val plain = state(FighterRank.CHAMPION, 0)

        val preview = plain.tryOn(gong)
        assertEquals(setOf("prop_gong"), preview.decorations)
        assertEquals(emptySet(), preview.accessories)
        assertFalse(plain.isWorn(gong))
        assertTrue(state(FighterRank.CHAMPION, 0, preview).isWorn(gong))
    }

    @Test
    fun `purchases of items that left the catalog are not owned`() {
        val data =
            AvatarData(
                purchases =
                    listOf(
                        Purchase("gi_blue", LocalDate(2026, 1, 1), 10),
                        Purchase("plant_only_item", LocalDate(2026, 1, 2), 30),
                    )
            )

        val owned = state(FighterRank.CHAMPION, 0, data).owned
        assertTrue("gi_blue" in owned)
        assertFalse("plant_only_item" in owned)
    }

    @Test
    fun `the seen plant stage is the seen rank`() {
        // a user who saw plant stage 5 (21 active days) is not told about rank 5 again
        val seen = AvatarData(seenStage = 5)

        assertNull(state(FighterRank.CONTENDER, 0, seen).newRank)
        assertEquals(FighterRank.FIGHTER, state(FighterRank.FIGHTER, 0, seen).newRank)
        assertNull(state(FighterRank.FIGHTER, 0, AvatarData(seenStage = 0)).newRank)
    }

    @Test
    fun `no level-up notices before the levels were first stored`() {
        // first run after the update: lots of XP from the plant days, nothing stored yet
        val xp = mapOf(FighterStat.STRENGTH to 50, FighterStat.STAMINA to 120)
        val firstRun = state(FighterRank.CHAMPION, 0, AvatarData(), xp)

        assertEquals(emptyMap(), firstRun.newStatLevels)
        assertEquals(5, firstRun.statLevels[FighterStat.STRENGTH])
        assertEquals(1, firstRun.statLevels[FighterStat.FOCUS])
    }

    @Test
    fun `a stat level above the seen one is new`() {
        val seen =
            AvatarData(
                seenStatLevels =
                    FighterStat.entries.associateWith { 1 } + (FighterStat.STRENGTH to 4)
            )
        val xp = mapOf(FighterStat.STRENGTH to 50, FighterStat.SPEED to 5, FighterStat.FOCUS to 4)

        assertEquals(
            mapOf(FighterStat.STRENGTH to 5, FighterStat.SPEED to 2),
            state(FighterRank.CHAMPION, 0, seen, xp).newStatLevels,
        )
        val after =
            seen.copy(seenStatLevels = mapOf(FighterStat.STRENGTH to 5, FighterStat.SPEED to 2))
        assertEquals(emptyMap(), state(FighterRank.CHAMPION, 0, after, xp).newStatLevels)
    }

    @Test
    fun `a fighter that was never renamed is called after its look`() {
        assertEquals("Kai", state(FighterRank.ROOKIE, 0).name)
        assertEquals("Mira", state(FighterRank.ROOKIE, 0, AvatarData(look = FighterLook.MIRA)).name)
        assertEquals("Fern", state(FighterRank.ROOKIE, 0, AvatarData(name = "Fern")).name)
    }

    @Test
    fun `a restored backup keeps the higher seen levels`() {
        val here = AvatarData(seenStatLevels = mapOf(FighterStat.STRENGTH to 3))
        val backup =
            AvatarData(seenStatLevels = mapOf(FighterStat.STRENGTH to 2, FighterStat.FOCUS to 4))

        assertEquals(
            mapOf(FighterStat.STRENGTH to 3, FighterStat.FOCUS to 4),
            here.mergedWith(backup).seenStatLevels,
        )
    }

    private fun FighterRank.prev(): FighterRank = FighterRank.entries[ordinal - 1]
}
