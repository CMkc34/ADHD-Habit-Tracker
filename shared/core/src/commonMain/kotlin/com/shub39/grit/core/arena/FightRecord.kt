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

/**
 * One fight in the arena's fight log (SPEC 9.8). Everything else (ladder progress, tickets used
 * today) is computed from the log by [FightLog].
 *
 * A fight is written with [Outcome.LOSS] when it starts, so it uses its ticket at once, and gets
 * its real [outcome] when it ends. A fight left in the middle stays a loss.
 */
data class FightRecord(
    /** The day the fight started; it uses a ticket of that day. */
    val date: LocalDate,
    /** [Opponent.id]; kept as text so ids from a newer app survive a backup. */
    val opponentId: String,
    val outcome: Outcome,
    /**
     * When the fight started, in epoch milliseconds. Tells apart two fights against the same
     * opponent on one day and finds the record to update when a fight ends.
     */
    val startedAt: Long,
) {
    val opponent: Opponent?
        get() = Opponent.byId(opponentId)

    /** Whether [other] is this fight, maybe with another outcome. */
    fun isSameFight(other: FightRecord): Boolean =
        date == other.date && opponentId == other.opponentId && startedAt == other.startedAt
}

/** Store entries in the form `"yyyy-MM-dd|opponentId|OUTCOME|startedAt"`. */
object FightRecordCodec {
    private const val SEPARATOR = "|"

    fun encode(record: FightRecord): String =
        listOf(record.date, record.opponentId, record.outcome.name, record.startedAt)
            .joinToString(SEPARATOR)

    /**
     * `null` for a broken entry. An unknown outcome counts as [Outcome.LOSS], since the fight still
     * used its ticket. An entry without the start time reads it as 0.
     */
    fun decode(value: String): FightRecord? {
        val parts = value.split(SEPARATOR)
        if (parts.size !in 3..4) return null
        val date = runCatching { LocalDate.parse(parts[0]) }.getOrNull() ?: return null
        val opponentId = parts[1].takeIf { it.isNotBlank() } ?: return null
        val outcome = Outcome.entries.firstOrNull { it.name == parts[2] } ?: Outcome.LOSS
        val startedAt = if (parts.size == 4) parts[3].toLongOrNull() ?: return null else 0L
        return FightRecord(date, opponentId, outcome, startedAt)
    }
}
