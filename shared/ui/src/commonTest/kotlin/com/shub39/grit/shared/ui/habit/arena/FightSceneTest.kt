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

import com.shub39.grit.core.arena.Battle
import com.shub39.grit.core.arena.BattleAction
import com.shub39.grit.core.arena.BattleEvent
import com.shub39.grit.core.arena.Combatant
import com.shub39.grit.core.arena.FightLog
import com.shub39.grit.core.arena.FightPlayback
import com.shub39.grit.core.arena.Opponent
import com.shub39.grit.core.arena.Side
import com.shub39.grit.core.arena.StartedFight
import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.shared.ui.habit.fighter.FighterPose
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate

class FightSceneTest {

    private val player = Combatant("Kai", maxHp = 90, attack = 12, speed = 3, focus = 3)
    private val opponent = Opponent.SNOOZE_BUTTON
    private val started =
        StartedFight(FightLog.start(opponent, LocalDate(2026, 11, 5), now = 1L), player)

    private fun fight(playback: FightPlayback, kicks: Set<Int> = emptySet()) =
        FightState(
            started = started,
            playback = playback,
            wasNext = true,
            stage = ArenaStage.BEDROOM,
            lighting = ArenaLighting.DAY,
            kicks = kicks,
        )

    /** Plays until the next event of [type] by [side] is the one playing. */
    private inline fun <reified T : BattleEvent> playingNext(
        side: Side,
        seed: Int = 1,
    ): FightPlayback {
        val random = Random(seed)
        var state = Battle.start(player, opponent.combatant, random)
        repeat(200) {
            val action =
                if (state.player.specialReady) BattleAction.SPECIAL else BattleAction.STRIKE
            val after = Battle.act(state, action, random)
            var playback = FightPlayback(state, after)
            while (!playback.isDone) {
                val event = playback.playing
                if (event is T && event.actor == side) return playback
                playback = playback.next()
            }
            if (after.isOver) state = Battle.start(player, opponent.combatant, random)
            else state = after
        }
        error("no ${T::class.simpleName} by $side")
    }

    @Test
    fun `idle fighters stand still`() {
        val scene =
            FightScene.of(
                fight(FightPlayback.opening(Battle.start(player, opponent.combatant))),
                0f,
            )

        assertEquals(FighterPose.STANCE, scene.playerPose)
        assertEquals(OpponentPose.IDLE, scene.opponentPose)
        assertEquals(0f, scene.playerDx)
        assertNull(scene.float)
        assertNull(scene.blast)
    }

    @Test
    fun `a player strike is a punch or a kick and makes the opponent flinch`() {
        val playback = playingNext<BattleEvent.Hit>(Side.PLAYER)
        val punch = FightScene.of(fight(playback), 0.5f)
        val kick = FightScene.of(fight(playback, kicks = setOf(playback.playingIndex)), 0.5f)

        assertEquals(FighterPose.PUNCH, punch.playerPose)
        assertEquals(FighterPose.KICK, kick.playerPose)
        assertEquals(OpponentPose.HIT, punch.opponentPose)
        assertTrue(punch.playerDx > 0f)
        assertEquals(Side.OPPONENT, assertNotNull(punch.float).over)
    }

    @Test
    fun `the special fires a ki blast across the stage`() {
        val playback = playingNext<BattleEvent.Special>(Side.PLAYER)
        val scene = FightScene.of(fight(playback), 0.4f)

        assertEquals(FighterPose.BLAST, scene.playerPose)
        assertEquals(0.4f, scene.blast)
    }

    @Test
    fun `an opponent strike makes the opponent lunge left`() {
        val playback = playingNext<BattleEvent.Hit>(Side.OPPONENT)
        val scene = FightScene.of(fight(playback), 0.5f)

        assertTrue(scene.opponentDx < 0f)
        assertEquals(Side.PLAYER, assertNotNull(scene.float).over)
    }

    @Test
    fun `a knocked out opponent lies down and the winner charges ki`() {
        val playback = playingNext<BattleEvent.Ko>(Side.PLAYER)
        val scene = FightScene.of(fight(playback), 0.5f)

        assertEquals(OpponentPose.KO, scene.opponentPose)
        assertEquals(FighterPose.CHARGE, scene.playerPose)
        assertEquals(FighterMood.READY, scene.playerMood)
    }
}
