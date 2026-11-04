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
package com.shub39.grit.shared.ui.habit.fighter

import com.shub39.grit.core.fighter.Belt
import com.shub39.grit.core.fighter.FighterItems
import com.shub39.grit.core.fighter.FighterLook
import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.core.fighter.FighterRank
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class FighterPixelsTest {

    private class Case(
        val look: FighterLook,
        val rank: FighterRank,
        val mood: FighterMood,
        val pose: FighterPose,
    ) {
        fun render(gear: Set<String> = emptySet(), frame: Int = 0) =
            FighterPixels.render(look, rank, mood, pose, gear = gear, frame = frame)

        override fun toString() = "$look $rank $mood $pose"
    }

    private val allCases =
        FighterLook.entries.flatMap { look ->
            FighterRank.entries.flatMap { rank ->
                FighterMood.entries.flatMap { mood ->
                    FighterPose.entries.map { pose -> Case(look, rank, mood, pose) }
                }
            }
        }

    private fun render(
        look: FighterLook = FighterLook.KAI,
        rank: FighterRank = FighterRank.ROOKIE,
        mood: FighterMood = FighterMood.READY,
        pose: FighterPose = FighterPose.STANCE,
        outfit: String = FighterItems.DEFAULT_OUTFIT,
        frame: Int = 0,
    ): List<Int> = FighterPixels.render(look, rank, mood, pose, outfit, frame = frame).argb.toList()

    @Test
    fun `every look, rank, mood and pose renders a full canvas`() {
        val allGear = FighterItems.gear.toSet()
        for (case in allCases) {
            for (frame in 0 until FighterPixels.FRAMES) {
                val image = case.render(allGear, frame)
                assertEquals(FighterPixels.SIZE, image.width)
                assertEquals(FighterPixels.SIZE, image.height)
                assertFalse(image.isBlank, "$case")
            }
        }
    }

    @Test
    fun `all 12 ranks look different`() {
        for (look in FighterLook.entries) for (mood in FighterMood.entries) {
            val images = FighterRank.entries.map { render(look, it, mood) }
            assertEquals(images.size, images.toSet().size, "$look $mood")
        }
    }

    @Test
    fun `belt colours are far apart and the black belts differ by their stripes`() {
        val plain = Belt.entries.filter { it.goldStripes == 0 }
        for (a in plain) for (b in plain) {
            if (a < b) {
                val apart = distance(FighterPixels.beltColor(a), FighterPixels.beltColor(b))
                assertTrue(apart >= 60, "$a and $b are too close")
            }
        }
        val black = Belt.entries.filter { it.name.startsWith("BLACK") }
        assertEquals(1, black.map { FighterPixels.beltColor(it) }.toSet().size)
        assertEquals(listOf(0, 1, 2), black.map { it.goldStripes })
    }

    @Test
    fun `the belt stands out against every gi`() {
        for (belt in Belt.entries) for (outfit in FighterItems.outfits) {
            val gi = FighterPixels.outfitColor(outfit)
            val apart =
                maxOf(
                    distance(FighterPixels.beltColor(belt), gi),
                    distance(FighterPixels.beltShade(belt), gi),
                )
            assertTrue(apart >= 40, "$belt on $outfit")
        }
    }

    @Test
    fun `every gear item shows for both looks, every rank, mood and pose`() {
        for (case in allCases) {
            val without = case.render().argb.toList()
            for (item in FighterItems.gear) {
                assertNotEquals(without, case.render(setOf(item)).argb.toList(), "$item on $case")
            }
        }
    }

    @Test
    fun `every outfit changes the gi and unknown outfits fall back to white`() {
        val images = FighterItems.outfits.map { render(outfit = it) }
        assertEquals(images.size, images.toSet().size)
        assertEquals(render(), render(outfit = "gone"))
    }

    @Test
    fun `rank upgrades start at ranks 4, 7, 9 and 12`() {
        fun startsAt(number: Int, has: (FighterRank) -> Boolean) =
            FighterRank.entries.filter(has) == FighterRank.entries.filter { it.number >= number }
        assertTrue(startsAt(4, FighterPixels::hasWristWraps))
        assertTrue(startsAt(7, FighterPixels::hasHeadband))
        assertTrue(startsAt(9, FighterPixels::hasEmblem))
        assertTrue(startsAt(12, FighterPixels::hasGoldenAura))
    }

    @Test
    fun `a tired fighter always sits, whatever the pose`() {
        val sitting = render(FighterLook.MIRA, mood = FighterMood.TIRED)
        for (pose in FighterPose.entries) {
            assertEquals(sitting, render(FighterLook.MIRA, mood = FighterMood.TIRED, pose = pose))
        }
        assertNotEquals(sitting, render(FighterLook.MIRA))
    }

    @Test
    fun `the stance bounces, ki flickers and frames wrap around`() {
        val half = FighterPixels.FRAMES / 2
        assertNotEquals(render(frame = 0), render(frame = half))
        assertEquals(render(frame = 0), render(frame = 1))
        val charge = FighterPose.CHARGE
        assertNotEquals(render(pose = charge, frame = 0), render(pose = charge, frame = 1))
        assertEquals(
            render(pose = charge, frame = 3),
            render(pose = charge, frame = 3 + FighterPixels.FRAMES),
        )
    }

    @Test
    fun `the ki blast has a size and a golden variant`() {
        val teal = FighterPixels.renderKiBlast()
        assertEquals(24, teal.width)
        assertEquals(12, teal.height)
        assertFalse(teal.isBlank)
        val pixels = teal.argb.toList()
        assertNotEquals(pixels, FighterPixels.renderKiBlast(golden = true).argb.toList())
        assertNotEquals(pixels, FighterPixels.renderKiBlast(frame = 1).argb.toList())
    }

    private fun distance(a: Int, b: Int): Int {
        fun channel(c: Int, shift: Int) = (c shr shift) and 0xFF
        return listOf(16, 8, 0).sumOf { abs(channel(a, it) - channel(b, it)) }
    }
}
