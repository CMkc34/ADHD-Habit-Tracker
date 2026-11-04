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
package com.shub39.grit.core.avatar

/**
 * Which plant grows on the meadow (#140). Every species goes through the same [PlantStage]s, wears
 * the same shop items and keeps stage, water and purchases when the user switches.
 */
enum class AvatarSpecies(val id: String) {
    /** The green plant that becomes a big leafy tree. */
    SPROUT("sprout"),

    /** A cherry tree with a pink blossom crown and a darker trunk. */
    CHERRY("cherry");

    companion object {
        /** The species stored as [id]; unknown or missing ids give [SPROUT]. */
        fun fromId(id: String?): AvatarSpecies = entries.firstOrNull { it.id == id } ?: SPROUT
    }
}
