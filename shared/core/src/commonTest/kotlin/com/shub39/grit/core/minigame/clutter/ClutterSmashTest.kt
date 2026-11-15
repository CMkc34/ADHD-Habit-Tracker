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
package com.shub39.grit.core.minigame.clutter

import com.shub39.grit.core.minigame.GameStats
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ClutterSmashTest {

    private val rookie = ClutterSmashConfig.of(GameStats())

    private fun game(config: ClutterSmashConfig = rookie, random: Random = Random(7)) =
        ClutterSmash(config, random)

    private fun ClutterSmash.run(seconds: Float, every: Float = 1f / 30f): List<ClutterEvent> {
        val events = mutableListOf<ClutterEvent>()
        var left = seconds
        while (left > 0f && !isOver) {
            step(every)
            events += this.events
            left -= every
        }
        return events
    }

    @Test
    fun `stats make the game easier but stay capped`() {
        assertEquals(ClutterSmashConfig(1, 15f, 0.28f, 38f), rookie)
        val strong =
            ClutterSmashConfig.of(GameStats(strength = 4, speed = 6, stamina = 6, focus = 5))
        assertEquals(2, strong.hitDamage)
        assertEquals(21f, strong.kiPerSmash, 0.001f)
        assertEquals(0.18f, strong.hitCooldown, 0.001f)
        assertEquals(32f, strong.lineY, 0.001f)
        val maxed = ClutterSmashConfig.of(GameStats(99, 99, 99, 99))
        assertEquals(ClutterSmashConfig(3, 30f, 0.10f, 26f), maxed)
    }

    @Test
    fun `junk spawns into the three columns and piles up on the floor`() {
        val game = game()
        game.step(0.01f)
        assertEquals(1, game.junk.size)
        assertTrue(game.junk.first().column in 0 until ClutterSmash.COLUMNS)

        game.run(6f)
        assertTrue(game.junk.any { it.landed })
        assertTrue(game.highestPileTop < ClutterSmash.FLOOR_Y)
    }

    @Test
    fun `tapping junk smashes it, alternates punch and kick, and builds a combo`() {
        val fast =
            ClutterSmashConfig.of(GameStats(strength = 11, speed = 11, stamina = 1, focus = 1))
        val game = game(config = fast)
        game.run(4f)
        assertTrue(game.junk.size >= 3)

        val first = game.junk[0]
        assertTrue(game.tap(first.centerX, first.centerY))
        assertTrue(game.lastStrikeKick)
        assertNotNull(game.lastBolt)
        assertEquals(1, game.combo)
        assertEquals(10, game.score)

        game.step(0.15f)
        val second = game.junk[0]
        assertTrue(game.hit(second.id))
        assertFalse(game.lastStrikeKick)
        assertEquals(2, game.combo)
        assertEquals(30, game.score)

        game.step(0.15f)
        val third = game.junk[0]
        assertTrue(game.hit(third.id))
        assertEquals(3, game.combo)
        assertEquals(60, game.score)
    }

    @Test
    fun `combo resets after the combo window passes`() {
        val fast = ClutterSmashConfig.of(GameStats(strength = 11, speed = 11))
        val game = game(config = fast)
        game.run(3f)

        assertTrue(game.hit(game.junk.first().id))
        assertEquals(1, game.combo)

        game.run(ClutterSmash.COMBO_WINDOW + 0.2f)
        assertEquals(0, game.combo)

        assertTrue(game.hit(game.junk.first().id))
        assertEquals(1, game.combo)
        assertEquals(20, game.score)
    }

    @Test
    fun `big junk cracks before breaking and breaks in one hit at high strength`() {
        val game = game(config = rookie, random = Random(1))
        while (game.junk.none { it.kind == JunkKind.PIZZA_BOX }) {
            game.step(0.2f)
        }
        val box = game.junk.first { it.kind == JunkKind.PIZZA_BOX }
        assertTrue(box.kind.isBig)
        assertEquals(3, box.hp)

        assertTrue(game.hit(box.id))
        assertEquals(2, box.hp)
        assertTrue(box.isDamaged)
        assertTrue(game.events.single() is ClutterEvent.JunkCracked)

        game.run(rookie.hitCooldown + 0.05f)
        assertTrue(game.hit(box.id))
        assertEquals(1, box.hp)

        game.run(rookie.hitCooldown + 0.05f)
        assertTrue(game.hit(box.id))
        assertTrue(game.events.any { it is ClutterEvent.JunkSmashed })
        assertTrue(game.junk.none { it.id == box.id })

        // With high Strength, a 3-HP pizza box breaks on the very first tap
        val strongGame =
            game(config = ClutterSmashConfig.of(GameStats(strength = 10)), random = Random(1))
        while (strongGame.junk.none { it.kind == JunkKind.PIZZA_BOX }) {
            strongGame.step(0.2f)
        }
        val box2 = strongGame.junk.first { it.kind == JunkKind.PIZZA_BOX }
        assertTrue(strongGame.hit(box2.id))
        assertTrue(strongGame.junk.none { it.id == box2.id })
    }

    @Test
    fun `speed controls the pause between hits`() {
        val game = game(config = rookie)
        game.run(2f)
        val first = game.junk[0]
        val second = game.junk[1]
        assertTrue(game.hit(first.id))
        assertFalse(game.canHit)
        assertFalse(game.hit(second.id))

        game.run(rookie.hitCooldown + 0.02f)
        assertTrue(game.canHit)
        assertTrue(game.hit(second.id))
    }

    @Test
    fun `smashes fill the ki bar and full ki fires a Ki Wave that clears all junk for 25 each`() {
        val focused =
            ClutterSmashConfig.of(GameStats(strength = 11, speed = 11, stamina = 11, focus = 11))
        val game = game(config = focused)
        // focus 11 gives 30 ki per smash, so 4 smashes fill the 100 ki bar
        while (!game.kiReady) {
            game.step(0.15f)
            val target = game.junk.firstOrNull()
            if (target != null && game.canHit) {
                game.hit(target.id)
            }
        }
        assertTrue(game.kiReady)
        assertEquals(1f, game.kiShare, 0.001f)

        // Let several pieces accumulate, then tap the fighter to fire the Ki Wave
        game.run(3f)
        val remaining = game.junk.size
        assertTrue(remaining >= 2)
        val scoreBefore = game.score

        assertTrue(game.tap(ClutterSmash.FIGHTER_LEFT + 16f, ClutterSmash.FIGHTER_TOP + 20f))
        assertFalse(game.kiReady)
        assertEquals(0f, game.ki)
        assertTrue(game.junk.isEmpty())
        assertEquals(scoreBefore + remaining * ClutterSmash.WAVE_POINTS, game.score)
        assertTrue(
            game.events.contains(
                ClutterEvent.KiWaveCleared(remaining, remaining * ClutterSmash.WAVE_POINTS)
            )
        )
    }

    @Test
    fun `a pile reaching the red line flashes danger first and then ends the run`() {
        val game = game()
        var sawDanger = false
        while (!game.isOver) {
            game.step(1f / 30f)
            if (game.isDanger) sawDanger = true
        }
        assertTrue(sawDanger)
        assertTrue(game.isOver)
        assertEquals(EndReason.TOO_HIGH, game.endReason)
        assertTrue(game.time < ClutterSmash.DURATION)
        assertEquals(ClutterEvent.GameOver(EndReason.TOO_HIGH), game.events.last())
    }

    @Test
    fun `clearing junk for 60 seconds ends the run with TIME_UP`() {
        val strong = ClutterSmashConfig.of(GameStats(11, 11, 11, 11))
        val game = game(config = strong)
        while (!game.isOver) {
            game.step(1f / 30f)
            if (game.kiReady && game.junk.size >= 3) {
                game.fireKiWave()
            } else {
                val target = game.junk.firstOrNull()
                if (target != null && game.canHit) game.hit(target.id)
            }
        }
        assertTrue(game.isOver)
        assertEquals(EndReason.TIME_UP, game.endReason)
        assertEquals(0f, game.timeLeft, 0.001f)
        assertTrue(game.score > 0)
        assertNull(game.junk.firstOrNull { !it.landed && game.tap(it.centerX, it.centerY) })
    }

    @Test
    fun `the same dice play the same run`() {
        fun replay(): Triple<Int, Float, EndReason?> {
            val game =
                game(config = ClutterSmashConfig.of(GameStats(5, 5, 5, 5)), random = Random(99))
            repeat(600) {
                game.step(1f / 30f)
                val target = game.junk.firstOrNull()
                if (target != null && game.canHit) game.hit(target.id)
            }
            return Triple(game.score, game.ki, game.endReason)
        }
        assertEquals(replay(), replay())
    }
}
