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

import com.shub39.grit.core.minigame.RunRecord
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/** The mini-game run log in a backup (phase 7, SPEC 10). */
@Serializable data class MiniGameSchema(val runs: List<RunSchema> = emptyList())

/** One run; [date] is `yyyy-MM-dd`, [gameId] the id of `MiniGame`. */
@Serializable
data class RunSchema(val date: String, val gameId: String, val score: Int, val startedAt: Long)

fun List<RunRecord>.toMiniGameSchema(): MiniGameSchema =
    MiniGameSchema(
        runs =
            sortedBy { it.startedAt }
                .map { RunSchema(it.date.toString(), it.gameId, it.score, it.startedAt) }
    )

/** The runs of the backup. A run with a broken date or game id is dropped. */
fun MiniGameSchema.toRunRecords(): List<RunRecord> =
    runs.mapNotNull { run ->
        val date = runCatching { LocalDate.parse(run.date) }.getOrNull() ?: return@mapNotNull null
        if (run.gameId.isBlank()) return@mapNotNull null
        RunRecord(date, run.gameId, run.score.coerceAtLeast(0), run.startedAt)
    }
