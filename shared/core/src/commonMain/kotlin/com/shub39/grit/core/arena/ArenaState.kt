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
package com.shub39.grit.core.arena

import com.shub39.grit.core.fighter.FighterStat
import com.shub39.grit.core.fighter.FighterState
import com.shub39.grit.core.habits.Habit
import com.shub39.grit.core.habits.HabitStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/** Where an opponent stands on the ladder for the player. */
enum class LadderStatus {
    /** The opponent below has not been beaten yet. */
    LOCKED,
    /** Unlocked but not beaten yet: the current challenge. */
    NEXT,
    /** Won against at least once; rematches are allowed. */
    BEATEN,
}

data class LadderEntry(val opponent: Opponent, val status: LadderStatus) {
    /** Whether the player may start a fight against [opponent]. */
    val canFight: Boolean
        get() = status != LadderStatus.LOCKED

    /** Protein shakes the first win against [opponent] still pays; 0 once it is beaten (#17). */
    val firstWinShakes: Int
        get() = if (status == LadderStatus.BEATEN) 0 else ArenaRewards.shakes(opponent)
}

/** Everything the arena tab needs, computed from the fight log and the habits. */
data class ArenaState(
    /** All opponents in ladder order. */
    val ladder: List<LadderEntry>,
    val tickets: TicketStatus,
    val player: Combatant,
    val today: LocalDate,
) {
    fun entry(opponent: Opponent): LadderEntry = ladder.first { it.opponent == opponent }

    /** Whether a fight against [opponent] may start now: unlocked and a ticket left. */
    fun canStart(opponent: Opponent): Boolean = tickets.available > 0 && entry(opponent).canFight

    /** The whole ladder has been beaten. */
    val isComplete: Boolean
        get() = ladder.all { it.status == LadderStatus.BEATEN }

    companion object {
        fun of(
            fights: List<FightRecord>,
            habits: List<Habit>,
            statuses: List<HabitStatus>,
            fighter: FighterState,
            fighterName: String,
            today: LocalDate,
        ): ArenaState {
            val beaten = FightLog.beaten(fights)
            return ArenaState(
                ladder = ladder(beaten),
                tickets =
                    FightTickets.status(
                        habits = habits,
                        statuses = statuses,
                        today = today,
                        fightsStartedToday = FightLog.fightsStartedOn(fights, today),
                    ),
                player = fighter.combatant(fighterName),
                today = today,
            )
        }

        fun ladder(beaten: Set<Opponent>): List<LadderEntry> =
            Ladder.opponents.map {
                val status =
                    when {
                        it in beaten -> LadderStatus.BEATEN
                        Ladder.isUnlocked(it, beaten) -> LadderStatus.NEXT
                        else -> LadderStatus.LOCKED
                    }
                LadderEntry(it, status)
            }
    }
}

/** The fighter's battle values (SPEC 9.4) from its rank and stat levels. */
fun FighterState.combatant(name: String): Combatant =
    Combatant.of(
        name = name,
        rankNumber = rank.number,
        strengthLevel = level(FighterStat.STRENGTH),
        speedLevel = level(FighterStat.SPEED),
        staminaLevel = level(FighterStat.STAMINA),
        focusLevel = level(FighterStat.FOCUS),
    )

/** A fight that has started and used its ticket; pass it back to [ArenaRepo.finishFight]. */
data class StartedFight(val record: FightRecord, val player: Combatant) {
    val opponent: Opponent
        get() = requireNotNull(record.opponent)
}

interface ArenaRepo {
    fun getArenaState(): Flow<ArenaState>

    /**
     * Starts a fight against [opponent]: writes it as a loss right away, so it uses a ticket and
     * leaving the app counts as a loss. `null` when the opponent is locked or no ticket is left.
     */
    suspend fun startFight(opponent: Opponent): StartedFight?

    /** Stores the real [outcome] of [fight]. */
    suspend fun finishFight(fight: StartedFight, outcome: Outcome)
}
