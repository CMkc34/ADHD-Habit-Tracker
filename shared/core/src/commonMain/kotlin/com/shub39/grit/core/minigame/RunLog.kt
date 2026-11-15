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
package com.shub39.grit.core.minigame

import kotlinx.datetime.LocalDate

/** Everything the Games tab needs, computed from the run log (SPEC 10). */
object RunLog {

    fun runsStartedOn(records: List<RunRecord>, game: MiniGame, day: LocalDate): Int =
        records.count { it.gameId == game.id && it.date == day }

    /** Free runs of [game] still left on [today], never negative. */
    fun runsLeft(records: List<RunRecord>, game: MiniGame, today: LocalDate): Int =
        (MiniGameRules.RUNS_PER_DAY - runsStartedOn(records, game, today)).coerceAtLeast(0)

    /** The high score of [game], 0 before the first finished run. */
    fun best(records: List<RunRecord>, game: MiniGame): Int =
        records.filter { it.gameId == game.id }.maxOfOrNull { it.score } ?: 0

    /**
     * A new run of [game] with score 0. The start time is at least one millisecond after every
     * stored run, so two runs never share it.
     */
    fun start(
        game: MiniGame,
        today: LocalDate,
        now: Long,
        records: List<RunRecord> = emptyList(),
    ): RunRecord =
        RunRecord(
            date = today,
            gameId = game.id,
            score = 0,
            startedAt = maxOf(now, (records.maxOfOrNull { it.startedAt } ?: 0L) + 1),
        )

    /** Whether [score] beats every other run of [game] in [records] (and is more than 0). */
    fun isNewBest(records: List<RunRecord>, run: RunRecord, score: Int): Boolean {
        val others = records.filter { it.gameId == run.gameId && !it.isSameRun(run) }
        return score > 0 && score > (others.maxOfOrNull { it.score } ?: 0)
    }

    /** Both logs together, for a backup import; the same run keeps its higher score. */
    fun merge(a: List<RunRecord>, b: List<RunRecord>): List<RunRecord> =
        (a + b)
            .groupBy { Triple(it.date, it.gameId, it.startedAt) }
            .map { (_, same) -> same.maxBy { it.score } }
            .sortedWith(compareBy({ it.startedAt }, { it.date }, { it.gameId }))
}
