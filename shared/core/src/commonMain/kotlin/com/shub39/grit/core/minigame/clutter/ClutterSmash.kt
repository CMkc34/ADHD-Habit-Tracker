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
import kotlin.math.hypot
import kotlin.math.min
import kotlin.random.Random

/**
 * The numbers of Clutter Smash (#43) that come from the fighter's stats, each with a cap so the
 * game stays fair: Strength breaks big junk in fewer hits, Focus fills the ki bar faster, Speed
 * shortens the pause between strikes, Stamina moves the red line a bit higher for more room.
 */
data class ClutterSmashConfig(
    /** Damage dealt to a piece of junk by one tap. */
    val hitDamage: Int,
    /** How much the ki bar (out of [ClutterSmash.MAX_KI]) fills when a piece of junk breaks. */
    val kiPerSmash: Float,
    /** Minimum pause in seconds between two taps. */
    val hitCooldown: Float,
    /** Y position of the dashed red line; smaller is higher up, leaving more room on the floor. */
    val lineY: Float,
) {
    companion object {
        fun of(stats: GameStats): ClutterSmashConfig =
            ClutterSmashConfig(
                hitDamage = 1 + min(2, (stats.strength - 1).coerceAtLeast(0) / 3),
                kiPerSmash = 15f + 1.5f * capped(stats.focus),
                hitCooldown = maxOf(0.10f, 0.28f - 0.02f * capped(stats.speed)),
                lineY = 38f - 1.2f * capped(stats.stamina),
            )

        /** Levels above 1 that count, at most 10. */
        private fun capped(level: Int): Int = (level - 1).coerceIn(0, 10)
    }
}

/**
 * The six kinds of everyday clutter that fall into the laundry room (#43). Small pieces break in
 * one hit; bigger pieces take two or three hits for a rookie fighter and fewer with more Strength.
 */
enum class JunkKind(val maxHp: Int, val width: Float, val height: Float) {
    /** A crumpled t-shirt. */
    SHIRT(maxHp = 2, width = 20f, height = 13f),
    /** An unopened letter with an urgent seal. */
    MAIL(maxHp = 1, width = 18f, height = 10f),
    /** A buzzing smartphone. */
    PHONE(maxHp = 2, width = 14f, height = 12f),
    /** A wide cardboard pizza box. */
    PIZZA_BOX(maxHp = 3, width = 24f, height = 12f),
    /** A coffee mug left standing around. */
    MUG(maxHp = 1, width = 15f, height = 11f),
    /** A single striped sock. */
    SOCK(maxHp = 1, width = 16f, height = 10f);

    val isBig: Boolean
        get() = maxHp > 1
}

/** One piece of falling or piled-up junk in one of the three columns. */
class Junk
internal constructor(
    val id: Int,
    val kind: JunkKind,
    val column: Int,
    var hp: Int = kind.maxHp,
    y: Float = 0f,
) {
    var y = y
        internal set

    /** True once it rests on the floor or on a landed pile. */
    var landed = false
        internal set

    /** Game time of the last hit that cracked it without breaking it yet. */
    var hitAt = -10f
        internal set

    val maxHp: Int
        get() = kind.maxHp

    val width: Float
        get() = kind.width

    val height: Float
        get() = kind.height

    val x: Float
        get() = ClutterSmash.columnCenterX(column) - width / 2f

    val centerX: Float
        get() = ClutterSmash.columnCenterX(column)

    val centerY: Float
        get() = y + height / 2f

    val isDamaged: Boolean
        get() = hp < maxHp
}

/** Why a Clutter Smash run ended. */
enum class EndReason {
    /** The 60-second clock ran out ("TIME!"). */
    TIME_UP,
    /** One of the three piles reached the dashed red line ("TOO HIGH!"). */
    TOO_HIGH,
}

/** A small ki blast flying from the fighter to a tapped piece of junk. */
data class StrikeBolt(val toX: Float, val toY: Float, val at: Float)

/** Something the screen shows, animates or buzzes for. */
sealed interface ClutterEvent {
    data class JunkCracked(val id: Int, val x: Float, val y: Float, val kind: JunkKind) :
        ClutterEvent

    data class JunkSmashed(
        val id: Int,
        val x: Float,
        val y: Float,
        val kind: JunkKind,
        val points: Int,
        val combo: Int,
    ) : ClutterEvent

    data object KiReady : ClutterEvent

    data class KiWaveCleared(val count: Int, val points: Int) : ClutterEvent

    data class GameOver(val reason: EndReason) : ClutterEvent
}

