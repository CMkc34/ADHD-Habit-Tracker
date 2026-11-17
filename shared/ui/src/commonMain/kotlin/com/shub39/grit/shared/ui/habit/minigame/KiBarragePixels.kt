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

import com.shub39.grit.core.minigame.kibarrage.KiBarrage
import com.shub39.grit.core.minigame.kibarrage.MinionKind
import com.shub39.grit.shared.ui.habit.arena.ArenaLighting
import com.shub39.grit.shared.ui.habit.arena.ArenaStage
import com.shub39.grit.shared.ui.habit.arena.ArenaStagePixels
import com.shub39.grit.shared.ui.habit.fighter.PixelImage
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter.Companion.INK
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter.Companion.argb
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter.Companion.mix
import kotlin.random.Random

/**
 * The pictures of Ki Barrage (#42) that the fighter and the arena do not have yet: the three
 * minions in two frames each, the ki bolt, the enemy shot, the hearts and the night backdrop with
 * the floor of the Screen World stage. Drawn like the preview GIF; pure Kotlin, so it is unit
 * tested and the screen only paints the results.
 */
object KiBarragePixels {
    /** Frames of a minion's flap or wobble. */
    const val FRAMES = 2

    /** Where the floor strip of the Screen World stage starts on the field. */
    const val FLOOR_Y = 164

    private const val FLOOR_FROM = 60
    private const val FLOOR_LEFT = 20

    fun minion(kind: MinionKind, frame: Int): PixelImage {
        val f = frame.mod(FRAMES)
        return when (kind) {
            MinionKind.ALARM -> sprite(if (f == 0) ALARM_0 else ALARM_1, ALARM_COLORS)
            MinionKind.BUBBLE -> sprite(if (f == 0) BUBBLE_0 else BUBBLE_1, BUBBLE_COLORS)
            MinionKind.SOCK -> sprite(if (f == 0) SOCK_0 else SOCK_1, SOCK_COLORS)
        }
    }

    /** The main colour of a minion, for the bits that fly when it pops. */
    fun color(kind: MinionKind): Int =
        when (kind) {
            MinionKind.ALARM -> ALARM_COLORS.getValue('R')
            MinionKind.BUBBLE -> BUBBLE_COLORS.getValue('P')
            MinionKind.SOCK -> SOCK_COLORS.getValue('T')
        }

    /** The small ki bolt, 3 x 7, white hot at the tip and in the fighter's [ki] colour behind. */
    fun bolt(ki: Int): PixelImage {
        val core = mix(ki, WHITE, 0.85f)
        val light = mix(ki, WHITE, 0.45f)
        val argb =
            IntArray(3 * 7) { i ->
                val x = i % 3
                val y = i / 3
                when {
                    x != 1 && y > 4 -> 0
                    x == 1 && y < 3 -> core
                    y < 5 -> light
                    else -> ki
                }
            }
        return PixelImage(3, 7, argb)
    }

    /** An enemy shot, a pink ping in an ink ring, 5 x 5. */
    val shot: PixelImage by lazy {
        sprite(listOf("SSS", "SWS", "SSS"), mapOf('S' to SHOT, 'W' to WHITE))
    }

    fun heart(full: Boolean): PixelImage =
        sprite(HEART, mapOf('R' to if (full) HEART_RED else HEART_EMPTY))

    /**
     * The whole field, [KiBarrage.WIDTH] x [KiBarrage.HEIGHT]: a banded night sky with stars over
     * the floor of the Screen World stage at night, where the fighter stands.
     */
    fun backdrop(): PixelImage {
        val width = KiBarrage.WIDTH.toInt()
        val height = KiBarrage.HEIGHT.toInt()
        val argb = IntArray(width * height)
        val sky = FLOOR_Y + 4
        for (y in 0 until height) {
            val t = (y / sky.toFloat()).coerceAtMost(1f)
            // soft bands of 8 rows, like the preview
            val band = (t * 8).toInt() / 8f
            val color = mix(SKY_TOP, SKY_LOW, band)
            for (x in 0 until width) argb[y * width + x] = color
        }
        val random = Random(7)
        repeat(40) { i ->
            val x = random.nextInt(width)
            val y = random.nextInt(height - 50)
            argb[y * width + x] = if (i % 3 == 0) STAR_GOLD else STAR
        }
        // subtle horizon glow and cyber-dojo skyline silhouette above the floor
        for (y in (FLOOR_Y - 14) until FLOOR_Y) {
            val t = (y - (FLOOR_Y - 14)) / 14f
            val band = (t * 4).toInt() / 4f
            val glow = mix(SKY_LOW, HORIZON_GLOW, band)
            for (x in 0 until width) argb[y * width + x] = glow
        }
        drawSkyline(argb, width)
        val stage = ArenaStagePixels.render(ArenaStage.SCREEN_WORLD, ArenaLighting.NIGHT)
        for (y in FLOOR_FROM until stage.height) for (x in 0 until width) {
            val c = stage[FLOOR_LEFT + x, y]
            val fy = FLOOR_Y + y - FLOOR_FROM
            if (c != 0 && fy < height) argb[fy * width + x] = c
        }
        return PixelImage(width, height, argb)
    }

