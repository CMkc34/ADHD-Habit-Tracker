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
package com.shub39.grit.shared.ui.navigation

/** Energy page of the activity streak (fork-specific), opened from the habits header. */
data object Energy : Routes

/** Own tab with the fighter, its stats and its shop (fork-specific, #9). */
data object Avatar : Routes

/** Own tab for the turn-based fights (fork-specific, #14): tickets, ladder and the fight screen. */
data object Arena : Routes

/** The shop of the fighter, opened from the Fighter tab (fork-specific, #138). */
data object PlantShop : Routes
