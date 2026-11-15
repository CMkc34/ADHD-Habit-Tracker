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

import com.shub39.grit.core.minigame.boardbreak.StrikeMaterial
import com.shub39.grit.shared.ui.habit.fighter.PixelImage
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter.Companion.INK
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter.Companion.argb
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter.Companion.mix
import kotlin.math.hypot

/**
 * The pixel art of Board Break (#44) on top of
 * [com.shub39.grit.shared.ui.habit.fighter.TrainingGroundPixels]: the dojo breaking stand, intact
 * layers of boards, bricks and ice, split left/right halves that fly apart when broken, and the ki
 * impact flash at the fighter's fist. Pure Kotlin so every sprite is unit tested and the screen
 * only paints the results.
 */
object BoardBreakPixels {
    const val LAYER_WIDTH = 32
    const val LAYER_HEIGHT = 6
    const val LAYER_STEP_Y = 4
    const val STAND_WIDTH = 38
    const val STAND_BASE_HEIGHT = 18

    /** An intact horizontal layer of [material] (`32 x 6` including its ink outline). */
    fun layer(material: StrikeMaterial): PixelImage =
        when (material) {
            StrikeMaterial.BOARDS -> sprite(BOARD_LAYER, BOARD_COLORS)
            StrikeMaterial.BRICKS -> sprite(BRICK_LAYER, BRICK_COLORS)
            StrikeMaterial.ICE -> sprite(ICE_LAYER, ICE_COLORS)
        }

    /**
     * One broken half of a layer of [material] (`17 x 6` including its jagged center fracture),
     * flung to the left (`rightHalf = false`) or right (`rightHalf = true`) when a strike lands.
     */
    fun halfLayer(material: StrikeMaterial, rightHalf: Boolean): PixelImage {
        val rows =
            when (material) {
                StrikeMaterial.BOARDS -> if (rightHalf) BOARD_RIGHT else BOARD_LEFT
                StrikeMaterial.BRICKS -> if (rightHalf) BRICK_RIGHT else BRICK_LEFT
                StrikeMaterial.ICE -> if (rightHalf) ICE_RIGHT else ICE_LEFT
            }
        val colors =
            when (material) {
                StrikeMaterial.BOARDS -> BOARD_COLORS
                StrikeMaterial.BRICKS -> BRICK_COLORS
                StrikeMaterial.ICE -> ICE_COLORS
            }
        return sprite(rows, colors)
    }

    /** Main colour of [material], for flying splinters and shards on impact. */
    fun color(material: StrikeMaterial): Int =
        when (material) {
            StrikeMaterial.BOARDS -> BOARD_COLORS.getValue('W')
            StrikeMaterial.BRICKS -> BRICK_COLORS.getValue('B')
            StrikeMaterial.ICE -> ICE_COLORS.getValue('I')
        }

    /**
     * The twin-post dojo breaking stand (`38 x 18`) that holds the stack of layers next to the
     * fighter: dark stone feet on the floor with wooden side pillars.
     */
    fun stand(): PixelImage = sprite(STAND_ROWS, STAND_COLORS)

