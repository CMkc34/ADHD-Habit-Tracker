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

/** One side of a fight with its derived battle values (SPEC 9.4). */
data class Combatant(
    val name: String,
    val maxHp: Int,
    val attack: Int,
    val speed: Int,
    val focus: Int,
) {
    init {
        require(maxHp > 0) { "maxHp must be positive" }
        require(attack > 0) { "attack must be positive" }
    }

    companion object {
        /**
         * Derives battle values from a fighter's rank number and stat levels.
         *
         * Takes plain numbers so the engine does not depend on how ranks and levels are stored; an
         * adapter from the fighter state only has to pass them in.
         */
        fun of(
            name: String,
            rankNumber: Int,
            strengthLevel: Int,
            speedLevel: Int,
            staminaLevel: Int,
            focusLevel: Int,
        ): Combatant =
            Combatant(
                name = name,
                maxHp =
                    ArenaRules.BASE_HP +
                        ArenaRules.HP_PER_RANK * rankNumber +
                        ArenaRules.HP_PER_STAMINA_LEVEL * staminaLevel,
                attack =
                    ArenaRules.BASE_ATTACK + ArenaRules.ATTACK_PER_STRENGTH_LEVEL * strengthLevel,
                speed = speedLevel,
                focus = focusLevel,
            )
    }
}
