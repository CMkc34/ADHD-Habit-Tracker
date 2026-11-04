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
package com.shub39.grit.shared.ui.habit.avatar

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.shub39.grit.core.avatar.AvatarSpecies
import com.shub39.grit.core.avatar.PlantMood
import com.shub39.grit.core.avatar.PlantStage
import com.shub39.grit.core.avatar.ShopCatalog
import com.shub39.grit.core.avatar.ShopItemKind
import com.shub39.grit.core.avatar.Weather
import com.shub39.grit.shared.ui.GritPreviewWrapper
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

/** The scene is twice as wide as high; the plant fills the square in the middle. */
const val MEADOW_ASPECT_RATIO = 2f

// seconds after which the motion of the decorations starts over
private const val LOOP_SECONDS = 60f

private val woodLight = Color(0xFFE2BC8E)
private val woodBench = Color(0xFFC98B4F)
private val metal = Color(0xFF4E5B52)
private val lanternGreen = Color(0xFF3E4F45)
private val lanternLight = Color(0xFFFFE79A)
private val grassDark = Color(0xFF6FB24F)
private val water = Color(0xFF6EC6E8)
private val waterLight = Color(0xFFA7DFF2)
private val pink = Color(0xFFFF8FA0)
private val pinkLight = Color(0xFFFFB3C1)
private val sky = Color(0xFF90D5FF)
private val cloudShade = Color(0xFF9DB5C4)
private val cloudGray = Color(0xFF8E979C)
private val sunYellow = Color(0xFFFFD45C)
private val sunRay = Color(0xFFFFB938)
private val fishOrange = Color(0xFFFF9F43)
private val mothWing = Color(0xFFE8DCC8)

/**
 * The plant in the middle of a wide meadow under a light blue sky, together with the decorations
 * the user shows. The [weather] decides how much sun and how many clouds there are. Every
 * decoration has its own fixed spot left or right of the plant, chosen so that even the mighty tree
 * with its arms never covers one. Animals sleep with the plant when it is tired; the small
 * movements, the drifting clouds and the turning sun follow the system's animation setting like the
 * plant's sway.
 *
 * [plantScale] above 1 makes the plant that much bigger and the scene as much higher, with more sky
 * on top; the width, the decorations, the sun and the clouds stay as they are. [sceneHeight] (in
 * meadow heights, at least [plantScale]) makes the scene even higher, for a meadow that fills a
 * whole page; [skyTop] then moves the sun and the clouds that far down, below what lies on top.
 */
@Composable
fun MeadowScene(
    stage: PlantStage,
    mood: PlantMood,
    modifier: Modifier = Modifier,
    potColor: String = ShopCatalog.DEFAULT_POT_COLOR,
    accessories: Set<String> = emptySet(),
    decorations: Set<String> = emptySet(),
    weather: Weather = Weather.FAIR,
    animate: Boolean = true,
    plantScale: Float = 1f,
    sceneHeight: Float = plantScale,
    skyTop: Float = 0f,
    shape: Shape = RoundedCornerShape(20.dp),
    species: AvatarSpecies = AvatarSpecies.SPROUT,
) {
    val height = maxOf(sceneHeight, plantScale)
    val sway = if (animate) rememberSway() else 0f
    val time = if (animate) rememberMeadowTime() else 0f
    val tired = mood == PlantMood.TIRED
    val shown = decorationsInOrder.filter { it.id in decorations }

    Canvas(modifier = modifier.aspectRatio(MEADOW_ASPECT_RATIO / height).clip(shape)) {
        // everything below is drawn in a 2 x height rectangle, the meadow in its lower 2 x 1
        scale(scale = size.width / MEADOW_ASPECT_RATIO, pivot = Offset.Zero) {
            val painter = DecorationPainter(tired = tired, time = time, moving = animate && !tired)
            with(painter) { drawSky(weather, height = height, skyTop = skyTop) }
            translate(top = height - 1f) {
                with(painter) { drawGround() }
                shown.filter { !it.inFront }.forEach { it.draw(this, painter) }
                // the plant grows from its foot on the hill
                scale(scale = plantScale, pivot = Offset(1f, PLANT_FOOT_Y)) {
                    translate(left = 0.5f) {
                        drawPlantAvatar(
                            stage,
                            mood,
                            potColor,
                            accessories,
                            sway,
                            time,
                            moving = animate,
                            species = species,
                        )
                    }
                }
                shown.filter { it.inFront }.forEach { it.draw(this, painter) }
            }
            // the grain covers 2 x 2, so a high scene gets it once more below
            for (tile in 0 until ceil(height / 2f).toInt()) {
                translate(top = tile * 2f) { drawPaperGrain() }
            }
        }
    }
}

