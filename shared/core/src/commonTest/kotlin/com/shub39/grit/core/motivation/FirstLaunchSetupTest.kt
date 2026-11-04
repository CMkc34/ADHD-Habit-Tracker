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

import com.shub39.grit.core.avatar.AvatarData
import com.shub39.grit.core.avatar.UserName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate

class FirstLaunchSetupTest {

    @Test
    fun `a fresh install gets the setup`() {
        assertTrue(FirstLaunchSetup.isDue(done = false, lastGreeting = null, hasRoutines = false))
    }

    @Test
    fun `an install in use or a finished setup does not`() {
        assertFalse(FirstLaunchSetup.isDue(done = true, lastGreeting = null, hasRoutines = false))
        assertFalse(
            FirstLaunchSetup.isDue(
                done = false,
                lastGreeting = LocalDate(2026, 11, 1),
                hasRoutines = false,
            )
        )
        assertFalse(FirstLaunchSetup.isDue(done = false, lastGreeting = null, hasRoutines = true))
    }

    @Test
    fun `names are trimmed and cut`() {
        assertEquals("Lena", UserName.clean("  Lena "))
        assertEquals("", UserName.clean("   "))
        assertEquals(UserName.MAX_LENGTH, UserName.clean("x".repeat(40)).length)
    }

    @Test
    fun `a restored backup fills in a missing user name only`() {
        assertEquals("Lena", AvatarData().mergedWith(AvatarData(userName = "Lena")).userName)
        assertEquals(
            "Mia",
            AvatarData(userName = "Mia").mergedWith(AvatarData(userName = "Lena")).userName,
        )
    }
}
