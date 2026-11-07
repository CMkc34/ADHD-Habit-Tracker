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
package com.shub39.grit.core.arena

import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Pure turn-based battle engine (SPEC 9.4) for one round of a [Match]. All randomness comes from
 * the injected [Random], and the draws happen in a fixed order, so the same seed and the same
 * player actions always give the same fight.
 *
 * Invalid input throws instead of returning an unchanged state:
 * - [act] with SPECIAL and less than [ArenaRules.SPECIAL_COST] charge: [IllegalArgumentException].
 * - [act] after the outcome is set: [IllegalStateException].
 *
 * The UI should only offer SPECIAL when [Corner.specialReady] and stop offering actions once
 * [BattleState.isOver].
 */
object Battle {

    /**
     * Starts a fight. The faster side goes first, the player on a tie. If the CPU is faster, its
     * opening turn is already resolved in the returned state, which uses [random].
     */
    fun start(
        player: Combatant,
        opponent: Combatant,
        random: Random = Random.Default,
    ): BattleState {
        val state =
            BattleState(
                player = Corner(player, hp = player.maxHp),
                opponent = Corner(opponent, hp = opponent.maxHp),
            )
        if (opponent.speed <= player.speed) return beginTurn(state, Side.PLAYER)
        val afterCpu = cpuTurn(state, random)
        return if (afterCpu.isOver) afterCpu else beginTurn(afterCpu, Side.PLAYER)
    }

    /** Resolves the player's [action] and, unless the fight ended, the CPU's reply. */
    fun act(state: BattleState, action: BattleAction, random: Random): BattleState {
        check(!state.isOver) { "The battle is already over" }
        require(action != BattleAction.SPECIAL || state.player.specialReady) {
            "SPECIAL needs ${ArenaRules.SPECIAL_COST} charge"
        }
        val afterPlayer = resolve(state, Side.PLAYER, action, random)
        if (afterPlayer.isOver) return afterPlayer
        val afterCpu = cpuTurn(afterPlayer, random)
        if (afterCpu.isOver) return afterCpu
        return beginTurn(afterCpu, Side.PLAYER)
    }

    /** Dodge chance of [defender] against a STRIKE from [attacker]. */
    fun dodgeChance(attacker: Combatant, defender: Combatant): Double {
        val chance = ArenaRules.DODGE_PER_SPEED * (defender.speed - attacker.speed)
        return chance.coerceIn(0.0, ArenaRules.MAX_DODGE)
    }

    /** Critical hit chance of a STRIKE from [attacker]. */
    fun critChance(attacker: Combatant): Double {
        val chance = ArenaRules.BASE_CRIT + ArenaRules.CRIT_PER_FOCUS * attacker.focus
        return minOf(chance, ArenaRules.MAX_CRIT)
    }

    private fun cpuTurn(state: BattleState, random: Random): BattleState {
        val started = beginTurn(state, Side.OPPONENT)
        return resolve(started, Side.OPPONENT, OpponentAi.choose(started, random), random)
    }

    /** Start of [side]'s own turn: its guard ends and it gains charge. */
    private fun beginTurn(state: BattleState, side: Side): BattleState {
        val me = state.corner(side)
        val them = state.corner(side.other)
        var gain = ArenaRules.CHARGE_PER_TURN
        if (me.combatant.focus >= them.combatant.focus) gain += ArenaRules.FOCUS_BONUS_CHARGE
        val started = me.copy(guarding = false, charge = addCharge(me.charge, gain))
        return state.withCorner(side, started)
    }

    private fun resolve(
        state: BattleState,
        side: Side,
        action: BattleAction,
        random: Random,
    ): BattleState {
        val me = state.corner(side)
        val them = state.corner(side.other)
        val events = mutableListOf<BattleEvent>()
        var next = state
        when (action) {
            BattleAction.GUARD -> {
                val charge = addCharge(me.charge, ArenaRules.GUARD_CHARGE)
                next = next.withCorner(side, me.copy(guarding = true, charge = charge))
                events += BattleEvent.Guard(side)
            }
            BattleAction.SPECIAL -> {
                require(me.specialReady) { "SPECIAL needs ${ArenaRules.SPECIAL_COST} charge" }
                val damage = ArenaRules.SPECIAL_DAMAGE_FACTOR * me.combatant.attack
                next = next.withCorner(side, me.copy(charge = 0))
                next = next.withCorner(side.other, them.hurt(damage))
                events += BattleEvent.Special(side, damage)
            }
            BattleAction.STRIKE -> {
                if (random.nextDouble() < dodgeChance(me.combatant, them.combatant)) {
                    events += BattleEvent.Dodge(side)
                } else {
                    val hit = strike(me.combatant, guarded = them.guarding, side, random)
                    next = next.withCorner(side.other, them.hurt(hit.damage))
                    events += hit
                }
            }
        }
        next = next.copy(turnsTaken = next.turnsTaken + 1)

        val outcome =
            when {
                next.corner(side.other).hp == 0 -> {
                    events += BattleEvent.Ko(side)
                    if (side == Side.PLAYER) Outcome.WIN else Outcome.LOSS
                }
                // the clock is on the opponent's side: nobody down at the limit is a loss
                next.turnsTaken >= 2 * ArenaRules.MAX_ROUNDS -> {
                    events += BattleEvent.TimeUp
                    Outcome.LOSS
                }
                else -> null
            }
        return next.copy(outcome = outcome, log = next.log + events)
    }

    /** Rolls a STRIKE that was not dodged: damage factor first, then crit. */
    private fun strike(
        attacker: Combatant,
        guarded: Boolean,
        side: Side,
        random: Random,
    ): BattleEvent.Hit {
        val spread = ArenaRules.STRIKE_MAX_FACTOR - ArenaRules.STRIKE_MIN_FACTOR
        val factor = ArenaRules.STRIKE_MIN_FACTOR + random.nextDouble() * spread
        val critical = random.nextDouble() < critChance(attacker)
        var damage = attacker.attack * factor
        if (critical) damage *= ArenaRules.CRIT_MULTIPLIER
        if (guarded) damage *= ArenaRules.GUARD_DAMAGE_FACTOR
        val dealt = damage.roundToInt().coerceAtLeast(ArenaRules.MIN_DAMAGE)
        return BattleEvent.Hit(side, dealt, critical, guarded)
    }

    private fun Corner.hurt(damage: Int): Corner = copy(hp = (hp - damage).coerceAtLeast(0))

    private fun addCharge(charge: Int, gain: Int): Int = minOf(charge + gain, ArenaRules.MAX_CHARGE)
}
