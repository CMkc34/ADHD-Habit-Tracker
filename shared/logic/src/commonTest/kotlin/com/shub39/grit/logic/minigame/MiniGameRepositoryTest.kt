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
package com.shub39.grit.logic.minigame

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
import com.shub39.grit.core.minigame.MiniGame
import com.shub39.grit.core.minigame.MiniGameRules
import com.shub39.grit.core.minigame.MiniGameStore
import com.shub39.grit.core.minigame.RunRecord
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest

class FakeMiniGameStore(runs: List<RunRecord> = emptyList()) : MiniGameStore {
    val runs = MutableStateFlow(runs)

    override fun getRuns(): Flow<List<RunRecord>> = runs

    override suspend fun addRun(record: RunRecord) = runs.update { it + record }

    override suspend fun updateRun(record: RunRecord) =
        runs.update { list -> list.map { if (it.isSameRun(record)) record else it } }

    override suspend fun replaceAll(records: List<RunRecord>) = runs.update { records }
}

/** A fighter with Strength level 3; only [getAvatarState] is used by the games. */
private class FakeAvatarRepo : AvatarRepo {
    override fun getAvatarState(): Flow<AvatarState> =
        flowOf(
            AvatarState(
                fighter =
                    FighterState(
                        0,
                        FighterRank.ROOKIE,
                        FighterMood.READY,
                        xp = mapOf(FighterStat.STRENGTH to 15),
                    ),
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

class MiniGameRepositoryTest {

    private val store = FakeMiniGameStore()
    private val repository = MiniGameRepository(store, FakeAvatarRepo())

    @Test
    fun `three free runs per game, then none`() = runTest {
        repeat(MiniGameRules.RUNS_PER_DAY) {
            assertNotNull(repository.startRun(MiniGame.KI_BARRAGE))
        }
        assertNull(repository.startRun(MiniGame.KI_BARRAGE))

        val state = repository.getMiniGameState().first()
        assertEquals(0, state.entry(MiniGame.KI_BARRAGE).runsLeft)
        // the other games keep their own runs
        assertEquals(3, state.entry(MiniGame.BOARD_BREAK).runsLeft)
        assertNotNull(repository.startRun(MiniGame.BOARD_BREAK))
    }

    @Test
    fun `a run is stored with score zero at the start and gets its score at the end`() = runTest {
        val run = assertNotNull(repository.startRun(MiniGame.CLUTTER_SMASH))
        assertEquals(0, store.runs.value.single().score)
        assertEquals(3, run.stats.strength)

        assertTrue(repository.finishRun(run, 420))
        assertEquals(420, store.runs.value.single().score)
        val state = repository.getMiniGameState().first()
        assertEquals(420, state.entry(MiniGame.CLUTTER_SMASH).best)
    }

    @Test
    fun `only a better score is a new best`() = runTest {
        val first = assertNotNull(repository.startRun(MiniGame.BOARD_BREAK))
        assertTrue(repository.finishRun(first, 12))
        val second = assertNotNull(repository.startRun(MiniGame.BOARD_BREAK))
        assertFalse(repository.finishRun(second, 12))
        val third = assertNotNull(repository.startRun(MiniGame.BOARD_BREAK))
        assertTrue(repository.finishRun(third, 13))
        assertEquals(3, store.runs.value.map { it.startedAt }.distinct().size)
    }
}
