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
package com.shub39.grit.core.minigame

import com.shub39.grit.core.fighter.FighterStat

/**
 * The arcade games of the Games tab (SPEC 10, #40). Each game has free runs every day and keeps a
 * high score; the fighter's trained [stats] make it easier, the games never raise them.
 */
enum class MiniGame(
    /** Fixed id for the run log and the backup. */
    val id: String,
    /** The stats that help in this game, most important first. */
    val stats: List<FighterStat>,
) {
    /** Drag the fighter, ki fires upward at waves of minions and a boss (#42). */
    KI_BARRAGE(
        "ki_barrage",
        listOf(FighterStat.SPEED, FighterStat.FOCUS, FighterStat.STAMINA, FighterStat.STRENGTH),
    ),

    /** Tap falling junk to smash it before the pile reaches the line (#43). */
    CLUTTER_SMASH(
        "clutter_smash",
        listOf(FighterStat.STRENGTH, FighterStat.FOCUS, FighterStat.SPEED, FighterStat.STAMINA),
    ),

    /** Charge ki and strike in the green zone: boards, bricks, ice (#44). */
    BOARD_BREAK("board_break", listOf(FighterStat.FOCUS, FighterStat.STRENGTH, FighterStat.SPEED));

    companion object {
        fun byId(id: String): MiniGame? = entries.firstOrNull { it.id == id }
    }
}

/** The numbers of the Games tab that are the same for every game. */
object MiniGameRules {
    /** Free runs per game and day (decided 2026-11-15: 3 for each game). Unused runs expire. */
    const val RUNS_PER_DAY = 3
}
