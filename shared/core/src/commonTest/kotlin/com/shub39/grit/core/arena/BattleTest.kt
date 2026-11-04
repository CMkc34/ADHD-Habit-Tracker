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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class BattleTest {

    private fun fighter(
        name: String = "P",
        hp: Int = 100,
        attack: Int = 10,
        speed: Int = 0,
        focus: Int = 0,
    ) = Combatant(name, hp, attack, speed, focus)

    /** Rolls for one CPU strike that is neither dodged nor critical, at 1.0 times attack. */
    private fun cpuStrike() = FixedRandom(MISS, MID, MISS)

    private fun BattleState.bestAction() =
        if (player.specialReady) BattleAction.SPECIAL else BattleAction.STRIKE

    @Test
    fun `combatant values follow the stat formulas`() {
        val kai =
            Combatant.of(
                "Kai",
                rankNumber = 3,
                strengthLevel = 2,
                speedLevel = 4,
                staminaLevel = 5,
                focusLevel = 1,
            )
        assertEquals(Combatant("Kai", maxHp = 98, attack = 8, speed = 4, focus = 1), kai)
    }

    @Test
    fun `same seed gives the same fight`() {
        fun play(seed: Int): BattleState {
            val random = Random(seed)
            val player = fighter(speed = 1, focus = 2)
            var s = Battle.start(player, fighter("O", speed = 2, focus = 1), random)
            while (!s.isOver) s = Battle.act(s, s.bestAction(), random)
            return s
        }
        repeat(20) { seed -> assertEquals(play(seed), play(seed)) }
    }

    @Test
    fun `strike damage stays within 85 to 115 percent`() {
        val start = Battle.start(fighter(attack = 20), fighter("O", hp = 1000))
        val low = Battle.act(start, BattleAction.STRIKE, FixedRandom(MISS, 0.0, MISS))
        val high = Battle.act(start, BattleAction.STRIKE, FixedRandom(MISS, 1.0, MISS))
        assertEquals(17, (low.log.first() as BattleEvent.Hit).damage)
        assertEquals(23, (high.log.first() as BattleEvent.Hit).damage)
    }

    @Test
    fun `strike deals at least 1 damage`() {
        val start = Battle.start(fighter(attack = 1), fighter("O"))
        val guarded = start.copy(opponent = start.opponent.copy(guarding = true))
        val after = Battle.act(guarded, BattleAction.STRIKE, FixedRandom(MISS, 0.0, MISS))
        assertEquals(1, (after.log.first() as BattleEvent.Hit).damage)
    }

    @Test
    fun `guard halves damage until the guard's next turn`() {
        val start = Battle.start(fighter(), fighter("O", attack = 20))
        val after = Battle.act(start, BattleAction.GUARD, cpuStrike())
        assertEquals(BattleEvent.Guard(Side.PLAYER), after.log[0])
        val expected = BattleEvent.Hit(Side.OPPONENT, 10, critical = false, guarded = true)
        assertEquals(expected, after.log[1])
        assertEquals(90, after.player.hp)
        assertFalse(after.player.guarding, "guard ends at the start of the player's next turn")
    }

    @Test
    fun `guard gives one extra charge`() {
        val start = Battle.start(fighter(focus = 0), fighter("O", focus = 5))
        assertEquals(1, start.player.charge)
        val after = Battle.act(start, BattleAction.GUARD, cpuStrike())
        // 1 + 1 for guarding + 1 at the start of the next turn
        assertEquals(3, after.player.charge)
    }

    @Test
    fun `special ignores dodge and guard and resets charge`() {
        val start = Battle.start(fighter(attack = 10), fighter("O", speed = 100), cpuStrike())
        val ready =
            start.copy(
                player = start.player.copy(charge = 3),
                opponent = start.opponent.copy(guarding = true),
            )
        // a 0.0 roll would dodge any strike; the special must not roll at all
        val after = Battle.act(ready, BattleAction.SPECIAL, FixedRandom(0.0))
        assertEquals(BattleEvent.Special(Side.PLAYER, 20), after.log[start.log.size])
        assertEquals(80, after.opponent.hp)
        assertTrue(after.player.charge < 3, "charge was reset before the next turn's gain")
    }

    @Test
    fun `special without full charge is rejected`() {
        val start = Battle.start(fighter(), fighter("O"))
        assertEquals(2, start.player.charge)
        assertFailsWith<IllegalArgumentException> {
            Battle.act(start, BattleAction.SPECIAL, FixedRandom(MISS))
        }
    }

    @Test
    fun `charge gains a focus bonus and is capped at 3`() {
        assertEquals(2, Battle.start(fighter(focus = 3), fighter("O", focus = 3)).player.charge)
        assertEquals(1, Battle.start(fighter(focus = 2), fighter("O", focus = 3)).player.charge)

        var s = Battle.start(fighter(focus = 3), fighter("O", focus = 3))
        repeat(3) { s = Battle.act(s, BattleAction.GUARD, cpuStrike()) }
        assertEquals(3, s.player.charge)
    }

    @Test
    fun `faster side goes first and the player wins ties`() {
        val tie = Battle.start(fighter(speed = 2), fighter("O", speed = 2), cpuStrike())
        assertEquals(0, tie.turnsTaken)

        val playerFaster = Battle.start(fighter(speed = 3), fighter("O", speed = 2), cpuStrike())
        assertEquals(0, playerFaster.turnsTaken)

        val cpuFaster = Battle.start(fighter(speed = 1), fighter("O", speed = 2), cpuStrike())
        assertEquals(1, cpuFaster.turnsTaken)
        assertEquals(Side.OPPONENT, cpuFaster.log.single().actor)
    }

    @Test
    fun `turns alternate after the first`() {
        var s = Battle.start(fighter(), fighter("O"))
        repeat(3) { s = Battle.act(s, BattleAction.GUARD, cpuStrike()) }
        val sides = listOf(Side.PLAYER, Side.OPPONENT)
        assertEquals(sides + sides + sides, s.log.map { it.actor })
        assertEquals(4, s.round)
    }

    @Test
    fun `dodge chance grows with speed lead and is capped at 25 percent`() {
        assertEquals(0.0, Battle.dodgeChance(fighter(speed = 5), fighter(speed = 1)))
        assertEquals(0.09, Battle.dodgeChance(fighter(speed = 1), fighter(speed = 4)), 1e-9)
        assertEquals(0.25, Battle.dodgeChance(fighter(speed = 0), fighter(speed = 50)))
    }

    @Test
    fun `a dodged strike deals no damage`() {
        // the CPU is 20 speed ahead, so its dodge chance is capped at 25 percent
        val start = Battle.start(fighter(speed = 0), fighter("O", speed = 20), cpuStrike())
        val dodged = Battle.act(start, BattleAction.STRIKE, FixedRandom(0.24, MISS, MID, MISS))
        assertEquals(BattleEvent.Dodge(Side.PLAYER), dodged.log[start.log.size])
        assertEquals(start.opponent.hp, dodged.opponent.hp)

        val hit = Battle.act(start, BattleAction.STRIKE, FixedRandom(0.25, MID, MISS))
        assertIs<BattleEvent.Hit>(hit.log[start.log.size])
    }

    @Test
    fun `crit chance grows with focus and is capped at 30 percent`() {
        assertEquals(0.05, Battle.critChance(fighter(focus = 0)), 1e-9)
        assertEquals(0.15, Battle.critChance(fighter(focus = 5)), 1e-9)
        assertEquals(0.30, Battle.critChance(fighter(focus = 40)), 1e-9)
    }

    @Test
    fun `a critical strike deals one and a half times damage`() {
        val start = Battle.start(fighter(attack = 10), fighter("O"))
        val after = Battle.act(start, BattleAction.STRIKE, FixedRandom(MISS, MID, 0.0, MISS))
        val expected = BattleEvent.Hit(Side.PLAYER, 15, critical = true, guarded = false)
        assertEquals(expected, after.log.first())
    }

    @Test
    fun `knockout ends the fight with a win`() {
        val start = Battle.start(fighter(attack = 10), fighter("O", hp = 5))
        val after = Battle.act(start, BattleAction.STRIKE, FixedRandom(MISS, MID, MISS))
        assertEquals(Outcome.WIN, after.outcome)
        assertEquals(0, after.opponent.hp)
        assertEquals(BattleEvent.Ko(Side.PLAYER), after.log.last())
        assertEquals(1, after.turnsTaken, "the CPU does not reply after a KO")
    }

    @Test
    fun `being knocked out is a loss`() {
        val start = Battle.start(fighter(hp = 5), fighter("O", attack = 10))
        val after = Battle.act(start, BattleAction.STRIKE, FixedRandom(MISS, MID, MISS))
        assertEquals(Outcome.LOSS, after.outcome)
        assertEquals(BattleEvent.Ko(Side.OPPONENT), after.log.last())
    }

    @Test
    fun `no action is accepted after the outcome`() {
        val start = Battle.start(fighter(attack = 10), fighter("O", hp = 5))
        val over = Battle.act(start, BattleAction.STRIKE, FixedRandom(MISS, MID, MISS))
        assertFailsWith<IllegalStateException> {
            Battle.act(over, BattleAction.GUARD, FixedRandom(MISS))
        }
    }

    @Test
    fun `after 30 rounds equal hp shares are a draw`() {
        var s = Battle.start(fighter(hp = 1000, attack = 1), fighter("O", hp = 1000, attack = 1))
        while (!s.isOver) s = Battle.act(s, s.bestAction(), FixedRandom(MISS, MID, MISS))
        assertEquals(Outcome.DRAW, s.outcome)
        assertEquals(BattleEvent.TimeUp, s.log.last())
        assertEquals(ArenaRules.MAX_ROUNDS * 2, s.turnsTaken)
        assertEquals(ArenaRules.MAX_ROUNDS, s.round)
        assertEquals(s.player.hp, s.opponent.hp)
    }

    @Test
    fun `after 30 rounds the higher hp share wins`() {
        // player 100 max hp, CPU 50 max hp with attack 1, one round before the limit
        val base = Battle.start(fighter(hp = 100), fighter("O", hp = 50, attack = 1))
        fun decide(playerHp: Int, opponentHp: Int): Outcome? {
            val lastRound =
                base.copy(
                    player = base.player.copy(hp = playerHp),
                    opponent = base.opponent.copy(hp = opponentHp),
                    turnsTaken = ArenaRules.MAX_ROUNDS * 2 - 2,
                )
            return Battle.act(lastRound, BattleAction.GUARD, cpuStrike()).outcome
        }
        // the CPU's guarded strike still deals the minimum of 1 before the limit is checked
        assertEquals(Outcome.DRAW, decide(playerHp = 81, opponentHp = 40))
        assertEquals(Outcome.WIN, decide(playerHp = 82, opponentHp = 40))
        assertEquals(Outcome.LOSS, decide(playerHp = 80, opponentHp = 40))
    }
}