/** Seconds from 0 to [LOOP_SECONDS], then again. */
@Composable
internal fun rememberMeadowTime(): Float {
    val transition = rememberInfiniteTransition(label = "meadow")
    val time by
        transition.animateFloat(
            initialValue = 0f,
            targetValue = LOOP_SECONDS,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis = 60_000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "time",
        )
    return time
}

// where the plant's pot or trunk stands on the hill
private const val PLANT_FOOT_Y = 0.86f

// the meadow is the top of this ellipse, so it falls off towards both edges
private const val GROUND_Y = 1.07f
private const val GROUND_RX = 1.12f
private const val GROUND_RY = 0.22f

/** Height of the meadow's edge at [x]; decorations stand on it so none floats in the air. */
private fun groundY(x: Float): Float {
    val dx = (x - 1f) / GROUND_RX
    return GROUND_Y - GROUND_RY * sqrt(max(0f, 1f - dx * dx))
}

/** One decoration: [inFront] ones stand before the plant's hill, the others behind the plant. */
private class Decoration(
    val id: String,
    val inFront: Boolean,
    val draw: DrawScope.(DecorationPainter) -> Unit,
)

private fun ellipsePath(center: Offset, rx: Float, ry: Float) =
    Path().apply { addOval(Rect(center.x - rx, center.y - ry, center.x + rx, center.y + ry)) }

private fun roundRectPath(x: Float, y: Float, w: Float, h: Float, r: Float) =
    Path().apply { addRoundRect(RoundRect(Rect(Offset(x, y), Size(w, h)), CornerRadius(r))) }

/** Drawing helpers for the decorations, in the 2 x 1 rectangle of [MeadowScene]. */
private class DecorationPainter(val tired: Boolean, val time: Float, val moving: Boolean) {

    fun Color.mood(): Color = if (tired) lerp(this, tiredGray, 0.4f) else this

    /** A bump from 0 to 1 and back that lasts [length] seconds and comes every [every] seconds. */
    fun pulse(every: Float, length: Float, offset: Float = 0f): Float {
        if (!moving) return 0f
        val k = ((time + offset) % every) / length
        return if (k < 1f) sin(PI.toFloat() * k) else 0f
    }

    /** -1..1 with one full wave every [period] seconds; [period] divides [LOOP_SECONDS]. */
    fun wave(period: Float, phase: Float = 0f): Float =
        if (moving) sin(2 * PI.toFloat() * time / period + phase) else 0f

    /** Every 7.5 seconds a fish leaps out of the pond in an arc and dives back in. */
    fun DrawScope.jumpingFish() {
        if (!moving) return
        val k = (time % 7.5f) - 4f
        if (k < 0f || k > 1.1f) return
        val q = k / 1.1f
        val at = Offset(0.24f + 0.12f * q, 0.94f - 0.09f * sin(PI.toFloat() * q))
        rotate(degrees = -60f + 120f * q, pivot = at) {
            val fish =
                Path().apply {
                    addOval(Rect(at.x - 0.022f, at.y - 0.011f, at.x + 0.022f, at.y + 0.011f))
                    moveTo(at.x - 0.018f, at.y)
                    lineTo(at.x - 0.036f, at.y - 0.012f)
                    lineTo(at.x - 0.036f, at.y + 0.012f)
                    close()
                }
            paper(fish, fishOrange)
            drawCircle(outline, radius = 0.003f, center = Offset(at.x + 0.01f, at.y - 0.003f))
        }
        // a splash where it dives back in
        if (q > 0.85f) {
            drawOval(
                Color.White.copy(alpha = 0.8f),
                topLeft = Offset(0.34f, 0.934f),
                size = Size(0.04f, 0.012f),
                style = Stroke(width = 0.004f),
            )
        }
    }

