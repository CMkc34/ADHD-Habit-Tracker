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

/**
 * Which characters, poses and stages already have generated image art (decided 2026-11-19: the
 * hand-drawn pixel art is replaced step by step by images from an image generator). Pure Kotlin, so
 * it is unit tested; [SpriteImages] turns the names into drawable resources.
 *
 * A character with art shows it in every pose: a pose that has no picture yet falls back to its
 * stance (or idle), so one fight never mixes the two styles. A character without any art keeps the
 * pixel art from [com.shub39.grit.shared.ui.habit.fighter.FighterPixels] and
 * [com.shub39.grit.shared.ui.habit.arena.OpponentPixels].
 *
 * Every fighter picture is a canvas of 96 x 48 stage pixels whose middle 48 x 48 is the fighter's
 * usual box, so punches and kicks may reach past it; every opponent picture is 124 x 62 around its
 * 62 x 62 box. Feet stand on the bottom edge.
 */
object SpriteArt {
    /** The pictures each fighter has so far; `tired` is the sitting picture of a tired fighter. */
    private val fighterPictures: Map<FighterCharacter, Set<String>> =
        mapOf(FighterCharacter.SHADOW_RUNNER to setOf("stance", "punch", "kick", "hurt"))

    private val opponentPoses: Map<Opponent, Set<OpponentPose>> =
        mapOf(Opponent.SNOOZE_BUTTON to setOf(OpponentPose.IDLE))

    private val stages: Set<ArenaStage> = setOf(ArenaStage.SCREEN_WORLD)

    /**
     * The picture's name, like `nyx_punch`, or `null` when [character] has no art at all. A tired
     * fighter sits down; until that picture exists it stands in its stance.
     */
    fun fighter(character: FighterCharacter, pose: FighterPose, mood: FighterMood): String? {
        val pictures = fighterPictures[character] ?: return null
        val wanted = if (mood == FighterMood.TIRED) TIRED else pose.name.lowercase()
        return "${fighterName(character)}_${if (wanted in pictures) wanted else STANCE}"
    }

    /** The picture's name, like `snooze_button_idle`, or `null` without art. */
    fun opponent(opponent: Opponent, pose: OpponentPose): String? {
        val poses = opponentPoses[opponent] ?: return null
        val shown = if (pose in poses) pose else OpponentPose.IDLE
        return "${opponent.id}_${shown.name.lowercase()}"
    }

    /** The picture's name, like `stage_screen_world`, or `null` while the stage is pixel art. */
    fun stage(stage: ArenaStage): String? =
        if (stage in stages) "stage_${stage.name.lowercase()}" else null

    /** Every picture name in use, so the drawables in [SPRITE_RESOURCES] can be checked. */
    val allNames: Set<String>
        get() =
            fighterPictures
                .flatMap { (c, pictures) -> pictures.map { "${fighterName(c)}_$it" } }
                .toSet() +
                opponentPoses.flatMap { (o, poses) ->
                    poses.map { "${o.id}_${it.name.lowercase()}" }
                } +
                stages.map { "stage_${it.name.lowercase()}" }

    private fun fighterName(character: FighterCharacter): String =
        when (character) {
            FighterCharacter.WIND_MONK -> "kai"
            FighterCharacter.STREET_BRAWLER -> "rook"
            FighterCharacter.SHADOW_RUNNER -> "nyx"
            FighterCharacter.SPARK_PRODIGY -> "mira"
            FighterCharacter.KARATEKA -> "haru"
        }

    private const val TIRED = "tired"
    private const val STANCE = "stance"
}
