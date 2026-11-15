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
package com.shub39.grit.shared.ui.habit.minigame

import com.shub39.grit.core.minigame.clutter.ClutterSmash
import com.shub39.grit.core.minigame.clutter.JunkKind
import com.shub39.grit.shared.ui.habit.arena.ArenaLighting
import com.shub39.grit.shared.ui.habit.arena.ArenaStage
import com.shub39.grit.shared.ui.habit.arena.ArenaStagePixels
import com.shub39.grit.shared.ui.habit.fighter.PixelImage
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter.Companion.INK
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter.Companion.argb
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter.Companion.mix

/**
 * The pixel art of Clutter Smash (#43): the six kinds of falling junk (intact and cracked), the
 * small horizontal ki bolt, the room-clearing Ki Wave crescent, and the tall Laundry Room stage
 * backdrop. Pure Kotlin so every sprite is unit tested and the screen only paints the results.
 */
object ClutterSmashPixels {
    /** Where the tiled floor of the Laundry stage starts on the [ClutterSmash] field. */
    const val FLOOR_TOP = 102

    private const val EXTRA_WALL = 32

    fun junk(kind: JunkKind, damaged: Boolean = false): PixelImage {
        val base =
            when (kind) {
                JunkKind.SHIRT -> sprite(SHIRT, SHIRT_COLORS)
                JunkKind.MAIL -> sprite(MAIL, MAIL_COLORS)
                JunkKind.PHONE -> sprite(PHONE, PHONE_COLORS)
                JunkKind.PIZZA_BOX -> sprite(PIZZA_BOX, PIZZA_COLORS)
                JunkKind.MUG -> sprite(MUG, MUG_COLORS)
                JunkKind.SOCK -> sprite(SOCK, SOCK_COLORS)
            }
        return if (damaged) crack(base) else base
    }

    /** Main colour of [kind], for the pixel bits that burst out when it is smashed. */
    fun color(kind: JunkKind): Int =
        when (kind) {
            JunkKind.SHIRT -> SHIRT_COLORS.getValue('R')
            JunkKind.MAIL -> MAIL_COLORS.getValue('P')
            JunkKind.PHONE -> PHONE_COLORS.getValue('S')
            JunkKind.PIZZA_BOX -> PIZZA_COLORS.getValue('B')
            JunkKind.MUG -> MUG_COLORS.getValue('M')
            JunkKind.SOCK -> SOCK_COLORS.getValue('T')
        }

    /**
     * The small ki bolt that flies from the fighter's fist or foot to a tapped piece of junk, 9 x
     * 5, white-hot at the right tip and trailing in the fighter's [ki] colour.
     */
    fun bolt(ki: Int): PixelImage {
        val core = mix(ki, WHITE, 0.85f)
        val light = mix(ki, WHITE, 0.45f)
        val w = 9
        val h = 5
        val argb =
            IntArray(w * h) { i ->
                val x = i % w
                val y = i / w
                when {
                    (y == 0 || y == h - 1) && x < 4 -> 0
                    (y == 0 || y == h - 1) && x == w - 1 -> 0
                    x >= 6 && y in 1..3 -> core
                    x >= 3 && y in 1..3 -> light
                    else -> ki
                }
            }
        return PixelImage(w, h, argb)
    }

    /**
     * The tall crescent of the Ki Wave that sweeps across the laundry room from left to right when
     * the ki bar is full, 14 x 72 in the fighter's [ki] colour.
     */
    fun wave(ki: Int): PixelImage {
        val core = mix(ki, WHITE, 0.85f)
        val light = mix(ki, WHITE, 0.45f)
        val w = 14
        val h = 72
        val mid = h / 2f
        val argb =
            IntArray(w * h) { i ->
                val x = i % w
                val y = i / w
                val dy = (y - mid) / mid
                val curve = (4f * (1f - dy * dy)).toInt()
                val front = 9 + curve
                when {
                    x > front -> 0
                    x >= front - 2 -> core
                    x >= front - 5 -> light
                    x >= front - 8 && (x + y) % 2 == 0 -> ki
                    else -> 0
                }
            }
        return PixelImage(w, h, argb)
    }

