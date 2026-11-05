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

import com.shub39.grit.core.arena.ArenaRepo
import com.shub39.grit.core.arena.ArenaState
import com.shub39.grit.core.arena.ArenaStore
import com.shub39.grit.core.arena.FightLog
import com.shub39.grit.core.arena.Opponent
import com.shub39.grit.core.arena.Outcome
import com.shub39.grit.core.arena.StartedFight
import com.shub39.grit.core.avatar.AvatarRepo
import com.shub39.grit.core.habits.HabitRepo
import com.shub39.grit.core.now
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.Single

/** Combines the fight log, the routines and the fighter into the [ArenaState] (SPEC 9.5, 9.6). */
@Single(binds = [ArenaRepo::class])
class ArenaRepository(
    private val store: ArenaStore,
    private val habitRepo: HabitRepo,
    private val avatarRepo: AvatarRepo,
) : ArenaRepo {

    // two quick taps must not start two fights on one ticket
    private val startLock = Mutex()

    override fun getArenaState(): Flow<ArenaState> =
        combine(
            store.getFights(),
            habitRepo.getHabitsWithAnalytics(),
            avatarRepo.getAvatarState(),
        ) { fights, habits, avatar ->
            ArenaState.of(
                fights = fights,
                habits = habits.map { it.habit },
                statuses = habits.flatMap { it.statuses },
                fighter = avatar.fighter,
                fighterName = avatar.name,
                today = LocalDate.now(),
            )
        }

    override suspend fun startFight(opponent: Opponent): StartedFight? =
        startLock.withLock {
            val state = getArenaState().first()
            if (!state.canStart(opponent)) return@withLock null

            val record =
                FightLog.start(
                    opponent = opponent,
                    today = state.today,
                    now = Clock.System.now().toEpochMilliseconds(),
                    records = store.getFights().first(),
                )
            store.addFight(record)
            StartedFight(record, state.player)
        }

    override suspend fun finishFight(fight: StartedFight, outcome: Outcome) {
        store.updateFight(fight.record.copy(outcome = outcome))
    }
}
