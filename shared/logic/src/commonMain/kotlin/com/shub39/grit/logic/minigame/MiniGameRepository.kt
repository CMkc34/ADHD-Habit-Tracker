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

import com.shub39.grit.core.arena.ArenaStore
import com.shub39.grit.core.arena.FightLog
import com.shub39.grit.core.arena.FightRecord
import com.shub39.grit.core.avatar.AvatarRepo
import com.shub39.grit.core.avatar.AvatarState
import com.shub39.grit.core.minigame.MiniGame
import com.shub39.grit.core.minigame.MiniGameRepo
import com.shub39.grit.core.minigame.MiniGameState
import com.shub39.grit.core.minigame.MiniGameStore
import com.shub39.grit.core.minigame.RunLog
import com.shub39.grit.core.minigame.RunRecord
import com.shub39.grit.core.minigame.StartedRun
import com.shub39.grit.core.now
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.Single

/**
 * Combines the run log, the fighter and the arena progress into the [MiniGameState] of the Games
 * tab (SPEC 10).
 */
@Single(binds = [MiniGameRepo::class])
class MiniGameRepository(
    private val store: MiniGameStore,
    private val avatarRepo: AvatarRepo,
    private val arenaStore: ArenaStore,
) : MiniGameRepo {

    // two quick taps must not start two runs
    private val startLock = Mutex()

    override fun getMiniGameState(): Flow<MiniGameState> =
        combine(store.getRuns(), avatarRepo.getAvatarState(), arenaStore.getFights(), ::state)

    private fun state(runs: List<RunRecord>, avatar: AvatarState, fights: List<FightRecord>) =
        MiniGameState.of(
            runs = runs,
            fighter = avatar.fighter,
            today = LocalDate.now(),
            beaten = FightLog.beaten(fights),
        )

    override suspend fun startRun(game: MiniGame): StartedRun? =
        startLock.withLock {
            val state = getMiniGameState().first()
            if (!state.canStart(game)) return@withLock null

            val record =
                RunLog.start(
                    game = game,
                    today = state.today,
                    now = Clock.System.now().toEpochMilliseconds(),
                    records = store.getRuns().first(),
                )
            store.addRun(record)
            StartedRun(record, state.stats, state.opponents)
        }

    override suspend fun finishRun(run: StartedRun, score: Int): Boolean {
        val finished = run.record.copy(score = score.coerceAtLeast(0))
        val newBest = RunLog.isNewBest(store.getRuns().first(), run.record, finished.score)
        store.updateRun(finished)
        return newBest
    }
}