    fun DrawScope.eye(center: Offset, r: Float) {
        if (tired) {
            drawArc(
                color = outline,
                startAngle = 25f,
                sweepAngle = 130f,
                useCenter = false,
                topLeft = Offset(center.x - r * 1.1f, center.y - r * 1.4f),
                size = Size(r * 2.2f, r * 2.2f),
                style = Stroke(width = r * 0.7f, cap = StrokeCap.Round),
            )
        } else {
            drawCircle(outline, radius = r, center = center)
            drawCircle(
                Color.White,
                radius = r * 0.35f,
                center = Offset(center.x + r * 0.35f, center.y - r * 0.35f),
            )
        }
    }

    /**
     * The sky over a [height] high scene; the sun and the clouds stay at its top, moved down by
     * [skyTop].
     */
    fun DrawScope.drawSky(weather: Weather, height: Float = 1f, skyTop: Float = 0f) {
        // the sky and the clouds turn grey the longer the user stays away
        val gray =
            when (weather) {
                Weather.CLOUDY -> 0.3f
                Weather.OVERCAST -> 0.55f
                else -> 0f
            }
        drawRect(
            lerp(sky, tiredGray, gray),
            topLeft = Offset.Zero,
            size = Size(MEADOW_ASPECT_RATIO, height),
        )
        translate(top = skyTop) {
            when (weather) {
                Weather.SUNNY -> sun(Offset(1.72f, 0.22f), 0.1f, rays = true)
                Weather.FAIR -> sun(Offset(1.72f, 0.22f), 0.085f, rays = true)
                Weather.HAZY -> sun(Offset(1.76f, 0.2f), 0.075f, rays = false)
                else -> Unit
            }
            // the clouds drift a little to and fro; the big ones stay clear of the plant's crown
            val drift = 0.03f * wave(LOOP_SECONDS)
            cloudsFor(weather).forEachIndexed { i, (x, y, width) ->
                val direction = if (i % 2 == 0) 1f else -0.6f
                cloud(Offset(x + drift * direction, y), width, lerp(Color.White, cloudGray, gray))
            }
        }
    }

    /** Clouds as (x, y, width); later ones are drawn in front of earlier ones and of the sun. */
    private fun cloudsFor(weather: Weather): List<Triple<Float, Float, Float>> {
        val few = listOf(Triple(0.24f, 0.2f, 0.11f), Triple(1.0f, 0.08f, 0.06f))
        val some = few + listOf(Triple(0.5f, 0.5f, 0.05f), Triple(1.93f, 0.44f, 0.05f))
        val many =
            listOf(
                Triple(0.24f, 0.2f, 0.12f),
                Triple(0.62f, 0.1f, 0.09f),
                Triple(1.0f, 0.07f, 0.08f),
                Triple(1.38f, 0.12f, 0.09f),
                Triple(1.74f, 0.24f, 0.12f),
                Triple(0.5f, 0.5f, 0.06f),
                Triple(1.92f, 0.46f, 0.07f),
            )
        return when (weather) {
            Weather.SUNNY -> listOf(Triple(0.24f, 0.18f, 0.08f), Triple(1.0f, 0.08f, 0.05f))
            Weather.FAIR -> some
            // one cloud half in front of the sun
            Weather.HAZY -> some + Triple(1.66f, 0.29f, 0.08f)
            Weather.CLOUDY -> many
            Weather.OVERCAST ->
                listOf(
                    Triple(0.06f, 0.08f, 0.13f),
                    Triple(1.94f, 0.08f, 0.13f),
                    Triple(0.85f, 0.22f, 0.1f),
                    Triple(1.2f, 0.26f, 0.1f),
                ) + many
        }
    }

