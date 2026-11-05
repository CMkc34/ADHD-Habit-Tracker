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

import com.shub39.grit.core.fighter.FighterItems
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter.Companion.argb

/**
 * The training-ground props from the shop (#7) as small pixel icons, in the same style as the
 * fighter: simple shapes, shaded and outlined by [PixelPainter]. The training ground scene (#8)
 * places them; until then the shop shows them below the fighter. Pure Kotlin, so it is unit tested.
 */
object PropPixels {
    const val SIZE = 24

    /** The icon of the prop [id] from [FighterItems.props], `null` for anything else. */
    fun render(id: String): PixelImage? {
        val p = PixelPainter(SIZE)
        when (id) {
            FighterItems.MAT -> {
                p.poly(listOf(Pt(5, 14), Pt(21, 14), Pt(19, 21), Pt(3, 21)), Paint(BLUE, "mat"))
                p.poly(listOf(Pt(11, 14), Pt(14, 14), Pt(13.5, 21), Pt(10.5, 21)), Paint(WHITE))
            }
            FighterItems.WATER_BOTTLE -> {
                p.capsule(Pt(12, 10), Pt(12, 19), 4f, Paint(AQUA))
                p.poly(listOf(Pt(10, 3), Pt(14, 3), Pt(14, 7), Pt(10, 7)), Paint(WHITE))
                p.poly(listOf(Pt(8, 12), Pt(16, 12), Pt(16, 15), Pt(8, 15)), Paint(BLUE))
            }
            FighterItems.PUNCHING_BAG -> {
                p.capsule(Pt(12, 0), Pt(12, 5), 0.7f, Paint(STEEL))
                p.capsule(Pt(12, 8), Pt(12, 17), 5f, Paint(RED))
                p.poly(listOf(Pt(7, 11), Pt(17, 11), Pt(17, 13), Pt(7, 13)), Paint(INK_SOFT))
            }
            FighterItems.DUMBBELLS -> {
                p.capsule(Pt(5, 16), Pt(19, 16), 1f, Paint(STEEL))
                p.ellipse(Pt(6.5, 16), 2.2f, 4.5f, Paint(INK_SOFT, "plate_l"))
                p.ellipse(Pt(17.5, 16), 2.2f, 4.5f, Paint(INK_SOFT, "plate_r"))
            }
            FighterItems.WOODEN_DUMMY -> {
                p.capsule(Pt(12, 8), Pt(12, 21), 3f, Paint(WOOD))
                p.circle(Pt(12, 5), 3f, Paint(WOOD))
                p.capsule(Pt(5, 11), Pt(19, 11), 1f, Paint(WOOD_LIGHT))
                p.capsule(Pt(12, 16), Pt(18, 19), 1f, Paint(WOOD_LIGHT))
            }
            FighterItems.BANNER -> {
                p.capsule(Pt(5, 1), Pt(5, 22), 0.8f, Paint(STEEL))
                p.poly(
                    listOf(Pt(6, 3), Pt(20, 3), Pt(20, 16), Pt(13, 12.5), Pt(6, 16)),
                    Paint(PURPLE),
                )
                p.circle(Pt(13, 8), 2.2f, Paint(GOLD))
            }
            FighterItems.TROPHY_SHELF -> {
                p.poly(listOf(Pt(2, 10), Pt(22, 10), Pt(22, 12), Pt(2, 12)), Paint(WOOD, "shelf"))
                p.poly(listOf(Pt(2, 20), Pt(22, 20), Pt(22, 22), Pt(2, 22)), Paint(WOOD, "shelf"))
                trophy(p, x = 7f, shelf = 10f, Paint(GOLD, "cup_gold"))
                trophy(p, x = 16f, shelf = 20f, Paint(SILVER, "cup_silver"))
                p.circle(Pt(16.5, 7.5), 2.2f, Paint(RED, "medal"))
            }
            FighterItems.GONG -> {
                p.capsule(Pt(4, 4), Pt(4, 22), 1f, Paint(WOOD, "frame"))
                p.capsule(Pt(20, 4), Pt(20, 22), 1f, Paint(WOOD, "frame"))
                p.capsule(Pt(3, 3.5), Pt(21, 3.5), 1.2f, Paint(WOOD, "frame"))
                p.circle(Pt(12, 13), 6f, Paint(GOLD))
                p.circle(Pt(12, 13), 2f, Paint(BRONZE))
            }
            else -> return null
        }
        return PixelImage(SIZE, SIZE, p.render())
    }

    /** A cup standing on the shelf board whose top is at [shelf]. */
    private fun trophy(p: PixelPainter, x: Float, shelf: Float, paint: Paint) {
        val top = shelf - 7f
        val cup = top + 3.5f
        p.poly(listOf(Pt(x - 3, top), Pt(x + 3, top), Pt(x + 1.5f, cup), Pt(x - 1.5f, cup)), paint)
        p.capsule(Pt(x, cup), Pt(x, shelf - 2f), 0.6f, paint)
        val base = shelf - 1.5f
        p.poly(listOf(Pt(x - 2, base), Pt(x + 2, base), Pt(x + 2, shelf), Pt(x - 2, shelf)), paint)
    }

    private val WHITE = argb(0xF4F1EA)
    private val BLUE = argb(0x4A78C8)
    private val AQUA = argb(0x6EC6E8)
    private val RED = argb(0xD9524A)
    private val PURPLE = argb(0x8E62C4)
    private val GOLD = argb(0xE8BE4A)
    private val BRONZE = argb(0xC08A3A)
    private val SILVER = argb(0xC7CCD6)
    private val STEEL = argb(0x8A8FA0)
    private val INK_SOFT = argb(0x3A3B4E)
    private val WOOD = argb(0xA86B3C)
    private val WOOD_LIGHT = argb(0xC88A52)
}
