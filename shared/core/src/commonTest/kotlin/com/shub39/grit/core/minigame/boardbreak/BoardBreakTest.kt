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
package com.shub39.grit.core.minigame.boardbreak

import com.shub39.grit.core.minigame.GameStats
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BoardBreakTest {

    @Test
    fun `stats scale green zone, layer count and needle speed with caps`() {
        val rookie = BoardBreakConfig.of(GameStats())
        val veteran =
            BoardBreakConfig.of(GameStats(strength = 11, speed = 11, stamina = 11, focus = 11))
        val runaway =
            BoardBreakConfig.of(GameStats(strength = 50, speed = 50, stamina = 50, focus = 50))

        assertTrue(veteran.greenHalfWidth > rookie.greenHalfWidth)
        assertTrue(veteran.perfectHalfWidth > rookie.perfectHalfWidth)
        assertTrue(veteran.bonusLayers > rookie.bonusLayers)
        assertTrue(veteran.needleSpeed < rookie.needleSpeed)

        assertEquals(0.12f, rookie.greenHalfWidth, 0.0001f)
        assertEquals(0.20f, veteran.greenHalfWidth, 0.0001f)
        assertEquals(0, rookie.bonusLayers)
        assertEquals(5, veteran.bonusLayers)
        assertEquals(1.55f, rookie.needleSpeed, 0.0001f)
        assertEquals(0.75f, veteran.needleSpeed, 0.0001f)

        assertEquals(veteran, runaway)
    }

    @Test
    fun `three strikes progress from boards to bricks to ice with more layers each time`() {
        val rookie = BoardBreakConfig.of(GameStats())
        val strong = BoardBreakConfig.of(GameStats(strength = 9))

        val b0 = rookie.layersFor(StrikeMaterial.BOARDS)
        val b1 = rookie.layersFor(StrikeMaterial.BRICKS)
        val b2 = rookie.layersFor(StrikeMaterial.ICE)
        assertEquals(4, b0)
        assertEquals(6, b1)
        assertEquals(8, b2)
        assertTrue(b0 < b1 && b1 < b2)

        assertEquals(8, strong.layersFor(StrikeMaterial.BOARDS))
        assertEquals(10, strong.layersFor(StrikeMaterial.BRICKS))
        assertEquals(12, strong.layersFor(StrikeMaterial.ICE))
    }

    @Test
    fun `needle position maps to PERFECT, GREAT, GOOD and MISS symmetrically`() {
        val config = BoardBreakConfig.of(GameStats())

        assertEquals(StrikeRating.PERFECT, config.rate(0.50f))
        assertEquals(StrikeRating.PERFECT, config.rate(0.47f))
        assertEquals(StrikeRating.PERFECT, config.rate(0.53f))

        assertEquals(StrikeRating.GREAT, config.rate(0.42f))
        assertEquals(StrikeRating.GREAT, config.rate(0.58f))

        assertEquals(StrikeRating.GOOD, config.rate(0.30f))
        assertEquals(StrikeRating.GOOD, config.rate(0.70f))

        assertEquals(StrikeRating.MISS, config.rate(0.10f))
        assertEquals(StrikeRating.MISS, config.rate(0.90f))
    }

    @Test
    fun `higher Focus widens the green zone so an off-center release upgrades from GOOD to GREAT`() {
        val rookie = BoardBreakConfig.of(GameStats(focus = 1))
        val focused = BoardBreakConfig.of(GameStats(focus = 11))

        // 0.33 is 0.17 away from center (0.50): outside rookie's 0.12 green half-width, inside
        // focused's 0.20
        assertEquals(StrikeRating.GOOD, rookie.rate(0.33f))
        assertEquals(StrikeRating.GREAT, focused.rate(0.33f))
    }

    @Test
    fun `layers broken strictly increase from MISS to GOOD to GREAT to PERFECT`() {
        val config = BoardBreakConfig.of(GameStats())
        for (material in StrikeMaterial.entries) {
            val total = config.layersFor(material)
            val miss = config.layersBroken(StrikeRating.MISS, total)
            val good = config.layersBroken(StrikeRating.GOOD, total)
            val great = config.layersBroken(StrikeRating.GREAT, total)
            val perfect = config.layersBroken(StrikeRating.PERFECT, total)

            assertEquals(0, miss)
            assertTrue(good in 1 until great)
            assertTrue(great in (good + 1) until perfect)
            assertEquals(total, perfect)
        }
    }

    @Test
    fun `holding charges ki and sweeps needle back and forth at Speed-dependent rate`() {
        val fastNeedle = BoardBreak(BoardBreakConfig.of(GameStats(speed = 1)))
        val slowNeedle = BoardBreak(BoardBreakConfig.of(GameStats(speed = 11)))

        assertNull(fastNeedle.releaseCharge())
        assertTrue(fastNeedle.startCharge())
        assertTrue(slowNeedle.startCharge())
        assertEquals(BoardBreakPhase.CHARGING, fastNeedle.phase)

        fastNeedle.step(0.25f)
        slowNeedle.step(0.25f)
        assertTrue(fastNeedle.needle > slowNeedle.needle)

        // Sweeping past 1.0 bounces the needle back toward 0.0
        fastNeedle.step(0.55f)
        assertEquals(-1, fastNeedle.needleDirection)
        assertTrue(fastNeedle.needle in 0f..1f)

        // Sweeping past 0.0 bounces back toward 1.0
        fastNeedle.step(0.70f)
        assertEquals(1, fastNeedle.needleDirection)
        assertTrue(fastNeedle.needle in 0f..1f)
    }

    @Test
    fun `full run of three strikes accumulates broken layers and completes after ice`() {
        val config = BoardBreakConfig.of(GameStats(strength = 5)) // +2 bonus layers: 6, 8, 10
        val game = BoardBreak(config)

        assertEquals(1, game.strikeNumber)
        assertEquals(StrikeMaterial.BOARDS, game.currentMaterial)
        assertEquals(6, game.currentTotalLayers)

        // Strike 1 (BOARDS, 6 layers): PERFECT -> 6 broken
        val first = assertNotNull(game.strikeAt(0.50f))
        assertEquals(StrikeRating.PERFECT, first.rating)
        assertEquals(6, first.broken)
        assertEquals(6, game.score)
        assertEquals(BoardBreakPhase.STRIKING, game.phase)
        assertTrue(game.events.any { it is BoardBreakEvent.StrikeLanded })

        game.step(BoardBreak.STRIKE_SETTLE_SECONDS + 0.05f)
        assertFalse(game.isOver)
        assertEquals(BoardBreakPhase.READY, game.phase)
        assertEquals(2, game.strikeNumber)
        assertEquals(StrikeMaterial.BRICKS, game.currentMaterial)
        assertEquals(8, game.currentTotalLayers)

        // Strike 2 (BRICKS, 8 layers): GOOD -> 4 broken
        val second = assertNotNull(game.strikeAt(0.28f))
        assertEquals(StrikeRating.GOOD, second.rating)
        assertEquals(4, second.broken)
        assertEquals(10, game.score)

        game.step(BoardBreak.STRIKE_SETTLE_SECONDS + 0.05f)
        assertFalse(game.isOver)
        assertEquals(3, game.strikeNumber)
        assertEquals(StrikeMaterial.ICE, game.currentMaterial)
        assertEquals(10, game.currentTotalLayers)

        // Strike 3 (ICE, 10 layers): GREAT -> 7 broken
        val third = assertNotNull(game.strikeAt(0.42f))
        assertEquals(StrikeRating.GREAT, third.rating)
        assertEquals(7, third.broken)
        assertEquals(17, game.score)

        game.step(BoardBreak.STRIKE_SETTLE_SECONDS + 0.05f)
        assertTrue(game.isOver)
        assertEquals(BoardBreakPhase.OVER, game.phase)
        assertEquals(3, game.outcomes.size)
        val completed = game.events.filterIsInstance<BoardBreakEvent.RunComplete>().single()
        assertEquals(17, completed.score)
        assertEquals(24, completed.maxPossible)
    }
}
