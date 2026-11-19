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
package com.shub39.grit.shared.ui.habit.sprite

import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.sprite_nyx_hurt
import grit.shared.ui.generated.resources.sprite_nyx_kick
import grit.shared.ui.generated.resources.sprite_nyx_punch
import grit.shared.ui.generated.resources.sprite_nyx_stance
import grit.shared.ui.generated.resources.sprite_snooze_button_idle
import grit.shared.ui.generated.resources.stage_screen_world
import org.jetbrains.compose.resources.DrawableResource

/**
 * The drawable of every name in [SpriteArt.allNames]. Kept apart from the drawing code, so tests
 * can read it without loading the graphics library.
 */
internal val SPRITE_RESOURCES: Map<String, DrawableResource> =
    mapOf(
        "nyx_stance" to Res.drawable.sprite_nyx_stance,
        "nyx_punch" to Res.drawable.sprite_nyx_punch,
        "nyx_kick" to Res.drawable.sprite_nyx_kick,
        "nyx_hurt" to Res.drawable.sprite_nyx_hurt,
        "snooze_button_idle" to Res.drawable.sprite_snooze_button_idle,
        "stage_screen_world" to Res.drawable.stage_screen_world,
    )