/**
 * Clutter Smash (#43) as a pure simulation on a laundry-room field of [WIDTH] x [HEIGHT] units: the
 * fighter stands on the left while junk falls into [COLUMNS] columns on the right and piles up on
 * the floor ([FLOOR_Y]). Tapping a piece of junk ([hit] or [tap]) sends a small ki blast that
 * cracks or smashes it; quick smashes within [COMBO_WINDOW] build a combo (`score ×combo`). Each
 * smash fills the ki bar; once full ([kiReady]), tapping the fighter ([fireKiWave]) sweeps a Ki
 * Wave across the room that clears all junk for [WAVE_POINTS] each. A pile reaching [config]'s red
 * line ends the run, otherwise the run ends after [DURATION] seconds.
 */
class ClutterSmash(val config: ClutterSmashConfig, private val random: Random = Random.Default) {
    companion object {
        const val WIDTH = 160f
        const val HEIGHT = 128f

        /** Floor row where the fighter and the bottoms of the three piles stand. */
        const val FLOOR_Y = 122f

        const val FIGHTER_LEFT = 6f
        const val FIGHTER_SIZE = 48f
        const val FIGHTER_TOP = FLOOR_Y - FIGHTER_SIZE

        const val COLUMNS = 3
        const val COLUMNS_LEFT = 58f
        const val COLUMN_WIDTH = 32f

        const val DURATION = 60f
        const val MAX_KI = 100f
        const val COMBO_WINDOW = 1.4f
        const val MAX_COMBO = 8

        /** Distance below the red line where "TOO HIGH!" starts flashing. */
        const val DANGER_DISTANCE = 18f

        const val SMASH_POINTS = 10
        const val WAVE_POINTS = 25

        private const val STEP = 1f / 60f

        fun columnCenterX(column: Int): Float = COLUMNS_LEFT + (column + 0.5f) * COLUMN_WIDTH
    }

    var score = 0
        private set

    var smashedCount = 0
        private set

    /** Seconds elapsed since the start of the run, up to [DURATION]. */
    var time = 0f
        private set

    val timeLeft: Float
        get() = (DURATION - time).coerceAtLeast(0f)

    var ki = 0f
        private set

    val kiShare: Float
        get() = (ki / MAX_KI).coerceIn(0f, 1f)

    val kiReady: Boolean
        get() = ki >= MAX_KI

    var combo = 0
        private set

    var isOver = false
        private set

    var endReason: EndReason? = null
        private set

    /** Game time of the last punch or kick. */
    var lastStrikeAt = -10f
        private set

    /** Alternates between punch (`false`) and kick (`true`) on each tap. */
    var lastStrikeKick = false
        private set

    var lastBolt: StrikeBolt? = null
        private set

    /** Game time of the last Ki Wave. */
    var lastWaveAt = -10f
        private set

    val junk: List<Junk>
        get() = _junk

    /** What happened in the last [step], [hit], [tap] or [fireKiWave]. */
    val events: List<ClutterEvent>
        get() = _events

    val canHit: Boolean
        get() = !isOver && time >= nextHitAt

    /** Top of the settled pile in [column], or [FLOOR_Y] when the column has no landed junk. */
    fun pileTop(column: Int): Float =
        _junk.filter { it.column == column && it.landed }.minOfOrNull { it.y } ?: FLOOR_Y

    /** Highest settled pile across all three columns (smallest y). */
    val highestPileTop: Float
        get() = (0 until COLUMNS).minOf { pileTop(it) }

    /** True when any settled pile is close enough to the red line to flash "TOO HIGH!". */
    val isDanger: Boolean
        get() = !isOver && highestPileTop <= config.lineY + DANGER_DISTANCE

    private val _junk = mutableListOf<Junk>()
    private val _events = mutableListOf<ClutterEvent>()

    private var nextId = 1
    private var nextSpawnAt = 0f
    private var nextHitAt = 0f
    private var lastSmashAt = -10f
    private var lastColumn = -1

    /** Advances the run by [seconds] in fixed small steps. */
    fun step(seconds: Float) {
        _events.clear()
        var left = seconds.coerceIn(0f, 0.25f)
        while (left > 0f && !isOver) {
            val dt = min(STEP, left)
            tick(dt)
            left -= dt
        }
    }

    /**
     * Strikes the piece of junk with [junkId]. Returns `true` if the fighter was ready to strike
     * and the piece existed.
     */
    fun hit(junkId: Int): Boolean {
        _events.clear()
        return performHit(junkId)
    }

    /**
     * Unleashes the Ki Wave when [kiReady] is true, clearing every piece of junk in the room for
     * [WAVE_POINTS] each.
     */
    fun fireKiWave(): Boolean {
        _events.clear()
        return performKiWave()
    }

    /**
     * Handles a tap at field coordinates ([x], [y]): taps on the fighter when [kiReady] fire the Ki
     * Wave; taps on or near a piece of junk strike the closest one.
     */
    fun tap(x: Float, y: Float): Boolean {
        _events.clear()
        if (isOver) return false
        if (kiReady && x < COLUMNS_LEFT && y in (FIGHTER_TOP - 16f)..(FLOOR_Y + 6f)) {
            return performKiWave()
        }
        val target =
            _junk
                .filter {
                    x in (it.x - 6f)..(it.x + it.width + 6f) &&
                        y in (it.y - 6f)..(it.y + it.height + 6f)
                }
                .minByOrNull { hypot(x - it.centerX, y - it.centerY) } ?: return false
        return performHit(target.id)
    }

