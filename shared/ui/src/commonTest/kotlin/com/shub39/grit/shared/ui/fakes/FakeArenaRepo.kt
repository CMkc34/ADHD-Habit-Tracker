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
package com.shub39.grit.shared.ui.fakes

import com.shub39.grit.core.arena.ArenaRepo
import com.shub39.grit.core.arena.ArenaState
import com.shub39.grit.core.arena.Combatant
import com.shub39.grit.core.arena.FightLog
import com.shub39.grit.core.arena.FightRecord
import com.shub39.grit.core.arena.FightTickets
import com.shub39.grit.core.arena.Opponent
import com.shub39.grit.core.arena.Outcome
import com.shub39.grit.core.arena.StartedFight
import com.shub39.grit.core.arena.TicketStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

/** Arena storage in memory: [earned] tickets today and the fight log in [fights]. */
class FakeArenaRepo(
    earned: Int = 3,
    private val player: Combatant =
        Combatant("Kai", maxHp = 90, attack = 12, speed = 3, focus = 3),
) : ArenaRepo {
    val today = LocalDate(2026, 11, 5)
    val fights = MutableStateFlow<List<FightRecord>>(emptyList())
    val earned = MutableStateFlow(earned)

    override fun getArenaState(): Flow<ArenaState> =
        combine(fights, this.earned) { fights, earned ->
            ArenaState(
                ladder = ArenaState.ladder(FightLog.beaten(fights)),
                tickets =
                    TicketStatus(
                        earned = earned,
                        available =
                            FightTickets.available(earned, FightLog.fightsStartedOn(fights, today)),
                    ),
                player = player,
                today = today,
            )
        }

    override suspend fun startFight(opponent: Opponent): StartedFight? {
        if (!getArenaState().first().canStart(opponent)) return null
        val record = FightLog.start(opponent, today, now = 1L, records = fights.value)
        fights.update { it + record }
        return StartedFight(record, player)
    }

    override suspend fun finishFight(fight: StartedFight, outcome: Outcome) {
        fights.update { FightLog.finish(it, fight.record, outcome) }
    }
}
