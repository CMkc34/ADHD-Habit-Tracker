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
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LadderTest {

    @Test
    fun `ten opponents in the agreed order`() {
        assertEquals(
            listOf(
                "snooze_button",
                "couch_gremlin",
                "doomscroll_imp",
                "laundry_mountain",
                "inbox_hydra",
                "distraction_swarm",
                "time_thief",
                "deadline_golem",
                "overwhelm_ogre",
                "procrastination_dragon",
            ),
            Ladder.opponents.map { it.id },
        )
        assertEquals((1..10).toList(), Ladder.opponents.map { it.number })
    }

    @Test
    fun `ids are unique and found again`() {
        assertEquals(10, Ladder.opponents.map { it.id }.toSet().size)
        Ladder.opponents.forEach { assertEquals(it, Opponent.byId(it.id)) }
        assertNull(Opponent.byId("unknown"))
    }

    @Test
    fun `only the first opponent is open at the start`() {
        assertEquals(listOf(Opponent.SNOOZE_BUTTON), Ladder.unlocked(emptySet()))
        assertEquals(Opponent.SNOOZE_BUTTON, Ladder.nextChallenge(emptySet()))
    }

    @Test
    fun `a win opens the next opponent`() {
        val beaten = setOf(Opponent.SNOOZE_BUTTON)
        assertEquals(
            listOf(Opponent.SNOOZE_BUTTON, Opponent.COUCH_GREMLIN),
            Ladder.unlocked(beaten),
        )
        assertTrue(Ladder.isUnlocked(Opponent.COUCH_GREMLIN, beaten))
        assertFalse(Ladder.isUnlocked(Opponent.DOOMSCROLL_IMP, beaten))
        assertEquals(Opponent.COUCH_GREMLIN, Ladder.nextChallenge(beaten))
    }

    @Test
    fun `beaten opponents stay open for rematches`() {
        val beaten = Ladder.opponents.take(4).toSet()
        val unlocked = Ladder.unlocked(beaten)
        assertEquals(Ladder.opponents.take(5), unlocked)
        beaten.forEach { assertTrue(Ladder.isUnlocked(it, beaten)) }
        assertEquals(Opponent.INBOX_HYDRA, Ladder.nextChallenge(beaten))
    }

    @Test
    fun `losses and draws open nothing`() {
        // Only wins go into the beaten set, so fighting without winning keeps the ladder closed.
        assertFalse(Ladder.isUnlocked(Opponent.COUCH_GREMLIN, emptySet()))
    }

    @Test
    fun `the whole ladder beaten leaves no next challenge`() {
        val all = Ladder.opponents.toSet()
        assertEquals(Ladder.opponents, Ladder.unlocked(all))
        assertNull(Ladder.nextChallenge(all))
    }

    @Test
    fun `opponents scale evenly without styles`() {
        Ladder.opponents.forEach {
            val level = it.number + 1
            assertEquals(Combatant(it.id, it.maxHp, 8 + 2 * level, level, level - 1), it.combatant)
        }
        Ladder.opponents.zipWithNext().forEach { (lower, higher) ->
            assertTrue(higher.maxHp > lower.maxHp, "$higher must have more HP than $lower")
        }
    }

    @Test
    fun `opponent combatant carries its values`() {
        val golem = Opponent.DEADLINE_GOLEM
        assertEquals(
            Combatant("deadline_golem", golem.maxHp, golem.attack, golem.speed, golem.focus),
            golem.combatant,
        )
        assertEquals(9, golem.recommendedRankNumber)
        assertEquals(Opponent.TIME_THIEF, golem.previous)
        assertEquals(Opponent.OVERWHELM_OGRE, golem.next)
    }
}
