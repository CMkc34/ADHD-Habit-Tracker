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

import kotlin.test.Test
import kotlin.test.assertEquals

class OpponentAiTest {

    private fun state(hp: Int, charge: Int): BattleState {
        val s = Battle.start(Combatant("P", 100, 10, 0, 0), Combatant("O", 100, 10, 0, 0))
        return s.copy(opponent = s.opponent.copy(hp = hp, charge = charge))
    }

    @Test
    fun `uses special when ready`() {
        val action = OpponentAi.choose(state(hp = 10, charge = 3), FixedRandom(0.0))
        assertEquals(BattleAction.SPECIAL, action)
    }

    @Test
    fun `guards a quarter of the time below 30 percent hp`() {
        assertEquals(BattleAction.GUARD, OpponentAi.choose(state(29, 0), FixedRandom(0.24)))
        assertEquals(BattleAction.STRIKE, OpponentAi.choose(state(29, 0), FixedRandom(0.25)))
    }

    @Test
    fun `strikes otherwise`() {
        assertEquals(BattleAction.STRIKE, OpponentAi.choose(state(30, 2), FixedRandom(0.0)))
    }
}
