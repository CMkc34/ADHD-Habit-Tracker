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

import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.core.fighter.FighterStat
import com.shub39.grit.core.fighter.FighterState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate

class RunLogTest {
    private val today = LocalDate(2026, 11, 15)
    private val yesterday = LocalDate(2026, 11, 14)

    private fun run(
        game: MiniGame = MiniGame.KI_BARRAGE,
        date: LocalDate = today,
        score: Int = 0,
        startedAt: Long = 1,
    ) = RunRecord(date, game.id, score, startedAt)

    @Test
    fun `three runs per game and day`() {
        assertEquals(3, RunLog.runsLeft(emptyList(), MiniGame.KI_BARRAGE, today))
        val runs = listOf(run(startedAt = 1), run(startedAt = 2))
        assertEquals(1, RunLog.runsLeft(runs, MiniGame.KI_BARRAGE, today))
    }

    @Test
    fun `runs of other games and other days do not count`() {
        val runs =
            listOf(
                run(game = MiniGame.BOARD_BREAK, startedAt = 1),
                run(game = MiniGame.CLUTTER_SMASH, startedAt = 2),
                run(date = yesterday, startedAt = 3),
                run(date = yesterday, startedAt = 4),
                run(date = yesterday, startedAt = 5),
            )
        assertEquals(3, RunLog.runsLeft(runs, MiniGame.KI_BARRAGE, today))
        assertEquals(2, RunLog.runsLeft(runs, MiniGame.BOARD_BREAK, today))
    }

    @Test
    fun `runs left never go below zero`() {
        val runs = (1L..5L).map { run(startedAt = it) }
        assertEquals(0, RunLog.runsLeft(runs, MiniGame.KI_BARRAGE, today))
    }

    @Test
    fun `best is the highest score of the game over all days`() {
        val runs =
            listOf(
                run(score = 120, date = yesterday, startedAt = 1),
                run(score = 80, startedAt = 2),
                run(game = MiniGame.BOARD_BREAK, score = 900, startedAt = 3),
            )
        assertEquals(120, RunLog.best(runs, MiniGame.KI_BARRAGE))
        assertEquals(0, RunLog.best(runs, MiniGame.CLUTTER_SMASH))
    }

    @Test
    fun `a started run has score zero and a unique start`() {
        val existing = listOf(run(startedAt = 500))
        val started = RunLog.start(MiniGame.CLUTTER_SMASH, today, now = 400, records = existing)
        assertEquals(0, started.score)
        assertEquals(MiniGame.CLUTTER_SMASH.id, started.gameId)
        assertEquals(501, started.startedAt)
        assertEquals(1000, RunLog.start(MiniGame.CLUTTER_SMASH, today, 1000, existing).startedAt)
    }

    @Test
    fun `new best only when the score beats the other runs`() {
        val current = run(startedAt = 3)
        val runs = listOf(run(score = 100, startedAt = 1), current)
        assertTrue(RunLog.isNewBest(runs, current, 101))
        assertFalse(RunLog.isNewBest(runs, current, 100))
        assertFalse(RunLog.isNewBest(listOf(current), current, 0))
        assertTrue(RunLog.isNewBest(listOf(current), current, 1))
    }

    @Test
    fun `merge keeps every run once with the higher score`() {
        val a = listOf(run(score = 0, startedAt = 1), run(score = 50, startedAt = 2))
        val b = listOf(run(score = 70, startedAt = 1), run(score = 10, startedAt = 3))
        val merged = RunLog.merge(a, b)
        assertEquals(listOf(70, 50, 10), merged.map { it.score })
    }

    @Test
    fun `codec round trip and broken entries`() {
        val record = run(game = MiniGame.BOARD_BREAK, score = 14, startedAt = 1234)
        assertEquals("2026-11-15|board_break|14|1234", RunRecordCodec.encode(record))
        assertEquals(record, RunRecordCodec.decode(RunRecordCodec.encode(record)))
        assertNull(RunRecordCodec.decode("2026-11-15|board_break|14"))
        assertNull(RunRecordCodec.decode("no-date|board_break|14|1"))
        assertNull(RunRecordCodec.decode("2026-11-15||14|1"))
        assertNull(RunRecordCodec.decode("2026-11-15|board_break|x|1"))
        assertEquals(0, RunRecordCodec.decode("2026-11-15|board_break|-5|1")?.score)
        // a game from a newer app survives
        assertEquals("future_game", RunRecordCodec.decode("2026-11-15|future_game|3|1")?.gameId)
        assertNull(RunRecordCodec.decode("2026-11-15|future_game|3|1")?.game)
    }

    @Test
    fun `state lists every game with runs left, best and the stat levels`() {
        val fighter =
            FighterState(
                activeDays = 10,
                rank = FighterRank.forActiveDays(10),
                mood = FighterMood.READY,
                xp = mapOf(FighterStat.STRENGTH to 15, FighterStat.FOCUS to 5),
            )
        val runs = listOf(run(score = 40, startedAt = 1), run(score = 90, startedAt = 2))
        val state = MiniGameState.of(runs, fighter, today)

        assertEquals(MiniGame.entries, state.games.map { it.game })
        assertEquals(1, state.entry(MiniGame.KI_BARRAGE).runsLeft)
        assertEquals(90, state.entry(MiniGame.KI_BARRAGE).best)
        assertEquals(3, state.entry(MiniGame.BOARD_BREAK).runsLeft)
        assertTrue(state.canStart(MiniGame.KI_BARRAGE))
        assertEquals(3, state.stats.strength)
        assertEquals(2, state.stats.focus)
        assertEquals(1, state.stats.speed)
        assertEquals(state.stats.strength, state.stats.level(FighterStat.STRENGTH))
    }

    @Test
    fun `ids are fixed and unique`() {
        assertEquals(
            listOf("ki_barrage", "clutter_smash", "board_break"),
            MiniGame.entries.map { it.id },
        )
        MiniGame.entries.forEach { assertEquals(it, MiniGame.byId(it.id)) }
    }
}
