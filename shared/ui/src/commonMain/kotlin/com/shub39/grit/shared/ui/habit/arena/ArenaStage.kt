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

import com.shub39.grit.core.arena.Opponent

/**
 * The 5 fight backgrounds (#14, decided 2026-11-05): each one is the home of two neighbouring
 * opponents on the ladder, so the scenery changes as the user climbs.
 */
enum class ArenaStage {
    /** A messy bedroom at night light: Snooze Button, Couch Gremlin. */
    BEDROOM,
    /** A glowing world inside a phone screen: Doomscroll Imp, Inbox Hydra. */
    SCREEN_WORLD,
    /** A laundry room with heaps and machines: Laundry Mountain, Distraction Swarm. */
    LAUNDRY,
    /** Gears and a giant clock face: Time Thief, Deadline Golem. */
    CLOCK_TOWER,
    /** Rocks and lava: Overwhelm Ogre, Procrastination Dragon. */
    VOLCANO;

    companion object {
        fun of(opponent: Opponent): ArenaStage = entries[(opponent.number - 1) / 2]
    }
}

/** Light of a fight, picked at random per fight so the same stage looks different. */
enum class ArenaLighting {
    DAY,
    DUSK,
    NIGHT,
}
