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
package com.shub39.grit.core.activity

import com.shub39.grit.core.fighter.FighterItems
import com.shub39.grit.core.avatar.Purchase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate

/** [ActivityPlanner] with [Reward]s, e.g. water for arena wins (#17). */
class RewardPlannerTest {

    private val start = LocalDate(2026, 10, 1)

    private fun day(n: Int) = LocalDate.fromEpochDays(start.toEpochDays() + n)

    private fun plan(
        activeDays: Set<LocalDate>,
        today: LocalDate,
        purchases: List<Purchase> = emptyList(),
        rewards: List<Reward>,
    ) =
        ActivityPlanner.plan(
            activeDays = activeDays,
            startDate = start,
            today = today,
            purchases = purchases,
            rewards = rewards,
        )

    @Test
    fun `without rewards the result is the same as before`() {
        val gi = Purchase(FighterItems.GI_BLUE, day(5), 10)
        val scenarios =
            listOf(
                Triple((0..4).map(::day).toSet(), day(4), emptyList()),
                Triple((0..6).map(::day).toSet() + day(8), day(8), emptyList()),
                Triple((0..3).map(::day).toSet() + day(5), day(5), emptyList()),
                Triple((0..12).map(::day).toSet() + day(15), day(16), listOf(gi)),
                Triple(emptySet(), day(3), listOf(gi)),
            )

        scenarios.forEach { (active, today, purchases) ->
            val before =
                ActivityPlanner.plan(
                    activeDays = active,
                    startDate = start,
                    today = today,
                    purchases = purchases,
                )
            val after = plan(active, today, purchases, rewards = emptyList())

            assertEquals(before, after)
            assertEquals(emptyList(), after.rewards)
        }
    }

    @Test
    fun `a reward is credited on its day`() {
        val reward = Reward(day(2), 15)

        val report = plan((0..4).map(::day).toSet(), day(4), rewards = listOf(reward))

        assertEquals(5 + 15, report.energy)
        assertEquals(listOf(reward), report.rewards)
    }

    @Test
    fun `a reward after today is not credited yet`() {
        val report =
            plan((0..4).map(::day).toSet(), day(4), rewards = listOf(Reward(day(5), 15)))

        assertEquals(5, report.energy)
        assertEquals(emptyList(), report.rewards)
    }

    @Test
    fun `a reward before the start date counts on the start date`() {
        val report =
            plan(setOf(day(0)), day(0), rewards = listOf(Reward(day(-3), 5)))

        assertEquals(1 + 5, report.energy)
        assertEquals(listOf(Reward(day(0), 5)), report.rewards)
    }

    @Test
    fun `a reward is credited before the purchases of its day`() {
        // 2 days give 2 water; without the reward the purchase would be cut to 0
        val purchase = Purchase(FighterItems.GI_BLUE, day(1), 10)

        val report =
            plan(
                setOf(day(0), day(1)),
                day(1),
                purchases = listOf(purchase),
                rewards = listOf(Reward(day(1), 10)),
            )

        assertEquals(2 + 10 - 10, report.energy)
    }

    @Test
    fun `a reward makes a later freeze affordable`() {
        val active = (0..2).map(::day).toSet() + day(4)

        val without = plan(active, day(4), rewards = emptyList())
        val with = plan(active, day(4), rewards = listOf(Reward(day(1), 5)))

        assertEquals(emptySet(), without.frozenDays)
        assertEquals(1, without.currentStreak)
        assertEquals(setOf(day(3)), with.frozenDays)
        assertEquals(4, with.currentStreak)
        assertEquals(3 + 5 - ActivityPlanner.FREEZE_COST + 1, with.energy)
    }

    @Test
    fun `rewards on one day add up`() {
        val report =
            plan(setOf(day(0)), day(0), rewards = listOf(Reward(day(0), 5), Reward(day(0), 10)))

        assertEquals(1 + 15, report.energy)
        assertEquals(2, report.rewards.size)
    }
}