    /**
     * The Laundry Room stage stretched to [ClutterSmash.WIDTH] x [ClutterSmash.HEIGHT]: the window
     * and clothesline at the top, extra tiled wall in the middle so the three piles have room to
     * grow, and the washing machines and floor at the bottom.
     */
    fun backdrop(): PixelImage {
        val width = ClutterSmash.WIDTH.toInt()
        val height = ClutterSmash.HEIGHT.toInt()
        val stage = ArenaStagePixels.render(ArenaStage.LAUNDRY, ArenaLighting.DAY)
        val wallTile = stage[1, 31]
        val wallSeam = stage[0, 32]
        val argb = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                argb[y * width + x] =
                    when {
                        y < 30 -> stage[x, y]
                        y < 30 + EXTRA_WALL -> if (y % 8 == 0 || x % 8 == 0) wallSeam else wallTile
                        else -> stage[x, y - EXTRA_WALL]
                    }
            }
        }
        return PixelImage(width, height, argb)
    }

    /** Draws a jagged ink crack across the middle of [image] for damaged multi-hit junk. */
    private fun crack(image: PixelImage): PixelImage {
        val out = image.argb.copyOf()
        val cx = image.width / 2
        for (y in 2 until image.height - 2) {
            val x = (cx + if (y % 2 == 0) -1 else 1).coerceIn(1, image.width - 2)
            if (out[y * image.width + x] != 0) {
                out[y * image.width + x] = INK
            }
        }
        return PixelImage(image.width, image.height, out)
    }

    /** A sprite from rows of letters ('.' is empty) with an ink outline around the filled ones. */
    private fun sprite(rows: List<String>, colors: Map<Char, Int>): PixelImage {
        val w = rows.maxOf { it.length } + 2
        val h = rows.size + 2
        val argb = IntArray(w * h)
        rows.forEachIndexed { y, row ->
            row.forEachIndexed { x, c ->
                if (c != '.') argb[(y + 1) * w + x + 1] = colors.getValue(c)
            }
        }
        val filled = argb.copyOf()
        for (y in 0 until h) for (x in 0 until w) {
            if (filled[y * w + x] != 0) continue
            val touches =
                listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1).any { (dx, dy) ->
                    val nx = x + dx
                    val ny = y + dy
                    nx in 0 until w && ny in 0 until h && filled[ny * w + nx] != 0
                }
            if (touches) argb[y * w + x] = INK
        }
        return PixelImage(w, h, argb)
    }

    private val WHITE = argb(0xF5F5F5)

    // Crumpled t-shirt (18 x 11 -> 20 x 13 with outline)
    private val SHIRT =
        listOf(
            "..RRRR..WWWW..RRRR",
            ".RRRRRRRRWWWRRRRRR",
            "RRRRRRRRRRRRRRRRRR",
            "RRRRrRRRRRRRRrRRRR",
            "...RRRRRRRRRRRR...",
            "...RRRRYYYYRRRR...",
            "...RRRRYYYYRRRR...",
            "...RRRRRRRRRRRR...",
            "...RRRRrRRRRRRR...",
            "...rrrrrrrrrrrr...",
            "....rrrrrrrrrr....",
        )
    private val SHIRT_COLORS =
        mapOf('R' to argb(0xE0524A), 'r' to argb(0xAD3832), 'W' to WHITE, 'Y' to argb(0xFFD65A))

    // Unopened mail envelope with urgent red seal (16 x 8 -> 18 x 10 with outline)
    private val MAIL =
        listOf(
            "PPPPPPPPPPPPPPPP",
            "PpPPPPPPPPPPPPpP",
            "PPpPPPPPPPPPPpPP",
            "PPPpPPPSSPPPpPPP",
            "PPPPpPPSSPPpPPPP",
            "PPPpPPPPPPPPpPPP",
            "PPpPPPPPPPPPPpPP",
            "pppppppppppppppp",
        )
    private val MAIL_COLORS =
        mapOf('P' to argb(0xF7EEDC), 'p' to argb(0xC9BFA8), 'S' to argb(0xE0405A))

    // Buzzing smartphone with lit screen and notification badge (12 x 10 -> 14 x 12 with outline)
    private val PHONE =
        listOf(
            ".DDDDDDDDDD.",
            "DDSSSSSSSSOO",
            "DDSSWWSSSSOO",
            "DDSSSSSSSSDD",
            "DDSSWWWWSSDD",
            "DDSSSSSSSSDD",
            "DDssWWWWssDD",
            "DDssssssssDD",
            "DDDDDWWDDDDD",
            ".DDDDDDDDDD.",
        )
    private val PHONE_COLORS =
        mapOf(
            'D' to argb(0x34384E),
            'S' to argb(0x58B8F0),
            's' to argb(0x3E8EC4),
            'W' to WHITE,
            'O' to argb(0xFF4A5A),
        )

    // Flat cardboard pizza box with red-and-green crest (22 x 10 -> 24 x 12 with outline)
    private val PIZZA_BOX =
        listOf(
            "..BBBBBBBBBBBBBBBBBB..",
            ".BBBBBBBBBBBBBBBBBBBB.",
            "BBBBBBBBRRRRRRBBBBBBBB",
            "BBBBBBBRRGGGGRRBBBBBBB",
            "BBBBBBBBRRRRRRBBBBBBBB",
            "BBBBBBBBBBBBBBBBBBBBBB",
            "WWWWWWWWWWWWWWWWWWWWWW",
            "bbbbbbbbbbbbbbbbbbbbbb",
            "bbbbBBBBbbbbbbBBBBbbbb",
            ".bbbbbbbbbbbbbbbbbbbb.",
        )
    private val PIZZA_COLORS =
        mapOf(
            'B' to argb(0xE0A858),
            'b' to argb(0xAE7C38),
            'R' to argb(0xD94842),
            'G' to argb(0x4FB35A),
            'W' to WHITE,
        )

    // Coffee mug with handle and dark coffee inside (13 x 9 -> 15 x 11 with outline)
    private val MUG =
        listOf(
            ".CCCCCCCCCC..",
            "MMMMMMMMMM...",
            "MWWMMMMMMMHHH",
            "MWWMMMMMMM..H",
            "MMMMMMMMMM..H",
            "MMMMMMMMMMHHH",
            "mmmmmmmmmm...",
            "mmmmmmmmmm...",
            ".mmmmmmmm....",
        )
    private val MUG_COLORS =
        mapOf(
            'M' to argb(0x9668DC),
            'm' to argb(0x6C46A8),
            'H' to argb(0x9668DC),
            'C' to argb(0x4A2E20),
            'W' to WHITE,
        )

    // Striped sock (14 x 8 -> 16 x 10 with outline)
    private val SOCK =
        listOf(
            "....TTTTTT....",
            "....SSSSSS....",
            "....TTTTTT....",
            "....SSSSSS....",
            "...SSSSSSSS...",
            ".SSSSSSSSSSSS.",
            "SSTTTTSSSSssss",
            ".ssssssssssss.",
        )
    private val SOCK_COLORS =
        mapOf('T' to argb(0x3CAAB4), 'S' to argb(0xECECE4), 's' to argb(0xBEBEB4))
}
