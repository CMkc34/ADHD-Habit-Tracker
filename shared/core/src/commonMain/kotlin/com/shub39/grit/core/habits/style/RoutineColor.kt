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
package com.shub39.grit.core.habits.style

import kotlin.math.pow

/**
 * The 24 colours a routine can have. [key] is stored and backed up, so it must never change; [argb]
 * is the colour as `0xAARRGGBB`.
 */
enum class RoutineColor(val key: String, val argb: Long) {
    RED("red", 0xFFE53935),
    RASPBERRY("raspberry", 0xFFD81B60),
    PINK("pink", 0xFFF06292),
    PURPLE("purple", 0xFF8E24AA),
    VIOLET("violet", 0xFF5E35B1),
    INDIGO("indigo", 0xFF3949AB),
    BLUE("blue", 0xFF1E88E5),
    SKY_BLUE("sky_blue", 0xFF29B6F6),
    TURQUOISE("turquoise", 0xFF00ACC1),
    PETROL("petrol", 0xFF00897B),
    MINT("mint", 0xFF4DB6AC),
    GREEN("green", 0xFF43A047),
    LIGHT_GREEN("light_green", 0xFF7CB342),
    LIME("lime", 0xFFC0CA33),
    YELLOW("yellow", 0xFFFDD835),
    AMBER("amber", 0xFFFFB300),
    ORANGE("orange", 0xFFFB8C00),
    CORAL("coral", 0xFFFF7043),
    RUST("rust", 0xFFBF360C),
    BROWN("brown", 0xFF6D4C41),
    SAND("sand", 0xFFBCAAA4),
    GREY("grey", 0xFF757575),
    SLATE("slate", 0xFF546E7A),
    ANTHRACITE("anthracite", 0xFF37474F);

    /** Black or white, whichever reads better on this colour (icon, check mark). */
    val contentArgb: Long
        get() = contentArgbOn(argb)

    companion object {
        fun fromKey(key: String): RoutineColor? = entries.firstOrNull { it.key == key }
    }
}

private const val BLACK = 0xFF000000
private const val WHITE = 0xFFFFFFFF

/**
 * Picks the content colour (icon, check mark) on [argb]: white while it keeps the WCAG contrast of
 * 3:1 that icons need, black on lighter colours.
 */
fun contentArgbOn(argb: Long): Long {
    val contrastWithWhite = 1.05 / (relativeLuminance(argb) + 0.05)
    return if (contrastWithWhite >= MIN_ICON_CONTRAST) WHITE else BLACK
}

private const val MIN_ICON_CONTRAST = 3.0

/** Relative luminance of an `0xAARRGGBB` colour as defined by WCAG 2. */
internal fun relativeLuminance(argb: Long): Double {
    fun channel(shift: Int): Double {
        val c = ((argb shr shift) and 0xFF) / 255.0
        return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
    }
    return 0.2126 * channel(16) + 0.7152 * channel(8) + 0.0722 * channel(0)
}
