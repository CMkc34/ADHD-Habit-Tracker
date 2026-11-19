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
package com.shub39.grit.shared.ui.habit.sprite

import com.shub39.grit.core.arena.Opponent
import com.shub39.grit.core.fighter.FighterCharacter
import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.shared.ui.habit.arena.ArenaStage
import com.shub39.grit.shared.ui.habit.arena.OpponentPose
import com.shub39.grit.shared.ui.habit.fighter.FighterPose
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SpriteArtTest {

    @Test
    fun `characters without art keep the pixel art`() {
        assertNull(
            SpriteArt.fighter(FighterCharacter.WIND_MONK, FighterPose.PUNCH, FighterMood.READY)
        )
        assertNull(SpriteArt.opponent(Opponent.PROCRASTINATION_DRAGON, OpponentPose.IDLE))
        assertNull(SpriteArt.stage(ArenaStage.VOLCANO))
    }

    @Test
    fun `a pose with a picture shows it`() {
        assertEquals(
            "nyx_kick",
            SpriteArt.fighter(FighterCharacter.SHADOW_RUNNER, FighterPose.KICK, FighterMood.READY),
        )
        assertEquals(
            "snooze_button_idle",
            SpriteArt.opponent(Opponent.SNOOZE_BUTTON, OpponentPose.IDLE),
        )
        assertEquals("stage_screen_world", SpriteArt.stage(ArenaStage.SCREEN_WORLD))
    }

    @Test
    fun `a missing pose falls back to the stance, never to the pixel art`() {
        val nyx = FighterCharacter.SHADOW_RUNNER
        assertEquals("nyx_stance", SpriteArt.fighter(nyx, FighterPose.BLAST, FighterMood.READY))
        assertEquals("nyx_stance", SpriteArt.fighter(nyx, FighterPose.PUNCH, FighterMood.TIRED))
        assertEquals(
            "snooze_button_idle",
            SpriteArt.opponent(Opponent.SNOOZE_BUTTON, OpponentPose.KO),
        )
    }

    @Test
    fun `every name the art can ask for has a drawable`() {
        val asked =
            FighterCharacter.entries.flatMap { c ->
                FighterPose.entries.flatMap { pose ->
                    FighterMood.entries.map { SpriteArt.fighter(c, pose, it) }
                }
            } +
                Opponent.entries.flatMap { o ->
                    OpponentPose.entries.map { SpriteArt.opponent(o, it) }
                } +
                ArenaStage.entries.map { SpriteArt.stage(it) }
        val names = asked.filterNotNull().toSet()
        assertTrue(names.all { it in SpriteArt.allNames }, "$names")
        assertEquals(SpriteArt.allNames, SPRITE_RESOURCES.keys)
    }
}
