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
package com.shub39.grit.shared.ui.habit.motivation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shub39.grit.shared.ui.habit.ui.component.WaterText
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.motivation_tap_to_continue
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

/** Background of a [CelebrationStage]: a radial gradient and the light rays turning on top. */
@Immutable
data class StageColors(val center: Color, val middle: Color, val edge: Color, val rays: Color)

/** Green stage after a check-in. */
val CheckInStageColors =
    StageColors(
        center = Color(0xFF3D9A46),
        middle = Color(0xFF1F5B27),
        edge = Color(0xFF123A18),
        rays = Color(0x29FFEB8C),
    )

/** Ice blue stage when a freeze saved the streak. */
val FreezeStageColors =
    StageColors(
        center = Color(0xFF4F9AD3),
        middle = Color(0xFF1F4F7A),
        edge = Color(0xFF11304D),
        rays = Color(0x29E3F4FF),
    )

/** Gold stage for the daily bonus, behind the blue water coin. */
val BonusStageColors =
    StageColors(
        center = Color(0xFFF2B33D),
        middle = Color(0xFFB8680F),
        edge = Color(0xFF6E3A06),
        rays = Color(0x33FFF6D6),
    )

/** Yellow of the energy pill and the header, the same as the plant's flowers. */
val StageYellow = Color(0xFFFFD45C)

/** Dark outline color of the plant, used for text on [StageYellow]. */
val StageInk = Color(0xFF2B3A2C)

private const val RAY_COUNT = 16
private const val RAY_WIDTH_DEGREES = 9f
private const val TAP_GUARD_MILLIS = 600L

/**
 * Full screen celebration with slowly turning light rays. Tapping anywhere or the back key closes
 * it. [content] is centered, a blinking "tap to continue" hint sits at the bottom. Without
 * [tapToDismiss] only the back key closes it and the hint is left out, for a stage with its own
 * buttons and input.
 */
@Composable
fun CelebrationStage(
    colors: StageColors,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    tapToDismiss: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val transition = rememberInfiniteTransition(label = "stage")
        val rotation by
            transition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(tween(30_000, easing = LinearEasing)),
                label = "rays",
            )
        val hintAlpha by
            transition.animateFloat(
                initialValue = 0.75f,
                targetValue = 0.3f,
                animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
                label = "hint",
            )

        // ignore taps right after opening, so the quote is not skipped by a double tap
        var ready by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            delay(TAP_GUARD_MILLIS)
            ready = true
        }

        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .drawWithCache {
                        val pivot = Offset(size.width / 2, size.height * 0.38f)
                        val background =
                            Brush.radialGradient(
                                0f to colors.center,
                                0.6f to colors.middle,
                                1f to colors.edge,
                                center = pivot,
                                radius = hypot(size.width, size.height) * 0.6f,
                            )
                        val rays = raysPath(pivot, hypot(size.width, size.height))
                        onDrawBehind {
                            drawRect(background)
                            rotate(rotation, pivot) { drawPath(rays, colors.rays) }
                        }
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { if (ready && tapToDismiss) onDismiss() },
                    )
        ) {
            Column(
                modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Spacer(Modifier.weight(1f))
                content()
                Spacer(Modifier.weight(1f))
                if (tapToDismiss) {
                    Text(
                        text = stringResource(Res.string.motivation_tap_to_continue),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        modifier = Modifier.graphicsLayer { alpha = hintAlpha },
                    )
                }
            }
        }
    }
}

/** Wedges around [pivot], long enough to reach every corner while turning. */
private fun raysPath(pivot: Offset, length: Float): Path =
    Path().apply {
        repeat(RAY_COUNT) { i ->
            val start = i * 360f / RAY_COUNT
            moveTo(pivot.x, pivot.y)
            lineTo(pivot + direction(start) * length)
            lineTo(pivot + direction(start + RAY_WIDTH_DEGREES) * length)
            close()
        }
    }

private fun direction(degrees: Float): Offset {
    val radians = degrees * PI / 180
    return Offset(cos(radians).toFloat(), sin(radians).toFloat())
}

private fun Path.lineTo(point: Offset) = lineTo(point.x, point.y)

/**
 * Pops the element in with a bounce after [delayMillis]. Use as `graphicsLayer { scaleX = it;
 * scaleY = it }`.
 */
@Composable
fun rememberPopIn(delayMillis: Long = 0): Float {
    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(delayMillis)
        scale.animateTo(
            targetValue = 1f,
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
        )
    }
    return scale.value
}

/** Counts from 0 up to [target] when shown. */
@Composable
fun rememberCountUp(target: Int): Int {
    val value = remember { Animatable(0f) }
    LaunchedEffect(target) {
        value.animateTo(target.toFloat(), tween(700 + target.coerceAtMost(60) * 8))
    }
    return value.value.toInt()
}

/** Yellow pill, e.g. for the day's "+1 💧". */
@Composable
fun StagePill(text: String, modifier: Modifier = Modifier) {
    WaterText(
        text = text,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
        color = StageInk,
        textAlign = TextAlign.Center,
        modifier =
            modifier
                .background(StageYellow, CircleShape)
                .padding(horizontal = 14.dp, vertical = 6.dp),
    )
}
