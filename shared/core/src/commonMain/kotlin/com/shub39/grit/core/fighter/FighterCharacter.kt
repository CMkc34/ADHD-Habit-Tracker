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
 * The five fighters the user can pick at the start (SPEC 9.9). Each one has its own silhouette and
 * ki colour; skin, hair and the two outfit colours are chosen with [FighterColors]. The [id] is
 * stored, so it never changes. [defaultName] is what the fighter is called until the user renames
 * it; Wind Monk and Spark Prodigy keep the names of the two looks they replace.
 */
enum class FighterCharacter(
    val id: String,
    val defaultName: String,
    val defaultColors: FighterColors,
) {
    /** Topknot, flowing scarf, wrapped forearms, wide pants. Orange ki. */
    WIND_MONK("wind_monk", "Kai", FighterColors(skin = 3, hair = 0, main = 0, accent = 0)),

    /** Messy hair, open hoodie, fingerless gloves, high-tops. Mint ki. */
    STREET_BRAWLER(
        "street_brawler",
        "Rook",
        FighterColors(skin = 1, hair = 3, main = 1, accent = 1),
    ),

    /** Hooded runner with a mask, long headband tails and arm guards. Violet ki. */
    SHADOW_RUNNER("shadow_runner", "Nyx", FighterColors(skin = 4, hair = 0, main = 2, accent = 2)),

    /** Cloud hair, goggles, a cropped jacket over the gi. Blue ki. */
    SPARK_PRODIGY("spark_prodigy", "Mira", FighterColors(skin = 5, hair = 5, main = 3, accent = 3)),

    /** The classic karateka: short hair, a plain gi with rolled sleeves, the rank belt. Red ki. */
    KARATEKA("karateka", "Haru", FighterColors(skin = 2, hair = 1, main = 8, accent = 6));

    companion object {
        val DEFAULT = WIND_MONK

        /** The character stored as [id]; unknown or missing ids give [DEFAULT]. */
        fun fromId(id: String?): FighterCharacter = entries.firstOrNull { it.id == id } ?: DEFAULT

        /**
         * The character stored as [id], or `null` when nothing (or something unknown) is stored.
         */
        fun fromIdOrNull(id: String?): FighterCharacter? = entries.firstOrNull { it.id == id }

        /** The two looks from before the character choice (SPEC 9.8) map to the closest fighter. */
        fun of(look: FighterLook): FighterCharacter =
            when (look) {
                FighterLook.KAI -> WIND_MONK
                FighterLook.MIRA -> SPARK_PRODIGY
            }
    }
}

/**
 * The colours the user picked for their fighter, each an index into one of the [FighterPalette]
 * lists. Only presets can be picked, so every combination keeps the pixel art readable; the light
 * and dark shades are derived from them when drawing.
 */
data class FighterColors(val skin: Int, val hair: Int, val main: Int, val accent: Int) {
    val skinColor: Int
        get() = FighterPalette.skins.wrapped(skin)

    val hairColor: Int
        get() = FighterPalette.hairs.wrapped(hair)

    val mainColor: Int
        get() = FighterPalette.mains.wrapped(main)

    val accentColor: Int
        get() = FighterPalette.accents.wrapped(accent)

    /** Stored as `skin|hair|main|accent`. */
    fun encode(): String = listOf(skin, hair, main, accent).joinToString("|")

    companion object {
        /** Reads [encode]d colours; anything broken or out of range gives [fallback]. */
        fun decode(value: String?, fallback: FighterColors): FighterColors {
            val parts = value?.split("|")?.map { it.toIntOrNull() } ?: return fallback
            if (parts.size != 4 || parts.any { it == null }) return fallback
            val (skin, hair, main, accent) = parts.map { it!! }
            val colors = FighterColors(skin, hair, main, accent)
            return if (colors.isValid()) colors else fallback
        }
    }

    fun isValid(): Boolean =
        skin in FighterPalette.skins.indices &&
            hair in FighterPalette.hairs.indices &&
            main in FighterPalette.mains.indices &&
            accent in FighterPalette.accents.indices
}

/** The colour presets, as opaque ARGB values. */
object FighterPalette {
    private fun argb(rgb: Long): Int = (0xFF000000 or rgb).toInt()

    val skins: List<Int> =
        listOf(0xF8D6BC, 0xF2C7A5, 0xE0A47A, 0xD99A6C, 0xB97A52, 0x8C5A3C, 0x6B4230, 0x4A2E22).map {
            argb(it.toLong())
        }

    val hairs: List<Int> =
        listOf(0x2A2238, 0x4A3020, 0x8A5A3A, 0xE07A3A, 0xF2D27A, 0xF2F0F7, 0x5B86D6, 0xE06C9F).map {
            argb(it.toLong())
        }

    /** The main outfit colour: top, hoodie, suit or jacket. */
    val mains: List<Int> =
        listOf(
                0xE8A33C,
                0x7B4FC9,
                0x2B3A5E,
                0xE85A6A,
                0x3E9C6A,
                0x3F7FD9,
                0xC84B3A,
                0x3A3F55,
                0xF4F1EA,
            )
            .map { argb(it.toLong()) }

    /** The accent: scarf, trims, headband or goggles. */
    val accents: List<Int> =
        listOf(0xC84B3A, 0x4FD1B0, 0xF28C28, 0xFFC93C, 0x58D7F0, 0xB06CFF, 0xF4F1EA, 0xE06C9F).map {
            argb(it.toLong())
        }
}

private fun List<Int>.wrapped(index: Int): Int = this[index.mod(size)]