    private fun performHit(junkId: Int): Boolean {
        if (!canHit) return false
        val index = _junk.indexOfFirst { it.id == junkId }
        if (index < 0) return false
        val target = _junk[index]
        nextHitAt = time + config.hitCooldown
        lastStrikeAt = time
        lastStrikeKick = !lastStrikeKick
        lastBolt = StrikeBolt(target.centerX, target.centerY, time)
        target.hp -= config.hitDamage
        target.hitAt = time
        if (target.hp > 0) {
            _events +=
                ClutterEvent.JunkCracked(target.id, target.centerX, target.centerY, target.kind)
            return true
        }
        _junk.removeAt(index)
        relayoutColumns()
        smashedCount++
        combo =
            if (lastSmashAt >= 0f && time - lastSmashAt <= COMBO_WINDOW) {
                min(MAX_COMBO, combo + 1)
            } else {
                1
            }
        lastSmashAt = time
        val points = SMASH_POINTS * combo
        score += points
        _events +=
            ClutterEvent.JunkSmashed(
                id = target.id,
                x = target.centerX,
                y = target.centerY,
                kind = target.kind,
                points = points,
                combo = combo,
            )
        val wasReady = kiReady
        ki = min(MAX_KI, ki + config.kiPerSmash)
        if (!wasReady && kiReady) {
            _events += ClutterEvent.KiReady
        }
        return true
    }

    private fun performKiWave(): Boolean {
        if (isOver || !kiReady) return false
        ki = 0f
        lastWaveAt = time
        val cleared = _junk.toList()
        _junk.clear()
        for (item in cleared) {
            score += WAVE_POINTS
            smashedCount++
            _events +=
                ClutterEvent.JunkSmashed(
                    id = item.id,
                    x = item.centerX,
                    y = item.centerY,
                    kind = item.kind,
                    points = WAVE_POINTS,
                    combo = 1,
                )
        }
        _events += ClutterEvent.KiWaveCleared(cleared.size, cleared.size * WAVE_POINTS)
        return true
    }

    private fun tick(dt: Float) {
        time += dt
        if (combo > 0 && time - lastSmashAt > COMBO_WINDOW) {
            combo = 0
        }
        if (time >= nextSpawnAt) {
            spawnJunk()
            val interval = maxOf(0.45f, 1.05f - 0.009f * time)
            nextSpawnAt = time + interval
        }
        moveJunk(dt)
        if (highestPileTop <= config.lineY) {
            isOver = true
            endReason = EndReason.TOO_HIGH
            _events += ClutterEvent.GameOver(EndReason.TOO_HIGH)
            return
        }
        if (time >= DURATION) {
            time = DURATION
            isOver = true
            endReason = EndReason.TIME_UP
            _events += ClutterEvent.GameOver(EndReason.TIME_UP)
        }
    }

    private fun spawnJunk() {
        var col = random.nextInt(COLUMNS)
        if (col == lastColumn && random.nextBoolean()) {
            col = (col + 1 + random.nextInt(COLUMNS - 1)) % COLUMNS
        }
        lastColumn = col
        val kinds = JunkKind.entries
        val kind = kinds[random.nextInt(kinds.size)]
        val topInCol = _junk.filter { it.column == col }.minOfOrNull { it.y } ?: FLOOR_Y
        val startY = minOf(0f, topInCol - kind.height - 4f)
        _junk += Junk(id = nextId++, kind = kind, column = col, y = startY)
    }

    private fun moveJunk(dt: Float) {
        val fallSpeed = 42f + 0.35f * time
        for (col in 0 until COLUMNS) {
            var supportY = FLOOR_Y
            var supportLanded = true
            for (item in _junk) {
                if (item.column != col) continue
                val restY = supportY - item.height
                if (item.y < restY) {
                    item.y = minOf(restY, item.y + fallSpeed * dt)
                } else {
                    item.y = restY
                }
                item.landed = supportLanded && item.y >= restY - 0.01f
                supportY = item.y
                supportLanded = item.landed
            }
        }
    }

    /** Marks items above a newly opened gap as falling again so the pile settles cleanly. */
    private fun relayoutColumns() {
        for (col in 0 until COLUMNS) {
            var supportY = FLOOR_Y
            var supportLanded = true
            for (item in _junk) {
                if (item.column != col) continue
                val restY = supportY - item.height
                item.landed = supportLanded && item.y >= restY - 0.01f
                supportY = item.y
                supportLanded = item.landed
            }
        }
    }
}
