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
package com.shub39.grit.core.motivation

import kotlinx.datetime.LocalDate

/**
 * The one-time setup on the very first start (#139): the plant asks for the user's name before its
 * welcome greeting. Installs that were already in use never see it.
 */
object FirstLaunchSetup {
    /**
     * Whether the setup is due: not done yet, the plant has never greeted and there are no
     * routines, so this is a fresh install. Once it is not due, it is marked done and never comes
     * back.
     */
    fun isDue(done: Boolean, lastGreeting: LocalDate?, hasRoutines: Boolean): Boolean =
        !done && lastGreeting == null && !hasRoutines
}