    /** A friendly sun in the plant's paper look; its rays turn once a minute. */
    private fun DrawScope.sun(center: Offset, r: Float, rays: Boolean) {
        if (rays) {
            // turned by hand, so every ray's shadow still falls down to the right
            val turn = if (moving) time * 360f / LOOP_SECONDS else 0f
            for (i in 0 until 10) {
                val a = (i * 36f + turn) * PI.toFloat() / 180f
                val dir = Offset(cos(a), sin(a))
                paperLine(
                    center + dir * (r * 1.3f),
                    center + dir * (r * 1.6f),
                    r * 0.14f,
                    sunRay,
                    depth = 0.5f,
                )
            }
        }
        paper(ellipsePath(center, r, r), sunYellow)
        eye(Offset(center.x - r * 0.33f, center.y - r * 0.1f), r * 0.11f)
        eye(Offset(center.x + r * 0.33f, center.y - r * 0.1f), r * 0.11f)
        drawArc(
            color = outline,
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(center.x - r * 0.3f, center.y - r * 0.15f),
            size = Size(r * 0.6f, r * 0.5f),
            style = Stroke(width = r * 0.09f, cap = StrokeCap.Round),
        )
        listOf(-1f, 1f).forEach { side ->
            drawOval(
                pink.copy(alpha = 0.5f),
                topLeft = Offset(center.x + side * r * 0.55f - r * 0.12f, center.y + r * 0.1f),
                size = Size(r * 0.24f, r * 0.14f),
            )
        }
    }

    /** A puffy cloud about [width] times 2.6 wide around [center], with a soft shade below. */
    private fun DrawScope.cloud(center: Offset, width: Float, color: Color) {
        val puffs =
            listOf(
                Triple(-0.85f, 0.15f, 0.45f),
                Triple(-0.35f, -0.2f, 0.6f),
                Triple(0.3f, -0.3f, 0.7f),
                Triple(0.9f, 0.1f, 0.45f),
                Triple(0.05f, 0.25f, 0.5f),
            )
        val path =
            Path().apply {
                puffs.forEach { (dx, dy, r) ->
                    addOval(Rect(Offset(center.x + dx * width, center.y + dy * width), r * width))
                }
            }
        drawPath(path, lerp(color, cloudShade, 0.3f))
        translate(top = -0.1f * width) { drawPath(path, color) }
    }

    fun DrawScope.drawGround() {
        clipRect(left = 0f, top = 0f, right = MEADOW_ASPECT_RATIO, bottom = 1f) {
            // two paler hills further back give the paper scene depth
            val far = lerp(meadowGreen, sky, 0.35f).mood()
            paper(ellipsePath(Offset(1.72f, 1.0f), 0.62f, 0.25f), far)
            paper(ellipsePath(Offset(0.3f, 1.0f), 0.66f, 0.27f), far)
            paper(ellipsePath(Offset(1f, GROUND_Y), GROUND_RX, GROUND_RY), meadowGreen.mood())
            // a few tufts so the wide meadow does not look empty
            listOf(
                    Offset(0.05f, 0.96f),
                    Offset(0.62f, 0.93f),
                    Offset(1.38f, 0.925f),
                    Offset(1.95f, 0.97f),
                    Offset(0.9f, 0.985f),
                )
                .forEach { tuft ->
                    for (side in -1..1) {
                        drawLine(
                            grassDark.mood(),
                            tuft,
                            Offset(tuft.x + side * 0.008f, tuft.y - 0.02f + (side * side) * 0.006f),
                            0.005f,
                            StrokeCap.Round,
                        )
                    }
                }
        }
    }
}

private val decorationsInOrder: List<Decoration> =
    listOf(
        Decoration("deco_fence", inFront = false) { fence(it) },
        Decoration("deco_hedgehog", inFront = false) { hedgehog(it) },
        Decoration("deco_bench", inFront = false) { bench(it) },
        Decoration("deco_lantern", inFront = false) { lantern(it) },
        Decoration("deco_mushrooms", inFront = true) { mushrooms(it) },
        Decoration("deco_snail", inFront = true) { snail(it) },
        Decoration("deco_rabbit", inFront = true) { rabbit(it) },
        Decoration("deco_pond", inFront = true) { pond(it) },
    )

