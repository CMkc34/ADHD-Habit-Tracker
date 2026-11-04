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
package com.shub39.grit.shared.ui.habit.style

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.shub39.grit.core.habits.style.RoutineColor
import com.shub39.grit.core.habits.style.RoutineIcon
import grit.shared.ui.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** The routine colour as a Compose colour. */
val RoutineColor.color: Color
    get() = Color(argb)

/** Black or white for icons and check marks on [color]. */
val RoutineColor.contentColor: Color
    get() = Color(contentArgb)

/** Name of the colour for screen readers. */
@Composable fun RoutineColor.label(): String = stringResource(nameRes())

/** Name of the icon for screen readers. */
@Composable fun RoutineIcon.label(): String = stringResource(nameRes())

internal fun RoutineColor.nameRes(): StringResource =
    when (this) {
        RoutineColor.RED -> Res.string.routine_color_red
        RoutineColor.RASPBERRY -> Res.string.routine_color_raspberry
        RoutineColor.PINK -> Res.string.routine_color_pink
        RoutineColor.PURPLE -> Res.string.routine_color_purple
        RoutineColor.VIOLET -> Res.string.routine_color_violet
        RoutineColor.INDIGO -> Res.string.routine_color_indigo
        RoutineColor.BLUE -> Res.string.routine_color_blue
        RoutineColor.SKY_BLUE -> Res.string.routine_color_sky_blue
        RoutineColor.TURQUOISE -> Res.string.routine_color_turquoise
        RoutineColor.PETROL -> Res.string.routine_color_petrol
        RoutineColor.MINT -> Res.string.routine_color_mint
        RoutineColor.GREEN -> Res.string.routine_color_green
        RoutineColor.LIGHT_GREEN -> Res.string.routine_color_light_green
        RoutineColor.LIME -> Res.string.routine_color_lime
        RoutineColor.YELLOW -> Res.string.routine_color_yellow
        RoutineColor.AMBER -> Res.string.routine_color_amber
        RoutineColor.ORANGE -> Res.string.routine_color_orange
        RoutineColor.CORAL -> Res.string.routine_color_coral
        RoutineColor.RUST -> Res.string.routine_color_rust
        RoutineColor.BROWN -> Res.string.routine_color_brown
        RoutineColor.SAND -> Res.string.routine_color_sand
        RoutineColor.GREY -> Res.string.routine_color_grey
        RoutineColor.SLATE -> Res.string.routine_color_slate
        RoutineColor.ANTHRACITE -> Res.string.routine_color_anthracite
    }

internal fun RoutineIcon.nameRes(): StringResource =
    when (this) {
        RoutineIcon.BOLT -> Res.string.routine_icon_bolt
        RoutineIcon.WATER -> Res.string.routine_icon_water
        RoutineIcon.DUMBBELL -> Res.string.routine_icon_dumbbell
        RoutineIcon.RUN -> Res.string.routine_icon_run
        RoutineIcon.BIKE -> Res.string.routine_icon_bike
        RoutineIcon.SWIM -> Res.string.routine_icon_swim
        RoutineIcon.MEDITATION -> Res.string.routine_icon_meditation
        RoutineIcon.SLEEP -> Res.string.routine_icon_sleep
        RoutineIcon.MORNING -> Res.string.routine_icon_morning
        RoutineIcon.ALARM -> Res.string.routine_icon_alarm
        RoutineIcon.BOOK -> Res.string.routine_icon_book
        RoutineIcon.WRITE -> Res.string.routine_icon_write
        RoutineIcon.LEARN -> Res.string.routine_icon_learn
        RoutineIcon.MUSIC -> Res.string.routine_icon_music
        RoutineIcon.PAINT -> Res.string.routine_icon_paint
        RoutineIcon.COOK -> Res.string.routine_icon_cook
        RoutineIcon.APPLE -> Res.string.routine_icon_apple
        RoutineIcon.COFFEE -> Res.string.routine_icon_coffee
        RoutineIcon.PILL -> Res.string.routine_icon_pill
        RoutineIcon.TOOTH -> Res.string.routine_icon_tooth
        RoutineIcon.SHOWER -> Res.string.routine_icon_shower
        RoutineIcon.CLEAN -> Res.string.routine_icon_clean
        RoutineIcon.FLOWER -> Res.string.routine_icon_flower
        RoutineIcon.LEAF -> Res.string.routine_icon_leaf
        RoutineIcon.HEART -> Res.string.routine_icon_heart
        RoutineIcon.BRAIN -> Res.string.routine_icon_brain
        RoutineIcon.SMILE -> Res.string.routine_icon_smile
        RoutineIcon.PHONE -> Res.string.routine_icon_phone
        RoutineIcon.PEOPLE -> Res.string.routine_icon_people
        RoutineIcon.PAW -> Res.string.routine_icon_paw
        RoutineIcon.SAVINGS -> Res.string.routine_icon_savings
        RoutineIcon.LAPTOP -> Res.string.routine_icon_laptop
        RoutineIcon.PHONE_OFF -> Res.string.routine_icon_phone_off
        RoutineIcon.STAR -> Res.string.routine_icon_star
        RoutineIcon.TARGET -> Res.string.routine_icon_target
        RoutineIcon.CHECK -> Res.string.routine_icon_check
    }

