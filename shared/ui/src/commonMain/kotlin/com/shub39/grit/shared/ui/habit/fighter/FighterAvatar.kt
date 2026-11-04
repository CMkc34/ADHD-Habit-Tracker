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
import com.shub39.grit.core.fighter.FighterItems
import com.shub39.grit.core.fighter.FighterLook
import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.shared.ui.GritPreviewWrapper
import kotlin.math.floor
import kotlin.math.min

/**
 * The fighter avatar (#6), an original chibi martial artist in pixel art with ki-energy moves. The
 * [rank] shows through the belt and a few upgrades (wrist wraps, headband, emblem, golden aura);
 * the [outfit] is the gi colour and [gear] are the worn items, both ids from [FighterItems]. A
 * [FighterMood.TIRED] fighter sits and dozes, otherwise the fighter takes the [pose]. The idle
 * motion follows the system's animation setting and stops completely when [animate] is false.
 */
@Composable
fun FighterAvatar(
    rank: FighterRank,
    look: FighterLook,
    mood: FighterMood,
    modifier: Modifier = Modifier,
    outfit: String = FighterItems.DEFAULT_OUTFIT,
    gear: Set<String> = emptySet(),
    pose: FighterPose = FighterPose.STANCE,
    animate: Boolean = true,
) {
    val frame = if (animate) rememberPixelFrame() else null
    // each frame is built once and then only painted
    val frames =
        remember(rank, look, mood, outfit, gear, pose) {
            arrayOfNulls<PixelImage>(FighterPixels.FRAMES)
        }

    Canvas(modifier = modifier.aspectRatio(1f)) {
        val f = frame?.value ?: 0
        val image =
            frames[f]
                ?: FighterPixels.render(look, rank, mood, pose, outfit, gear, f).also {
                    frames[f] = it
                }
        drawPixels(image)
    }
}

/** The flying ki blast of the special attack, for the arena (#14); it travels to the right. */
@Composable
fun KiBlast(modifier: Modifier = Modifier, golden: Boolean = false, animate: Boolean = true) {
    val frame = if (animate) rememberPixelFrame() else null
    val frames = remember(golden) { Array(2) { FighterPixels.renderKiBlast(golden, it) } }

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
@Preview(widthDp = 480, heightDp = 160)
@Composable
private fun KaiRanksPreview() {
    FighterGrid(look = FighterLook.KAI)
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview(widthDp = 480, heightDp = 160)
@Composable
private fun MiraRanksPreview() {
    FighterGrid(look = FighterLook.MIRA)
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview(widthDp = 480, heightDp = 160)
@Composable
private fun TiredRanksPreview() {
    FighterGrid(look = FighterLook.MIRA, mood = FighterMood.TIRED)
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview(widthDp = 480, heightDp = 160)
@Composable
private fun KaiAllGearPreview() {
    FighterGrid(look = FighterLook.KAI, gear = FighterItems.gear.toSet())
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview(widthDp = 480, heightDp = 160)
@Composable
private fun MiraAllGearPreview() {
    FighterGrid(look = FighterLook.MIRA, gear = FighterItems.gear.toSet())
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview(widthDp = 480, heightDp = 240)
@Composable
private fun MoodsAndPosesPreview() {
    Column {
        FighterLook.entries.forEach { look ->
            Row {
                FighterPose.entries.forEach { pose ->
                    FighterAvatar(
                        rank = FighterRank.CONTENDER,
                        look = look,
                        mood = FighterMood.READY,
                        pose = pose,
                        animate = false,
                        modifier = Modifier.size(120.dp),
                    )
                }
                FighterAvatar(
                    rank = FighterRank.CONTENDER,
                    look = look,
                    mood = FighterMood.TIRED,
                    animate = false,
                    modifier = Modifier.size(120.dp),
                )
            }
        }
    }
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview(widthDp = 480, heightDp = 200)
@Composable
private fun OutfitsAndKiBlastPreview() {
    Column {
        Row {
            FighterItems.outfits.forEach { outfit ->
                FighterAvatar(
                    rank = FighterRank.BRAWLER,
                    look = FighterLook.KAI,
                    mood = FighterMood.READY,
                    outfit = outfit,
                    animate = false,
                    modifier = Modifier.size(80.dp),
                )
            }
        }
        Row {
            KiBlast(animate = false, modifier = Modifier.width(160.dp))
            KiBlast(golden = true, animate = false, modifier = Modifier.width(160.dp))
        }
    }
}

@Composable
private fun FighterGrid(
    look: FighterLook,
    mood: FighterMood = FighterMood.READY,
    gear: Set<String> = emptySet(),
) {
    Column {
        FighterRank.entries.chunked(6).forEach { row ->
            Row {
                row.forEach { rank ->
                    FighterAvatar(
                        rank = rank,
                        look = look,
                        mood = mood,
                        gear = gear,
                        animate = false,
                        modifier = Modifier.size(80.dp),
                    )
                }
            }
        }
    }
}
