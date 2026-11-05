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

/**
 * Every number of the arena fight (SPEC 9.4, 9.6) lives here, so balance changes touch one file.
 */
object ArenaRules {
    const val BASE_HP = 40
    const val HP_PER_RANK = 6
    const val HP_PER_STAMINA_LEVEL = 8
    const val BASE_ATTACK = 4
    const val ATTACK_PER_STRENGTH_LEVEL = 2

    const val STRIKE_MIN_FACTOR = 0.85
    const val STRIKE_MAX_FACTOR = 1.15
    const val MIN_DAMAGE = 1
    const val GUARD_DAMAGE_FACTOR = 0.5
    const val SPECIAL_DAMAGE_FACTOR = 2

    const val SPECIAL_COST = 3
    const val MAX_CHARGE = 3
    const val CHARGE_PER_TURN = 1
    const val FOCUS_BONUS_CHARGE = 1
    const val GUARD_CHARGE = 1

    const val DODGE_PER_SPEED = 0.03
    const val MAX_DODGE = 0.25
    const val BASE_CRIT = 0.05
    const val CRIT_PER_FOCUS = 0.02
    const val MAX_CRIT = 0.30
    const val CRIT_MULTIPLIER = 1.5

    const val MAX_ROUNDS = 30

    const val CPU_LOW_HP_SHARE = 0.30
    const val CPU_GUARD_CHANCE = 0.25

    /**
     * Opponent n of the ladder scales evenly with its level n + 1 (SPEC 9.5): speed = level (the
     * player still starts on the tie), focus = level − 1, attack like a fighter's plus a bonus.
     * Only HP is set per opponent in [Opponent], tuned by `LadderBalanceTest`.
     */
    const val OPPONENT_ATTACK_BONUS = 4
    const val OPPONENT_FOCUS_OFFSET = -1

    /** Fight tickets a day can earn, one per routine done today (SPEC 9.6). */
    const val TICKETS_PER_DAY = 3
}
