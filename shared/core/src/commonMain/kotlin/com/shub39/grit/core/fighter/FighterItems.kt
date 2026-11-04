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

/**
 * Ids of everything the fighter can wear (SPEC 9.7). The fighter drawing (#6) knows how to draw
 * each of them; prices and minimum ranks belong to the shop catalog (#7). The ids are stored with
 * each purchase, so they never change.
 */
object FighterItems {
    /** The free gi everybody owns. */
    const val GI_WHITE = "gi_white"
    const val GI_BLUE = "gi_blue"
    const val GI_RED = "gi_red"
    const val GI_BLACK = "gi_black"
    const val GI_PURPLE = "gi_purple"
    const val GI_GOLD = "gi_gold"

    const val SWEATBAND = "gear_sweatband"
    const val GLOVES = "gear_gloves"
    const val SNEAKERS = "gear_sneakers"
    const val CAPE = "gear_cape"
    const val SUNGLASSES = "gear_sunglasses"
    const val CHAMPION_BELT = "gear_champion_belt"
    const val CROWN = "gear_crown"

    const val DEFAULT_OUTFIT = GI_WHITE

    /** Exactly one is worn. */
    val outfits: List<String> = listOf(GI_WHITE, GI_BLUE, GI_RED, GI_BLACK, GI_PURPLE, GI_GOLD)

    /** Any number can be worn at the same time. */
    val gear: List<String> =
        listOf(SWEATBAND, GLOVES, SNEAKERS, CAPE, SUNGLASSES, CHAMPION_BELT, CROWN)
}
