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
package com.shub39.grit.shared.ui.habit.avatar

import com.shub39.grit.core.avatar.AvatarData
import com.shub39.grit.core.avatar.AvatarState
import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.core.fighter.FighterStat
import com.shub39.grit.core.fighter.FighterState
import com.shub39.grit.shared.ui.habit.HabitState
import com.shub39.grit.shared.ui.habit.motivation.BonusPopupState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate

class FighterNoticeTest {

    private val allSeenAtOne = FighterStat.entries.associateWith { 1 }

    private fun avatar(
        activeDays: Int = 14,
        seenStage: Int = FighterRank.BRAWLER.number,
        xp: Map<FighterStat, Int> = emptyMap(),
    ) =
        AvatarState(
            fighter =
                FighterState(
                    activeDays = activeDays,
                    rank = FighterRank.forActiveDays(activeDays),
                    mood = FighterMood.READY,
                    xp = xp,
                ),
            energy = 0,
            data = AvatarData(seenStage = seenStage, seenStatLevels = allSeenAtOne),
            today = LocalDate(2026, 11, 5),
        )

    @Test
    fun `nothing new gives no notice`() {
        assertNull(FighterNotice.of(HabitState(avatar = avatar())))
        assertNull(FighterNotice.of(HabitState()))
    }

    @Test
    fun `a new rank comes before new stat levels`() {
        val avatar =
            avatar(
                activeDays = 21,
                seenStage = FighterRank.BRAWLER.number,
                xp = mapOf(FighterStat.STRENGTH to 5),
            )

        assertEquals(
            FighterNotice.NewRank(avatar, FighterRank.CONTENDER),
            FighterNotice.of(HabitState(avatar = avatar)),
        )
    }

    @Test
    fun `new stat levels are announced together`() {
        val avatar = avatar(xp = mapOf(FighterStat.STRENGTH to 15, FighterStat.FOCUS to 5))

        assertEquals(
            FighterNotice.NewStatLevels(
                avatar,
                mapOf(FighterStat.STRENGTH to 3, FighterStat.FOCUS to 2),
            ),
            FighterNotice.of(HabitState(avatar = avatar)),
        )
    }

    @Test
    fun `the notice waits for other pop-ups`() {
        val avatar = avatar(xp = mapOf(FighterStat.STRENGTH to 15))

        assertNull(FighterNotice.of(HabitState(avatar = avatar, bonusPopup = BonusPopupState(3))))
        assertNull(FighterNotice.of(HabitState(avatar = avatar, nameSetup = true)))
    }
}
