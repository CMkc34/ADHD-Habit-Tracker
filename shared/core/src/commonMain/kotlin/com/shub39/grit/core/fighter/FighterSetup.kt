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
package com.shub39.grit.core.fighter

import kotlin.random.Random

/** One of the four colours the user picks for their fighter at setup (#26). */
enum class FighterColorSlot {
    SKIN,
    HAIR,
    MAIN,
    ACCENT;

    /** The presets of this slot from the [FighterPalette], as ARGB values. */
    val presets: List<Int>
        get() =
            when (this) {
                SKIN -> FighterPalette.skins
                HAIR -> FighterPalette.hairs
                MAIN -> FighterPalette.mains
                ACCENT -> FighterPalette.accents
            }

    /** The preset index [colors] use for this slot. */
    fun indexIn(colors: FighterColors): Int =
        when (this) {
            SKIN -> colors.skin
            HAIR -> colors.hair
            MAIN -> colors.main
            ACCENT -> colors.accent
        }

    /** [colors] with this slot set to the preset [index]; an index out of range is wrapped. */
    fun set(colors: FighterColors, index: Int): FighterColors {
        val wrapped = index.mod(presets.size)
        return when (this) {
            SKIN -> colors.copy(skin = wrapped)
            HAIR -> colors.copy(hair = wrapped)
            MAIN -> colors.copy(main = wrapped)
            ACCENT -> colors.copy(accent = wrapped)
        }
    }
}

/**
 * The pure rules of the fighter picker at setup (#26): flipping through the five fighters and
 * rolling random colours. The choice is final once the setup is done.
 */
object FighterSetup {
    /** The fighter [steps] places after [current], wrapping around at both ends. */
    fun step(current: FighterCharacter, steps: Int): FighterCharacter =
        FighterCharacter.entries[(current.ordinal + steps).mod(FighterCharacter.entries.size)]

    /** A random preset for each slot. */
    fun randomColors(random: Random = Random): FighterColors =
        FighterColors(
            skin = random.nextInt(FighterPalette.skins.size),
            hair = random.nextInt(FighterPalette.hairs.size),
            main = random.nextInt(FighterPalette.mains.size),
            accent = random.nextInt(FighterPalette.accents.size),
        )
}
