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
package com.shub39.grit.logic.arena

import com.shub39.grit.core.arena.LadderStatus
import com.shub39.grit.core.arena.Opponent
import com.shub39.grit.core.arena.Outcome
import com.shub39.grit.core.avatar.AvatarData
import com.shub39.grit.core.avatar.AvatarRepo
import com.shub39.grit.core.avatar.AvatarState
import com.shub39.grit.core.avatar.PurchaseCheck
import com.shub39.grit.core.fighter.FighterCharacter
import com.shub39.grit.core.fighter.FighterColors
import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.core.fighter.FighterStat
import com.shub39.grit.core.fighter.FighterState
import com.shub39.grit.core.now
import com.shub39.grit.logic.avatar.FakeHabitRepo
import com.shub39.grit.logic.avatar.routine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

/** A rookie called Kai; only [getAvatarState] is used by the arena. */
private class FakeAvatarRepo : AvatarRepo {
    override fun getAvatarState(): Flow<AvatarState> =
        flowOf(
            AvatarState(
                fighter = FighterState(0, FighterRank.ROOKIE, FighterMood.READY),
                energy = 0,
                data = AvatarData(name = "Kai"),
            )
        )

    override suspend fun buy(itemId: String) = PurchaseCheck.OK

    override suspend fun rename(name: String) = Unit

    override suspend fun setUserName(name: String) = Unit

    override suspend fun chooseFighter(character: FighterCharacter, colors: FighterColors) = Unit

    override suspend fun wear(itemId: String, worn: Boolean) = Unit

    override suspend fun markRankSeen(rank: FighterRank) = Unit

    override suspend fun markStatLevelsSeen(levels: Map<FighterStat, Int>) = Unit
}

class ArenaRepositoryTest {

    private val today = LocalDate.now()
    private val store = FakeArenaStore()
    private val habits = FakeHabitRepo((1L..3L).map(::routine))
    private val repository = ArenaRepository(store, habits, FakeAvatarRepo())

    @Test
    fun `no fight without a ticket`() = runTest {
        assertNull(repository.startFight(Opponent.SNOOZE_BUTTON))
        assertEquals(emptyList(), store.fights.value)
    }

    @Test
    fun `no fight against a locked opponent`() = runTest {
        habits.checkIn(1, today)

        assertNull(repository.startFight(Opponent.COUCH_GREMLIN))
    }

    @Test
    fun `starting uses the ticket at once and counts as a loss`() = runTest {
        habits.checkIn(1, today)

        val fight = assertNotNull(repository.startFight(Opponent.SNOOZE_BUTTON))

        assertEquals(Outcome.LOSS, store.fights.value.single().outcome)
        assertEquals(0, repository.getArenaState().first().tickets.available)
        assertEquals("Kai", fight.player.name)
        // leaving now: no free retry
        assertNull(repository.startFight(Opponent.SNOOZE_BUTTON))
    }

    @Test
    fun `finishing a fight stores the real outcome and opens the next opponent`() = runTest {
        habits.checkIn(1, today)
        habits.checkIn(2, today)

        val lost = assertNotNull(repository.startFight(Opponent.SNOOZE_BUTTON))
        repository.finishFight(lost, Outcome.LOSS)
        val won = assertNotNull(repository.startFight(Opponent.SNOOZE_BUTTON))
        repository.finishFight(won, Outcome.WIN)

        assertEquals(listOf(Outcome.LOSS, Outcome.WIN), store.fights.value.map { it.outcome })
        val state = repository.getArenaState().first()
        assertEquals(LadderStatus.BEATEN, state.entry(Opponent.SNOOZE_BUTTON).status)
        assertEquals(LadderStatus.NEXT, state.entry(Opponent.COUCH_GREMLIN).status)
        assertEquals(0, state.tickets.available)
    }
}