private fun DrawScope.fence(p: DecorationPainter) {
    with(p) {
        // pickets stand upright, their feet and the rails follow the slope of the meadow
        val wood = woodLight.mood()
        val start = 0.03f
        val end = 0.36f
        listOf(-0.085f, -0.045f).forEach { above ->
            val rail =
                Path().apply {
                    moveTo(start, groundY(start) + above)
                    lineTo(end, groundY(end) + above)
                    lineTo(end, groundY(end) + above + 0.018f)
                    lineTo(start, groundY(start) + above + 0.018f)
                    close()
                }
            paper(rail, wood)
        }
        for (i in 0 until 5) {
            val x = 0.045f + i * 0.07f
            val foot = groundY(x + 0.016f) + 0.006f
            val top = foot - 0.125f
            val picket =
                Path().apply {
                    moveTo(x, foot)
                    lineTo(x, top + 0.02f)
                    lineTo(x + 0.016f, top)
                    lineTo(x + 0.032f, top + 0.02f)
                    lineTo(x + 0.032f, foot)
                    close()
                }
            paper(picket, wood)
        }
    }
}

private fun DrawScope.mushrooms(p: DecorationPainter) {
    with(p) {
        listOf(Triple(0.585f, 0.972f, 0.03f), Triple(0.64f, 0.982f, 0.022f)).forEachIndexed {
            i,
            (x, y, s) ->
            // they bob as if they were breathing
            val bob = wave(2.5f, phase = i * 1.7f)
            scale(scaleX = 1f - 0.04f * bob, scaleY = 1f + 0.06f * bob, pivot = Offset(x, y)) {
                paper(
                    roundRectPath(x - s * 0.35f, y - s * 1.1f, s * 0.7f, s * 1.15f, s * 0.25f),
                    Color(0xFFF3E3C3),
                )
                val cap =
                    Path().apply {
                        moveTo(x - s, y - s * 0.95f)
                        quadraticTo(x - s, y - s * 2.1f, x, y - s * 2.1f)
                        quadraticTo(x + s, y - s * 2.1f, x + s, y - s * 0.95f)
                        quadraticTo(x, y - s * 1.15f, x - s, y - s * 0.95f)
                        close()
                    }
                paper(cap, Color(0xFFE5533D).mood())
                listOf(
                        Triple(-0.45f, -1.35f, 0.17f),
                        Triple(0.2f, -1.7f, 0.14f),
                        Triple(0.5f, -1.2f, 0.12f),
                    )
                    .forEach { (dx, dy, r) ->
                        drawCircle(
                            Color.White,
                            radius = r * s,
                            center = Offset(x + dx * s, y + dy * s),
                        )
                    }
            }
        }
    }
}

private fun DrawScope.snail(p: DecorationPainter) {
    with(p) {
        val shell = Color(0xFFF2B84B).mood()
        val skin = Color(0xFFD8C7A0).mood()
        // crawls back and forth, turning at each end
        val x = 1.36f + 0.025f * wave(20f)
        val heading = if (moving && cos(2 * PI.toFloat() * time / 20f) < 0f) -1f else 1f
        translate(left = x, top = 0.955f) {
            scale(scaleX = heading, scaleY = 1f, pivot = Offset.Zero) {
                // the feelers wiggle while it crawls
                val wiggle = 0.006f * wave(1.25f)
                listOf(
                        Offset(0.038f, -0.04f) to Offset(0.03f + wiggle, -0.068f),
                        Offset(0.047f, -0.038f) to Offset(0.055f - wiggle, -0.064f),
                    )
                    .forEach { (from, to) ->
                        paperLine(from, to, 0.004f, skin)
                        drawCircle(outline, radius = 0.006f, center = to)
                    }
                val body =
                    Path().apply {
                        moveTo(-0.045f, 0f)
                        quadraticTo(-0.05f, -0.018f, -0.02f, -0.02f)
                        lineTo(0.03f, -0.02f)
                        quadraticTo(0.045f, -0.045f, 0.052f, -0.03f)
                        quadraticTo(0.06f, -0.005f, 0.04f, 0f)
                        close()
                    }
                paper(body, skin)
                paper(ellipsePath(Offset(-0.005f, -0.04f), 0.03f, 0.028f), shell)
                val spiral =
                    Path().apply {
                        var a = 0f
                        while (a < 9f) {
                            val r = 0.004f + a * 0.0026f
                            val p = Offset(-0.005f + r * cos(a), -0.04f + r * sin(a))
                            if (a == 0f) moveTo(p.x, p.y) else lineTo(p.x, p.y)
                            a += 0.25f
                        }
                    }
                drawPath(spiral, outline, style = Stroke(width = 0.005f, cap = StrokeCap.Round))
            }
        }
    }
}

