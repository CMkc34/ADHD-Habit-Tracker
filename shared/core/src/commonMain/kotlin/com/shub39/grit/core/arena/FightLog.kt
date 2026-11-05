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

import kotlinx.datetime.LocalDate

/** Pure rules on the fight log ("store little, compute everything"). */
object FightLog {

    /** Opponents won against at least once. Records of unknown opponents are ignored. */
    fun beaten(records: List<FightRecord>): Set<Opponent> =
        records.filter { it.outcome == Outcome.WIN }.mapNotNull { it.opponent }.toSet()

    /** Fights started on [day], whatever their outcome; each one used a ticket. */
    fun fightsStartedOn(records: List<FightRecord>, day: LocalDate): Int =
        records.count { it.date == day }

    /**
     * The record for a fight that starts at [now] (epoch milliseconds); it counts as a loss until
     * [finish] is called. The start time is kept later than every fight in [records], so two fights
     * never share one.
     */
    fun start(
        opponent: Opponent,
        today: LocalDate,
        now: Long,
        records: List<FightRecord> = emptyList(),
    ): FightRecord =
        FightRecord(
            date = today,
            opponentId = opponent.id,
            outcome = Outcome.LOSS,
            startedAt = maxOf(now, (records.maxOfOrNull { it.startedAt } ?: 0L) + 1),
        )

    /** [records] with [fight] set to [outcome]. */
    fun finish(
        records: List<FightRecord>,
        fight: FightRecord,
        outcome: Outcome,
    ): List<FightRecord> =
        records.map { if (it.isSameFight(fight)) it.copy(outcome = outcome) else it }

    /**
     * Joins two logs, e.g. the device's and a restored backup's: every fight of either log is kept
     * once, so the ladder progress is the best of both. A fight found in both (same day, opponent
     * and start) keeps its better outcome, e.g. a backup taken during a fight still says LOSS.
     * Sorted by start time.
     */
    fun merge(a: List<FightRecord>, b: List<FightRecord>): List<FightRecord> =
        (a + b)
            .groupBy { Triple(it.date, it.opponentId, it.startedAt) }
            .map { (_, same) -> same.maxBy { it.outcome.rank } }
            .sortedWith(compareBy({ it.startedAt }, { it.date }, { it.opponentId }))

    private val Outcome.rank: Int
        get() =
            when (this) {
                Outcome.LOSS -> 0
                Outcome.DRAW -> 1
                Outcome.WIN -> 2
            }
}
