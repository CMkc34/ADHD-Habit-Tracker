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
import com.shub39.grit.core.fighter.FighterCharacter
import com.shub39.grit.core.fighter.FighterColors
import com.shub39.grit.core.fighter.FighterItems
import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.core.fighter.FighterPalette
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter.Companion.distance
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class FighterPixelsTest {

    private class Case(
        val character: FighterCharacter,
        val rank: FighterRank,
        val mood: FighterMood,
        val pose: FighterPose,
    ) {
        fun render(gear: Set<String> = emptySet(), frame: Int = 0) =
            FighterPixels.render(character, rank, mood, pose = pose, gear = gear, frame = frame)

        override fun toString() = "$character $rank $mood $pose"
    }

    private val allCases =
        FighterCharacter.entries.flatMap { character ->
            FighterRank.entries.flatMap { rank ->
                FighterMood.entries.flatMap { mood ->
                    FighterPose.entries.map { pose -> Case(character, rank, mood, pose) }
                }
            }
        }

    private fun render(
        character: FighterCharacter = FighterCharacter.KARATEKA,
        rank: FighterRank = FighterRank.ROOKIE,
        mood: FighterMood = FighterMood.READY,
        pose: FighterPose = FighterPose.STANCE,
        colors: FighterColors = character.defaultColors,
        frame: Int = 0,
        outfit: String? = null,
    ): List<Int> =
        FighterPixels.render(character, rank, mood, colors, pose, frame = frame, outfit = outfit)
            .argb
            .toList()

    @Test
    fun `every character, rank, mood and pose renders a full canvas`() {
        val allGear = FighterItems.gear.toSet()
        for (case in allCases) {
            for (frame in listOf(0, 3, FighterPixels.FRAMES / 2)) {
                val image = case.render(allGear, frame)
                assertEquals(FighterPixels.SIZE, image.width)
                assertEquals(FighterPixels.SIZE, image.height)
                assertFalse(image.isBlank, "$case")
            }
        }
    }

    @Test
    fun `the characters look different`() {
        val images = FighterCharacter.entries.map { render(it) }
        assertEquals(images.size, images.toSet().size)
    }

    @Test
    fun `all 12 ranks look different for every character`() {
        for (character in FighterCharacter.entries) for (mood in FighterMood.entries) {
            val images = FighterRank.entries.map { render(character, it, mood) }
            assertEquals(images.size, images.toSet().size, "$character $mood")
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
    fun `every gear item shows on every character, rank, mood and pose`() {
        for (case in allCases) {
            val without = case.render().argb.toList()
            for (item in FighterItems.gear) {
                assertNotEquals(without, case.render(setOf(item)).argb.toList(), "$item on $case")
            }
        }
    }

    @Test
    fun `every colour slot changes the picture of every character`() {
        for (character in FighterCharacter.entries) {
            val base = character.defaultColors
            val plain = render(character, colors = base)
            val changed =
                listOf(
                    base.copy(skin = (base.skin + 1) % FighterPalette.skins.size),
                    base.copy(main = (base.main + 1) % FighterPalette.mains.size),
                    base.copy(accent = (base.accent + 1) % FighterPalette.accents.size),
                    base.copy(hair = (base.hair + 1) % FighterPalette.hairs.size),
                )
            for (colors in changed) {
                assertNotEquals(plain, render(character, colors = colors), "$character $colors")
            }
        }
    }

    @Test
    fun `every outfit replaces the main colour and unknown outfits change nothing`() {
        for (character in FighterCharacter.entries) {
            fun wearing(outfit: String?) = render(character, outfit = outfit)
            val images = FighterItems.outfits.map { wearing(it) }
            assertEquals(images.size, images.toSet().size, "$character")
            assertEquals(wearing(null), wearing("gone"))
            assertEquals(render(character), wearing(null))
        }
    }

    @Test
    fun `outfits stand apart from every belt`() {
        for (outfit in FighterItems.outfits) {
            val gi = FighterPixels.outfitColor(outfit)!!
            val apart = Belt.entries.all { distance(FighterPixels.beltColor(it), gi) >= 20 }
            assertTrue(apart, outfit)
        }
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
        val sitting = render(mood = FighterMood.TIRED)
        for (pose in FighterPose.entries) {
            assertEquals(sitting, render(mood = FighterMood.TIRED, pose = pose))
        }
        assertNotEquals(sitting, render())
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
    fun `every character has its own ki colour and the Legend's ki is gold`() {
        val colors = FighterCharacter.entries.map { FighterPixels.kiColor(it) }
        assertEquals(colors.size, colors.toSet().size)
        val gold = FighterCharacter.entries.map { FighterPixels.kiColor(it, FighterRank.LEGEND) }
        assertEquals(1, gold.toSet().size)
    }

    @Test
    fun `the ki blast has a size and flickers`() {
        val blast = FighterPixels.renderKiBlast(FighterCharacter.WIND_MONK)
        assertEquals(24, blast.width)
        assertEquals(12, blast.height)
        assertFalse(blast.isBlank)
        val pixels = blast.argb.toList()
        val other = FighterPixels.renderKiBlast(FighterCharacter.SHADOW_RUNNER)
        assertNotEquals(pixels, other.argb.toList())
        val flicker = FighterPixels.renderKiBlast(FighterCharacter.WIND_MONK, frame = 1)
        assertNotEquals(pixels, flicker.argb.toList())
    }
}
