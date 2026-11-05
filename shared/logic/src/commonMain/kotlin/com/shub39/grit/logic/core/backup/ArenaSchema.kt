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
package com.shub39.grit.logic.core.backup

import com.shub39.grit.core.arena.FightRecord
import com.shub39.grit.core.arena.Outcome
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/** The arena's fight log in a backup (phase 6). */
@Serializable data class ArenaSchema(val fights: List<FightSchema> = emptyList())

/** One fight; [date] is `yyyy-MM-dd`, [outcome] the name of `Outcome`. */
@Serializable
data class FightSchema(
    val date: String,
    val opponentId: String,
    val outcome: String,
    val startedAt: Long = 0,
)

fun List<FightRecord>.toArenaSchema(): ArenaSchema =
    ArenaSchema(
        fights =
            sortedBy { it.startedAt }
                .map {
                    FightSchema(it.date.toString(), it.opponentId, it.outcome.name, it.startedAt)
                }
    )

/**
 * The fights of the backup. A fight with a broken date is dropped; an unknown outcome counts as a
 * loss, like in the store.
 */
fun ArenaSchema.toFightRecords(): List<FightRecord> =
    fights.mapNotNull { fight ->
        val date = runCatching { LocalDate.parse(fight.date) }.getOrNull() ?: return@mapNotNull null
        FightRecord(
            date = date,
            opponentId = fight.opponentId,
            outcome = Outcome.entries.firstOrNull { it.name == fight.outcome } ?: Outcome.LOSS,
            startedAt = fight.startedAt,
        )
    }
