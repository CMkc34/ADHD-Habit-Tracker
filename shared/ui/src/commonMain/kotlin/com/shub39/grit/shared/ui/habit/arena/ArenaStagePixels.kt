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
package com.shub39.grit.shared.ui.habit.arena

import com.shub39.grit.shared.ui.habit.fighter.PixelImage
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter.Companion.INK
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter.Companion.argb
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter.Companion.mix
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * The 5 fight backgrounds (#14) in the fighters' pixel art, [WIDTH] x [HEIGHT] pixels: a back wall
 * or sky down to [FLOOR_TOP], then the floor the fighters stand on with their feet at [FOOT_Y].
 * Each [ArenaStage] can be lit by day, at dusk or at night ([ArenaLighting]): the light tints the
 * whole scene, while lamps, screens and lava keep glowing. Pure Kotlin, so it is unit tested and
 * `ArenaBackdrop` only paints the result.
 */
object ArenaStagePixels {
    const val WIDTH = 160
    const val HEIGHT = 96
    const val FLOOR_TOP = 70

    /** Row where both fighters' feet stand (the bottom edge of their pictures). */
    const val FOOT_Y = 90

    /** Left edge of the player's 48 px picture; the opponent's mirrors it on the right. */
    const val PLAYER_LEFT = 18
    const val OPPONENT_LEFT = WIDTH - PLAYER_LEFT - 48

    fun render(stage: ArenaStage, lighting: ArenaLighting): PixelImage {
        val c = Scene(lighting)
        when (stage) {
            ArenaStage.BEDROOM -> c.bedroom()
            ArenaStage.SCREEN_WORLD -> c.screenWorld()
            ArenaStage.LAUNDRY -> c.laundry()
            ArenaStage.CLOCK_TOWER -> c.clockTower()
            ArenaStage.VOLCANO -> c.volcano()
        }
        return c.image()
    }

    /** The sky seen through windows and above the volcano, in [lighting]. */
    fun skyColor(lighting: ArenaLighting, y: Int): Int {
        val t = y / FLOOR_TOP.toFloat()
        return when (lighting) {
            ArenaLighting.DAY -> mix(argb(0x6FB6E8), argb(0xCDEBF5), t)
            ArenaLighting.DUSK -> mix(argb(0x5A3C78), argb(0xF59A5A), t)
            ArenaLighting.NIGHT -> mix(argb(0x0E1430), argb(0x2A2F5C), t)
        }
    }

    private class Scene(val lighting: ArenaLighting) {
        private val argb = IntArray(WIDTH * HEIGHT)
        // pixels that give off light keep their colour in any lighting
        private val glow = BooleanArray(WIDTH * HEIGHT)

        operator fun set(x: Int, y: Int, color: Int) = put(x, y, color, false)

        fun put(x: Int, y: Int, color: Int, lit: Boolean) {
            if (x !in 0 until WIDTH || y !in 0 until HEIGHT) return
            argb[y * WIDTH + x] = color
            glow[y * WIDTH + x] = lit
        }

        fun rect(x0: Int, y0: Int, x1: Int, y1: Int, color: Int, lit: Boolean = false) {
            for (y in y0 until y1) for (x in x0 until x1) put(x, y, color, lit)
        }

        /** A rectangle with an ink outline and a light top and dark bottom edge. */
        fun box(x0: Int, y0: Int, x1: Int, y1: Int, color: Int, lit: Boolean = false) {
            rect(x0, y0, x1, y1, INK, lit)
            rect(x0 + 1, y0 + 1, x1 - 1, y1 - 1, color, lit)
            rect(x0 + 1, y0 + 1, x1 - 1, y0 + 2, light(color), lit)
            rect(x0 + 1, y1 - 2, x1 - 1, y1 - 1, dark(color), lit)
        }

        fun disc(cx: Float, cy: Float, r: Float, color: Int, lit: Boolean = false, edge: Boolean = true) {
            for (y in 0 until HEIGHT) for (x in 0 until WIDTH) {
                val d = hypot(x + 0.5f - cx, y + 0.5f - cy)
                if (d < r) put(x, y, color, lit) else if (edge && d < r + 1f) put(x, y, INK, lit)
            }
        }

        fun line(x0: Float, y0: Float, x1: Float, y1: Float, color: Int, lit: Boolean = false) {
            val steps = maxOf(abs(x1 - x0), abs(y1 - y0)).toInt() + 1
            for (i in 0..steps) {
                val t = i / steps.toFloat()
                put((x0 + (x1 - x0) * t).toInt(), (y0 + (y1 - y0) * t).toInt(), color, lit)
            }
        }

        fun sky(x0: Int, y0: Int, x1: Int, y1: Int) {
            for (y in y0 until y1) for (x in x0 until x1) put(x, y, skyColor(lighting, y), true)
            when (lighting) {
                ArenaLighting.NIGHT ->
                    for (y in y0 until y1) for (x in x0 until x1) {
                        if ((x * 13 + y * 7) % 37 == 0) put(x, y, argb(0xE8E6FF), true)
                    }
                ArenaLighting.DAY ->
                    if (x1 - x0 > 14) {
                        rect(x0 + 3, y0 + 4, x0 + 10, y0 + 6, argb(0xFFFFFF), true)
                        rect(x0 + 5, y0 + 3, x0 + 8, y0 + 4, argb(0xFFFFFF), true)
                    }
                ArenaLighting.DUSK -> Unit
            }
        }

        fun window(x0: Int, y0: Int, x1: Int, y1: Int, frame: Int) {
            rect(x0 - 2, y0 - 2, x1 + 2, y1 + 2, INK)
            rect(x0 - 1, y0 - 1, x1 + 1, y1 + 1, frame)
            sky(x0, y0, x1, y1)
            if (lighting == ArenaLighting.NIGHT) disc(x1 - 6f, y0 + 6f, 3f, argb(0xF4EFCF), true, false)
            val mx = (x0 + x1) / 2
            rect(mx, y0, mx + 1, y1, frame)
            rect(x0, (y0 + y1) / 2, x1, (y0 + y1) / 2 + 1, frame)
        }

        /** Floor planks from [FLOOR_TOP] down. */
        fun planks(base: Int) {
            for (y in FLOOR_TOP until HEIGHT) {
                val row = (y - FLOOR_TOP) / 5
                for (x in 0 until WIDTH) {
                    val seam = (y - FLOOR_TOP) % 5 == 0 || (x + row * 23) % 40 == 0
                    this[x, y] = if (seam) dark(base) else if ((x + y) % 11 == 0) light(base) else base
                }
            }
            rect(0, FLOOR_TOP, WIDTH, FLOOR_TOP + 1, INK)
        }

        fun tiles(base: Int, size: Int, y0: Int, y1: Int) {
            for (y in y0 until y1) for (x in 0 until WIDTH) {
                val seam = (y - y0) % size == 0 || x % size == 0
                this[x, y] = if (seam) dark(base) else base
            }
        }

        // --- 1 and 2: Snooze Button, Couch Gremlin
        fun bedroom() {
            for (y in 0 until FLOOR_TOP) for (x in 0 until WIDTH) {
                this[x, y] = if ((x / 6) % 2 == 0) argb(0x5D6A9E) else argb(0x56628F)
            }
            rect(0, FLOOR_TOP - 6, WIDTH, FLOOR_TOP, argb(0x3E3352))
            window(64, 10, 96, 36, argb(0xD9D2C4))
            // curtains
            rect(58, 6, 64, 42, argb(0x8E3A55))
            rect(96, 6, 102, 42, argb(0x8E3A55))
            rect(56, 5, 104, 7, INK)
            // poster
            box(18, 14, 38, 38, argb(0xE0C35A))
            rect(23, 20, 33, 32, argb(0x3A2F4A))
            // bed on the left
            box(0, 44, 46, FLOOR_TOP, argb(0x7E5A3C))
            box(0, 38, 8, FLOOR_TOP, argb(0x6A4A30))
            box(4, 46, 44, 58, argb(0x3F6F8E))
            box(8, 42, 20, 49, argb(0xEDE6D8))
            // nightstand with a lamp and the alarm clock
            box(122, 50, 146, FLOOR_TOP, argb(0x6A4A30))
            box(128, 40, 140, 50, argb(0xE8C870), lighting != ArenaLighting.DAY)
            rect(133, 36, 135, 40, INK)
            box(142, 26, 158, 40, argb(0xC64B4B))
            rect(149, 29, 150, 34, INK)
            rect(150, 33, 154, 34, INK)
            planks(argb(0x8B6A4E))
            // rug
            for (y in FLOOR_TOP + 6 until FLOOR_TOP + 16) for (x in 40 until 120) {
                val stripe = (y - FLOOR_TOP) % 4 == 0
                this[x, y] = if (stripe) argb(0xC9A64E) else argb(0x7D3A52)
            }
        }

        // --- 3 and 4: Doomscroll Imp, Inbox Hydra
        fun screenWorld() {
            for (y in 0 until FLOOR_TOP) for (x in 0 until WIDTH) {
                this[x, y] = mix(argb(0x1A1238), argb(0x3B1F5E), y / FLOOR_TOP.toFloat())
            }
            // floating app icons and notification bubbles, all glowing
            val icons =
                listOf(
                    Triple(12, 10, argb(0xE0405A)),
                    Triple(40, 26, argb(0x4FD1B0)),
                    Triple(70, 8, argb(0x58A6F0)),
                    Triple(104, 22, argb(0xF5C542)),
                    Triple(134, 6, argb(0xB06CFF)),
                    Triple(146, 40, argb(0x58D7F0)),
                    Triple(4, 44, argb(0xF28C28)),
                )
            for ((x, y, color) in icons) {
                box(x, y, x + 11, y + 11, color, true)
                rect(x + 3, y + 3, x + 8, y + 8, light(color), true)
                disc(x + 10.5f, y + 0.5f, 2.5f, argb(0xFF4A4A), true)
            }
            for ((x, y) in listOf(22 to 50, 84 to 40, 118 to 52)) {
                box(x, y, x + 22, y + 8, argb(0xF2F2FA), true)
                rect(x + 3, y + 3, x + 16, y + 4, argb(0x8A8AA8), true)
                rect(x + 3, y + 5, x + 11, y + 6, argb(0x8A8AA8), true)
            }
            // neon grid floor
            for (y in FLOOR_TOP until HEIGHT) for (x in 0 until WIDTH) put(x, y, argb(0x150E2C), false)
            val neon = argb(0xE45AD8)
            for (y in listOf(0, 2, 5, 9, 14, 20, 26)) rect(0, FLOOR_TOP + y, WIDTH, FLOOR_TOP + y + 1, neon, true)
            for (i in -8..8) line(WIDTH / 2f + i * 4f, FLOOR_TOP.toFloat(), WIDTH / 2f + i * 22f, HEIGHT.toFloat(), neon, true)
        }

        // --- 5 and 6: Laundry Mountain, Distraction Swarm
        fun laundry() {
            tiles(argb(0xBFD6DA), 8, 0, FLOOR_TOP)
            window(70, 8, 92, 26, argb(0xEDEDED))
            // clothes line with socks and shirts
            line(0f, 12f, WIDTH.toFloat(), 16f, INK)
            for ((x, color) in listOf(8 to argb(0xD9524A), 24 to argb(0x4A78C8), 104 to argb(0x6FB35A), 124 to argb(0xE8BE4A), 144 to argb(0x8E62C4))) {
                val y = 13 + x / 40
                box(x, y, x + 8, y + 12, color)
                rect(x + 1, y + 9, x + 12, y + 12, color)
                rect(x + 1, y + 12, x + 12, y + 13, INK)
            }
            // two washing machines
            for (x in listOf(4, 126)) {
                box(x, 36, x + 30, FLOOR_TOP, argb(0xE8E8EE))
                rect(x + 2, 38, x + 28, 42, argb(0xC8C8D2))
                disc(x + 15f, 55f, 9f, argb(0x8C9AB0))
                disc(x + 15f, 55f, 6f, argb(0x6FB6E8), lighting == ArenaLighting.NIGHT)
                rect(x + 22, 39, x + 26, 41, argb(0x4FD1B0), true)
            }
            // a heap of clothes
            for ((cx, cy, color) in listOf(Triple(46f, 66f, argb(0x9B5CD0)), Triple(54f, 63f, argb(0xD9524A)), Triple(62f, 67f, argb(0x3F7FD9)))) {
                disc(cx, cy, 6f, color)
            }
            tiles(argb(0x9AA6B8), 10, FLOOR_TOP, HEIGHT)
            rect(0, FLOOR_TOP, WIDTH, FLOOR_TOP + 1, INK)
        }

        // --- 7 and 8: Time Thief, Deadline Golem
        fun clockTower() {
            for (y in 0 until FLOOR_TOP) for (x in 0 until WIDTH) {
                val row = y / 6
                val seam = y % 6 == 0 || (x + row * 9) % 18 == 0
                this[x, y] = if (seam) argb(0x4E4A58) else argb(0x6E6878)
            }
            // gears
            gear(22f, 20f, 14f, argb(0xB08A3E))
            gear(140f, 50f, 12f, argb(0x9C7A36))
            gear(146f, 14f, 8f, argb(0xB08A3E))
            // the big clock face
            disc(80f, 30f, 24f, argb(0x3A3442))
            disc(80f, 30f, 21f, argb(0xF0E6C8), lighting == ArenaLighting.NIGHT)
            for (h in 0 until 12) {
                val a = h * PI / 6
                val x = 80f + sin(a).toFloat() * 18f
                val y = 30f - cos(a).toFloat() * 18f
                rect(x.toInt(), y.toInt(), x.toInt() + 2, y.toInt() + 2, INK, true)
            }
            // five to twelve: the deadline is close
            line(80f, 30f, 80f, 15f, INK, true)
            line(80f, 30f, 72f, 18f, INK, true)
            line(81f, 30f, 73f, 18f, INK, true)
            disc(80f, 30f, 2f, argb(0xC64B4B), true)
            // arched windows
            for (x in listOf(40, 112)) {
                window(x, 42, x + 10, 60, argb(0x4E4A58))
            }
            planks(argb(0x6A4E3A))
        }

        fun gear(cx: Float, cy: Float, r: Float, color: Int) {
            for (i in 0 until 8) {
                val a = i * PI / 4
                val x = cx + cos(a).toFloat() * r
                val y = cy + sin(a).toFloat() * r
                disc(x, y, r * 0.22f, color, edge = false)
            }
            disc(cx, cy, r * 0.85f, color)
            disc(cx, cy, r * 0.3f, argb(0x4E4A58))
        }

        // --- 9 and 10: Overwhelm Ogre, Procrastination Dragon
        fun volcano() {
            sky(0, 0, WIDTH, FLOOR_TOP)
            // smoke
            for ((cx, cy, r) in listOf(Triple(80f, 8f, 7f), Triple(88f, 4f, 6f), Triple(72f, 2f, 5f))) {
                disc(cx, cy, r, argb(0x5A5560), edge = false)
            }
            // the volcano with a lava stream
            for (y in 14 until FLOOR_TOP) {
                val half = 10 + (y - 14) * 1.4f
                for (x in (80 - half).toInt()..(80 + half).toInt()) this[x, y] = argb(0x4A3A44)
                put((80 - half).toInt() - 1, y, INK, false)
                put((80 + half).toInt() + 1, y, INK, false)
            }
            rect(70, 13, 91, 15, argb(0xFF7A2A), true)
            for (y in 15 until FLOOR_TOP) {
                val x = 80 + (sin(y / 5.0) * 4).toInt()
                rect(x - 2, y, x + 2, y + 1, if (y % 3 == 0) argb(0xFFC94A) else argb(0xFF7A2A), true)
            }
            // far hills
            for (x in 0 until WIDTH) {
                val top = FLOOR_TOP - 10 + (sin(x / 9.0) * 4).toInt()
                for (y in top until FLOOR_TOP) if (argb[y * WIDTH + x] != argb(0x4A3A44)) this[x, y] = argb(0x3A2E36)
            }
            // rock floor with glowing cracks
            for (y in FLOOR_TOP until HEIGHT) for (x in 0 until WIDTH) {
                this[x, y] = if ((x * 3 + y * 5) % 13 == 0) argb(0x3E3238) else argb(0x2E262C)
            }
            for (i in 0 until 6) {
                val x0 = 6 + i * 27
                line(x0.toFloat(), FLOOR_TOP + 4f, x0 + 12f, FLOOR_TOP + 14f, argb(0xFF7A2A), true)
                line(x0 + 12f, FLOOR_TOP + 14f, x0 + 6f, HEIGHT.toFloat(), argb(0xFFC94A), true)
            }
            rect(0, FLOOR_TOP, WIDTH, FLOOR_TOP + 1, INK)
            // rocks at the edges
            disc(6f, 64f, 8f, argb(0x55464E))
            disc(154f, 62f, 9f, argb(0x55464E))
        }

        fun image(): PixelImage {
            val out = IntArray(argb.size) { i ->
                val c = argb[i]
                if (c == 0 || glow[i]) c else tint(c)
            }
            return PixelImage(WIDTH, HEIGHT, out)
        }

        private fun tint(c: Int): Int =
            when (lighting) {
                ArenaLighting.DAY -> c
                ArenaLighting.DUSK -> mix(c, argb(0x8A3A4A), 0.28f)
                ArenaLighting.NIGHT -> mix(c, argb(0x141A38), 0.55f)
            }
    }

    private fun light(c: Int) = mix(c, argb(0xFFFFFF), 0.25f)

    private fun dark(c: Int) = mix(c, INK, 0.3f)
}
