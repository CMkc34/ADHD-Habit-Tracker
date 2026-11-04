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
package com.shub39.grit.core.motivation

import com.shub39.grit.core.motivation.QuoteTone.MOTIVATING
import com.shub39.grit.core.motivation.QuoteTone.SARCASTIC
import com.shub39.grit.core.motivation.StreakTier.BUILDING
import com.shub39.grit.core.motivation.StreakTier.ESTABLISHED
import com.shub39.grit.core.motivation.StreakTier.LEGEND
import com.shub39.grit.core.motivation.StreakTier.STARTER

/** All quotes the engine can pick from. Texts live in the UI's string resources, keyed by id. */
object QuoteCatalog {
    /** Shown when no quote fits the streak, so a check-in never ends without a message. */
    val fallback = MotivationQuote("quote_fallback", MOTIVATING, STARTER)

    val all: List<MotivationQuote> =
        listOf(
            MotivationQuote("quote_starter_motivating_1", MOTIVATING, STARTER),
            MotivationQuote("quote_starter_motivating_2", MOTIVATING, STARTER),
            MotivationQuote("quote_starter_motivating_3", MOTIVATING, STARTER),
            MotivationQuote("quote_starter_motivating_4", MOTIVATING, STARTER),
            MotivationQuote("quote_starter_sarcastic_1", SARCASTIC, STARTER),
            MotivationQuote("quote_starter_sarcastic_2", SARCASTIC, STARTER),
            MotivationQuote("quote_starter_sarcastic_3", SARCASTIC, STARTER),
            MotivationQuote("quote_starter_sarcastic_4", SARCASTIC, STARTER),
            MotivationQuote("quote_building_motivating_1", MOTIVATING, BUILDING),
            MotivationQuote("quote_building_motivating_2", MOTIVATING, BUILDING),
            MotivationQuote("quote_building_motivating_3", MOTIVATING, BUILDING),
            MotivationQuote("quote_building_motivating_4", MOTIVATING, BUILDING),
            MotivationQuote("quote_building_sarcastic_1", SARCASTIC, BUILDING),
            MotivationQuote("quote_building_sarcastic_2", SARCASTIC, BUILDING),
            MotivationQuote("quote_building_sarcastic_3", SARCASTIC, BUILDING),
            MotivationQuote("quote_building_sarcastic_4", SARCASTIC, BUILDING),
            MotivationQuote("quote_established_motivating_1", MOTIVATING, ESTABLISHED),
            MotivationQuote("quote_established_motivating_2", MOTIVATING, ESTABLISHED),
            MotivationQuote("quote_established_motivating_3", MOTIVATING, ESTABLISHED),
            MotivationQuote("quote_established_motivating_4", MOTIVATING, ESTABLISHED),
            MotivationQuote("quote_established_sarcastic_1", SARCASTIC, ESTABLISHED),
            MotivationQuote("quote_established_sarcastic_2", SARCASTIC, ESTABLISHED),
            MotivationQuote("quote_established_sarcastic_3", SARCASTIC, ESTABLISHED),
            MotivationQuote("quote_established_sarcastic_4", SARCASTIC, ESTABLISHED),
            MotivationQuote("quote_legend_motivating_1", MOTIVATING, LEGEND),
            MotivationQuote("quote_legend_motivating_2", MOTIVATING, LEGEND),
            MotivationQuote("quote_legend_motivating_3", MOTIVATING, LEGEND),
            MotivationQuote("quote_legend_motivating_4", MOTIVATING, LEGEND),
            MotivationQuote("quote_legend_sarcastic_1", SARCASTIC, LEGEND),
            MotivationQuote("quote_legend_sarcastic_2", SARCASTIC, LEGEND),
            MotivationQuote("quote_legend_sarcastic_3", SARCASTIC, LEGEND),
            MotivationQuote("quote_legend_sarcastic_4", SARCASTIC, LEGEND),
        )
}