private fun DrawScope.hedgehog(p: DecorationPainter) {
    with(p) {
        // it trots a little back and forth and sniffs
        val x = 0.47f + 0.015f * wave(15f)
        val y = groundY(x) - 0.001f
        val sniff = 0.006f * pulse(every = 2.6f, length = 0.5f)
        val step = 0.003f * wave(0.5f)
        val spikes =
            Path().apply {
                moveTo(x - 0.055f, y)
                for (i in 0..10) {
                    val a = PI.toFloat() + i * PI.toFloat() / 10
                    val r = if (i % 2 == 1) 0.055f else 0.042f
                    lineTo(x - 0.005f + r * cos(a) * 1.05f, y + r * sin(a) * 0.9f)
                }
                close()
            }
        paper(spikes, Color(0xFF7A5C4A).mood())
        val skin = Color(0xFFE8C9A0).mood()
        val face =
            Path().apply {
                moveTo(x + 0.02f, y - 0.03f)
                quadraticTo(x + 0.07f + sniff, y - 0.02f, x + 0.075f + sniff, y - 0.006f)
                quadraticTo(x + 0.06f, y + 0.004f, x + 0.02f, y)
                close()
            }
        paper(face, skin)
        drawCircle(outline, radius = 0.007f, center = Offset(x + 0.077f + sniff, y - 0.009f))
        eye(Offset(x + 0.045f, y - 0.018f), 0.0055f)
        listOf(-0.03f to step, 0f to -step).forEach { (dx, lift) ->
            paper(roundRectPath(x + dx, y - 0.006f - abs(lift), 0.012f, 0.012f, 0.005f), skin)
        }
    }
}

private fun DrawScope.rabbit(p: DecorationPainter) {
    with(p) {
        val fur = Color(0xFFF4EFE7).mood()
        // a high hop in a small arc that lands where it started
        val hop = 0.055f * pulse(every = 5f, length = 0.6f)
        val twitch = 14f * pulse(every = 3f, length = 0.3f, offset = 1.2f)
        val x = 1.76f - 0.25f * hop
        val y = groundY(x) - 0.001f - hop
        if (!tired) {
            drawOval(
                Color.Black.copy(alpha = 0.15f),
                topLeft = Offset(x - 0.045f + hop / 2, groundY(x) + 0.001f),
                size = Size(0.09f - hop, 0.016f),
            )
        }
        paper(ellipsePath(Offset(x - 0.048f, y - 0.028f), 0.012f, 0.012f), Color.White)
        paper(ellipsePath(Offset(x, y - 0.025f), 0.045f, 0.032f), fur)
        // the ears stand up and twitch, asleep they lie back
        listOf(-1f to 0f, 1f to twitch).forEach { (side, wiggle) ->
            val base = Offset(x + 0.035f + side * 0.009f, y - 0.072f)
            val degrees = if (tired) -100f - side * 8f else side * 10f - wiggle
            rotate(degrees = degrees, pivot = base) {
                val center = Offset(base.x, base.y - 0.036f)
                paper(ellipsePath(center, 0.012f, 0.036f), fur)
                drawOval(
                    pinkLight,
                    topLeft = Offset(center.x - 0.005f, center.y - 0.024f),
                    size = Size(0.01f, 0.048f),
                )
            }
        }
        paper(ellipsePath(Offset(x + 0.038f, y - 0.058f), 0.028f, 0.025f), fur)
        eye(Offset(x + 0.05f, y - 0.062f), 0.005f)
        drawCircle(pink, radius = 0.005f, center = Offset(x + 0.064f, y - 0.052f))
        drawOval(
            pink.copy(alpha = 0.6f),
            topLeft = Offset(x + 0.032f, y - 0.05f),
            size = Size(0.016f, 0.01f),
        )
        if (tired) {
            // a small "z" above the sleeping head
            listOf(Offset(x + 0.075f, y - 0.11f) to 0.02f, Offset(x + 0.1f, y - 0.135f) to 0.014f)
                .forEach { (at, s) ->
                    val z =
                        Path().apply {
                            moveTo(at.x, at.y)
                            lineTo(at.x + s, at.y)
                            lineTo(at.x, at.y + s)
                            lineTo(at.x + s, at.y + s)
                        }
                    drawPath(
                        z,
                        outline,
                        style =
                            Stroke(
                                width = s * 0.25f,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round,
                            ),
                    )
                }
        }
    }
}

