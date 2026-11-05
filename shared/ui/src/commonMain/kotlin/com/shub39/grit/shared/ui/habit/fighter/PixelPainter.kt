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

import kotlin.math.abs
import kotlin.math.roundToInt

/** A point in canvas pixels; fractions are fine, shapes are sampled at pixel centres. */
internal data class Pt(val x: Float, val y: Float) {
    constructor(x: Number, y: Number) : this(x.toFloat(), y.toFloat())

    operator fun plus(o: Pt) = Pt(x + o.x, y + o.y)

    operator fun minus(o: Pt) = Pt(x - o.x, y - o.y)

    fun lerp(to: Pt, t: Float) = Pt(x + (to.x - x) * t, y + (to.y - y) * t)

    val px: Int
        get() = x.roundToInt()

    val py: Int
        get() = y.roundToInt()
}

/**
 * A colour for [PixelPainter] shapes. Shapes of one [group] melt together; where a shape of another
 * group lies on top, a dark line separates them.
 */
internal class Paint(val color: Int, val group: String = color.toString())

/**
 * Rasterises simple shapes (ellipses, polygons, capsules) into a pixel canvas and turns them into
 * pixel art: every region is shaded in three tones with the light from the top left, parts are
 * separated by dark inner lines, and the whole figure gets an ink outline. [face] pixels are put on
 * before the outline, [effect] pixels (ki, sleepy "z") after it.
 */
internal class PixelPainter(val width: Int, val height: Int = width) {
    private val region = IntArray(width * height) { -1 }
    private val paints = ArrayList<Paint>()
    private val order = ArrayList<Int>()
    private var step = 0
    private val face = HashMap<Int, Int>()
    private val effect = HashMap<Int, Int>()

    private fun id(paint: Paint): Int {
        val i = paints.indexOf(paint)
        val id =
            if (i >= 0) i
            else {
                paints += paint
                order += 0
                paints.size - 1
            }
        order[id] = ++step
        return id
    }

    private inline fun fill(paint: Paint, inside: (Float, Float) -> Boolean) {
        val id = id(paint)
        for (y in 0 until height) for (x in 0 until width) {
            if (inside(x + 0.5f, y + 0.5f)) region[y * width + x] = id
        }
    }

    fun ellipse(c: Pt, rx: Float, ry: Float, paint: Paint) =
        fill(paint) { x, y ->
            val dx = (x - c.x) / rx
            val dy = (y - c.y) / ry
            dx * dx + dy * dy <= 1f
        }

    fun circle(c: Pt, r: Float, paint: Paint) = ellipse(c, r, r, paint)

    fun poly(points: List<Pt>, paint: Paint) =
        fill(paint) { x, y ->
            var inside = false
            var j = points.lastIndex
            for (i in points.indices) {
                val a = points[i]
                val b = points[j]
                if ((a.y > y) != (b.y > y) && x < (b.x - a.x) * (y - a.y) / (b.y - a.y) + a.x) {
                    inside = !inside
                }
                j = i
            }
            inside
        }

    /** A thick line from [a] to [b], [ra] wide at the start and [rb] at the end. */
    fun capsule(a: Pt, b: Pt, ra: Float, paint: Paint, rb: Float = ra) =
        fill(paint) { x, y ->
            val dx = b.x - a.x
            val dy = b.y - a.y
            val len = dx * dx + dy * dy
            val t =
                if (len == 0f) 0f else (((x - a.x) * dx + (y - a.y) * dy) / len).coerceIn(0f, 1f)
            val qx = a.x + t * dx - x
            val qy = a.y + t * dy - y
            val r = ra + (rb - ra) * t
            qx * qx + qy * qy <= r * r
        }

    /** Hand-placed pixels: every character of [rows] other than `.` is looked up in [colors]. */
    fun face(x: Int, y: Int, rows: List<String>, colors: Map<Char, Int>) =
        put(face, x, y, rows, colors)

    fun effect(x: Int, y: Int, rows: List<String>, colors: Map<Char, Int>) =
        put(effect, x, y, rows, colors)

