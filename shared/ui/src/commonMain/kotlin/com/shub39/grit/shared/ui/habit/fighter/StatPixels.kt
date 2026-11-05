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

import com.shub39.grit.core.fighter.FighterStat
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter.Companion.argb

/**
 * The four stats as small pixel icons in the fighter's style (#9): a fist for strength, a lightning
 * bolt for speed, a heart for stamina and an eye for focus. Simple shapes, shaded and outlined by
 * [PixelPainter]. Pure Kotlin, so it is unit tested.
 */
object StatPixels {
    const val SIZE = 16

    fun render(stat: FighterStat): PixelImage {
        val p = PixelPainter(SIZE)
        when (stat) {
            FighterStat.STRENGTH -> {
                p.poly(listOf(Pt(3, 8), Pt(13, 8), Pt(13, 12.5), Pt(3, 12.5)), Paint(SKIN, "palm"))
                listOf(4.4f, 6.8f, 9.2f, 11.6f).forEachIndexed { i, x ->
                    p.capsule(Pt(x, 5.2f), Pt(x, 8.5f), 1.3f, Paint(SKIN, "finger$i"))
                }
                p.capsule(Pt(3.6f, 10.6f), Pt(8.6f, 10.6f), 1.2f, Paint(SKIN, "thumb"))
                p.poly(listOf(Pt(4, 12.5), Pt(12, 12.5), Pt(12, 15), Pt(4, 15)), Paint(TAPE))
            }
            FighterStat.SPEED ->
                p.poly(
                    listOf(
                        Pt(9.5f, 1f),
                        Pt(3f, 9f),
                        Pt(7.5f, 9f),
                        Pt(5.5f, 15f),
                        Pt(13f, 6.5f),
                        Pt(8.5f, 6.5f),
                        Pt(11f, 1f),
                    ),
                    Paint(YELLOW),
                )
            FighterStat.STAMINA -> {
                val heart = Paint(RED, "heart")
                p.circle(Pt(5.3f, 6f), 3.2f, heart)
                p.circle(Pt(10.7f, 6f), 3.2f, heart)
                p.poly(listOf(Pt(2.2f, 7f), Pt(13.8f, 7f), Pt(8f, 14.5f)), heart)
            }
            FighterStat.FOCUS -> {
                p.ellipse(Pt(8f, 8f), 6.8f, 4.3f, Paint(WHITE))
                p.circle(Pt(8f, 8f), 2.8f, Paint(VIOLET))
                p.circle(Pt(8f, 8f), 1.1f, Paint(PUPIL, "pupil"))
            }
        }
        return PixelImage(SIZE, SIZE, p.render())
    }

    private val SKIN = argb(0xF2C7A5)
    private val TAPE = argb(0xC84B3A)
    private val YELLOW = argb(0xFFC93C)
    private val RED = argb(0xE85A6A)
    private val WHITE = argb(0xF4F1EA)
    private val VIOLET = argb(0x7B4FC9)
    private val PUPIL = argb(0x2A2238)
}
