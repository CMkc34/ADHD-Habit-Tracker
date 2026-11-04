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
package com.shub39.grit.core.fighter

/**
 * Who trains with the user (SPEC 9.8). Every look reaches the same ranks, wears the same shop items
 * and keeps rank, water and purchases when the user switches.
 *
 * [id] is stored under `avatar_species` and in the backup field `species`. It keeps the ids of the
 * plant species the look replaces, so older backups and older app versions stay compatible.
 */
enum class FighterLook(val id: String, val defaultName: String) {
    /** Replaces the green sprout. */
    KAI("sprout", "Kai"),

    /** Replaces the cherry tree. */
    MIRA("cherry", "Mira");

    companion object {
        val DEFAULT = KAI

        /** The look stored as [id]; unknown or missing ids give [KAI]. */
        fun fromId(id: String?): FighterLook = entries.firstOrNull { it.id == id } ?: DEFAULT
    }
}
