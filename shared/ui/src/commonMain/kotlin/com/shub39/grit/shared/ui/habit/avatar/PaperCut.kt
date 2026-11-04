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

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import kotlin.random.Random

// Paper cut look (#123): every shape is a piece of paper lying on the ones below it and casts a
// soft shadow down and to the right instead of having a dark outline. The shadow is faked with two
// offset, half transparent copies of the shape, so it needs no blur and stays cheap to animate.

/** Shadow offset in units of the avatar's size. */
internal const val SHADOW = 0.012f

private val shadowColor = Color(0xFF1B2A1C)
private val nearShadow = shadowColor.copy(alpha = 0.14f)
private val farShadow = shadowColor.copy(alpha = 0.12f)

/** The soft shadow of [path]; [depth] scales how far it falls, small parts get a shorter one. */
internal fun DrawScope.paperShadow(path: Path, depth: Float = 1f) {
    val d = SHADOW * depth
    translate(left = d * 0.25f, top = d * 0.55f) { drawPath(path, nearShadow) }
    translate(left = d * 0.45f, top = d) {
        drawPath(path, farShadow)
        // a thin fringe softens the far edge
        drawPath(path, farShadow, style = Stroke(width = d * 0.6f, join = StrokeJoin.Round))
    }
}

/** [path] filled with [color] on top of its shadow. */
internal fun DrawScope.paper(path: Path, color: Color, depth: Float = 1f) {
    paperShadow(path, depth)
    drawPath(path, color)
}

/** A round ended stroke of [width] along [path], like a paper strip, with its shadow. */
internal fun DrawScope.paperStroke(path: Path, width: Float, color: Color, depth: Float = 1f) {
    val d = SHADOW * depth
    translate(left = d * 0.25f, top = d * 0.55f) {
        drawPath(
            path,
            nearShadow,
            style = Stroke(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
    translate(left = d * 0.45f, top = d) {
        drawPath(
            path,
            farShadow,
            style = Stroke(width = width + d * 0.6f, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
    drawPath(
        path,
        color,
        style = Stroke(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
}

/** A straight paper strip from [from] to [to], with its shadow. */
internal fun DrawScope.paperLine(
    from: Offset,
    to: Offset,
    width: Float,
    color: Color,
    depth: Float = 1f,
) {
    paperStroke(
        Path().apply {
            moveTo(from.x, from.y)
            lineTo(to.x, to.y)
        },
        width,
        color,
        depth,
    )
}

// fibres of the paper, the same on every frame, spread over a 2 x 2 area
private val grainDark = grainPoints(seed = 7)
private val grainLight = grainPoints(seed = 11)

private fun grainPoints(seed: Int): List<Offset> {
    val random = Random(seed)
    return List(900) { Offset(random.nextFloat() * 2f, random.nextFloat() * 2f) }
}

/** A fine paper texture over everything drawn so far, in the scene's units. */
internal fun DrawScope.drawPaperGrain() {
    drawPoints(
        grainDark,
        PointMode.Points,
        Color.Black.copy(alpha = 0.06f),
        strokeWidth = 0.004f,
        cap = StrokeCap.Round,
    )
    drawPoints(
        grainLight,
        PointMode.Points,
        Color.White.copy(alpha = 0.12f),
        strokeWidth = 0.004f,
        cap = StrokeCap.Round,
    )
}
