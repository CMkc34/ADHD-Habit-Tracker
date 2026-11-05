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
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FightPlaybackTest {

    private val player = Combatant("Kai", maxHp = 60, attack = 10, speed = 3, focus = 3)
    private val slow = Combatant("cpu", maxHp = 50, attack = 8, speed = 1, focus = 1)
    private val fast = Combatant("cpu", maxHp = 50, attack = 8, speed = 9, focus = 1)

    @Test
    fun `opening without a cpu move has nothing to play`() {
        val playback = FightPlayback.opening(Battle.start(player, slow))

        assertTrue(playback.isDone)
        assertNull(playback.playing)
        assertEquals(-1, playback.playingIndex)
        assertEquals(60, playback.hp(Side.PLAYER))
    }

    @Test
    fun `opening plays the faster cpu's first move`() {
        val started = Battle.start(player, fast, FixedRandom(MISS, MID, MISS))
        val playback = FightPlayback.opening(started)

        assertFalse(playback.isDone)
        val hit = assertIs<BattleEvent.Hit>(playback.playing)
        assertEquals(Side.OPPONENT, hit.actor)
        assertEquals(0, playback.playingIndex)
        assertEquals(60 - 8, playback.hp(Side.PLAYER))
        assertTrue(playback.next().isDone)
    }

    @Test
    fun `hp drops event by event and ends at the engine's values`() {
        val start = Battle.start(player, slow)
        val after = Battle.act(start, BattleAction.STRIKE, FixedRandom(MISS, MID, MISS, MISS, MID, MISS))
        var playback = FightPlayback(start, after)

        assertEquals(2, playback.events.size)
        // the player's strike is playing: only the opponent is hurt
        assertEquals(50 - 10, playback.hp(Side.OPPONENT))
        assertEquals(60, playback.hp(Side.PLAYER))
        assertEquals(1, playback.shownLog.size)

        playback = playback.next()
        assertEquals(60 - 8, playback.hp(Side.PLAYER))
        assertEquals(2, playback.shownLog.size)
        assertEquals(1, playback.playingIndex)

        playback = playback.next()
        assertTrue(playback.isDone)
        assertEquals(after.player.hp, playback.hp(Side.PLAYER))
        assertEquals(after.opponent.hp, playback.hp(Side.OPPONENT))
        assertEquals(after.player.charge, playback.charge(Side.PLAYER))
        assertEquals(after.log, playback.shownLog)
    }

    @Test
    fun `a played special empties the charge right away`() {
        var state = Battle.start(player, slow)
        val random = FixedRandom(MISS, MID, MISS)
        while (!state.player.specialReady) state = Battle.act(state, BattleAction.GUARD, random)
        val playback = FightPlayback(state, Battle.act(state, BattleAction.SPECIAL, random))

        assertIs<BattleEvent.Special>(playback.playing)
        assertEquals(0, playback.charge(Side.PLAYER))
        assertEquals(state.opponent.hp - 20, playback.hp(Side.OPPONENT))
    }

    @Test
    fun `hp never shows below zero`() {
        var state = Battle.start(player, slow.copy(maxHp = 5))
        state = Battle.act(state, BattleAction.STRIKE, Random(1))
        val playback = FightPlayback(Battle.start(player, slow.copy(maxHp = 5)), state)

        assertEquals(0, playback.hp(Side.OPPONENT))
        assertIs<BattleEvent.Ko>(playback.events.last())
    }
}
