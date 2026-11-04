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
 */ package com.shub39.grit.core.fighter

/**
 * Ranks of the fighter avatar, earned with total active days. The thresholds are the same as the
 * plant's growth stages, so switching avatars keeps the user's progress. A rank is never lost.
 */
enum class FighterRank(val minActiveDays: Int, val belt: Belt) {
    ROOKIE(0, Belt.WHITE),
    TRAINEE(3, Belt.YELLOW),
    SPARRING_PARTNER(7, Belt.ORANGE),
    BRAWLER(14, Belt.GREEN),
    CONTENDER(21, Belt.BLUE),
    FIGHTER(30, Belt.PURPLE),
    CHALLENGER(45, Belt.BROWN),
    VETERAN(60, Belt.RED),
    CHAMPION(90, Belt.BLACK),
    MASTER(150, Belt.BLACK_ONE_STRIPE),
    GRANDMASTER(240, Belt.BLACK_TWO_STRIPES),
    LEGEND(365, Belt.GOLD);

    /** 1-based number shown to the user. */
    val number: Int
        get() = ordinal + 1

    val next: FighterRank?
        get() = entries.getOrNull(ordinal + 1)

    companion object {
        fun forActiveDays(activeDays: Int): FighterRank =
            entries.last { activeDays >= it.minActiveDays }
    }
}

/** The belt the fighter wears at each rank, used for the drawing. */
enum class Belt(val goldStripes: Int = 0) {
    WHITE,
    YELLOW,
    ORANGE,
    GREEN,
    BLUE,
    PURPLE,
    BROWN,
    RED,
    BLACK,
    BLACK_ONE_STRIPE(goldStripes = 1),
    BLACK_TWO_STRIPES(goldStripes = 2),
    GOLD,
}
