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

import com.shub39.grit.core.avatar.Weather
import com.shub39.grit.core.fighter.FighterItems
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter.Companion.INK
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter.Companion.argb
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter.Companion.light
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter.Companion.mix
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter.Companion.shade
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * The training ground (#8) in the fighter's pixel art: an outdoor dojo floor of wooden planks with
 * a low fence in the back, under a sky that shows the [Weather]. The ground is [WIDTH] x
 * [GROUND_HEIGHT] pixels; a higher scene gets more sky on top. The fighter stands in the middle
 * with its feet at [FOOT_Y], the shop's props stand on fixed [propSpots] left and right of it, and
 * the mat lies under its feet. Pure Kotlin, so it is unit tested and [TrainingGroundScene] only
 * paints the result.
 */
object TrainingGroundPixels {
    const val WIDTH = 160
    const val GROUND_HEIGHT = 80

    /** Row of the ground where the fighter's feet stand (the bottom edge of its picture). */
    const val FOOT_Y = 67

    /** The biggest fighter (in scene pixels per fighter pixel) the props leave room for. */
    const val MAX_FIGHTER_SCALE = 1.6f

    /** Seconds after which the drifting clouds and the turning sun start over. */
    const val LOOP_SECONDS = 60f

    /** How far the clouds drift to and fro, in pixels. */
    const val CLOUD_DRIFT = 3

    /** The sun's rays turn in steps of this many degrees, a new step every 0.67 seconds. */
    const val RAY_STEP_DEGREES = 4

    private const val BACK_ROW = 56
    private const val FRONT_ROW = 79

    /** A prop's fixed spot: its icon's left edge at [x], its bottom edge at [bottom]. */
    class PropSpot(val id: String, val x: Int, val bottom: Int)

    /**
     * Where the props stand, drawn in this order: the back row in front of the fence, then the
     * front row. The mat is not here, it lies under the fighter's feet.
     */
    val propSpots: List<PropSpot> =
        listOf(
            PropSpot(FighterItems.TROPHY_SHELF, x = 0, bottom = BACK_ROW),
            PropSpot(FighterItems.BANNER, x = 19, bottom = BACK_ROW),
            PropSpot(FighterItems.WOODEN_DUMMY, x = 112, bottom = BACK_ROW),
            PropSpot(FighterItems.GONG, x = 136, bottom = BACK_ROW),
            PropSpot(FighterItems.DUMBBELLS, x = 4, bottom = FRONT_ROW),
            PropSpot(FighterItems.WATER_BOTTLE, x = 28, bottom = FRONT_ROW),
            PropSpot(FighterItems.PUNCHING_BAG, x = 128, bottom = FRONT_ROW),
        )

    /** Left edge of the fighter's picture when each of its pixels is [scale] scene pixels wide. */
    fun fighterLeft(scale: Float): Float = WIDTH / 2f - FighterPixels.SIZE * scale / 2f

    /**
     * Top edge of the fighter's picture in the ground, at [scale]; negative reaches into the sky.
     */
    fun fighterTop(scale: Float): Float = FOOT_Y - FighterPixels.SIZE * scale

    /** One step of the sky's motion: the clouds' drift in pixels and the sun's turn in degrees. */
    data class SkyFrame(val drift: Int, val rayAngle: Int) {
        companion object {
            val STILL = SkyFrame(0, 0)

            /** The step [seconds] into the loop of [LOOP_SECONDS]. */
            fun at(seconds: Float): SkyFrame {
                val t = seconds.mod(LOOP_SECONDS)
                val drift = (CLOUD_DRIFT * sin(2 * PI * t / LOOP_SECONDS)).roundToInt()
                // a full turn a minute; the ten rays look the same again after 36 degrees
                val degrees = floor(t * 360f / LOOP_SECONDS / RAY_STEP_DEGREES).toInt()
                return SkyFrame(drift, (degrees * RAY_STEP_DEGREES).mod(RAY_ANGLE_LOOP))
            }
        }
    }

    private const val RAY_ANGLE_LOOP = 36

    /**
     * The sky of a [height] pixels high scene in the [weather]: a pale band at the horizon, the sun
     * and the clouds near the top, moved down by [skyTop] pixels, and in the motion step [frame].
     * The ground is painted over its lower [GROUND_HEIGHT] rows.
     */
    fun renderSky(
        weather: Weather,
        height: Int,
        skyTop: Int = 0,
        frame: SkyFrame = SkyFrame.STILL,
    ): PixelImage {
        val canvas = Canvas(WIDTH, height)
        // the sky and the clouds turn grey the longer the user stays away
        val gray =
            when (weather) {
                Weather.CLOUDY -> 0.3f
                Weather.OVERCAST -> 0.55f
                else -> 0f
            }
        val sky = mix(SKY, TIRED_GRAY, gray)
        val haze = mix(sky, WHITE, 0.25f)
        // the fence stands at row 50 of the ground; the sky gets paler just above it
        val horizon = height - GROUND_HEIGHT + 50
        for (y in 0 until height) for (x in 0 until WIDTH) {
            val d = horizon - y
            canvas[x, y] =
                when {
                    d < 8 -> haze
                    d < 12 && (x + y) % 2 == 0 -> haze
                    else -> sky
                }
        }

        when (weather) {
            Weather.SUNNY -> sun(canvas, 138, 18 + skyTop, 8f, frame.rayAngle, rays = true)
            Weather.FAIR -> sun(canvas, 138, 18 + skyTop, 7f, frame.rayAngle, rays = true)
            Weather.HAZY -> sun(canvas, 141, 16 + skyTop, 6f, frame.rayAngle, rays = false)
            else -> Unit
        }
        val color = mix(WHITE, CLOUD_GRAY, gray)
        cloudsFor(weather).forEachIndexed { i, (x, y, width) ->
            // neighbouring clouds drift the other way
            val drift = if (i % 2 == 0) frame.drift else (-0.6f * frame.drift).roundToInt()
            cloud(canvas, x * GROUND_HEIGHT + drift, y * GROUND_HEIGHT + skyTop, width, color)
        }
        return canvas.image()
    }

    /** Clouds as (x, y, width) in ground heights; later ones cover earlier ones and the sun. */
    private fun cloudsFor(weather: Weather): List<Triple<Float, Float, Float>> {
        val few = listOf(Triple(0.24f, 0.2f, 0.11f), Triple(1.0f, 0.08f, 0.06f))
        val some = few + listOf(Triple(0.5f, 0.5f, 0.05f), Triple(1.93f, 0.44f, 0.05f))
        val many =
            listOf(
                Triple(0.24f, 0.2f, 0.12f),
                Triple(0.62f, 0.1f, 0.09f),
                Triple(1.0f, 0.07f, 0.08f),
                Triple(1.38f, 0.12f, 0.09f),
                Triple(1.74f, 0.24f, 0.12f),
                Triple(0.5f, 0.5f, 0.06f),
                Triple(1.92f, 0.46f, 0.07f),
            )
        return when (weather) {
            Weather.SUNNY -> listOf(Triple(0.24f, 0.18f, 0.08f), Triple(1.0f, 0.08f, 0.05f))
            Weather.FAIR -> some
            // one cloud half in front of the sun
            Weather.HAZY -> some + Triple(1.66f, 0.29f, 0.08f)
            Weather.CLOUDY -> many
            Weather.OVERCAST ->
                listOf(
                    Triple(0.06f, 0.08f, 0.13f),
                    Triple(1.94f, 0.08f, 0.13f),
                    Triple(0.85f, 0.22f, 0.1f),
                    Triple(1.2f, 0.26f, 0.1f),
                ) + many
        }
    }

    /** A friendly round sun with an ink outline and a face; [rays] turned by [angle] degrees. */
    private fun sun(canvas: Canvas, cx: Int, cy: Int, r: Float, angle: Int, rays: Boolean) {
        val x0 = cx + 0.5f
        val y0 = cy + 0.5f
        if (rays) {
            for (i in 0 until 10) {
                val a = (i * 36 + angle) * PI.toFloat() / 180f
                val dx = cos(a)
                val dy = sin(a)
                canvas.capsule(
                    x0 + dx * (r + 2.6f),
                    y0 + dy * (r + 2.6f),
                    x0 + dx * (r + 5f),
                    y0 + dy * (r + 5f),
                    1f,
                    SUN_RAY,
                )
            }
        }
        canvas.disc(x0, y0, r, SUN_YELLOW)
        // eyes, rosy cheeks and a smile
        val eyeX = (r * 0.4f).roundToInt()
        val eyeY = cy - (r * 0.2f).roundToInt()
        canvas.set(cx - eyeX, eyeY, INK)
        canvas.set(cx + eyeX, eyeY, INK)
        val mouth = cy + (r * 0.25f).roundToInt()
        canvas.set(cx - 2, mouth, INK)
        canvas.set(cx + 2, mouth, INK)
        for (x in cx - 1..cx + 1) canvas.set(x, mouth + 1, INK)
        val cheek = (r * 0.65f).roundToInt()
        canvas.set(cx - cheek, mouth, PINK)
        canvas.set(cx + cheek, mouth, PINK)
    }

    /** A puffy cloud about [width] ground heights times 2.6 wide around ([x], [y]). */
    private fun cloud(canvas: Canvas, x: Float, y: Float, width: Float, color: Int) {
        val w = width * GROUND_HEIGHT
        val puffs =
            listOf(
                Triple(-0.85f, 0.15f, 0.45f),
                Triple(-0.35f, -0.2f, 0.6f),
                Triple(0.3f, -0.3f, 0.7f),
                Triple(0.9f, 0.1f, 0.45f),
                Triple(0.05f, 0.25f, 0.5f),
            )
        fun inside(px: Float, py: Float, lift: Float) =
            puffs.any { (dx, dy, r) ->
                val ex = px - (x + dx * w)
                val ey = py - (y + dy * w - lift)
                ex * ex + ey * ey <= r * w * r * w
            }
        val shadow = mix(color, CLOUD_SHADE, 0.35f)
        val edge = mix(shadow, INK, 0.25f)
        // the cloud's body sits a little higher than its shadow, so the shadow shows below
        val reach = 1.4f * w
        canvas.paint(edge, x - reach, y - reach, x + reach, y + reach) { px, py ->
            when {
                inside(px, py, 0.12f * w) -> color
                inside(px, py, 0f) -> shadow
                else -> 0
            }
        }
    }

    /**
     * The ground with the [props] the user shows, [WIDTH] x [GROUND_HEIGHT]: hills behind a low
     * fence, the plank floor, the mat under the fighter and the other props on their spots. A
     * [tired] fighter's ground is a little grey.
     */
    fun renderGround(props: Set<String>, tired: Boolean = false): PixelImage {
        val p = PixelPainter(WIDTH, GROUND_HEIGHT)
        p.ellipse(Pt(24, 54), 56f, 20f, Paint(HILL, "hill"))
        p.ellipse(Pt(140, 54), 50f, 17f, Paint(HILL, "hill"))

        // the low fence in the back: posts and two rails
        val post = Paint(FENCE, "post")
        for (x in 5 until WIDTH step 15) p.capsule(Pt(x + 0.5f, 36), Pt(x + 0.5f, 50), 1.3f, post)
        val rail = Paint(FENCE_RAIL, "rail")
        p.poly(listOf(Pt(0, 38), Pt(WIDTH, 38), Pt(WIDTH, 40), Pt(0, 40)), rail)
        p.poly(listOf(Pt(0, 44), Pt(WIDTH, 44), Pt(WIDTH, 46), Pt(0, 46)), rail)

        // the planks get wider towards the front, which gives the floor depth
        PLANK_ROWS.zipWithNext().forEachIndexed { i, (top, bottom) ->
            val color = if (i % 2 == 0) PLANK else PLANK_ALT
            p.poly(
                listOf(Pt(0, top), Pt(WIDTH, top), Pt(WIDTH, bottom), Pt(0, bottom)),
                Paint(color, "plank$i"),
            )
            // the joints between the boards, shifted from row to row
            val joint = shade(color)
            var x = (i * 23) % PLANK_LENGTH + 6
            while (x < WIDTH) {
                p.face(x, top + 1, List(bottom - top - 2) { "j" }, mapOf('j' to joint))
                x += PLANK_LENGTH
            }
        }
        val front = PLANK_ROWS.last()
        p.poly(
            listOf(Pt(0, front), Pt(WIDTH, front), Pt(WIDTH, GROUND_HEIGHT), Pt(0, GROUND_HEIGHT)),
            Paint(FLOOR_FRONT, "front"),
        )

        if (FighterItems.MAT in props) {
            p.poly(listOf(Pt(54, 61), Pt(106, 61), Pt(110, 70), Pt(50, 70)), Paint(MAT, "mat"))
            p.poly(
                listOf(Pt(77, 61), Pt(83, 61), Pt(83.5, 70), Pt(76.5, 70)),
                Paint(MAT_STRIPE, "stripe"),
            )
        }

        val out = p.render()
        for (spot in propSpots) {
            if (spot.id !in props) continue
            val icon = PropPixels.render(spot.id) ?: continue
            val top = spot.bottom - icon.height
            for (y in 0 until icon.height) for (x in 0 until icon.width) {
                val c = icon[x, y]
                val gx = spot.x + x
                val gy = top + y
                if (c != 0 && gx in 0 until WIDTH && gy in 0 until GROUND_HEIGHT) {
                    out[gy * WIDTH + gx] = c
                }
            }
        }
        if (tired) {
            for (i in out.indices) if (out[i] != 0) out[i] = mix(out[i], TIRED_GRAY, 0.4f)
        }
        return PixelImage(WIDTH, GROUND_HEIGHT, out)
    }

    /** The rows where the plank rows begin, and where the front edge of the floor begins. */
    private val PLANK_ROWS = listOf(49, 53, 58, 64, 71)
    private const val PLANK_LENGTH = 34

    /** A plain pixel canvas for the sky, which needs no outlines between its regions. */
    private class Canvas(val width: Int, val height: Int) {
        val argb = IntArray(width * height)

        operator fun set(x: Int, y: Int, color: Int) {
            if (x in 0 until width && y in 0 until height) argb[y * width + x] = color
        }

        fun image() = PixelImage(width, height, argb)

        /**
         * Paints the colour [color] returns for each pixel centre in the box from ([left], [top])
         * to ([right], [bottom]) (0 leaves the pixel), with an [edge] line around what it painted.
         */
        fun paint(
            edge: Int,
            left: Float,
            top: Float,
            right: Float,
            bottom: Float,
            color: (Float, Float) -> Int,
        ) {
            val x0 = (left.toInt() - 1).coerceAtLeast(0)
            val y0 = (top.toInt() - 1).coerceAtLeast(0)
            val x1 = (right.toInt() + 2).coerceAtMost(width)
            val y1 = (bottom.toInt() + 2).coerceAtMost(height)
            if (x0 >= x1 || y0 >= y1) return
            val w = x1 - x0
            val shape = IntArray(w * (y1 - y0))
            for (y in y0 until y1) for (x in x0 until x1) {
                shape[(y - y0) * w + x - x0] = color(x + 0.5f, y + 0.5f)
            }
            fun filled(x: Int, y: Int) =
                x in x0 until x1 && y in y0 until y1 && shape[(y - y0) * w + x - x0] != 0
            for (y in y0 until y1) for (x in x0 until x1) {
                val c = shape[(y - y0) * w + x - x0]
                if (c != 0) {
                    argb[y * width + x] = c
                } else if (PixelPainter.NEIGHBOURS.any { (dx, dy) -> filled(x + dx, y + dy) }) {
                    argb[y * width + x] = edge
                }
            }
        }

        /** A disc in [color] lit at the top left, shaded at the bottom right and inked. */
        fun disc(cx: Float, cy: Float, r: Float, color: Int) {
            fun at(x: Int, y: Int): Boolean {
                val dx = x + 0.5f - cx
                val dy = y + 0.5f - cy
                return dx * dx + dy * dy <= r * r
            }
            paint(INK, cx - r, cy - r, cx + r, cy + r) { fx, fy ->
                val x = fx.toInt()
                val y = fy.toInt()
                when {
                    !at(x, y) -> 0
                    !at(x + 1, y + 1) -> shade(color)
                    !at(x - 1, y - 1) -> light(color)
                    else -> color
                }
            }
        }

        fun capsule(ax: Float, ay: Float, bx: Float, by: Float, r: Float, color: Int) {
            val x0 = (minOf(ax, bx) - r).toInt().coerceAtLeast(0)
            val x1 = (maxOf(ax, bx) + r + 1).toInt().coerceAtMost(width)
            val y0 = (minOf(ay, by) - r).toInt().coerceAtLeast(0)
            val y1 = (maxOf(ay, by) + r + 1).toInt().coerceAtMost(height)
            val dx = bx - ax
            val dy = by - ay
            val len = dx * dx + dy * dy
            for (y in y0 until y1) for (x in x0 until x1) {
                val px = x + 0.5f
                val py = y + 0.5f
                val t =
                    if (len == 0f) 0f
                    else (((px - ax) * dx + (py - ay) * dy) / len).coerceIn(0f, 1f)
                val qx = ax + t * dx - px
                val qy = ay + t * dy - py
                if (qx * qx + qy * qy <= r * r) argb[y * width + x] = color
            }
        }
    }

    private val SKY = argb(0x90D5FF)
    private val WHITE = argb(0xFFFFFF)
    private val TIRED_GRAY = argb(0xA3A79F)
    private val CLOUD_SHADE = argb(0x9DB5C4)
    private val CLOUD_GRAY = argb(0x8E979C)
    private val SUN_YELLOW = argb(0xFFD45C)
    private val SUN_RAY = argb(0xFFB938)
    private val PINK = argb(0xFF8FA0)
    private val HILL = argb(0x8CCB72)
    private val FENCE = argb(0x9A6438)
    private val FENCE_RAIL = argb(0xB07846)
    private val PLANK = argb(0xD9A066)
    private val PLANK_ALT = argb(0xCC9459)
    private val FLOOR_FRONT = argb(0x8A5530)
    private val MAT = argb(0x4A78C8)
    private val MAT_STRIPE = argb(0xF4F1EA)
}
