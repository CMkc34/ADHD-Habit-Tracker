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

/**
 * One run of a [MiniGame] in the run log (SPEC 10). Runs left today and the high scores are
 * computed from the log by [RunLog].
 *
 * A run is written with score 0 when it starts, so it uses one of the day's runs at once, and gets
 * its real [score] when it ends. A run left in the middle keeps 0.
 */
data class RunRecord(
    /** The day the run started; it uses a run of that day. */
    val date: LocalDate,
    /** [MiniGame.id]; kept as text so ids from a newer app survive a backup. */
    val gameId: String,
    val score: Int,
    /** When the run started, in epoch milliseconds; finds the record to update at the end. */
    val startedAt: Long,
) {
    val game: MiniGame?
        get() = MiniGame.byId(gameId)

    /** Whether [other] is this run, maybe with another score. */
    fun isSameRun(other: RunRecord): Boolean =
        date == other.date && gameId == other.gameId && startedAt == other.startedAt
}

/** Store entries in the form `"yyyy-MM-dd|gameId|score|startedAt"`. */
object RunRecordCodec {
    private const val SEPARATOR = "|"

    fun encode(record: RunRecord): String =
        listOf(record.date, record.gameId, record.score, record.startedAt).joinToString(SEPARATOR)

    /** `null` for a broken entry. A negative score reads as 0. */
    fun decode(value: String): RunRecord? {
        val parts = value.split(SEPARATOR)
        if (parts.size != 4) return null
        val date = runCatching { LocalDate.parse(parts[0]) }.getOrNull() ?: return null
        val gameId = parts[1].takeIf { it.isNotBlank() } ?: return null
        val score = parts[2].toIntOrNull()?.coerceAtLeast(0) ?: return null
        val startedAt = parts[3].toLongOrNull() ?: return null
        return RunRecord(date, gameId, score, startedAt)
    }
}
