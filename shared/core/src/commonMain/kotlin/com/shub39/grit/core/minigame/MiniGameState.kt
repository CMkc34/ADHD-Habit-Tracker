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

import com.shub39.grit.core.arena.Ladder
import com.shub39.grit.core.arena.Opponent
import com.shub39.grit.core.fighter.FighterStat
import com.shub39.grit.core.fighter.FighterState
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/** One card of the Games tab. */
data class MiniGameEntry(val game: MiniGame, val runsLeft: Int, val best: Int) {
    val canStart: Boolean
        get() = runsLeft > 0
}

/** The Games tab (SPEC 10): runs left and best score per game, and the fighter's stat levels. */
data class MiniGameState(
    val games: List<MiniGameEntry>,
    val stats: GameStats,
    val today: LocalDate,
    /** The arena opponents the fighter has unlocked; Ki Barrage takes its bosses from them (#42). */
    val opponents: List<Opponent> = listOf(Opponent.entries.first()),
) {
    fun entry(game: MiniGame): MiniGameEntry = games.first { it.game == game }

    fun canStart(game: MiniGame): Boolean = entry(game).canStart

    companion object {
        fun of(
            runs: List<RunRecord>,
            fighter: FighterState,
            today: LocalDate,
            beaten: Set<Opponent> = emptySet(),
        ): MiniGameState =
            MiniGameState(
                games =
                    MiniGame.entries.map {
                        MiniGameEntry(
                            game = it,
                            runsLeft = RunLog.runsLeft(runs, it, today),
                            best = RunLog.best(runs, it),
                        )
                    },
                stats = GameStats.of(fighter),
                today = today,
                opponents = Ladder.unlocked(beaten),
            )
    }
}

/**
 * The fighter's four stat levels as the games see them. Each game turns them into its own numbers
 * (#42, #43, #44), always with a cap, so a strong fighter has it easier but the game stays a game.
 */
data class GameStats(
    val strength: Int = 1,
    val speed: Int = 1,
    val stamina: Int = 1,
    val focus: Int = 1,
) {
    fun level(stat: FighterStat): Int =
        when (stat) {
            FighterStat.STRENGTH -> strength
            FighterStat.SPEED -> speed
            FighterStat.STAMINA -> stamina
            FighterStat.FOCUS -> focus
        }

    companion object {
        fun of(fighter: FighterState): GameStats =
            GameStats(
                strength = fighter.level(FighterStat.STRENGTH),
                speed = fighter.level(FighterStat.SPEED),
                stamina = fighter.level(FighterStat.STAMINA),
                focus = fighter.level(FighterStat.FOCUS),
            )
    }
}

/** A run that has started and used one of the day's runs. */
data class StartedRun(
    val record: RunRecord,
    val stats: GameStats,
    val opponents: List<Opponent> = listOf(Opponent.entries.first()),
) {
    val game: MiniGame
        get() = requireNotNull(record.game)
}

/** Stores the run log, e.g. in the Preferences DataStore. */
interface MiniGameStore {
    fun getRuns(): Flow<List<RunRecord>>

    suspend fun addRun(record: RunRecord)

    /** Replaces the stored run with the same day, game and start as [record]. */
    suspend fun updateRun(record: RunRecord)

    /** Replaces the whole log with [records], used when restoring a backup. */
    suspend fun replaceAll(records: List<RunRecord>)
}

interface MiniGameRepo {
    fun getMiniGameState(): Flow<MiniGameState>

    /** Starts a run of [game] and uses one of today's runs; `null` when none is left. */
    suspend fun startRun(game: MiniGame): StartedRun?

    /** Stores the final [score] of [run]; `true` when it is a new high score. */
    suspend fun finishRun(run: StartedRun, score: Int): Boolean
}
