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

import kotlin.random.Random

/** CPU behaviour (SPEC 9.4): special when ready, sometimes guard when low, otherwise strike. */
object OpponentAi {
    fun choose(state: BattleState, random: Random): BattleAction {
        val me = state.opponent
        if (me.specialReady) return BattleAction.SPECIAL
        val low = me.hp < me.combatant.maxHp * ArenaRules.CPU_LOW_HP_SHARE
        if (low && random.nextDouble() < ArenaRules.CPU_GUARD_CHANCE) return BattleAction.GUARD
        return BattleAction.STRIKE
    }
}
