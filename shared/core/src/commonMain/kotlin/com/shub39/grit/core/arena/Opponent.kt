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

/**
 * The 10 CPU opponents of the ladder (SPEC 9.5): playful "everyday enemies" of an ADHD brain.
 *
 * They have no fighting styles (decided 2026-11-05): speed, focus and attack follow one formula of
 * the opponent's [level] (see [ArenaRules.OPPONENT_ATTACK_BONUS]). Only [maxHp] is set per
 * opponent, tuned so a typical fighter at [recommendedRankNumber] wins 55 to 75 % of the fights
 * (checked by `LadderBalanceTest`).
 *
 * [id] is stable and goes into storage and backups; never rename it. Names are UI strings.
 */
enum class Opponent(val id: String, val maxHp: Int) {
    SNOOZE_BUTTON("snooze_button", maxHp = 55),
    COUCH_GREMLIN("couch_gremlin", maxHp = 70),
    DOOMSCROLL_IMP("doomscroll_imp", maxHp = 86),
    LAUNDRY_MOUNTAIN("laundry_mountain", maxHp = 106),
    INBOX_HYDRA("inbox_hydra", maxHp = 125),
    DISTRACTION_SWARM("distraction_swarm", maxHp = 143),
    TIME_THIEF("time_thief", maxHp = 160),
    DEADLINE_GOLEM("deadline_golem", maxHp = 178),
    OVERWHELM_OGRE("overwhelm_ogre", maxHp = 194),
    PROCRASTINATION_DRAGON("procrastination_dragon", maxHp = 211);

    /** 1-based place on the ladder. */
    val number: Int
        get() = ordinal + 1

    /** Rank number (and stat level) a fighter should have to beat this opponent most times. */
    val recommendedRankNumber: Int
        get() = number + 1

    /** The opponent's level, which its speed, focus and attack scale with. */
    val level: Int
        get() = number + 1

    val attack: Int
        get() =
            ArenaRules.BASE_ATTACK +
                ArenaRules.ATTACK_PER_STRENGTH_LEVEL * level +
                ArenaRules.OPPONENT_ATTACK_BONUS

    val speed: Int
        get() = level

    val focus: Int
        get() = level + ArenaRules.OPPONENT_FOCUS_OFFSET

    /** The opponent's values for [Battle]; [Combatant.name] is the stable [id]. */
    val combatant: Combatant
        get() = Combatant(id, maxHp = maxHp, attack = attack, speed = speed, focus = focus)

    /** The opponent below on the ladder, whose first defeat unlocks this one. */
    val previous: Opponent?
        get() = entries.getOrNull(ordinal - 1)

    val next: Opponent?
        get() = entries.getOrNull(ordinal + 1)

    companion object {
        fun byId(id: String): Opponent? = entries.firstOrNull { it.id == id }
    }
}
