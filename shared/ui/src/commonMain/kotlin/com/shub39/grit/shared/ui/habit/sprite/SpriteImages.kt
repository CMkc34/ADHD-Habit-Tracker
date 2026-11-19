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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.shub39.grit.shared.ui.habit.fighter.FighterPixels
import com.shub39.grit.shared.ui.habit.fighter.rememberPixelFrame
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.sprite_nyx_hurt
import grit.shared.ui.generated.resources.sprite_nyx_kick
import grit.shared.ui.generated.resources.sprite_nyx_punch
import grit.shared.ui.generated.resources.sprite_nyx_stance
import grit.shared.ui.generated.resources.sprite_snooze_button_idle
import grit.shared.ui.generated.resources.stage_screen_world
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.imageResource

/** The drawable of every name in [SpriteArt.allNames]. */
internal val SPRITE_RESOURCES: Map<String, DrawableResource> =
    mapOf(
        "nyx_stance" to Res.drawable.sprite_nyx_stance,
        "nyx_punch" to Res.drawable.sprite_nyx_punch,
        "nyx_kick" to Res.drawable.sprite_nyx_kick,
        "nyx_hurt" to Res.drawable.sprite_nyx_hurt,
        "snooze_button_idle" to Res.drawable.sprite_snooze_button_idle,
        "stage_screen_world" to Res.drawable.stage_screen_world,
    )

/** Loads the pictures [names] (`null`s skipped), keyed by name. */
@Composable
internal fun rememberSprites(names: List<String?>): Map<String, ImageBitmap> =
    names.filterNotNull().distinct().associateWith { name ->
        imageResource(SPRITE_RESOURCES.getValue(name))
    }

/**
 * Paints a sprite canvas so its box (the middle half) covers [boxLeft]..[boxLeft] + [boxSize]
 * horizontally and ends at [boxTop] + [boxSize]. [flash] washes it almost white for a landed blow.
 */
internal fun DrawScope.drawImageSprite(
    image: ImageBitmap,
    boxLeft: Float,
    boxTop: Float,
    boxSize: Float,
    alpha: Float = 1f,
    flash: Boolean = false,
) {
    val width = boxSize * 2f
    drawImage(
        image = image,
        srcOffset = IntOffset.Zero,
        srcSize = IntSize(image.width, image.height),
        dstOffset = IntOffset((boxLeft - boxSize / 2f).roundToInt(), boxTop.roundToInt()),
        dstSize = IntSize(width.roundToInt(), boxSize.roundToInt()),
        alpha = alpha.coerceIn(0f, 1f),
        colorFilter = if (flash) FLASH else null,
        filterQuality = FilterQuality.Medium,
    )
}

/** Paints a stage picture over the whole [size]. */
internal fun DrawScope.drawStage(image: ImageBitmap, topLeft: Offset, size: Size) {
    drawImage(
        image = image,
        srcOffset = IntOffset.Zero,
        srcSize = IntSize(image.width, image.height),
        dstOffset = IntOffset(topLeft.x.roundToInt(), topLeft.y.roundToInt()),
        dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
        filterQuality = FilterQuality.Medium,
    )
}

private val FLASH = ColorFilter.tint(Color.White.copy(alpha = 0.7f), BlendMode.SrcAtop)

/**
 * A square avatar of the picture [name]: its box fills the square, a reaching punch may stick out
 * at the sides. With [bounce] it bobs one stage pixel like the pixel art's idle bounce.
 */
@Composable
internal fun SpriteAvatar(
    name: String,
    boxPixels: Int,
    modifier: Modifier = Modifier,
    bounce: Boolean = false,
    description: String? = null,
) {
    val image = imageResource(SPRITE_RESOURCES.getValue(name))
    val frame = if (bounce) rememberPixelFrame() else null
    Canvas(
        modifier =
            modifier.aspectRatio(1f).let { m ->
                if (description != null) m.semantics { contentDescription = description } else m
            }
    ) {
        val down = if ((frame?.value ?: 0) >= FighterPixels.FRAMES / 2) 1f else 0f
        drawImageSprite(image, 0f, down * size.width / boxPixels, size.width)
    }
}