    fun effectPixel(x: Int, y: Int, color: Int) {
        if (x in 0 until width && y in 0 until height) effect[y * width + x] = color
    }

    private fun put(
        into: HashMap<Int, Int>,
        x: Int,
        y: Int,
        rows: List<String>,
        colors: Map<Char, Int>,
    ) {
        rows.forEachIndexed { dy, row ->
            row.forEachIndexed { dx, c ->
                val px = x + dx
                val py = y + dy
                if (c != '.' && px in 0 until width && py in 0 until height) {
                    into[py * width + px] = colors.getValue(c)
                }
            }
        }
    }

    private fun regionAt(x: Int, y: Int): Int =
        if (x in 0 until width && y in 0 until height) region[y * width + x] else -1

    /**
     * The finished picture. [halo] puts a one pixel glow around the outline (ki aura, golden aura);
     * [haloLit] decides per pixel whether it is lit, for shimmering.
     */
    fun render(
        halo: Int? = null,
        haloLit: (Int, Int) -> Boolean = { _, _ -> true },
        beforeEffects: (IntArray) -> Unit = {},
    ): IntArray {
        val out = IntArray(width * height)
        for (y in 0 until height) for (x in 0 until width) {
            val r = regionAt(x, y)
            if (r < 0) continue
            val paint = paints[r]
            fun same(dx: Int, dy: Int) = regionAt(x + dx, y + dy) == r
            var c =
                when {
                    !same(1, 1) || (!same(2, 2) && !same(0, 2)) -> shade(paint.color)
                    !same(-1, -1) || !same(0, -1) -> light(paint.color)
                    else -> paint.color
                }
            for ((dx, dy) in NEIGHBOURS) {
                val o = regionAt(x + dx, y + dy)
                if (o >= 0 && paints[o].group != paint.group && order[o] > order[r]) {
                    c = mix(shade(paint.color), INK, 0.55f)
                }
            }
            out[y * width + x] = c
        }
        for ((i, c) in face) out[i] = c

        fun filled(x: Int, y: Int) =
            x in 0 until width && y in 0 until height && out[y * width + x] != 0
        val outline =
            (0 until width * height).filter { i ->
                val x = i % width
                val y = i / width
                out[i] == 0 && NEIGHBOURS.any { (dx, dy) -> filled(x + dx, y + dy) }
            }
        for (i in outline) out[i] = INK

        if (halo != null) {
            val glow =
                (0 until width * height).filter { i ->
                    val x = i % width
                    val y = i / width
                    out[i] == 0 &&
                        AROUND.any { (dx, dy) -> filled(x + dx, y + dy) } &&
                        haloLit(x, y)
                }
            for (i in glow) out[i] = halo
        }
        beforeEffects(out)
        for ((i, c) in effect) out[i] = c
        return out
    }

    companion object {
        val INK: Int = argb(0x231B2E)
        val NEIGHBOURS = listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)
        val AROUND = NEIGHBOURS + listOf(1 to 1, -1 to -1, 1 to -1, -1 to 1)

        fun argb(rgb: Long): Int = (0xFF000000 or rgb).toInt()

        fun mix(a: Int, b: Int, t: Float): Int {
            fun ch(c: Int, s: Int) = (c shr s) and 0xFF
            fun m(s: Int) = (ch(a, s) + (ch(b, s) - ch(a, s)) * t).roundToInt().coerceIn(0, 255)
            return (0xFF shl 24) or (m(16) shl 16) or (m(8) shl 8) or m(0)
        }

        fun light(c: Int): Int = mix(c, argb(0xFFFFFF), 0.32f)

        fun shade(c: Int): Int = mix(c, INK, 0.35f)

        /** Sum of the channel differences, for keeping colours apart. */
        fun distance(a: Int, b: Int): Int =
            listOf(16, 8, 0).sumOf { abs(((a shr it) and 0xFF) - ((b shr it) and 0xFF)) }
    }
}
