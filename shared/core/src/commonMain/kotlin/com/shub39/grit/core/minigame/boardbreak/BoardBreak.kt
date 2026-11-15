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
import kotlin.math.abs

/**
 * The numbers of Board Break (#44) that come from the fighter's stats, each with a cap so the
 * timing challenge stays fair: Focus widens the green and bright-center zones on the meter,
 * Strength stacks more layers on the stand for each strike, Speed slows the sweeping needle down.
 */
data class BoardBreakConfig(
    /** Half-width around the meter center (0.5) that scores [StrikeRating.PERFECT]. */
    val perfectHalfWidth: Float,
    /** Half-width around the meter center (0.5) that scores at least [StrikeRating.GREAT]. */
    val greenHalfWidth: Float,
    /** Half-width around the meter center (0.5) that scores at least [StrikeRating.GOOD]. */
    val yellowHalfWidth: Float,
    /** Extra layers added to every strike on top of [StrikeMaterial.baseLayers]. */
    val bonusLayers: Int,
    /** Meter widths (0..1) the needle traverses per second while charging. */
    val needleSpeed: Float,
) {
    /** Total layers stacked on the stand for [material]. */
    fun layersFor(material: StrikeMaterial): Int = material.baseLayers + bonusLayers

    /** Total layers across all three strikes of a run. */
    val maxPossibleScore: Int
        get() = StrikeMaterial.entries.sumOf { layersFor(it) }

    /** Rates a needle release at [needle] (in `0f..1f`, where `0.5f` is the bright center). */
    fun rate(needle: Float): StrikeRating {
        val distance = abs(needle.coerceIn(0f, 1f) - BoardBreak.METER_CENTER)
        return when {
            distance <= perfectHalfWidth -> StrikeRating.PERFECT
            distance <= greenHalfWidth -> StrikeRating.GREAT
            distance <= yellowHalfWidth -> StrikeRating.GOOD
            else -> StrikeRating.MISS
        }
    }

    /** How many of [totalLayers] break for a strike with [rating]. */
    fun layersBroken(rating: StrikeRating, totalLayers: Int): Int {
        if (totalLayers <= 0) return 0
        return when (rating) {
            StrikeRating.PERFECT -> totalLayers
            StrikeRating.GREAT ->
                ((totalLayers * 3) / 4).coerceIn(1, (totalLayers - 1).coerceAtLeast(1))
            StrikeRating.GOOD -> (totalLayers / 2).coerceIn(1, (totalLayers - 2).coerceAtLeast(1))
            StrikeRating.MISS -> 0
        }
    }

    companion object {
        fun of(stats: GameStats): BoardBreakConfig {
            val focus = capped(stats.focus)
            val strength = capped(stats.strength)
            val speed = capped(stats.speed)
            return BoardBreakConfig(
                perfectHalfWidth = 0.04f + 0.003f * focus,
                greenHalfWidth = 0.12f + 0.008f * focus,
                yellowHalfWidth = 0.26f + 0.008f * focus,
                bonusLayers = strength / 2,
                needleSpeed = maxOf(0.75f, 1.55f - 0.08f * speed),
            )
        }

        /** Levels above 1 that count, at most 10. */
        private fun capped(level: Int): Int = (level - 1).coerceIn(0, 10)
    }
}

/**
 * The three stacks of a Board Break run (#44), in order: wooden boards first, then clay bricks,
 * then thick ice slabs, with more layers each time.
 */
enum class StrikeMaterial(val baseLayers: Int) {
    BOARDS(baseLayers = 4),
    BRICKS(baseLayers = 6),
    ICE(baseLayers = 8),
}

/** Rating of a single strike from where the needle was released on the meter. */
enum class StrikeRating {
    /** Bright center: breaks every layer on the stand. */
    PERFECT,
    /** Green zone: breaks three quarters of the stack. */
    GREAT,
    /** Yellow zone: breaks half of the stack. */
    GOOD,
    /** Outer black zone: the strike bounces off, 0 layers broken. */
    MISS,
}

/** Current stage of a Board Break run. */
enum class BoardBreakPhase {
    /** Waiting for the player to press and hold to gather ki. */
    READY,
    /** The player is holding; the fighter is in `CHARGE` pose and the needle sweeps the meter. */
    CHARGING,
    /** The player just let go; the fighter dashes in with a punch and broken layers fly apart. */
    STRIKING,
    /** All three strikes are finished. */
    OVER,
}

/** What happened on one of the three strikes of a run. */
data class StrikeOutcome(
    /** 1-based index of the strike (`1..3`). */
    val strikeNumber: Int,
    val material: StrikeMaterial,
    val rating: StrikeRating,
    val broken: Int,
    val total: Int,
    val needle: Float,
)

/** Something the screen shows, animates or buzzes for. */
sealed interface BoardBreakEvent {
    data object ChargeStarted : BoardBreakEvent

    data class StrikeLanded(val outcome: StrikeOutcome) : BoardBreakEvent

    data class NextStrike(
        val strikeNumber: Int,
        val material: StrikeMaterial,
        val totalLayers: Int,
    ) : BoardBreakEvent

    data class RunComplete(val score: Int, val maxPossible: Int) : BoardBreakEvent
}

/**
 * Board Break (#44) as a pure simulation on the Training Ground stage ([WIDTH] x [HEIGHT] units): a
 * stand next to the fighter holds stacked layers for 3 strikes ([StrikeMaterial.BOARDS],
 * [StrikeMaterial.BRICKS], [StrikeMaterial.ICE]). Holding ([startCharge]) puts the fighter in the
 * `CHARGE` pose while a needle sweeps back and forth across a meter (`0f..1f`, center
 * [METER_CENTER]). Releasing ([releaseCharge]) rates the strike ([StrikeRating]), dashes the
 * fighter into the stand with a punch, splits and flings the top `broken` layers apart, and adds
 * `broken` to [score].
 */
