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
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * The numbers of Ki Barrage (#42) that come from the fighter's stats, each with a cap so the game
 * stays a game: Speed moves faster, Focus fires faster, Stamina gives hearts, Strength hits harder.
 */
data class KiBarrageConfig(
    /** How fast the fighter follows the finger, field units per second. */
    val moveSpeed: Float,
    /** Seconds between two ki bolts. */
    val fireInterval: Float,
    val hearts: Int,
    /** Damage of one ki bolt. */
    val boltDamage: Int,
) {
    companion object {
        fun of(stats: GameStats): KiBarrageConfig =
            KiBarrageConfig(
                moveSpeed = 70f + 12f * capped(stats.speed),
                fireInterval = maxOf(0.15f, 0.34f - 0.02f * capped(stats.focus)),
                hearts = 3 + min(3, (stats.stamina - 1).coerceAtLeast(0) / 2),
                boltDamage = 1 + min(3, (stats.strength - 1).coerceAtLeast(0) / 3),
            )

        /** Levels above 1 that count, at most 10. */
        private fun capped(level: Int): Int = (level - 1).coerceIn(0, 10)
    }
}

/** The small enemies of a wave, each from one of the everyday enemies. */
enum class MinionKind {
    /** A little alarm clock with bells (from the Snooze Button). */
    ALARM,
    /** A notification bubble with a red badge (from the Doomscroll Imp). */
    BUBBLE,
    /** A flying striped sock (from Laundry Mountain). */
    SOCK,
}

/** How a wave moves: entering on a curve and swaying, or diving in and zig-zagging. */
enum class Flight {
    SWOOP,
    ZIGZAG,
}

class Minion
internal constructor(
    val kind: MinionKind,
    val flight: Flight,
    /** Place in the wave, from 0. */
    val index: Int,
    val waveSize: Int,
    /** Wave time when it enters. */
    val enterAt: Float,
    var hp: Int,
) {
    var x = -50f
        internal set

    var y = -50f
        internal set

    var alive = true
        internal set

    /** Whether it is on the field already. */
    var entered = false
        internal set
}

class Boss internal constructor(val opponent: Opponent, val maxHp: Int, val enterAt: Float) {
    var hp = maxHp
        internal set

    var x = 0f
        internal set

    var y = -KiBarrage.BOSS_SIZE
        internal set

    /** Game time of the last hit, for the flinch. */
    var hitAt = -10f
        internal set

    /** Game time of the knock-out, `null` while it fights. */
    var koAt: Float? = null
        internal set

    /** Game time of the last volley, for the attack pose. */
    var firedAt = -10f
        internal set

    val canBeHit: Boolean
        get() = koAt == null && y >= KiBarrage.BOSS_TOP - 1f
}

data class Bolt(val x: Float, val y: Float)

data class Shot(val x: Float, val y: Float, val vx: Float, val vy: Float)

/** Something the screen shows, plays or buzzes for. */
sealed interface KiBarrageEvent {
    data class WaveStarted(val wave: Int, val boss: Opponent?) : KiBarrageEvent

    data class MinionPopped(val x: Float, val y: Float, val kind: MinionKind, val points: Int) :
        KiBarrageEvent

    data class BossHit(val x: Float, val y: Float, val points: Int) : KiBarrageEvent

    data class BossKnockedOut(val points: Int) : KiBarrageEvent

    data object PlayerHit : KiBarrageEvent

    data object GameOver : KiBarrageEvent
}

/**
 * Ki Barrage (#42) as a pure simulation on a field of [WIDTH] x [HEIGHT] units: the fighter at the
 * bottom follows the finger ([target]), ki bolts fire upward by themselves, waves of minions swoop
 * and zig-zag and drop shots, and every [BOSS_EVERY]th wave an arena opponent from [bosses] is the
 * boss. The screen calls [step] every frame and draws the state; [events] holds what happened in
 * the last step. The dice are injected, so tests replay a run exactly.
 */
class KiBarrage(
    val config: KiBarrageConfig,
    private val bosses: List<Opponent> = listOf(Opponent.entries.first()),
    private val random: Random = Random.Default,
) {
    companion object {
        const val WIDTH = 120f
        const val HEIGHT = 200f

        /** Top of the 48 x 48 fighter picture. */
        const val FIGHTER_TOP = 152f
        const val FIGHTER_SIZE = 48f

        /** Where the ki bolts leave: the ki ball at the fighter's hip, left of its centre. */
        const val KI_OFFSET_X = -16f
        const val KI_Y = 170f

        /** How far the fighter's centre can go; the ki ball then reaches from 6 to 96. */
        const val MIN_X = 22f
        const val MAX_X = 112f

        /** Minions stay where the ki reaches. */
        private const val FIELD_LEFT = 8f
        private const val FIELD_RIGHT = 96f

        const val MINION_SIZE = 11f
        const val BOSS_SIZE = 62f
        const val BOSS_TOP = 4f

        const val BOSS_EVERY = 3
        const val BOLT_SPEED = 120f
        const val INVULNERABLE = 1.5f
        const val WAVE_PAUSE = 1.5f

        const val MINION_POINTS = 10
        const val BOSS_HIT_POINTS = 25
        const val BOSS_KO_POINTS = 500

        private const val STEP = 1f / 60f
        private const val BOSS_ENTER = 2.5f
        private const val BOSS_FALL = 2f
    }

    /** Centre of the fighter, which follows [target]. */
    var fighterX = WIDTH / 2
        private set

    /**
     * Where the finger wants the fighter; the screen sets it while dragging. The ki ball sits left
     * of the fighter's centre, so the fighter may step a little out of the field on the right to
     * reach every minion.
     */
    var target = WIDTH / 2
        set(value) {
            field = value.coerceIn(MIN_X, MAX_X)
        }

    var hearts = config.hearts
        private set

    var score = 0
        private set

    /** Seconds since the start. */
    var time = 0f
        private set

    var wave = 0
        private set

    /** Game time of the last hit on the fighter. */
    var hurtAt = -10f
        private set

    var isOver = false
        private set

    val minions: List<Minion>
        get() = _minions

    val bolts: List<Bolt>
        get() = _bolts

    val shots: List<Shot>
        get() = _shots

    var boss: Boss? = null
        private set

    /** What happened in the last [step]. */
    val events: List<KiBarrageEvent>
        get() = _events

    val isInvulnerable: Boolean
        get() = time - hurtAt < INVULNERABLE

    private val _minions = mutableListOf<Minion>()
    private val _bolts = mutableListOf<Bolt>()
    private val _shots = mutableListOf<Shot>()
    private val _events = mutableListOf<KiBarrageEvent>()

    private var waveStart = 0f
    private var nextWaveAt = 0f
    private var nextBolt = 0f
    private var bossesBeaten = 0

    /** Advances the game by [seconds] (in fixed small steps, so a slow frame plays the same). */
    fun step(seconds: Float) {
        _events.clear()
        var left = seconds.coerceIn(0f, 0.25f)
        while (left > 0f && !isOver) {
            val dt = min(STEP, left)
            tick(dt)
            left -= dt
        }
    }

    private fun tick(dt: Float) {
        time += dt
        if (wave == 0 || (waveCleared() && time >= nextWaveAt)) startWave()
        moveFighter(dt)
        fire()
        moveBolts(dt)
        moveMinions(dt)
        moveBoss(dt)
        moveShots(dt)
        if (waveCleared() && nextWaveAt <= waveStart) nextWaveAt = time + WAVE_PAUSE
    }

    private fun waveCleared(): Boolean {
        if (_minions.any { it.alive }) return false
        val ko = boss?.let { it.koAt ?: return false }
        return ko == null || time - ko > BOSS_FALL
    }

    private fun startWave() {
        wave++
        waveStart = time
        nextWaveAt = time
        _minions.clear()
        boss = null
        if (wave % BOSS_EVERY == 0 && bosses.isNotEmpty()) {
            val opponent = bosses[bossesBeaten % bosses.size]
            boss = Boss(opponent, maxHp = 10 + 6 * bossesBeaten, enterAt = time)
            _events += KiBarrageEvent.WaveStarted(wave, opponent)
            return
        }
        val size = min(4 + wave, 10)
        val flight = if (wave % 2 == 1) Flight.SWOOP else Flight.ZIGZAG
        val hp = 1 + (wave - 1) / 4
        repeat(size) { i ->
            val kind =
                when (flight) {
                    Flight.SWOOP -> MinionKind.ALARM
                    Flight.ZIGZAG -> if (i % 2 == 0) MinionKind.SOCK else MinionKind.BUBBLE
                }
            _minions += Minion(kind, flight, i, size, enterAt = 0.2f * i, hp = hp)
        }
        _events += KiBarrageEvent.WaveStarted(wave, null)
    }

    private fun moveFighter(dt: Float) {
        val delta = target - fighterX
        val move = config.moveSpeed * dt
        fighterX += delta.coerceIn(-move, move)
    }

    private fun fire() {
        if (time < nextBolt || time - hurtAt < 0.3f) return
        _bolts += Bolt(fighterX + KI_OFFSET_X, KI_Y)
        nextBolt = time + config.fireInterval
    }

    private fun moveBolts(dt: Float) {
        val moved = _bolts.map { it.copy(y = it.y - BOLT_SPEED * dt) }
        _bolts.clear()
        for (bolt in moved) {
            if (bolt.y < -8f) continue
            if (hitMinion(bolt) || hitBoss(bolt)) continue
            _bolts += bolt
        }
    }

    private fun hitMinion(bolt: Bolt): Boolean {
        val minion =
            _minions.firstOrNull {
                it.alive &&
                    it.entered &&
                    bolt.x in (it.x - 1f)..(it.x + MINION_SIZE + 1f) &&
                    bolt.y in (it.y - 2f)..(it.y + MINION_SIZE)
            } ?: return false
        minion.hp -= config.boltDamage
        if (minion.hp <= 0) {
            minion.alive = false
            score += MINION_POINTS
            _events +=
                KiBarrageEvent.MinionPopped(
                    minion.x + MINION_SIZE / 2,
                    minion.y + MINION_SIZE / 2,
                    minion.kind,
                    MINION_POINTS,
                )
        }
        return true
    }

    private fun hitBoss(bolt: Bolt): Boolean {
        val boss = boss ?: return false
        if (!boss.canBeHit) return false
        if (bolt.x !in (boss.x + 8f)..(boss.x + 56f) || bolt.y !in (boss.y + 20f)..(boss.y + 60f)) {
            return false
        }
        boss.hp -= config.boltDamage
        boss.hitAt = time
        score += BOSS_HIT_POINTS
        _events += KiBarrageEvent.BossHit(bolt.x, bolt.y, BOSS_HIT_POINTS)
        if (boss.hp <= 0) {
            boss.hp = 0
            boss.koAt = time
            bossesBeaten++
            score += BOSS_KO_POINTS
            _events += KiBarrageEvent.BossKnockedOut(BOSS_KO_POINTS)
            _shots.clear()
        }
        return true
    }

    private fun moveMinions(dt: Float) {
        val u = time - waveStart
        val descent = 1.5f + 0.5f * wave
        val shotRate = 0.12f + 0.03f * wave
        for (m in _minions) {
            if (!m.alive || u < m.enterAt) continue
            val t = u - m.enterAt
            val (x, y) = position(m, t)
            m.x = x
            m.y = y + descent * (t - 2f).coerceAtLeast(0f)
            m.entered = m.y > 0f
            if (m.entered && random.nextFloat() < shotRate * dt) {
                _shots += Shot(m.x + MINION_SIZE / 2, m.y + MINION_SIZE, 0f, 32f + 2f * wave)
            }
            // a minion that reaches the fighter rams it and is gone
            if (m.y > FIGHTER_TOP) {
                m.alive = false
                hurt()
            }
        }
    }

    /** Where the minion is [t] seconds after it entered, before it starts to sink. */
    private fun position(m: Minion, t: Float): Pair<Float, Float> {
        val u = t * 10f
        val i = m.index
        val width = FIELD_RIGHT - FIELD_LEFT - MINION_SIZE - 8f
        val spread = width / (m.waveSize - 1).coerceAtLeast(1)
        return when (m.flight) {
            Flight.SWOOP -> {
                val k = min(1f, u / 18f)
                val e = 1 - (1 - k) * (1 - k)
                val tx = FIELD_LEFT + 4f + i * spread + 4f * sin(u / 6f + i)
                val ty = 30f + (i % 3) * 12f + 4f * sin(u / 5f)
                val sx = -15f + i * 6f
                val sy = -12f
                val x = sx + (tx - sx) * e + (1 - e) * 30f * sin(e * 3f)
                Pair(x, sy + (ty - sy) * e)
            }
            Flight.ZIGZAG -> {
                val side = if (i % 2 == 1) 1f else -1f
                val middle = (FIELD_LEFT + FIELD_RIGHT - MINION_SIZE) / 2
                val x = middle + side * 36f * sin(u / 9f + i)
                Pair(x, -12f + min(u, 40f) * 1.6f + (i / 2) * 12f)
            }
        }
    }

    private fun moveBoss(dt: Float) {
        val boss = boss ?: return
        val u = time - boss.enterAt
        boss.x = (WIDTH - BOSS_SIZE) / 2 + 26f * sin(u * 0.9f)
        val ko = boss.koAt
        if (ko != null) {
            boss.y += 30f * dt
            return
        }
        boss.y = minOf(BOSS_TOP, -BOSS_SIZE + (BOSS_TOP + BOSS_SIZE) * u / BOSS_ENTER)
        val interval = maxOf(0.6f, 1.1f - 0.1f * bossesBeaten)
        if (boss.canBeHit && time - boss.firedAt >= interval) {
            boss.firedAt = time
            val speed = 34f + 3f * bossesBeaten
            for (vx in listOf(-14f, 0f, 14f)) {
                _shots += Shot(boss.x + BOSS_SIZE / 2, boss.y + 50f, vx, speed)
            }
        }
    }

    private fun moveShots(dt: Float) {
        val moved = _shots.map { it.copy(x = it.x + it.vx * dt, y = it.y + it.vy * dt) }
        _shots.clear()
        for (shot in moved) {
            if (shot.y > HEIGHT || shot.x < -4f || shot.x > WIDTH + 4f) continue
            val hits =
                shot.x in (fighterX - 9f)..(fighterX + 9f) &&
                    shot.y in (FIGHTER_TOP + 4f)..(FIGHTER_TOP + FIGHTER_SIZE)
            if (hits && !isInvulnerable) {
                hurt()
                continue
            }
            _shots += shot
        }
    }

    private fun hurt() {
        if (isInvulnerable || isOver) return
        hurtAt = time
        hearts--
        _events += KiBarrageEvent.PlayerHit
        if (hearts <= 0) {
            isOver = true
            _events += KiBarrageEvent.GameOver
        }
    }
}
