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

/**
 * Where and how the two fighters stand at one moment of the fight screen (#14): the poses and the
 * offsets (in stage pixels, see [ArenaStagePixels]) while [FightState.playback] plays an event,
 * [progress] running from 0 to 1. Pure Kotlin, so the composables only place what this says.
 */
data class FightScene(
    val playerPose: FighterPose,
    val playerMood: FighterMood,
    val playerDx: Float,
    val opponentPose: OpponentPose,
    val opponentDx: Float,
    /** Where the ki blast flies, 0 at the fighter's hands to 1 at the opponent; `null` for none. */
    val blast: Float?,
    /** A short white flash over the stage when the opponent's special lands, 0 to 1. */
    val flash: Float,
    /** The text floating up over a fighter, or `null`. */
    val float: FloatText?,
) {
    companion object {
        fun of(fight: FightState, progress: Float): FightScene {
            val event = fight.playback.playing
            val index = fight.playback.playingIndex
            val playerDown = fight.hp(Side.PLAYER) == 0
            val opponentDown = fight.hp(Side.OPPONENT) == 0
            val arc = sin(progress * PI).toFloat()
            val shake = sin(progress * PI * 6).toFloat() * 2f * (1f - progress)
            val won = event == null && fight.battle.outcome == Outcome.WIN

            var playerPose = if (won) FighterPose.CHARGE else FighterPose.STANCE
            var playerDx = 0f
            var opponentPose = OpponentPose.IDLE
            var opponentDx = 0f
            var blast: Float? = null
            var flash = 0f
            var float: FloatText? = null

            when (event) {
                is BattleEvent.Hit ->
                    if (event.actor == Side.PLAYER) {
                        playerPose =
                            if (fight.isKick(index)) FighterPose.KICK else FighterPose.PUNCH
                        playerDx = arc * LUNGE
                        if (progress > HIT_AT) opponentPose = OpponentPose.HIT
                        opponentDx = shake
                        float = FloatText.damage(Side.OPPONENT, event)
                    } else {
                        if (event.guarded) playerPose = FighterPose.GUARD
                        opponentDx = -arc * LUNGE
                        playerDx = shake
                        float = FloatText.damage(Side.PLAYER, event)
                    }
                is BattleEvent.Dodge ->
                    if (event.actor == Side.PLAYER) {
                        playerPose =
                            if (fight.isKick(index)) FighterPose.KICK else FighterPose.PUNCH
                        playerDx = arc * LUNGE
                        opponentDx = arc * HOP
                        float = FloatText(Side.OPPONENT, FloatKind.DODGE)
                    } else {
                        opponentDx = -arc * LUNGE
                        playerDx = -arc * HOP
                        float = FloatText(Side.PLAYER, FloatKind.DODGE)
                    }
                is BattleEvent.Guard ->
                    if (event.actor == Side.PLAYER) {
                        playerPose = FighterPose.GUARD
                        float = FloatText(Side.PLAYER, FloatKind.GUARD)
                    } else {
                        float = FloatText(Side.OPPONENT, FloatKind.GUARD)
                    }
                is BattleEvent.Special ->
                    if (event.actor == Side.PLAYER) {
                        playerPose = FighterPose.BLAST
                        blast = progress
                        if (progress > BLAST_HITS_AT) {
                            opponentPose = OpponentPose.HIT
                            opponentDx = shake
                        }
                        float = FloatText(Side.OPPONENT, FloatKind.DAMAGE, event.damage)
                    } else {
                        opponentDx = -arc * LUNGE * 1.5f
                        playerDx = shake * 2f
                        flash = arc
                        float = FloatText(Side.PLAYER, FloatKind.DAMAGE, event.damage)
                    }
                is BattleEvent.Ko -> if (event.actor == Side.PLAYER) playerPose = FighterPose.CHARGE
                BattleEvent.TimeUp,
                null -> Unit
            }
            if (opponentDown) opponentPose = OpponentPose.KO
            return FightScene(
                playerPose = playerPose,
                playerMood = if (playerDown) FighterMood.TIRED else FighterMood.READY,
                playerDx = playerDx,
                opponentPose = opponentPose,
                opponentDx = opponentDx,
                blast = blast,
                flash = flash,
                float = float,
            )
        }

        /** How far an attacker steps forward and a dodger hops back, in stage pixels. */
        const val LUNGE = 12f
        const val HOP = 8f

        private const val HIT_AT = 0.3f
        private const val BLAST_HITS_AT = 0.7f
    }
}

enum class FloatKind {
    DAMAGE,
    CRIT,
    DODGE,
    GUARD,
}

/** A short text that floats up over [over]: damage numbers, "CRIT!", "DODGE!" or "GUARD". */
data class FloatText(val over: Side, val kind: FloatKind, val damage: Int = 0) {
    companion object {
        fun damage(over: Side, hit: BattleEvent.Hit) =
            FloatText(over, if (hit.critical) FloatKind.CRIT else FloatKind.DAMAGE, hit.damage)
    }
}