fun RoutineIcon.drawable(): DrawableResource =
    when (this) {
        RoutineIcon.BOLT -> Res.drawable.routine_icon_bolt
        RoutineIcon.WATER -> Res.drawable.routine_icon_water
        RoutineIcon.DUMBBELL -> Res.drawable.routine_icon_dumbbell
        RoutineIcon.RUN -> Res.drawable.routine_icon_run
        RoutineIcon.BIKE -> Res.drawable.routine_icon_bike
        RoutineIcon.SWIM -> Res.drawable.routine_icon_swim
        RoutineIcon.MEDITATION -> Res.drawable.routine_icon_meditation
        RoutineIcon.SLEEP -> Res.drawable.routine_icon_sleep
        RoutineIcon.MORNING -> Res.drawable.routine_icon_morning
        RoutineIcon.ALARM -> Res.drawable.routine_icon_alarm
        RoutineIcon.BOOK -> Res.drawable.routine_icon_book
        RoutineIcon.WRITE -> Res.drawable.routine_icon_write
        RoutineIcon.LEARN -> Res.drawable.routine_icon_learn
        RoutineIcon.MUSIC -> Res.drawable.routine_icon_music
        RoutineIcon.PAINT -> Res.drawable.routine_icon_paint
        RoutineIcon.COOK -> Res.drawable.routine_icon_cook
        RoutineIcon.APPLE -> Res.drawable.routine_icon_apple
        RoutineIcon.COFFEE -> Res.drawable.routine_icon_coffee
        RoutineIcon.PILL -> Res.drawable.routine_icon_pill
        RoutineIcon.TOOTH -> Res.drawable.routine_icon_tooth
        RoutineIcon.SHOWER -> Res.drawable.routine_icon_shower
        RoutineIcon.CLEAN -> Res.drawable.routine_icon_clean
        RoutineIcon.FLOWER -> Res.drawable.routine_icon_flower
        RoutineIcon.LEAF -> Res.drawable.routine_icon_leaf
        RoutineIcon.HEART -> Res.drawable.routine_icon_heart
        RoutineIcon.BRAIN -> Res.drawable.routine_icon_brain
        RoutineIcon.SMILE -> Res.drawable.routine_icon_smile
        RoutineIcon.PHONE -> Res.drawable.routine_icon_phone
        RoutineIcon.PEOPLE -> Res.drawable.routine_icon_people
        RoutineIcon.PAW -> Res.drawable.routine_icon_paw
        RoutineIcon.SAVINGS -> Res.drawable.routine_icon_savings
        RoutineIcon.LAPTOP -> Res.drawable.routine_icon_laptop
        RoutineIcon.PHONE_OFF -> Res.drawable.routine_icon_phone_off
        RoutineIcon.STAR -> Res.drawable.routine_icon_star
        RoutineIcon.TARGET -> Res.drawable.routine_icon_target
        RoutineIcon.CHECK -> Res.drawable.routine_icon_check
    }