    /**
     * Star-shaped ki shockwave (`15 x 15`) painted at the point of impact when the fighter's punch
     * strikes the stack.
     */
    fun impactBurst(ki: Int): PixelImage {
        val size = 15
        val c = size / 2f
        val core = mix(ki, WHITE, 0.85f)
        val light = mix(ki, WHITE, 0.45f)
        val argb =
            IntArray(size * size) { i ->
                val x = (i % size) + 0.5f
                val y = (i / size) + 0.5f
                val dx = kotlin.math.abs(x - c)
                val dy = kotlin.math.abs(y - c)
                val d = hypot(dx, dy)
                val onRay =
                    (dx < 1.2f && dy < 6.8f) ||
                        (dy < 1.2f && dx < 6.8f) ||
                        kotlin.math.abs(dx - dy) < 1.0f && d < 5.8f
                when {
                    d <= 2.4f -> core
                    d <= 4.2f || (onRay && d <= 5.2f) -> light
                    onRay -> ki
                    else -> 0
                }
            }
        return PixelImage(size, size, argb)
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

    private val WHITE = argb(0xF8FAFF)

    // Wooden board layer (30 x 4 -> 32 x 6 with outline)
    private val BOARD_LAYER =
        listOf(
            "LLLLLLLLLLLLLLLLLLLLLLLLLLLLLL",
            "WWWWWWGWWWWWWWWWWGWWWWWWWWWWWW",
            "WWWWWWWWWWWWGWWWWWWWWWWGWWWWWW",
            "dddddddddddddddddddddddddddddd",
        )
    private val BOARD_LEFT =
        listOf("LLLLLLLLLLLLLLL", "WWWWWWGWWWWWWW.", "WWWWWWWWWWWWGW.", "dddddddddddddd.")
    private val BOARD_RIGHT =
        listOf(".LLLLLLLLLLLLLL", "WWWWGWWWWWWWWWW", ".WWWWWWWWGWWWWW", "ddddddddddddddd")
    private val BOARD_COLORS =
        mapOf(
            'L' to argb(0xF5CB86),
            'W' to argb(0xE5A95C),
            'G' to argb(0xC98B42),
            'd' to argb(0xAD712E),
        )

    // Clay brick layer (30 x 4 -> 32 x 6 with outline)
    private val BRICK_LAYER =
        listOf(
            "LLLLLLLLLLMMLLLLLLLLLLMMLLLLLL",
            "BBBBBBBBBBMMBBBBBBBBBBMMBBBBBB",
            "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBB",
            "ddddddddddMMddddddddddMMdddddd",
        )
    private val BRICK_LEFT =
        listOf("LLLLLLLLLLMMLLL", "BBBBBBBBBBMMBB.", "BBBBBBBBBBBBBB.", "ddddddddddMMdd.")
    private val BRICK_RIGHT =
        listOf(".LLLLLLLMMLLLLL", "BBBBBBBBMMBBBBB", ".BBBBBBBBBBBBBB", "ddddddddMMddddd")
    private val BRICK_COLORS =
        mapOf(
            'L' to argb(0xEC8672),
            'B' to argb(0xD45A46),
            'M' to argb(0xD8CFC4),
            'd' to argb(0x9E3828),
        )

    // Ice slab layer (30 x 4 -> 32 x 6 with outline)
    private val ICE_LAYER =
        listOf(
            "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
            "IIIIFFIIIIIIIIIIFFIIIIIIIIFFII",
            "IIIIIIIIIIFFIIIIIIIIIIFFIIIIII",
            "dddddddddddddddddddddddddddddd",
        )
    private val ICE_LEFT =
        listOf("FFFFFFFFFFFFFFF", "IIIIFFIIIIIIII.", "IIIIIIIIIIFFII.", "dddddddddddddd.")
    private val ICE_RIGHT =
        listOf(".FFFFFFFFFFFFFF", "IIFFIIIIIIIIFFI", ".IIIIIIIIIFFIII", "ddddddddddddddd")
    private val ICE_COLORS =
        mapOf('F' to argb(0xE4FAFF), 'I' to argb(0x68D4F8), 'd' to argb(0x389AC4))

    // Twin-post breaking stand (36 x 16 -> 38 x 18 with outline)
    private val STAND_ROWS =
        listOf(
            ".PPPP..........................PPPP.",
            ".PPPP..........................PPPP.",
            ".PPPP..........................PPPP.",
            ".PPPP..........................PPPP.",
            ".PPPP..........................PPPP.",
            ".PPPP..........................PPPP.",
            ".PPPP..........................PPPP.",
            ".PPPP..........................PPPP.",
            ".PPPP..........................PPPP.",
            ".PPPP..........................PPPP.",
            ".PPPP..........................PPPP.",
            ".PPPP..........................PPPP.",
            "SSSSSS........................SSSSSS",
            "SSSSSS........................SSSSSS",
            "ssssssSSSSSSSSSSSSSSSSSSSSSSSSssssss",
            "ssssssssssssssssssssssssssssssssssss",
        )
    private val STAND_COLORS =
        mapOf('P' to argb(0x8C5832), 'S' to argb(0x5E6472), 's' to argb(0x3E434E))
}
