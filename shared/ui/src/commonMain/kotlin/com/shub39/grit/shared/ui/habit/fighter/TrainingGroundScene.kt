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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.shub39.grit.core.avatar.AvatarData
import com.shub39.grit.core.avatar.Weather
import com.shub39.grit.core.fighter.FighterItems
import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.shared.ui.GritPreviewWrapper
import com.shub39.grit.shared.ui.habit.fighter.TrainingGroundPixels.GROUND_HEIGHT
import com.shub39.grit.shared.ui.habit.fighter.TrainingGroundPixels.SkyFrame
import com.shub39.grit.shared.ui.habit.fighter.TrainingGroundPixels.WIDTH
import kotlin.math.roundToInt

/** The training ground is twice as wide as high; a higher scene only gets more sky. */
const val GROUND_ASPECT_RATIO = WIDTH.toFloat() / GROUND_HEIGHT

/**
 * The fighter on its training ground (#8): an outdoor dojo floor with a low fence under a sky that
 * shows the [weather], drawn in the fighter's pixel art. The fighter wears the outfit and gear of
 * [data] and stands in the middle; the props of [data] stand on fixed spots left and right of it,
 * the mat under its feet, in the character and colours picked at setup (#26). When the fighter is
 * [FighterMood.TIRED] the ground turns a little grey and the sky stands still; otherwise the clouds
 * drift and the sun's rays turn, following the system's animation setting, and nothing moves when
 * [animate] is false.
 *
 * [fighterScale] above 1 makes the fighter that much bigger (at most
 * [TrainingGroundPixels.MAX_FIGHTER_SCALE]) and the scene as much higher, with more sky on top; the
 * width, the props, the sun and the clouds stay as they are. [sceneHeight] (in ground heights, at
 * least [fighterScale]) makes the scene even higher, for a training ground that fills a whole page;
 * [skyTop] then moves the sun and the clouds that far down, below what lies on top.
 */
@Composable
fun TrainingGroundScene(
    data: AvatarData,
    rank: FighterRank,
    mood: FighterMood,
    modifier: Modifier = Modifier,
    weather: Weather = Weather.FAIR,
    animate: Boolean = true,
    fighterScale: Float = 1f,
    sceneHeight: Float = fighterScale,
    skyTop: Float = 0f,
    shape: Shape = RoundedCornerShape(20.dp),
) {
    val scale = fighterScale.coerceIn(1f, TrainingGroundPixels.MAX_FIGHTER_SCALE)
    val height = (maxOf(sceneHeight, scale) * GROUND_HEIGHT).roundToInt()
    val skyTopPx = (skyTop * GROUND_HEIGHT).roundToInt()
    val tired = mood == FighterMood.TIRED
    val frame = if (animate && !tired) rememberSkyFrame() else null
    val ground =
        remember(data.decorations, tired) {
            TrainingGroundPixels.renderGround(data.decorations, tired)
        }
    val sky = remember(weather, height, skyTopPx) { SkyCache(weather, height, skyTopPx) }

    BoxWithConstraints(
        modifier = modifier.aspectRatio(WIDTH.toFloat() / height).clip(shape),
        contentAlignment = Alignment.TopStart,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val px = size.width / WIDTH
            drawPixels(sky[frame?.value ?: SkyFrame.STILL], Offset.Zero, px)
            drawPixels(ground, Offset(0f, (height - GROUND_HEIGHT) * px), px)
        }
        val px = maxWidth / WIDTH
        FighterAvatar(
            character = data.character,
            colors = data.colors,
            rank = rank,
            mood = mood,
            outfit = data.outfit,
            gear = data.accessories,
            animate = animate,
            modifier =
                Modifier.offset(
                        x = px * TrainingGroundPixels.fighterLeft(scale),
                        y = px * (height - GROUND_HEIGHT + TrainingGroundPixels.fighterTop(scale)),
                    )
                    .size(px * FighterPixels.SIZE * scale),
        )
    }
}

/** Builds the sky only when its motion steps on, not on every animation frame. */
private class SkyCache(val weather: Weather, val height: Int, val skyTop: Int) {
    private var frame: SkyFrame? = null
    private var image: PixelImage? = null

    operator fun get(frame: SkyFrame): PixelImage {
        val cached = image
        if (cached != null && frame == this.frame) return cached
        return TrainingGroundPixels.renderSky(weather, height, skyTop, frame).also {
            this.frame = frame
            image = it
        }
    }
}

/**
 * The sky's current motion step. Read it only while drawing, so a new step repaints the canvas
 * without recomposing.
 */
@Composable
private fun rememberSkyFrame(): State<SkyFrame> {
    val transition = rememberInfiniteTransition(label = "sky")
    val time =
        transition.animateFloat(
            initialValue = 0f,
            targetValue = TrainingGroundPixels.LOOP_SECONDS,
            animationSpec =
                infiniteRepeatable(
                    animation =
                        tween(
                            durationMillis = (TrainingGroundPixels.LOOP_SECONDS * 1000).toInt(),
                            easing = LinearEasing,
                        )
                ),
            label = "time",
        )
    return remember(time) { derivedStateOf { SkyFrame.at(time.value) } }
}

/** The pixel icon of the training-ground prop [id]; draws nothing for other ids. */
@Composable
fun PropIcon(id: String, modifier: Modifier = Modifier) {
    val image = remember(id) { PropPixels.render(id) } ?: return
    Canvas(modifier = modifier.aspectRatio(1f)) { drawPixels(image) }
}

private val previewFighter =
    AvatarData(
        outfit = FighterItems.GI_BLUE,
        accessories = setOf(FighterItems.SWEATBAND, FighterItems.GLOVES),
    )

@PreviewWrapper(GritPreviewWrapper::class)
@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun AllPropsPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(Weather.SUNNY, Weather.FAIR, Weather.HAZY).forEach { weather ->
            TrainingGroundScene(
                data = previewFighter.copy(decorations = FighterItems.props.toSet()),
                rank = FighterRank.FIGHTER,
                mood = FighterMood.READY,
                weather = weather,
                animate = false,
                modifier = Modifier.width(360.dp),
            )
        }
    }
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview(widthDp = 600, heightDp = 480)
@Composable
private fun AllPropsTabletPreview() {
    TrainingGroundScene(
        data = previewFighter.copy(decorations = FighterItems.props.toSet()),
        rank = FighterRank.LEGEND,
        mood = FighterMood.READY,
        weather = Weather.SUNNY,
        animate = false,
        fighterScale = TrainingGroundPixels.MAX_FIGHTER_SCALE,
        sceneHeight = 2.4f,
        modifier = Modifier.width(600.dp),
    )
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview(widthDp = 360, heightDp = 200)
@Composable
private fun NoPropsPreview() {
    TrainingGroundScene(
        data = previewFighter,
        rank = FighterRank.ROOKIE,
        mood = FighterMood.READY,
        weather = Weather.HAZY,
        animate = false,
        modifier = Modifier.width(360.dp),
    )
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview(widthDp = 360, heightDp = 400)
@Composable
private fun TiredPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(Weather.CLOUDY, Weather.OVERCAST).forEach { weather ->
            TrainingGroundScene(
                data = previewFighter.copy(decorations = FighterItems.props.toSet()),
                rank = FighterRank.CHAMPION,
                mood = FighterMood.TIRED,
                weather = weather,
                animate = false,
                modifier = Modifier.width(360.dp),
            )
        }
    }
}
