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
import com.shub39.grit.core.arena.MatchState
import com.shub39.grit.core.arena.Opponent
import com.shub39.grit.core.arena.Side
import com.shub39.grit.core.arena.StartedFight
import com.shub39.grit.core.fighter.FighterCharacter
import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.shared.ui.habit.fighter.FighterPixels
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

    private fun geometry(o: Opponent = opponent): FightGeometry {
        fun fighter(pose: FighterPose) =
            FighterPixels.render(
                FighterCharacter.WIND_MONK,
                FighterRank.CONTENDER,
                FighterMood.READY,
                pose = pose,
            )
        val launch = OpponentPixels.launchPoint(o)
        return FightGeometry.of(
            punch = fighter(FighterPose.PUNCH),
            kick = fighter(FighterPose.KICK),
            stance = fighter(FighterPose.STANCE),
            idle = OpponentPixels.render(o, OpponentPose.IDLE),
            attack = OpponentPixels.render(o, OpponentPose.ATTACK),
            launch = StagePoint(launch.x, launch.y),
        )
    }

    private fun fight(playback: FightPlayback, kicks: Set<Int> = emptySet()) =
        FightState(
            started = started,
            match = MatchState(playback.after),
            playback = playback,
            wasNext = true,
            stage = ArenaStage.BEDROOM,
            lighting = ArenaLighting.DAY,
            kicks = kicks,
            intro = false,
        )

    private fun scene(fight: FightState, millis: Int) = FightScene.of(fight, millis, geometry())

    /** Plays until the next event of type [T] by [side] that matches [where] is playing. */
    private inline fun <reified T : BattleEvent> playingNext(
        side: Side,
        seed: Int = 1,
        fighter: Combatant = player,
        action: (Int) -> BattleAction = { BattleAction.STRIKE },
        ending: Boolean = false,
        where: (T) -> Boolean = { true },
    ): FightPlayback {
        val random = Random(seed)
        var state = Battle.start(fighter, opponent.combatant, random)
        repeat(400) { turn ->
            val move = if (state.player.specialReady) BattleAction.SPECIAL else action(turn)
            val after = Battle.act(state, move, random)
            var playback = FightPlayback(state, after)
            while (!playback.isDone) {
                val event = playback.playing
                val fits = event is T && event.actor == side && where(event)
                if (fits && after.isOver == ending) return playback
                playback = playback.next()
            }
            state = if (after.isOver) Battle.start(fighter, opponent.combatant, random) else after
        }
        error("no ${T::class.simpleName} by $side")
    }

    @Test
    fun `idle fighters stand still`() {
        val scene = scene(fight(FightPlayback.opening(Battle.start(player, opponent.combatant))), 0)

        assertEquals(FighterPose.STANCE, scene.playerPose)
        assertEquals(OpponentPose.IDLE, scene.opponentPose)
        assertEquals(0f, scene.playerDx)
        assertNull(scene.float)
        assertNull(scene.blast)
        assertNull(scene.spark)
    }

    @Test
    fun `a punch winds up, dashes over and lands on the body`() {
        val fight = fight(playingNext<BattleEvent.Hit>(Side.PLAYER) { !it.guarded })
        val g = geometry()

        assertTrue(scene(fight, 60).playerDx < 0f, "no wind-up")
        val landed = scene(fight, 320)
        assertEquals(FighterPose.PUNCH, landed.playerPose)
        assertEquals(g.punchDx, landed.playerDx)
        assertEquals(OpponentPose.HIT, landed.opponentPose)
        assertTrue(landed.opponentFlash)
        assertTrue(landed.shake > 0f)
        assertEquals(g.punchAt, assertNotNull(landed.spark).at)
        assertEquals(Side.OPPONENT, assertNotNull(landed.float).over)
        // back home at the end
        assertEquals(0f, scene(fight, FightScene.durationOf(fight.playback.playing)).playerDx)
    }

    @Test
    fun `the dash brings the fist into the opponent's outline`() {
        for (o in Opponent.entries) {
            val g = geometry(o)
            assertTrue(g.punchDx > ArenaStagePixels.PLAYER_LEFT / 2f, "$o: dash ${g.punchDx}")
            val idle = OpponentPixels.render(o)
            val x = (g.punchAt.x - ArenaStagePixels.OPPONENT_LEFT).toInt()
            val rows = (g.punchAt.y - FightGeometry.OPPONENT_TOP).toInt().let { it - 14..it + 14 }
            assertTrue(
                rows.any { y -> y in 0 until idle.height && idle[x, y] != 0 },
                "$o: the fist lands in the air at ${g.punchAt}",
            )
        }
    }

    @Test
    fun `a kick shows the kick pose`() {
        val playback = playingNext<BattleEvent.Hit>(Side.PLAYER)
        val kick = scene(fight(playback, kicks = setOf(playback.playingIndex)), 320)

        assertEquals(FighterPose.KICK, kick.playerPose)
        assertEquals(geometry().kickDx, kick.playerDx)
    }

    @Test
    fun `a dodge leaves a ghost and the blow cuts the air`() {
        // the snooze button is faster than this slow fighter, so it dodges now and then
        val slow = player.copy(speed = 0)
        val fight = fight(playingNext<BattleEvent.Dodge>(Side.PLAYER, fighter = slow))
        val scene = scene(fight, 320)

        assertTrue(scene.opponentDx > 0f)
        assertTrue(scene.opponentDy < 0f)
        assertEquals(0f, scene.opponentGhost)
        assertNotNull(scene.whiff)
        assertEquals(FloatKind.DODGE, assertNotNull(scene.float).kind)
    }

    @Test
    fun `the opponent winds up, throws its own attack and the fighter is hit`() {
        val fight = fight(playingNext<BattleEvent.Hit>(Side.OPPONENT) { !it.guarded })
        val impact = assertNotNull(FightScene.impactAt(fight.playback.playing))

        assertTrue(scene(fight, 100).opponentTurn)
        assertEquals(OpponentPose.WINDUP, scene(fight, 400).opponentPose)
        val flying = scene(fight, impact - 100)
        assertEquals(OpponentPose.ATTACK, flying.opponentPose)
        assertNotNull(flying.shot)
        val hit = scene(fight, impact + 20)
        assertEquals(FighterPose.HURT, hit.playerPose)
        assertTrue(hit.playerFlash)
        assertNotNull(hit.spark)
        assertEquals(Side.PLAYER, assertNotNull(hit.float).over)
    }

    @Test
    fun `a raised guard stays up and blocks with a blue spark`() {
        // the player guards every turn, so every CPU strike hits the guard
        val fight =
            fight(
                playingNext<BattleEvent.Hit>(Side.OPPONENT, action = { BattleAction.GUARD }) {
                    it.guarded
                }
            )
        val impact = assertNotNull(FightScene.impactAt(fight.playback.playing))

        assertEquals(FighterPose.GUARD, scene(fight, 0).playerPose)
        val blocked = scene(fight, impact + 20)
        assertEquals(FighterPose.GUARD, blocked.playerPose)
        assertTrue(assertNotNull(blocked.spark).blocked)
        assertEquals(FloatKind.BLOCKED, assertNotNull(blocked.float).kind)
    }

    @Test
    fun `the special fires a ki blast across the stage`() {
        val fight = fight(playingNext<BattleEvent.Special>(Side.PLAYER))

        assertEquals(FighterPose.CHARGE, scene(fight, 100).playerPose)
        val flying = scene(fight, 550)
        assertEquals(FighterPose.BLAST, flying.playerPose)
        assertNotNull(flying.blast)
        assertTrue(scene(fight, 760).flash > 0f)
    }

    @Test
    fun `a knocked out opponent lies down and the winner charges ki`() {
        val scene = scene(fight(playingNext<BattleEvent.Ko>(Side.PLAYER, ending = true)), 500)

        assertEquals(OpponentPose.KO, scene.opponentPose)
        assertEquals(FighterPose.CHARGE, scene.playerPose)
        assertEquals(FighterMood.READY, scene.playerMood)
    }

    @Test
    fun `every move plays long enough to read and its blow lands inside it`() {
        val events =
            listOf(
                BattleEvent.Hit(Side.PLAYER, 5, critical = false, guarded = false),
                BattleEvent.Hit(Side.OPPONENT, 5, critical = false, guarded = true),
                BattleEvent.Dodge(Side.PLAYER),
                BattleEvent.Dodge(Side.OPPONENT),
                BattleEvent.Guard(Side.PLAYER),
                BattleEvent.Guard(Side.OPPONENT),
                BattleEvent.Special(Side.PLAYER, 10),
                BattleEvent.Special(Side.OPPONENT, 10),
                BattleEvent.Ko(Side.PLAYER),
                BattleEvent.TimeUp,
            )
        for (event in events) {
            val duration = FightScene.durationOf(event)
            assertTrue(duration >= 600, "$event plays only $duration ms")
            FightScene.impactAt(event)?.let { assertTrue(it in 1 until duration, "$event") }
        }
    }
}