private fun DrawScope.bench(p: DecorationPainter) {
    with(p) {
        val wood = woodBench.mood()
        val x = 1.44f
        // the bench leans with the meadow so all four feet touch the ground
        val left = x + 0.028f
        val right = x + 0.248f
        val degrees = atan2(groundY(right) - groundY(left), right - left) * 180f / PI.toFloat()
        translate(left = left, top = groundY(left) + 0.004f) {
            rotate(degrees = degrees, pivot = Offset.Zero) {
                // drawn with its feet at y = 0.875
                translate(left = -left, top = -0.875f) {
                    listOf(x + 0.025f, x + 0.235f).forEach {
                        paper(roundRectPath(it, 0.71f, 0.016f, 0.165f, 0.006f), metal)
                    }
                    paper(roundRectPath(x, 0.72f, 0.275f, 0.024f, 0.008f), wood)
                    paper(roundRectPath(x, 0.752f, 0.275f, 0.024f, 0.008f), wood)
                    paper(roundRectPath(x - 0.01f, 0.8f, 0.295f, 0.024f, 0.008f), wood)
                    listOf(x + 0.02f, x + 0.24f).forEach {
                        paper(roundRectPath(it, 0.815f, 0.016f, 0.06f, 0.006f), metal)
                    }
                }
            }
        }
    }
}

private fun DrawScope.pond(p: DecorationPainter) {
    with(p) {
        // reeds behind the water
        listOf(Triple(0.14f, 0.93f, 0.11f), Triple(0.165f, 0.935f, 0.085f)).forEachIndexed {
            i,
            (x, y, h) ->
            val sway = 0.008f * wave(4f, phase = i.toFloat())
            paperLine(
                Offset(x, y),
                Offset(x + 0.004f + sway, y - h),
                0.006f,
                Color(0xFF3FA34D).mood(),
            )
            paper(
                roundRectPath(x - 0.006f + sway, y - h - 0.005f, 0.016f, 0.04f, 0.008f),
                Color(0xFF8A5A3B).mood(),
            )
        }
        paper(ellipsePath(Offset(0.3f, 0.945f), 0.2f, 0.048f), water.mood())
        drawOval(waterLight.mood(), topLeft = Offset(0.14f, 0.915f), size = Size(0.24f, 0.044f))
        if (moving) {
            // a ripple that grows and fades every three seconds
            val k = (time % 3f) / 3f
            drawOval(
                Color.White.copy(alpha = 0.7f * (1 - k)),
                topLeft = Offset(0.36f - 0.015f - 0.05f * k, 0.955f - 0.005f - 0.016f * k),
                size = Size(2 * (0.015f + 0.05f * k), 2 * (0.005f + 0.016f * k)),
                style = Stroke(width = 0.004f),
            )
        }
        jumpingFish()
        val pad =
            Path().apply {
                moveTo(0.22f, 0.95f)
                arcTo(Rect(Offset(0.22f, 0.95f), 0.028f), 20f, 320f, false)
                close()
            }
        scale(scaleX = 1f, scaleY = 0.45f, pivot = Offset(0.22f, 0.95f)) {
            paper(pad, Color(0xFF58B368).mood())
        }
        val flower = Offset(0.226f, 0.943f)
        for (i in 0 until 5) {
            val a = i * 72f * PI.toFloat() / 180f
            drawCircle(
                Color(0xFFFFC2D1),
                radius = 0.007f,
                center = Offset(flower.x + 0.008f * sin(a), flower.y - 0.008f * cos(a)),
            )
        }
        drawCircle(Color(0xFFFFD45C), radius = 0.005f, center = flower)
    }
}