    /** Draws a distant cyber-dojo skyline silhouette just above [FLOOR_Y]. */
    private fun drawSkyline(argb: IntArray, width: Int) {
        // (startX, width, heightAboveFloor, pagodaEaves)
        val towers =
            listOf(
                listOf(2, 12, 16, 0),
                listOf(15, 16, 22, 1),
                listOf(33, 10, 12, 0),
                listOf(45, 18, 18, 0),
                listOf(66, 14, 24, 1),
                listOf(82, 12, 14, 0),
                listOf(96, 15, 20, 1),
                listOf(112, 8, 13, 0),
            )
        for ((sx, tw, th, pagoda) in towers) {
            val top = FLOOR_Y - th
            for (y in top until FLOOR_Y) {
                for (x in sx until (sx + tw).coerceAtMost(width)) {
                    val isEdge = y == top || x == sx || x == sx + tw - 1
                    argb[y * width + x] = if (isEdge) SKYLINE_EDGE else SKYLINE
                }
            }
            if (pagoda == 1) {
                val roofY = top - 2
                for (x in (sx - 1).coerceAtLeast(0) until (sx + tw + 1).coerceAtMost(width)) {
                    argb[(roofY + 1) * width + x] = SKYLINE_EDGE
                }
                for (x in (sx + 2) until (sx + tw - 2).coerceAtMost(width)) {
                    argb[roofY * width + x] = SKYLINE_EDGE
                }
            }
            // subtle distant cyber-lantern / window pixels
            for (wy in (top + 4) until (FLOOR_Y - 3) step 5) {
                for (wx in (sx + 3) until (sx + tw - 2) step 4) {
                    if ((wx + wy) % 3 == 0 && wx < width) {
                        argb[wy * width + wx] = if (pagoda == 1) WINDOW_GOLD else WINDOW_CYAN
                    }
                }
            }
        }
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
    private val SHOT = argb(0xFF506E)
    private val HEART_RED = argb(0xE8405A)
    private val HEART_EMPTY = argb(0x5A5064)
    private val SKY_TOP = argb(0x171638)
    private val SKY_LOW = argb(0x2B1E56)
    private val HORIZON_GLOW = argb(0x46286E)
    private val SKYLINE = argb(0x161230)
    private val SKYLINE_EDGE = argb(0x261F4D)
    private val WINDOW_CYAN = argb(0x4895B8)
    private val WINDOW_GOLD = argb(0xB89248)
    private val STAR = argb(0xC8C8FF)
    private val STAR_GOLD = argb(0xFFDC78)

    // a little alarm clock with ringing bells, yellow slit eyes and a jagged grin
    private val ALARM_0 =
        listOf(
            "B.......B",
            "BB.....BB",
            ".RRRRRRR.",
            "RRRRRRRRR",
            "RYYRRRYYR",
            "RRRRRRRRR",
            "RWDWDWDWR",
            ".rrrrrrr.",
            ".L.....L.",
        )
    private val ALARM_1 = listOf(".B.....B.", "BBB...BBB") + ALARM_0.drop(2)
    private val ALARM_COLORS =
        mapOf(
            'B' to argb(0xFAC850),
            'R' to argb(0xD64046),
            'r' to argb(0x962836),
            'Y' to argb(0xFFEC5A),
            'W' to WHITE,
            'D' to argb(0x3C141E),
            'L' to argb(0x3C3246),
        )

    // a notification bubble with a red badge and a wiggling tail
    private val BUBBLE_0 =
        listOf(
            ".......OO",
            ".PPPPPPOO",
            "PPPPPPPPP",
            "PYYPPPYYP",
            "PPPPPPPPP",
            "PPWDWDWPP",
            ".ppppppp.",
            ".pp......",
            ".p.......",
        )
    private val BUBBLE_1 = BUBBLE_0.dropLast(2) + listOf("..pp.....", "...p.....")
    private val BUBBLE_COLORS =
        mapOf(
            'P' to argb(0x965ADC),
            'p' to argb(0x643CA0),
            'O' to argb(0xF03C46),
            'Y' to argb(0x78FFC8),
            'W' to WHITE,
            'D' to argb(0x281432),
        )

    // a flying striped sock with red eyes and teeth at the toe
    private val SOCK_0 =
        listOf("..TTTT", "..SSSS", "..TTTT", "..SSSS", ".SYSYS", "SSSSSS", "SWDWDS", "SSSS..")
    private val SOCK_1 = SOCK_0.take(4) + ".SSYSY" + SOCK_0.drop(5)
    private val SOCK_COLORS =
        mapOf(
            'T' to argb(0x3CAAB4),
            'S' to argb(0xECECE4),
            'Y' to argb(0xF03C3C),
            'W' to argb(0xFFFFFF),
            'D' to argb(0x281E28),
        )

    private val HEART = listOf(".RR.RR.", "RRRRRRR", "RRRRRRR", ".RRRRR.", "..RRR..", "...R...")
}
