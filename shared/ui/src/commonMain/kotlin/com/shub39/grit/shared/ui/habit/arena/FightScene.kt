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
package com.shub39.grit.shared.ui.habit.arena

import com.shub39.grit.core.arena.BattleEvent
import com.shub39.grit.core.arena.Outcome
import com.shub39.grit.core.arena.Side
import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.shared.ui.habit.fighter.FighterPose
import kotlin.math.PI
import kotlin.math.sin

/** A point on the stage, in stage pixels (see [ArenaStagePixels]). */
data class StagePoint(val x: Float, val y: Float)

/**
 * Where and how the two fighters stand at one moment of the fight screen: poses, offsets (in stage
 * pixels), what flies and what flashes while [FightState.playback] plays an event, [millis] into
 * it. Pure Kotlin, so the composables only place what this says.
 *
 * Reworked 2026-11-07 so fights feel satisfying: strikes dash over and land on the body with a
 * short freeze, a spark and a shake; every opponent answers with its own [OpponentAttack]; guards
 * stay up and shimmer until the guard's next turn; dodges jump away and leave the blow in the air.
 */
data class FightScene(
    val playerPose: FighterPose,
    val playerMood: FighterMood,
    val playerDx: Float,
    val opponentPose: OpponentPose,
    val opponentDx: Float,
    val playerDy: Float = 0f,
    val opponentDy: Float = 0f,
    /** The fighter's picture flashes white as a blow lands. */
    val playerFlash: Boolean = false,
    val opponentFlash: Boolean = false,
    /** A fading copy left behind by a dodge, at this dx; `null` for none. */
    val playerGhost: Float? = null,
    val opponentGhost: Float? = null,
    /** Where the ki blast flies, 0 at the fighter's hands to 1 at the opponent; `null` for none. */
    val blast: Float? = null,
    /** The opponent's attack in flight, `null` for none. */
    val shot: Shot? = null,
    val spark: Spark? = null,
    /** Where a strike cut through the air after a dodge, `null` for none. */
    val whiff: StagePoint? = null,
    /** How far the whole stage shakes, in stage pixels. */
    val shake: Float = 0f,
    /** A white flash over the stage when a special lands, 0 to 1. */
    val flash: Float = 0f,
    /** The text floating up over a fighter, or `null`; [floatAge] runs from 0 to 1. */
    val float: FloatText? = null,
    val floatAge: Float = 0f,
    /** The opponent's move is playing. */
    val opponentTurn: Boolean = false,
) {
    companion object {
        fun of(fight: FightState, millis: Int, geometry: FightGeometry): FightScene {
            val event = fight.playback.playing
            val playerDown = fight.hp(Side.PLAYER) == 0
            val opponentDown = fight.hp(Side.OPPONENT) == 0
            val won = event == null && fight.battle.outcome == Outcome.WIN
            val idle =
                FightScene(
                    playerPose =
                        when {
                            won -> FighterPose.CHARGE
                            fight.guarding(Side.PLAYER) -> FighterPose.GUARD
                            else -> FighterPose.STANCE
                        },
                    playerMood = if (playerDown) FighterMood.TIRED else FighterMood.READY,
                    playerDx = 0f,
                    opponentPose =
                        if (fight.guarding(Side.OPPONENT)) OpponentPose.GUARD
                        else OpponentPose.IDLE,
                    opponentDx = 0f,
                )
            val t = millis.toFloat()
            val kick = fight.isKick(fight.playback.playingIndex)
            val path = OpponentAttack.path(fight.opponent)
            val scene =
                when (event) {
                    is BattleEvent.Special ->
                        if (event.actor == Side.PLAYER) playerSpecial(idle, event, t, geometry)
                        else opponentAttack(idle, event, path, t, geometry)
                    is BattleEvent.Hit,
                    is BattleEvent.Dodge ->
                        if (event.actor == Side.PLAYER) playerStrike(idle, event, kick, t, geometry)
                        else opponentAttack(idle, event, path, t, geometry)
                    is BattleEvent.Guard ->
                        if (event.actor == Side.PLAYER)
                            idle.copy(
                                playerPose = FighterPose.GUARD,
                                float = FloatText(Side.PLAYER, FloatKind.GUARD),
                                floatAge = t / FLOAT_MILLIS,
                            )
                        else
                            idle.copy(
                                opponentTurn = true,
                                opponentPose =
                                    if (t < READY) OpponentPose.IDLE else OpponentPose.GUARD,
                                float =
                                    if (t < READY) null
                                    else FloatText(Side.OPPONENT, FloatKind.GUARD),
                                floatAge = (t - READY) / FLOAT_MILLIS,
                            )
                    is BattleEvent.Ko ->
                        if (event.actor == Side.PLAYER) idle.copy(playerPose = FighterPose.CHARGE)
                        else idle.copy(opponentTurn = true)
                    BattleEvent.TimeUp,
                    null -> idle
                }
            return if (opponentDown) scene.copy(opponentPose = OpponentPose.KO) else scene
        }

        /** How long [event] plays, in milliseconds. */
        fun durationOf(event: BattleEvent?): Int =
            when (event) {
                null -> 0
                is BattleEvent.Hit,
                is BattleEvent.Dodge ->
                    if (event.actor == Side.PLAYER) STRIKE_TOTAL
                    else opponentImpact(false).toInt() + OPPONENT_AFTER
                is BattleEvent.Special ->
                    if (event.actor == Side.PLAYER) SPECIAL_TOTAL
                    else opponentImpact(true).toInt() + OPPONENT_AFTER
                is BattleEvent.Guard -> if (event.actor == Side.PLAYER) 700 else 950
                is BattleEvent.Ko -> 1000
                BattleEvent.TimeUp -> 800
            }

        /** When a blow of [event] lands, for the vibration; `null` if nothing lands. */
        fun impactAt(event: BattleEvent?): Int? =
            when (event) {
                is BattleEvent.Hit ->
                    if (event.actor == Side.PLAYER) STRIKE_LANDS.toInt()
                    else opponentImpact(false).toInt()
                is BattleEvent.Special ->
                    if (event.actor == Side.PLAYER) BLAST_LANDS.toInt()
                    else opponentImpact(true).toInt()
                else -> null
            }

        // -- the player's moves ---------------------------------------------------------------

        private fun playerStrike(
            idle: FightScene,
            event: BattleEvent,
            kick: Boolean,
            t: Float,
            g: FightGeometry,
        ): FightScene {
            val contact = if (kick) g.kickDx else g.punchDx
            val at = if (kick) g.kickAt else g.punchAt
            val strikePose = if (kick) FighterPose.KICK else FighterPose.PUNCH
            val back = phase(t, RETURN_AT, RETURN_AT + 240f)
            val dx =
                when {
                    t < WIND_UP -> -3f * ease(t / WIND_UP)
                    t < STRIKE_LANDS -> lerp(-3f, contact, ease(phase(t, WIND_UP, STRIKE_LANDS)))
                    t < RETURN_AT -> contact
                    else -> lerp(contact, 0f, ease(back))
                }
            val base =
                idle.copy(
                    playerPose =
                        if (t >= STRIKE_LANDS - 60f && t < RETURN_AT) strikePose
                        else FighterPose.STANCE,
                    playerDx = dx,
                    playerDy = if (t >= RETURN_AT) -hop(back) * 4f else 0f,
                )
            val after = t - STRIKE_LANDS
            return when (event) {
                is BattleEvent.Hit -> {
                    val knock =
                        when {
                            event.guarded -> 1f
                            event.critical -> 6f
                            else -> 4f
                        }
                    val shake =
                        when {
                            event.critical -> 4f
                            event.guarded -> 1.5f
                            else -> 3f
                        }
                    base
                        .landed(after = after, at = at, blocked = event.guarded, shake = shake)
                        .copy(
                            opponentPose =
                                if (event.guarded) OpponentPose.GUARD
                                else if (after >= 0 && t < RETURN_AT) OpponentPose.HIT
                                else idle.opponentPose,
                            opponentFlash = !event.guarded && after in 0f..90f,
                            opponentDx = knockBack(t, STRIKE_LANDS, RETURN_AT, knock),
                            float = if (after < 0) null else FloatText.damage(Side.OPPONENT, event),
                            floatAge = after / FLOAT_MILLIS,
                        )
                }
                else -> {
                    // the opponent jumps away just before the blow, which cuts through the air
                    val hop = phase(t, STRIKE_LANDS - 40f, STRIKE_LANDS + 160f)
                    val home = phase(t, RETURN_AT + 40f, RETURN_AT + 290f)
                    base.copy(
                        opponentDx =
                            if (t < RETURN_AT + 40f) DODGE_HOP * ease(hop)
                            else DODGE_HOP * (1f - ease(home)),
                        opponentDy = -hop(hop) * 10f,
                        opponentGhost =
                            if (t in (STRIKE_LANDS - 40f)..(STRIKE_LANDS + 260f)) 0f else null,
                        whiff = if (after in 0f..150f) at else null,
                        float = if (after < 0) null else FloatText(Side.OPPONENT, FloatKind.DODGE),
                        floatAge = after / FLOAT_MILLIS,
                    )
                }
            }
        }

        private fun playerSpecial(
            idle: FightScene,
            event: BattleEvent.Special,
            t: Float,
            g: FightGeometry,
        ): FightScene {
            val after = t - BLAST_LANDS
            return idle
                .copy(
                    playerPose =
                        when {
                            t < BLAST_FIRES -> FighterPose.CHARGE
                            t < BLAST_LANDS + 200f -> FighterPose.BLAST
                            else -> FighterPose.STANCE
                        },
                    blast =
                        if (t in BLAST_FIRES..BLAST_LANDS) phase(t, BLAST_FIRES, BLAST_LANDS)
                        else null,
                )
                .landed(after = after, at = g.punchAt, blocked = false, shake = 4f, big = true)
                .copy(
                    flash = if (after >= 0) (1f - after / 250f).coerceIn(0f, 1f) else 0f,
                    opponentPose =
                        if (after >= 0 && after < 450f) OpponentPose.HIT else idle.opponentPose,
                    opponentFlash = after in 0f..120f,
                    opponentDx = knockBack(t, BLAST_LANDS, BLAST_LANDS + 350f, 6f),
                    float =
                        if (after < 0) null
                        else FloatText(Side.OPPONENT, FloatKind.DAMAGE, event.damage),
                    floatAge = after / FLOAT_MILLIS,
                )
        }

        // -- the opponent's own attack --------------------------------------------------------

        private fun opponentAttack(
            idle: FightScene,
            event: BattleEvent,
            path: AttackPath,
            t: Float,
            g: FightGeometry,
        ): FightScene {
            val special = event is BattleEvent.Special
            val impact = opponentImpact(special)
            val launch = READY + WINDUP_MILLIS
            val after = t - impact
            val pose =
                when {
                    t < READY -> idle.opponentPose
                    t < launch -> OpponentPose.WINDUP
                    t < impact + 250f -> OpponentPose.ATTACK
                    else -> OpponentPose.IDLE
                }
            val flight = phase(t, launch, impact)
            val base = idle.copy(opponentTurn = true, opponentPose = pose)

            // the attack itself: thrown, shot, along the floor, or the opponent rushing in
            val rush = path == AttackPath.RUSH
            val opponentDx =
                if (!rush) 0f
                else
                    when {
                        t < launch -> 0f
                        t < impact -> g.rushDx * ease(flight)
                        t < impact + 300f -> g.rushDx
                        else -> g.rushDx * (1f - ease(phase(t, impact + 300f, impact + 550f)))
                    }
            val dodged = event is BattleEvent.Dodge
            val shot =
                if (rush || t < launch) null
                else if (t < impact) flying(path, g, flight, special)
                else if (dodged && after < 150f)
                    flying(path, g, 1f + after / 300f, special).copy(alpha = 1f - after / 150f)
                else if (path == AttackPath.CHAIN && after < 400f)
                // the watch is pulled back on its chain
                flying(path, g, 1f - ease(phase(after, 150f, 400f)), special)
                else null
            val scene = base.copy(opponentDx = opponentDx, shot = shot)

            if (dodged) {
                val hop = phase(t, impact - 150f, impact + 50f)
                val home = phase(t, impact + 450f, impact + 700f)
                return scene.copy(
                    playerDx =
                        if (t < impact + 450f) -DODGE_HOP * ease(hop)
                        else -DODGE_HOP * (1f - ease(home)),
                    playerDy = -hop(hop) * 8f,
                    playerGhost = if (t in (impact - 150f)..(impact + 150f)) 0f else null,
                    float = if (after < 0) null else FloatText(Side.PLAYER, FloatKind.DODGE),
                    floatAge = after / FLOAT_MILLIS,
                )
            }
            val guarded = event is BattleEvent.Hit && event.guarded
            val damageText =
                when (event) {
                    is BattleEvent.Hit -> FloatText.damage(Side.PLAYER, event)
                    is BattleEvent.Special -> FloatText(Side.PLAYER, FloatKind.DAMAGE, event.damage)
                    else -> null
                }
            val knock =
                when {
                    guarded -> 1f
                    special -> 6f
                    else -> 4f
                }
            val at = if (rush) g.rushAt else g.target
            return scene
                .landed(
                    after = after,
                    at = at,
                    blocked = guarded,
                    shake = if (special) 4f else if (guarded) 1.5f else 3f,
                    big = !guarded,
                )
                .copy(
                    playerPose =
                        when {
                            guarded -> FighterPose.GUARD
                            after >= 0 && after < 450f -> FighterPose.HURT
                            else -> idle.playerPose
                        },
                    playerFlash = !guarded && after in 0f..90f,
                    playerDx = -knockBack(t, impact, impact + 450f, knock),
                    flash = if (special && after >= 0) (1f - after / 250f).coerceIn(0f, 1f) else 0f,
                    float = if (after < 0) null else damageText,
                    floatAge = after / FLOAT_MILLIS,
                )
        }

        /** The opponent's attack [progress] of the way from its launch point to the fighter. */
        private fun flying(
            path: AttackPath,
            g: FightGeometry,
            progress: Float,
            special: Boolean,
        ): Shot {
            val from = g.launch
            val to = g.target
            val x = lerp(from.x, to.x, progress)
            val y =
                when (path) {
                    AttackPath.SHOT -> lerp(from.y, to.y, progress)
                    AttackPath.GROUND -> ArenaStagePixels.FOOT_Y.toFloat()
                    else -> lerp(from.y, to.y, progress) - sin(progress * PI).toFloat() * 10f
                }
            return Shot(
                at = StagePoint(x, y),
                special = special,
                chainFrom = if (path == AttackPath.CHAIN) from else null,
                trail = path == AttackPath.GROUND,
            )
        }

        /** Adds the spark, the freeze and the shake of a blow that landed [after] ms ago. */
        private fun FightScene.landed(
            after: Float,
            at: StagePoint,
            blocked: Boolean,
            shake: Float,
            big: Boolean = !blocked,
        ): FightScene {
            if (after < 0f) return this
            return copy(
                spark =
                    if (after < SPARK_MILLIS) Spark(at, after / SPARK_MILLIS, blocked, big)
                    else null,
                shake = shake * (1f - after / SHAKE_MILLIS).coerceIn(0f, 1f),
            )
        }

        /** Pushed back after the freeze, held, then back home at [home]. */
        private fun knockBack(t: Float, lands: Float, home: Float, distance: Float): Float =
            when {
                t < lands + FREEZE -> 0f
                t < home -> distance * ease(phase(t, lands + FREEZE, lands + FREEZE + 150f))
                else -> distance * (1f - ease(phase(t, home, home + 200f)))
            }

        private fun opponentImpact(special: Boolean): Float =
            READY + WINDUP_MILLIS + if (special) 400f else 300f

        private fun phase(t: Float, from: Float, to: Float): Float =
            ((t - from) / (to - from)).coerceIn(0f, 1f)

        private fun ease(x: Float): Float = x * x * (3f - 2f * x)

        private fun hop(x: Float): Float = sin(x * PI).toFloat()

        private fun lerp(a: Float, b: Float, x: Float): Float = a + (b - a) * x

        // -- timing, in milliseconds ----------------------------------------------------------

        private const val WIND_UP = 120f
        private const val STRIKE_LANDS = 300f
        private const val RETURN_AT = 760f
        private const val STRIKE_TOTAL = 1100
        private const val BLAST_FIRES = 350f
        private const val BLAST_LANDS = 750f
        private const val SPECIAL_TOTAL = 1350

        /** A short pause at the start of the opponent's move, so its turn reads as its own. */
        private const val READY = 300f
        private const val WINDUP_MILLIS = 250f
        private const val OPPONENT_AFTER = 700

        /** The freeze when a blow lands, before anybody is pushed back. */
        private const val FREEZE = 120f
        private const val SPARK_MILLIS = 300f
        private const val SHAKE_MILLIS = 300f
        private const val FLOAT_MILLIS = 700f

        /** How far a dodger jumps away, in stage pixels. */
        const val DODGE_HOP = 14f
    }
}

/** The opponent's attack in flight: [chainFrom] for a watch on a chain, [trail] for spikes. */
data class Shot(
    val at: StagePoint,
    val special: Boolean,
    val chainFrom: StagePoint? = null,
    val trail: Boolean = false,
    val alpha: Float = 1f,
)

/** The burst where a blow lands; pale blue and small on a guard. [age] runs from 0 to 1. */
data class Spark(val at: StagePoint, val age: Float, val blocked: Boolean, val big: Boolean)

enum class FloatKind {
    DAMAGE,
    CRIT,
    BLOCKED,
    DODGE,
    GUARD,
}

/** A short text that floats up over [over]: damage numbers, "CRIT!", "DODGE!" or "GUARD". */
data class FloatText(val over: Side, val kind: FloatKind, val damage: Int = 0) {
    companion object {
        fun damage(over: Side, hit: BattleEvent.Hit) =
            FloatText(
                over,
                when {
                    hit.guarded -> FloatKind.BLOCKED
                    hit.critical -> FloatKind.CRIT
                    else -> FloatKind.DAMAGE
                },
                hit.damage,
            )
    }
}
