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

/**
 * Where blows land in one fight, measured once from the pixel pictures, in stage pixels: how far
 * the fighter dashes so the fist or foot touches the opponent's outline, how far a rushing opponent
 * comes, where its attack starts and where it hits the fighter.
 */
data class FightGeometry(
    /** The fighter's dash for a punch, and where the fist meets the opponent. */
    val punchDx: Float,
    val punchAt: StagePoint,
    /** The same for a kick. */
    val kickDx: Float,
    val kickAt: StagePoint,
    /** A rushing opponent's (negative) dash, and where it meets the fighter. */
    val rushDx: Float,
    val rushAt: StagePoint,
    /** Where the opponent's attack leaves it. */
    val launch: StagePoint,
    /** Where the opponent's attack hits the fighter: the chest. */
    val target: StagePoint,
) {
    companion object {
        /** Top of the fighter's picture on the stage. */
        const val PLAYER_TOP = ArenaStagePixels.FOOT_Y - 48

        /** Top of the opponent's picture on the stage. */
        const val OPPONENT_TOP = ArenaStagePixels.FOOT_Y - OpponentPixels.SIZE

        private const val PLAYER_LEFT = ArenaStagePixels.PLAYER_LEFT
        private const val OPPONENT_LEFT = ArenaStagePixels.OPPONENT_LEFT

        /**
         * Measures the fighter's [punch], [kick] and [stance] pictures against the opponent's
         * [idle] and [attack] pictures; [launch] is where the attack leaves, in [attack]'s pixels.
         */
        fun of(
            punch: PixelImage,
            kick: PixelImage,
            stance: PixelImage,
            idle: PixelImage,
            attack: PixelImage,
            launch: StagePoint,
        ): FightGeometry {
            val (punchDx, punchAt) = strike(punch, idle)
            val (kickDx, kickAt) = strike(kick, idle)

            // the rushing opponent's front meets the fighter's front at the same height
            val (frontX, frontY) = attack.leftmost()
            val row = OPPONENT_TOP + frontY
            val fighterFront = stance.rightEdge(row - PLAYER_TOP) ?: stance.rightmost().first
            val meet = PLAYER_LEFT + fighterFront
            val rushDx = (meet - 1 - (OPPONENT_LEFT + frontX)).toFloat()

            return FightGeometry(
                punchDx = punchDx,
                punchAt = punchAt,
                kickDx = kickDx,
                kickAt = kickAt,
                rushDx = rushDx.coerceAtMost(0f),
                rushAt = StagePoint(meet.toFloat(), row.toFloat()),
                launch = StagePoint(OPPONENT_LEFT + launch.x, OPPONENT_TOP + launch.y),
                target = StagePoint(PLAYER_LEFT + 28f, PLAYER_TOP + 26f),
            )
        }

        /** The dash that brings the tip of [strike] one pixel into [opponent]'s outline. */
        private fun strike(strike: PixelImage, opponent: PixelImage): Pair<Float, StagePoint> {
            val (tipX, tipY) = strike.rightmost()
            val row = PLAYER_TOP + tipY
            val edge = opponent.leftEdge(row - OPPONENT_TOP) ?: opponent.leftmost().first
            val contact = OPPONENT_LEFT + edge
            val dx = contact + 1 - (PLAYER_LEFT + tipX)
            return dx.toFloat().coerceAtLeast(0f) to StagePoint(contact.toFloat(), row.toFloat())
        }

        private fun PixelImage.filled(x: Int, y: Int) =
            x in 0 until width && y in 0 until height && this[x, y] != 0

        /** The rightmost filled pixel, the highest one on a tie. */
        private fun PixelImage.rightmost(): Pair<Int, Int> {
            for (x in width - 1 downTo 0) for (y in 0 until height) if (filled(x, y)) return x to y
            return 0 to height / 2
        }

        /** The leftmost filled pixel, the highest one on a tie. */
        private fun PixelImage.leftmost(): Pair<Int, Int> {
            for (x in 0 until width) for (y in 0 until height) if (filled(x, y)) return x to y
            return 0 to height / 2
        }

        /** The leftmost filled column near [row], searching further up and down until found. */
        private fun PixelImage.leftEdge(row: Int): Int? {
            for (band in BANDS) {
                val x =
                    (0 until width).firstOrNull { x ->
                        (row - band..row + band).any { filled(x, it) }
                    }
                if (x != null) return x
            }
            return null
        }

        private fun PixelImage.rightEdge(row: Int): Int? {
            for (band in BANDS) {
                val x =
                    (width - 1 downTo 0).firstOrNull { x ->
                        (row - band..row + band).any { filled(x, it) }
                    }
                if (x != null) return x
            }
            return null
        }

        private val BANDS = listOf(2, 5, 9, 14)
    }
}
