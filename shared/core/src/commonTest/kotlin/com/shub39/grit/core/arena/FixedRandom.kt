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
package com.shub39.grit.core.arena

import kotlin.random.Random

/** A roll that never dodges, never crits and never makes the CPU guard. */
internal const val MISS = 0.99

/** A damage-factor roll that gives exactly 1.0 times attack. */
internal const val MID = 0.5

/** Returns the given doubles in order, then repeats the last one. Only nextDouble() works. */
internal class FixedRandom(private vararg val values: Double) : Random() {
    private var index = 0

    override fun nextDouble(): Double = values[minOf(index++, values.lastIndex)]

    override fun nextBits(bitCount: Int): Int = error("the engine must only use nextDouble()")
}
