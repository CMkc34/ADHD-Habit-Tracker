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

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.shub39.grit.core.fighter.FighterCharacter
import com.shub39.grit.core.fighter.FighterColors
import com.shub39.grit.core.fighter.FighterItems
import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.shared.ui.GritPreviewWrapper
import kotlin.math.floor
import kotlin.math.min

/**
 * The fighter avatar (#6): one of the [FighterCharacter]s in pixel art, in the [colors] the user
 * picked at the start, with ki-energy moves. The [rank] shows through the belt and a few upgrades
 * (wrist wraps, headband, emblem, golden aura). A bought [outfit] replaces the main colour, [gear]
 * are the worn items; both are ids from [FighterItems]. A [FighterMood.TIRED] fighter sits and
 * dozes, otherwise the fighter takes the [pose]. The idle motion follows the system's animation
 * setting and stops completely when [animate] is false.
 */
@Composable
fun FighterAvatar(
    character: FighterCharacter,
    rank: FighterRank,
    mood: FighterMood,
    modifier: Modifier = Modifier,
    colors: FighterColors = character.defaultColors,
    outfit: String? = null,
    gear: Set<String> = emptySet(),
    pose: FighterPose = FighterPose.STANCE,
    animate: Boolean = true,
) {
    val frame = if (animate) rememberPixelFrame() else null
    // each frame is built once and then only painted
    val frames =
        remember(character, rank, mood, colors, outfit, gear, pose) {
            arrayOfNulls<PixelImage>(FighterPixels.FRAMES)
        }

    Canvas(modifier = modifier.aspectRatio(1f)) {
        val f = frame?.value ?: 0
        val image =
            frames[f]
                ?: FighterPixels.render(character, rank, mood, colors, pose, gear, f, outfit).also {
                    frames[f] = it
                }
        drawPixels(image)
    }
}

/**
 * The flying ki blast of the special attack in the [character]'s ki colour, for the arena (#14); it
 * travels to the right.
 */
@Composable
fun KiBlast(
    character: FighterCharacter,
    modifier: Modifier = Modifier,
    rank: FighterRank = FighterRank.ROOKIE,
    animate: Boolean = true,
) {
    val frame = if (animate) rememberPixelFrame() else null
    val frames =
        remember(character, rank) { Array(2) { FighterPixels.renderKiBlast(character, rank, it) } }

    Canvas(modifier = modifier.aspectRatio(2f)) { drawPixels(frames[(frame?.value ?: 0) % 2]) }
}

/**
 * The current animation frame, 0 until [FighterPixels.FRAMES], one every 100 ms. Read it only while
 * drawing, so a new frame repaints the canvas without recomposing.
 */
@Composable
private fun rememberPixelFrame(): State<Int> {
    val transition = rememberInfiniteTransition(label = "fighter")
    val time =
        transition.animateFloat(
            initialValue = 0f,
            targetValue = FighterPixels.FRAMES.toFloat(),
            animationSpec =
                infiniteRepeatable(
                    animation =
                        tween(durationMillis = FighterPixels.FRAMES * 100, easing = LinearEasing)
                ),
            label = "frame",
        )
    return remember(time) { derivedStateOf { time.value.toInt().mod(FighterPixels.FRAMES) } }
}

/**
 * Paints [image] as crisp square pixels, centred and as large as fits. Runs of equal colour are
 * merged into one rectangle, and the edges are snapped to whole device pixels so no seams show.
 */
internal fun DrawScope.drawPixels(image: PixelImage) {
    val px = min(size.width / image.width, size.height / image.height)
    val ox = (size.width - px * image.width) / 2f
    val oy = (size.height - px * image.height) / 2f
    drawPixels(image, Offset(ox, oy), px)
}

/** Paints [image] with its top left corner at [topLeft], each pixel [px] canvas pixels wide. */
internal fun DrawScope.drawPixels(image: PixelImage, topLeft: Offset, px: Float) {
    val ox = topLeft.x
    val oy = topLeft.y
    for (y in 0 until image.height) {
        val top = floor(oy + y * px)
        val bottom = floor(oy + (y + 1) * px)
        var x = 0
        while (x < image.width) {
            val c = image[x, y]
            var end = x + 1
            while (end < image.width && image[end, y] == c) end++
            if (c != 0) {
                val left = floor(ox + x * px)
                val right = floor(ox + end * px)
                drawRect(
                    color = Color(c),
                    topLeft = Offset(left, top),
                    size = Size(right - left, bottom - top),
                )
            }
            x = end
        }
    }
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview(widthDp = 480, heightDp = 200)
@Composable
private fun CharactersPreview() {
    Column {
        Row {
            FighterCharacter.entries.forEach {
                FighterAvatar(
                    it,
                    FighterRank.CONTENDER,
                    FighterMood.READY,
                    Modifier.size(96.dp),
                    animate = false,
                )
            }
        }
        Row {
            FighterCharacter.entries.forEach {
                FighterAvatar(
                    it,
                    FighterRank.CONTENDER,
                    FighterMood.READY,
                    Modifier.size(96.dp),
                    colors = FighterColors(skin = 6, hair = 4, main = 4, accent = 7),
                    animate = false,
                )
            }
        }
    }
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview(widthDp = 480, heightDp = 400)
@Composable
private fun PosesPreview() {
    Column {
        FighterCharacter.entries.forEach { character ->
            Row {
                FighterPose.entries.forEach { pose ->
                    FighterAvatar(
                        character,
                        FighterRank.CONTENDER,
                        FighterMood.READY,
                        Modifier.size(80.dp),
                        pose = pose,
                        animate = false,
                    )
                }
                FighterAvatar(
                    character,
                    FighterRank.CONTENDER,
                    FighterMood.TIRED,
                    Modifier.size(80.dp),
                    animate = false,
                )
                KiBlast(character, Modifier.width(80.dp), animate = false)
            }
        }
    }
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview(widthDp = 480, heightDp = 100)
@Composable
private fun OutfitsPreview() {
    Row {
        FighterItems.outfits.forEach { outfit ->
            FighterAvatar(
                FighterCharacter.KARATEKA,
                FighterRank.BRAWLER,
                FighterMood.READY,
                Modifier.size(80.dp),
                outfit = outfit,
                animate = false,
            )
        }
    }
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview(widthDp = 480, heightDp = 160)
@Composable
private fun RanksPreview() {
    RankGrid(FighterCharacter.KARATEKA)
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview(widthDp = 480, heightDp = 160)
@Composable
private fun AllGearPreview() {
    RankGrid(FighterCharacter.SPARK_PRODIGY, gear = FighterItems.gear.toSet())
}

@Composable
private fun RankGrid(character: FighterCharacter, gear: Set<String> = emptySet()) {
    Column {
        FighterRank.entries.chunked(6).forEach { row ->
            Row {
                row.forEach { rank ->
                    FighterAvatar(
                        character,
                        rank,
                        FighterMood.READY,
                        Modifier.size(80.dp),
                        gear = gear,
                        animate = false,
                    )
                }
            }
        }
    }
}
