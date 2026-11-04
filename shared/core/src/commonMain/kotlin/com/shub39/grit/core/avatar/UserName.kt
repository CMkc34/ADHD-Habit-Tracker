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
package com.shub39.grit.core.avatar

/** The user's own name, which the plant uses in its greetings (#139). */
object UserName {
    /** The longest name that still fits in a header. */
    const val MAX_LENGTH = 24

    /** Trimmed and cut to [MAX_LENGTH]; empty means no name. */
    fun clean(raw: String): String = raw.trim().take(MAX_LENGTH).trim()
}
