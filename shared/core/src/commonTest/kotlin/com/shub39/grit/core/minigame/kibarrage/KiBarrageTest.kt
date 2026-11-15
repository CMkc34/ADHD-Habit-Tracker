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
package com.shub39.grit.core.minigame.kibarrage

import com.shub39.grit.core.arena.Opponent
import com.shub39.grit.core.minigame.GameStats
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KiBarrageTest {

    private val rookie = KiBarrageConfig.of(GameStats())

    /** A random that never fires a minion shot. */
    private val calm =
        object : Random() {
            override fun nextBits(bitCount: Int): Int = (1 shl bitCount) - 1
        }

    private fun game(
        config: KiBarrageConfig = rookie,
        bosses: List<Opponent> = listOf(Opponent.SNOOZE_BUTTON),
        random: Random = calm,
    ) = KiBarrage(config, bosses, random)

    private fun KiBarrage.run(seconds: Float, every: Float = 1f / 30f): List<KiBarrageEvent> {
        val events = mutableListOf<KiBarrageEvent>()
        var left = seconds
        while (left > 0f && !isOver) {
            step(every)
            events += this.events
            left -= every
        }
        return events
    }

    /** Keeps the fighter under the lowest living minion, or under the boss. */
    private fun KiBarrage.aim() {
        val minion = minions.filter { it.alive && it.entered }.maxByOrNull { it.y }
        target =
            when {
                minion != null -> minion.x + KiBarrage.MINION_SIZE / 2 - KiBarrage.KI_OFFSET_X
                boss != null -> boss!!.x + KiBarrage.BOSS_SIZE / 2 - KiBarrage.KI_OFFSET_X
                else -> target
            }
    }

    private fun KiBarrage.play(seconds: Float): List<KiBarrageEvent> {
        val events = mutableListOf<KiBarrageEvent>()
        var left = seconds
        while (left > 0f && !isOver) {
            aim()
            step(1f / 30f)
            events += this.events
            left -= 1f / 30f
        }
        return events
    }

    @Test
    fun `stats make the game easier but stay capped`() {
        assertEquals(KiBarrageConfig(70f, 0.34f, 3, 1), rookie)
        val strong = KiBarrageConfig.of(GameStats(strength = 7, speed = 6, stamina = 5, focus = 6))
        assertEquals(130f, strong.moveSpeed)
        assertEquals(0.24f, strong.fireInterval, 0.001f)
        assertEquals(5, strong.hearts)
        assertEquals(3, strong.boltDamage)
        val maxed = KiBarrageConfig.of(GameStats(99, 99, 99, 99))
        assertEquals(KiBarrageConfig(190f, 0.15f, 6, 4), maxed)
    }

    @Test
    fun `the first wave starts at once and the fighter fires by itself`() {
        val game = game()
        game.step(0.01f)
        assertEquals(1, game.wave)
        assertEquals(KiBarrageEvent.WaveStarted(1, null), game.events.first())
        assertEquals(5, game.minions.size)
        assertEquals(1, game.bolts.size)

        game.run(1f)
        // one bolt every 0.34 s, flying up and gone after about 1.5 s
        assertEquals(3, game.bolts.size, "bolts ${game.bolts}")
        assertTrue(game.bolts.all { it.x == KiBarrage.WIDTH / 2 + KiBarrage.KI_OFFSET_X })
    }

    @Test
    fun `the fighter follows the finger at its move speed`() {
        val game = game()
        game.target = KiBarrage.WIDTH
        game.step(0.1f)
        assertEquals(KiBarrage.WIDTH / 2 + 7f, game.fighterX, 0.01f)
        game.run(2f)
        assertEquals(KiBarrage.MAX_X, game.fighterX, 0.01f)
    }

    @Test
    fun `a bolt pops a minion for ten points`() {
        val game = game()
        val events = game.play(6f)
        val pops = events.filterIsInstance<KiBarrageEvent.MinionPopped>()
        assertTrue(pops.isNotEmpty())
        assertEquals(10 * pops.size, game.score)
    }

    @Test
    fun `clearing waves leads to a boss on wave three`() {
        val game = game(config = KiBarrageConfig.of(GameStats(5, 11, 11, 11)))
        val events = game.play(60f)
        val waves = events.filterIsInstance<KiBarrageEvent.WaveStarted>()
        assertEquals(listOf(1, 2, 3), waves.take(3).map { it.wave })
        assertNull(waves[0].boss)
        assertEquals(Opponent.SNOOZE_BUTTON, waves[2].boss)
    }

    @Test
    fun `knocking out the boss gives 500 points and the next wave follows`() {
        val game = game(config = KiBarrageConfig.of(GameStats(11, 11, 11, 11)))
        val events = game.play(120f)
        val ko = events.filterIsInstance<KiBarrageEvent.BossKnockedOut>()
        val waves = events.filterIsInstance<KiBarrageEvent.WaveStarted>()
        assertTrue(ko.isNotEmpty(), "waves: $waves")
        assertEquals(500, ko.first().points)
        assertTrue(events.filterIsInstance<KiBarrageEvent.WaveStarted>().any { it.wave == 4 })
    }

    @Test
    fun `bosses come from the unlocked opponents in turn`() {
        val bosses = listOf(Opponent.SNOOZE_BUTTON, Opponent.COUCH_GREMLIN)
        val game = game(config = KiBarrageConfig.of(GameStats(11, 11, 11, 11)), bosses = bosses)
        val events = game.play(240f)
        val seen = events.filterIsInstance<KiBarrageEvent.WaveStarted>().mapNotNull { it.boss }
        assertEquals(bosses, seen.take(2))
    }

    @Test
    fun `a hit costs a heart, then the fighter is safe for a moment`() {
        val game = game(random = Random(1))
        game.target = 0f
        game.step(0.01f)
        val hearts = game.hearts
        // a minion that sinks down to the fighter rams it
        val events = game.run(60f)
        assertTrue(KiBarrageEvent.PlayerHit in events)
        assertTrue(game.hearts < hearts || game.isOver)
    }

    @Test
    fun `the run ends when the last heart is gone`() {
        val game = game(random = Random(3))
        // the fighter never moves under a minion and fires into the void
        game.target = 0f
        val events = game.run(600f)
        assertTrue(game.isOver)
        assertEquals(0, game.hearts)
        assertEquals(KiBarrageEvent.GameOver, events.last())
        assertEquals(rookie.hearts, events.count { it == KiBarrageEvent.PlayerHit })
        val score = game.score
        game.step(1f)
        assertEquals(score, game.score)
        assertTrue(game.events.isEmpty())
    }

    @Test
    fun `the same dice play the same run`() {
        fun replay(): Pair<Int, Int> {
            val game = game(random = Random(42))
            game.play(90f)
            return game.score to game.hearts
        }
        assertEquals(replay(), replay())
    }

    @Test
    fun `invulnerable right after a hit`() {
        val game = game(random = Random(3))
        game.target = 0f
        val events = mutableListOf<KiBarrageEvent>()
        while (KiBarrageEvent.PlayerHit !in events && !game.isOver) {
            game.step(1f / 30f)
            events += game.events
        }
        assertTrue(game.isInvulnerable)
        val hearts = game.hearts
        game.run(KiBarrage.INVULNERABLE - 0.2f)
        assertEquals(hearts, game.hearts)
        assertFalse(game.isOver)
        assertNotNull(game.minions)
    }
}