class BoardBreak(val config: BoardBreakConfig) {
    companion object {
        const val WIDTH = 160f
        const val HEIGHT = 128f
        const val FLOOR_Y = 115f
        const val FIGHTER_REST_X = 30f
        const val FIGHTER_STRIKE_X = 56f
        const val FIGHTER_TOP = 67f
        const val STAND_X = 96f

        const val STRIKES_PER_RUN = 3
        const val METER_CENTER = 0.5f
        const val STEP = 1f / 60f
        const val STRIKE_SETTLE_SECONDS = 1.05f
    }

    private val _outcomes = mutableListOf<StrikeOutcome>()
    val outcomes: List<StrikeOutcome>
        get() = _outcomes

    private val _events = mutableListOf<BoardBreakEvent>()
    val events: List<BoardBreakEvent>
        get() = _events

    var time = 0f
        private set

    var phase = BoardBreakPhase.READY
        private set

    /** 0-based index into [StrikeMaterial.entries] (`0..2`). */
    var strikeIndex = 0
        private set

    /** 1-based strike number (`1..3`). */
    val strikeNumber: Int
        get() = strikeIndex + 1

    val currentMaterial: StrikeMaterial
        get() = StrikeMaterial.entries[strikeIndex]

    val currentTotalLayers: Int
        get() = config.layersFor(currentMaterial)

    /** Needle position on the meter in `0f..1f`; `0.5f` is the bright center. */
    var needle = 0f
        private set

    var needleDirection = 1
        private set

    /** Total layers broken across all completed strikes in this run. */
    var score = 0
        private set

    /**
     * Outcome of the strike currently being shown during [BoardBreakPhase.STRIKING], or the last
     * one.
     */
    var lastOutcome: StrikeOutcome? = null
        private set

    /** Game time when the current strike landed, for punch dash and flying layer pieces. */
    var strikeStartedAt = -10f
        private set

    val isOver: Boolean
        get() = phase == BoardBreakPhase.OVER

    private var carry = 0f

    /** Begins charging ki when the player presses and holds on the screen. */
    fun startCharge(): Boolean {
        _events.clear()
        if (phase != BoardBreakPhase.READY) return false
        phase = BoardBreakPhase.CHARGING
        needle = 0f
        needleDirection = 1
        _events += BoardBreakEvent.ChargeStarted
        return true
    }

    /**
     * Releases the gathered ki at the current [needle] position, rating the strike and breaking the
     * corresponding number of layers on the stand.
     */
    fun releaseCharge(): StrikeOutcome? {
        _events.clear()
        if (phase != BoardBreakPhase.CHARGING) return null
        return landStrike(needle)
    }

    /**
     * Convenience for tests: charges and releases directly at [needlePosition] (`0f..1f`) when
     * [phase] is [BoardBreakPhase.READY] or [BoardBreakPhase.CHARGING].
     */
    fun strikeAt(needlePosition: Float): StrikeOutcome? {
        _events.clear()
        if (phase != BoardBreakPhase.READY && phase != BoardBreakPhase.CHARGING) return null
        needle = needlePosition.coerceIn(0f, 1f)
        return landStrike(needle)
    }

    /** Advances the game by [seconds] in fixed steps of [STEP]. */
    fun step(seconds: Float) {
        _events.clear()
        if (isOver || seconds <= 0f) return
        carry += seconds
        while (carry >= STEP && !isOver) {
            carry -= STEP
            tick()
        }
    }

    private fun tick() {
        time += STEP
        when (phase) {
            BoardBreakPhase.READY,
            BoardBreakPhase.OVER -> Unit
            BoardBreakPhase.CHARGING -> {
                needle += needleDirection * config.needleSpeed * STEP
                if (needle >= 1f) {
                    needle = (2f - needle).coerceIn(0f, 1f)
                    needleDirection = -1
                } else if (needle <= 0f) {
                    needle = (-needle).coerceIn(0f, 1f)
                    needleDirection = 1
                }
            }
            BoardBreakPhase.STRIKING -> {
                if (time - strikeStartedAt >= STRIKE_SETTLE_SECONDS) {
                    advanceAfterStrike()
                }
            }
        }
    }

    private fun landStrike(atNeedle: Float): StrikeOutcome {
        val total = currentTotalLayers
        val rating = config.rate(atNeedle)
        val broken = config.layersBroken(rating, total)
        val outcome =
            StrikeOutcome(
                strikeNumber = strikeNumber,
                material = currentMaterial,
                rating = rating,
                broken = broken,
                total = total,
                needle = atNeedle,
            )
        score += broken
        lastOutcome = outcome
        _outcomes += outcome
        phase = BoardBreakPhase.STRIKING
        strikeStartedAt = time
        _events += BoardBreakEvent.StrikeLanded(outcome)
        return outcome
    }

    private fun advanceAfterStrike() {
        if (strikeIndex + 1 < STRIKES_PER_RUN) {
            strikeIndex += 1
            phase = BoardBreakPhase.READY
            needle = 0f
            needleDirection = 1
            _events +=
                BoardBreakEvent.NextStrike(
                    strikeNumber = strikeNumber,
                    material = currentMaterial,
                    totalLayers = currentTotalLayers,
                )
        } else {
            phase = BoardBreakPhase.OVER
            _events +=
                BoardBreakEvent.RunComplete(score = score, maxPossible = config.maxPossibleScore)
        }
    }
}
