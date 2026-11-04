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
package com.shub39.grit.shared.ui.habit.avatar

import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.core.fighter.FighterRank

/*
 * Placeholder (#5): the data layer knows only the fighter now, but the fighter is not drawn yet
 * (#6, #8, #9). Until then the UI keeps drawing the plant, with one plant stage per rank. Delete
 * this file together with the plant drawings.
 */

/** The plant drawing for each [FighterRank], in the same order. */
enum class PlantStage {
    SEED,
    GERM,
    SEEDLING,
    SPROUT,
    PLANTLET,
    YOUNG_PLANT,
    SAPLING,
    YOUNG_TREE,
    TREE,
    BIG_TREE,
    BLOSSOMING_TREE,
    MIGHTY_TREE;

    /** 1-based number shown to the user, the same as the rank's. */
    val number: Int
        get() = ordinal + 1
}

/** How the plant drawing looks for a [FighterMood]. */
enum class PlantMood {
    HAPPY,
    TIRED,
}

val FighterRank.plantStage: PlantStage
    get() = PlantStage.entries[ordinal]

val FighterMood.plantMood: PlantMood
    get() =
        when (this) {
            FighterMood.READY -> PlantMood.HAPPY
            FighterMood.TIRED -> PlantMood.TIRED
        }