private fun DrawScope.lantern(p: DecorationPainter) {
    with(p) {
        val x = 1.94f
        // drawn with its foot at y = 0.885, then moved onto the meadow
        translate(top = groundY(x) - 0.885f) {
            val glow = if (moving) 0.75f + 0.25f * sin(time * 7f) * sin(time * 2.3f) else 0.9f
            drawCircle(
                Brush.radialGradient(
                    colors =
                        listOf(
                            lanternLight.copy(alpha = 0.75f * glow),
                            lanternLight.copy(alpha = 0f),
                        ),
                    center = Offset(x, 0.555f),
                    radius = 0.09f,
                ),
                radius = 0.09f,
                center = Offset(x, 0.555f),
            )
            paper(roundRectPath(x - 0.008f, 0.58f, 0.016f, 0.3f, 0.006f), lanternGreen)
            paper(roundRectPath(x - 0.022f, 0.875f, 0.044f, 0.016f, 0.006f), lanternGreen)
            val head =
                Path().apply {
                    moveTo(x - 0.026f, 0.585f)
                    lineTo(x - 0.032f, 0.53f)
                    lineTo(x + 0.032f, 0.53f)
                    lineTo(x + 0.026f, 0.585f)
                    close()
                }
            paper(head, lanternLight)
            val roof =
                Path().apply {
                    moveTo(x - 0.045f, 0.532f)
                    lineTo(x, 0.5f)
                    lineTo(x + 0.045f, 0.532f)
                    close()
                }
            paper(roof, lanternGreen)
            drawLine(lanternGreen, Offset(x, 0.53f), Offset(x, 0.585f), 0.004f)
            if (moving) {
                // a moth circles the light
                val a = 2 * PI.toFloat() * time / 2.5f
                val moth = Offset(x - 0.02f + 0.045f * cos(a), 0.555f + 0.03f * sin(a * 2f))
                val beat = 0.4f + 0.6f * abs(sin(time * 22f))
                scale(scaleX = beat, scaleY = 1f, pivot = moth) {
                    listOf(-1f, 1f).forEach { side ->
                        val wing =
                            ellipsePath(Offset(moth.x + side * 0.008f, moth.y), 0.008f, 0.006f)
                        paper(wing, mothWing, depth = 0.3f)
                    }
                }
            }
        }
    }
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview(widthDp = 400, heightDp = 620)
@Composable
private fun MeadowScenePreview() {
    val all = ShopCatalog.items.filter { it.kind == ShopItemKind.DECORATION }.map { it.id }.toSet()
    Column {
        listOf(
                Triple(PlantStage.SEEDLING, PlantMood.HAPPY, Weather.HAZY),
                Triple(PlantStage.TREE, PlantMood.HAPPY, Weather.SUNNY),
                Triple(PlantStage.MIGHTY_TREE, PlantMood.TIRED, Weather.CLOUDY),
            )
            .forEach { (stage, mood, weather) ->
                MeadowScene(
                    stage = stage,
                    mood = mood,
                    decorations = all,
                    weather = weather,
                    animate = false,
                    modifier = Modifier.width(400.dp),
                )
            }
    }
}
