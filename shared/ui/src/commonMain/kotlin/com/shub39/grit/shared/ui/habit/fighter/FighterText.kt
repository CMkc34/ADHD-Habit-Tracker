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
package com.shub39.grit.shared.ui.habit.fighter

import androidx.compose.runtime.Composable
import com.shub39.grit.core.fighter.FighterCharacter
import com.shub39.grit.core.fighter.FighterColorSlot
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.fighter_character_karateka
import grit.shared.ui.generated.resources.fighter_character_karateka_desc
import grit.shared.ui.generated.resources.fighter_character_shadow_runner
import grit.shared.ui.generated.resources.fighter_character_shadow_runner_desc
import grit.shared.ui.generated.resources.fighter_character_spark_prodigy
import grit.shared.ui.generated.resources.fighter_character_spark_prodigy_desc
import grit.shared.ui.generated.resources.fighter_character_street_brawler
import grit.shared.ui.generated.resources.fighter_character_street_brawler_desc
import grit.shared.ui.generated.resources.fighter_character_wind_monk
import grit.shared.ui.generated.resources.fighter_character_wind_monk_desc
import grit.shared.ui.generated.resources.fighter_slot_accent
import grit.shared.ui.generated.resources.fighter_slot_hair
import grit.shared.ui.generated.resources.fighter_slot_main
import grit.shared.ui.generated.resources.fighter_slot_skin
import org.jetbrains.compose.resources.stringResource

/** The kind of fighter, like "Wind Monk", as opposed to its name, like "Kai" (#26). */
@Composable
fun characterName(character: FighterCharacter): String =
    stringResource(
        when (character) {
            FighterCharacter.WIND_MONK -> Res.string.fighter_character_wind_monk
            FighterCharacter.STREET_BRAWLER -> Res.string.fighter_character_street_brawler
            FighterCharacter.SHADOW_RUNNER -> Res.string.fighter_character_shadow_runner
            FighterCharacter.SPARK_PRODIGY -> Res.string.fighter_character_spark_prodigy
            FighterCharacter.KARATEKA -> Res.string.fighter_character_karateka
        }
    )

/** One short line about how the [character] fights, for the picker. */
@Composable
fun characterDescription(character: FighterCharacter): String =
    stringResource(
        when (character) {
            FighterCharacter.WIND_MONK -> Res.string.fighter_character_wind_monk_desc
            FighterCharacter.STREET_BRAWLER -> Res.string.fighter_character_street_brawler_desc
            FighterCharacter.SHADOW_RUNNER -> Res.string.fighter_character_shadow_runner_desc
            FighterCharacter.SPARK_PRODIGY -> Res.string.fighter_character_spark_prodigy_desc
            FighterCharacter.KARATEKA -> Res.string.fighter_character_karateka_desc
        }
    )

/** The label of a colour row in the picker: skin, hair, main colour or accent. */
@Composable
fun colorSlotName(slot: FighterColorSlot): String =
    stringResource(
        when (slot) {
            FighterColorSlot.SKIN -> Res.string.fighter_slot_skin
            FighterColorSlot.HAIR -> Res.string.fighter_slot_hair
            FighterColorSlot.MAIN -> Res.string.fighter_slot_main
            FighterColorSlot.ACCENT -> Res.string.fighter_slot_accent
        }
    )
