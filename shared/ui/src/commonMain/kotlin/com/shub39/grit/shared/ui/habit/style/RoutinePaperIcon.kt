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

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.PathParser
import com.shub39.grit.core.habits.style.RoutineIcon
import com.shub39.grit.core.habits.style.RoutineStyle
import com.shub39.grit.shared.ui.habit.avatar.SHADOW
import com.shub39.grit.shared.ui.habit.avatar.paper
import com.shub39.grit.shared.ui.habit.avatar.paperStroke

/**
 * One piece of paper of a routine icon in a 100 x 100 box: an SVG [path], filled with [color] or,
 * when [stroke] is above 0, drawn as a round strip that wide. [depth] is how far its shadow falls.
 */
internal class PaperLayer(
    val path: String,
    val color: Long,
    val depth: Float = 0f,
    val stroke: Float = 0f,
)

/**
 * The routine's icon in the paper cut look (#126): a colourful motif of a few paper layers, on a
 * light tile in the routine's colour when [withTile] is set. Square, fills [modifier]'s size.
 */
@Composable
fun RoutinePaperIcon(style: RoutineStyle, modifier: Modifier = Modifier, withTile: Boolean = true) {
    val layers =
        remember(style.icon) {
            style.icon.paperLayers().map { it to PathParser().parsePathString(it.path).toPath() }
        }
    val tint = lerp(style.color.color, Color.White, TILE_TINT)
    Canvas(modifier = modifier) {
        scale(scale = size.minDimension / BOX, pivot = Offset.Zero) {
            if (withTile) {
                paper(tilePath, tint, depth = 5f / SHADOW)
                // the motif sits a bit smaller in the middle of the tile
                translate(left = 6f, top = 4f) {
                    scale(scale = 0.84f, pivot = Offset.Zero) { drawLayers(layers) }
                }
            } else {
                drawLayers(layers)
            }
        }
    }
}

private const val BOX = 100f
private const val TILE_TINT = 0.78f

private val tilePath =
    Path().apply {
        addRoundRect(RoundRect(Rect(Offset(4f, 2f), Offset(92f, 90f)), CornerRadius(24f)))
    }

private fun DrawScope.drawLayers(layers: List<Pair<PaperLayer, Path>>) {
    layers.forEach { (layer, path) ->
        val color = Color(layer.color)
        // depth is in units of the box, the paper helpers count in SHADOW steps
        val depth = layer.depth / SHADOW
        if (layer.stroke > 0f) {
            if (layer.depth > 0f) paperStroke(path, layer.stroke, color, depth)
            else
                drawPath(
                    path,
                    color,
                    style =
                        Stroke(width = layer.stroke, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
        } else {
            if (layer.depth > 0f) paper(path, color, depth) else drawPath(path, color)
        }
    }
}

/** The paper layers of each icon, from back to front. */
internal fun RoutineIcon.paperLayers(): List<PaperLayer> =
    when (this) {
        RoutineIcon.BOLT ->
            listOf(
                PaperLayer("M57,12L25,58L46,58L40,88L75,40L54,40Z", 0xFFFF9F43, depth = 4f),
                PaperLayer("M54,24L35,53L50,53L46,72L65,45L50,45Z", 0xFFFFD45C, depth = 2f),
            )
        RoutineIcon.WATER ->
            listOf(
                PaperLayer(
                    "M50,16 C50,16 28,42 28,58 A22,22 0 0,0 72,58 C72,42 50,16 50,16Z",
                    0xFF4FB3F0,
                    depth = 4f,
                ),
                PaperLayer(
                    "M50,34 C50,34 38,50 38,59 A12,12 0 0,0 62,59 C62,50 50,34 50,34Z",
                    0xFFA9DDF8,
                    depth = 2f,
                ),
                PaperLayer("M40.6,57A3.4,3.4 0 1,0 47.4,57A3.4,3.4 0 1,0 40.6,57Z", 0xFFFFFFFF),
            )
        RoutineIcon.DUMBBELL ->
            listOf(
                PaperLayer("M26,50H74", 0xFF78909C, depth = 3f, stroke = 8f),
                PaperLayer(
                    "M18,32H22Q26,32 26,36V64Q26,68 22,68H18Q14,68 14,64V36Q14,32 18,32Z",
                    0xFF455A64,
                    depth = 4f,
                ),
                PaperLayer(
                    "M78,32H82Q86,32 86,36V64Q86,68 82,68H78Q74,68 74,64V36Q74,32 78,32Z",
                    0xFF455A64,
                    depth = 4f,
                ),
                PaperLayer(
                    "M30,38H32Q35,38 35,41V59Q35,62 32,62H30Q27,62 27,59V41Q27,38 30,38Z",
                    0xFF78909C,
                    depth = 2f,
                ),
                PaperLayer(
                    "M68,38H70Q73,38 73,41V59Q73,62 70,62H68Q65,62 65,59V41Q65,38 68,38Z",
                    0xFF78909C,
                    depth = 2f,
                ),
            )
        RoutineIcon.RUN ->
            listOf(
                PaperLayer(
                    "M18,62 Q18,50 30,50 L56,50 Q64,36 72,40 Q84,46 82,62 Q82,72 72,72 L28,72 Q18,72 18,62Z",
                    0xFFFF8A75,
                    depth = 4f,
                ),
                PaperLayer(
                    "M23,66H77Q82,66 82,71V71Q82,76 77,76H23Q18,76 18,71V71Q18,66 23,66Z",
                    0xFFFFF8E7,
                    depth = 2f,
                ),
                PaperLayer("M50,50L55,43M58,50L63,43M66,52L71,46", 0xFFFFFFFF, stroke = 3f),
                PaperLayer("M14,40H28M10,48H22", 0xFFCFD8DC, stroke = 3f),
            )
        RoutineIcon.BIKE ->
            listOf(
                PaperLayer(
                    "M13,64A15,15 0 1,0 43,64A15,15 0 1,0 13,64Z",
                    0xFF455A64,
                    depth = 2f,
                    stroke = 5f,
                ),
                PaperLayer(
                    "M57,64A15,15 0 1,0 87,64A15,15 0 1,0 57,64Z",
                    0xFF455A64,
                    depth = 2f,
                    stroke = 5f,
                ),
                PaperLayer(
                    "M28,64L42,42H64L72,64M42,42L52,64L64,42",
                    0xFFE5533D,
                    depth = 2f,
                    stroke = 5f,
                ),
                PaperLayer("M36,34H50", 0xFFA0704C, depth = 1f, stroke = 5f),
                PaperLayer("M64,42L62,32H72", 0xFF455A64, depth = 1f, stroke = 4f),
                PaperLayer("M48,64A4,4 0 1,0 56,64A4,4 0 1,0 48,64Z", 0xFF78909C),
            )
        RoutineIcon.SWIM ->
            listOf(
                PaperLayer("M32,40A10,10 0 1,0 52,40A10,10 0 1,0 32,40Z", 0xFFF1C7A0, depth = 3f),
                PaperLayer("M32,40 A10,10 0 0,1 52,40 Z", 0xFFE5533D, depth = 1f),
                PaperLayer("M52,46 Q64,30 78,40", 0xFFF1C7A0, depth = 2f, stroke = 7f),
                PaperLayer(
                    "M12,60 Q20,53 28,60 T44,60 T60,60 T76,60 T92,60",
                    0xFF4FB3F0,
                    depth = 2f,
                    stroke = 7f,
                ),
                PaperLayer(
                    "M12,76 Q20,69 28,76 T44,76 T60,76 T76,76 T92,76",
                    0xFFA9DDF8,
                    depth = 2f,
                    stroke = 7f,
                ),
            )
        RoutineIcon.MEDITATION ->
            listOf(
                PaperLayer("M35,32A15,15 0 1,0 65,32A15,15 0 1,0 35,32Z", 0xFFFFF3C4, depth = 1f),
                PaperLayer("M20,74A30,9 0 1,0 80,74A30,9 0 1,0 20,74Z", 0xFF9B6BD6, depth = 3f),
                PaperLayer("M36,72 Q36,46 50,46 Q64,46 64,72 Z", 0xFFCDB4F0, depth = 3f),
                PaperLayer(
                    "M40,56 Q30,64 26,72M60,56 Q70,64 74,72",
                    0xFFF1C7A0,
                    depth = 1f,
                    stroke = 6f,
                ),
                PaperLayer("M40,33A10,10 0 1,0 60,33A10,10 0 1,0 40,33Z", 0xFFF1C7A0, depth = 3f),
            )
        RoutineIcon.SLEEP ->
            listOf(
                PaperLayer("M58,20 A30,30 0 1,0 80,64 A23,23 0 1,1 58,20Z", 0xFFFFD45C, depth = 4f),
                PaperLayer(
                    "M73,22L74.7,28.3L81,30L74.7,31.7L73,38L71.3,31.7L65,30L71.3,28.3Z",
                    0xFFFFF3C4,
                    depth = 2f,
                ),
                PaperLayer(
                    "M66,47L67.06,50.94L71,52L67.06,53.06L66,57L64.94,53.06L61,52L64.94,50.94Z",
                    0xFFFFF3C4,
                    depth = 1.5f,
                ),
            )
        RoutineIcon.MORNING ->
            listOf(
                PaperLayer(
                    "M25.57,51.11L16.17,47.69M33.29,40.08L26.86,32.42M45.49,34.39L43.75,24.55M54.51,34.39L56.25,24.55M66.71,40.08L73.14,32.42M74.43,51.11L83.83,47.69",
                    0xFFFF9F43,
                    depth = 1f,
                    stroke = 5f,
                ),
                PaperLayer("M30,60A20,20 0 1,0 70,60A20,20 0 1,0 30,60Z", 0xFFFFD45C, depth = 3f),
                PaperLayer("M10,70 Q50,52 90,70 V86 H10 Z", 0xFF6CCB5F, depth = 4f),
                PaperLayer("M10,80 Q40,68 90,80 V86 H10 Z", 0xFF3FA34D, depth = 2f),
            )
        RoutineIcon.ALARM ->
            listOf(
                PaperLayer("M21,30A9,9 0 1,0 39,30A9,9 0 1,0 21,30Z", 0xFFFFD45C, depth = 3f),
                PaperLayer("M61,30A9,9 0 1,0 79,30A9,9 0 1,0 61,30Z", 0xFFFFD45C, depth = 3f),
                PaperLayer("M34,78L28,86M66,78L72,86", 0xFF455A64, depth = 1f, stroke = 5f),
                PaperLayer("M24,54A26,26 0 1,0 76,54A26,26 0 1,0 24,54Z", 0xFFE5533D, depth = 4f),
                PaperLayer("M31,54A19,19 0 1,0 69,54A19,19 0 1,0 31,54Z", 0xFFFFF8E7, depth = 2f),
                PaperLayer("M50,54V41M50,54L59,59", 0xFF2B3A2C, stroke = 3.4f),
            )
        RoutineIcon.BOOK ->
            listOf(
                PaperLayer("M20,28 Q35,23 50,30 V78 Q35,71 20,76Z", 0xFFFFF8E7, depth = 3f),
                PaperLayer("M80,28 Q65,23 50,30 V78 Q65,71 80,76Z", 0xFFF3E6C8, depth = 3f),
                PaperLayer("M47,30 H53 V82 L50,77 L47,82Z", 0xFFE5533D, depth = 2f),
                PaperLayer(
                    "M27,40 Q35,37 43,41M27,49 Q35,46 43,50M27,58 Q35,55 43,59M57,41 Q65,37 73,40M57,50 Q65,46 73,49",
                    0xFFC9B48A,
                    stroke = 1.8f,
                ),
            )
        RoutineIcon.WRITE ->
            listOf(
                PaperLayer(
                    "M21,14H61Q66,14 66,19V71Q66,76 61,76H21Q16,76 16,71V19Q16,14 21,14Z",
                    0xFFFFF8E7,
                    depth = 3f,
                ),
                PaperLayer("M24,28H56M24,38H56M24,48H46", 0xFFC9B48A, stroke = 2f),
                PaperLayer("M34,72L68,38L77,47L43,81Z", 0xFFFFD45C, depth = 3f),
                PaperLayer("M34,72L43,81L26,88Z", 0xFFF1C7A0, depth = 2f),
                PaperLayer("M29.5,82L32,84.5L26,88Z", 0xFF2B3A2C),
                PaperLayer("M68,38L74,32L83,41L77,47Z", 0xFFFF9DB0, depth = 2f),
            )
        RoutineIcon.LEARN ->
            listOf(
                PaperLayer("M28,46 V62 Q50,74 72,62 V46 L50,56 Z", 0xFF2F5A88, depth = 3f),
                PaperLayer("M50,24L88,40L50,56L12,40Z", 0xFF3A6EA5, depth = 4f),
                PaperLayer("M78,44 V62", 0xFFFFD45C, depth = 1f, stroke = 3f),
                PaperLayer("M74,65A4,4 0 1,0 82,65A4,4 0 1,0 74,65Z", 0xFFFFD45C, depth = 1f),
            )
        RoutineIcon.MUSIC ->
            listOf(
                PaperLayer("M42,30L78,20L78,30L42,40Z", 0xFF9B6BD6, depth = 3f),
                PaperLayer("M42,32V70M78,22V62", 0xFF9B6BD6, depth = 2f, stroke = 5f),
                PaperLayer("M24,70A10,8 0 1,0 44,70A10,8 0 1,0 24,70Z", 0xFF9B6BD6, depth = 3f),
                PaperLayer("M60,62A10,8 0 1,0 80,62A10,8 0 1,0 60,62Z", 0xFF9B6BD6, depth = 3f),
                PaperLayer("M28,67A3,2 0 1,0 34,67A3,2 0 1,0 28,67Z", 0xFFCDB4F0),
                PaperLayer("M64,59A3,2 0 1,0 70,59A3,2 0 1,0 64,59Z", 0xFFCDB4F0),
            )
        RoutineIcon.PAINT ->
            listOf(
                PaperLayer(
                    "M50,16 C76,16 90,34 88,52 C86,66 72,62 66,70 C60,80 68,86 52,86 C28,86 12,70 12,50 C12,30 28,16 50,16Z",
                    0xFFD2A26E,
                    depth = 4f,
                ),
                PaperLayer("M25,46A7,7 0 1,0 39,46A7,7 0 1,0 25,46Z", 0xFFE5533D, depth = 2f),
                PaperLayer("M39,30A7,7 0 1,0 53,30A7,7 0 1,0 39,30Z", 0xFFFFD45C, depth = 2f),
                PaperLayer("M59,32A7,7 0 1,0 73,32A7,7 0 1,0 59,32Z", 0xFF4FB3F0, depth = 2f),
                PaperLayer("M23,66A7,7 0 1,0 37,66A7,7 0 1,0 23,66Z", 0xFF6CCB5F, depth = 2f),
                PaperLayer("M45,70A5,5 0 1,0 55,70A5,5 0 1,0 45,70Z", 0xFFFFF8E7),
            )
        RoutineIcon.COOK ->
            listOf(
                PaperLayer("M40,24 Q36,18 40,12M60,24 Q56,18 60,12", 0xFFCFD8DC, stroke = 3f),
                PaperLayer("M12,54H24M76,54H88", 0xFF455A64, depth = 2f, stroke = 6f),
                PaperLayer(
                    "M22,46 H78 V70 Q78,82 66,82 H34 Q22,82 22,70 Z",
                    0xFFE5533D,
                    depth = 4f,
                ),
                PaperLayer(
                    "M23,38H77Q82,38 82,43V43Q82,48 77,48H23Q18,48 18,43V43Q18,38 23,38Z",
                    0xFF78909C,
                    depth = 3f,
                ),
                PaperLayer("M45,34A5,5 0 1,0 55,34A5,5 0 1,0 45,34Z", 0xFF455A64, depth = 2f),
            )
        RoutineIcon.APPLE ->
            listOf(
                PaperLayer(
                    "M50,36 C40,28 22,32 22,52 C22,70 36,82 44,80 Q50,78 56,80 C64,82 78,70 78,52 C78,32 60,28 50,36Z",
                    0xFFE5533D,
                    depth = 4f,
                ),
                PaperLayer("M50,36 Q49,28 53,20", 0xFFA0704C, depth = 2f, stroke = 4f),
                PaperLayer("M53,27 Q60,16 72,20 Q65,31 53,27Z", 0xFF6CCB5F, depth = 2f),
                PaperLayer("M30,48A5,8 0 1,0 40,48A5,8 0 1,0 30,48Z", 0xFFFF8A75),
            )
        RoutineIcon.COFFEE ->
            listOf(
                PaperLayer("M38,26 Q34,20 38,14M52,26 Q48,20 52,14", 0xFFD2A26E, stroke = 3f),
                PaperLayer("M14,82A32,6 0 1,0 78,82A32,6 0 1,0 14,82Z", 0xFFD2A26E, depth = 3f),
                PaperLayer("M68,48 Q84,48 82,60 Q80,70 68,68", 0xFFFFF8E7, depth = 2f, stroke = 6f),
                PaperLayer(
                    "M22,38 H70 V62 Q70,80 52,80 H40 Q22,80 22,62 Z",
                    0xFFFFF8E7,
                    depth = 4f,
                ),
                PaperLayer("M24,40A22,5 0 1,0 68,40A22,5 0 1,0 24,40Z", 0xFFA0704C),
            )
        RoutineIcon.PILL ->
            listOf(
                PaperLayer("M59,72A13,13 0 1,0 85,72A13,13 0 1,0 59,72Z", 0xFFFFFFFF, depth = 3f),
                PaperLayer("M64,72H80", 0xFFCFD8DC, stroke = 3f),
                PaperLayer(
                    "M51.78,57.78L39.05,70.51A11,11 0 0,1 23.49,54.95L36.22,42.22Z",
                    0xFFE5533D,
                    depth = 4f,
                ),
                PaperLayer(
                    "M51.78,57.78L64.51,45.05A11,11 0 0,0 48.95,29.49L36.22,42.22Z",
                    0xFFFFF8E7,
                    depth = 4f,
                ),
                PaperLayer("M29,52A3,7 0 1,0 35,52A3,7 0 1,0 29,52Z", 0xFFFF8A75),
            )
        RoutineIcon.TOOTH ->
            listOf(
                PaperLayer(
                    "M30,22 C40,16 46,24 50,24 C54,24 60,16 70,22 C82,30 78,48 74,56 C70,66 70,84 63,84 C56,84 57,66 50,66 C43,66 44,84 37,84 C30,84 30,66 26,56 C22,48 18,30 30,22Z",
                    0xFFFFFFFF,
                    depth = 4f,
                ),
                PaperLayer("M32,34A4,7 0 1,0 40,34A4,7 0 1,0 32,34Z", 0xFFA9DDF8),
                PaperLayer(
                    "M78,13L79.91,20.09L87,22L79.91,23.91L78,31L76.09,23.91L69,22L76.09,20.09Z",
                    0xFFFFD45C,
                    depth = 2f,
                ),
            )
        RoutineIcon.SHOWER ->
            listOf(
                PaperLayer("M24,14 Q48,12 50,26", 0xFF78909C, depth = 2f, stroke = 6f),
                PaperLayer("M32,28L68,28L62,42L38,42Z", 0xFF78909C, depth = 3f),
                PaperLayer(
                    "M40,50 C40,50 36,56 36,59 A4,4 0 0,0 44,59 C44,56 40,50 40,50Z",
                    0xFF4FB3F0,
                    depth = 1.5f,
                ),
                PaperLayer(
                    "M50,56 C50,56 46,62 46,65 A4,4 0 0,0 54,65 C54,62 50,56 50,56Z",
                    0xFF4FB3F0,
                    depth = 1.5f,
                ),
                PaperLayer(
                    "M60,50 C60,50 56,56 56,59 A4,4 0 0,0 64,59 C64,56 60,50 60,50Z",
                    0xFF4FB3F0,
                    depth = 1.5f,
                ),
                PaperLayer(
                    "M44,70 C44,70 40,76 40,79 A4,4 0 0,0 48,79 C48,76 44,70 44,70Z",
                    0xFF4FB3F0,
                    depth = 1.5f,
                ),
                PaperLayer(
                    "M56,72 C56,72 52,78 52,81 A4,4 0 0,0 60,81 C60,78 56,72 56,72Z",
                    0xFF4FB3F0,
                    depth = 1.5f,
                ),
            )
        RoutineIcon.CLEAN ->
            listOf(
                PaperLayer("M28,26A8,8 0 1,0 44,26A8,8 0 1,0 28,26Z", 0xFFA9DDF8, depth = 2f),
                PaperLayer("M48,20A6,6 0 1,0 60,20A6,6 0 1,0 48,20Z", 0xFFA9DDF8, depth = 2f),
                PaperLayer("M55,32A9,9 0 1,0 73,32A9,9 0 1,0 55,32Z", 0xFFA9DDF8, depth = 2f),
                PaperLayer("M32,24A2,2 0 1,0 36,24A2,2 0 1,0 32,24Z", 0xFFFFFFFF),
                PaperLayer("M58.6,29A2.4,2.4 0 1,0 63.4,29A2.4,2.4 0 1,0 58.6,29Z", 0xFFFFFFFF),
                PaperLayer("M28,46L72,46L66,84L34,84Z", 0xFF4FB3F0, depth = 4f),
                PaperLayer(
                    "M26.5,40H73.5Q78,40 78,44.5V44.5Q78,49 73.5,49H26.5Q22,49 22,44.5V44.5Q22,40 26.5,40Z",
                    0xFF3A6EA5,
                    depth = 2f,
                ),
            )
        RoutineIcon.FLOWER ->
            listOf(
                PaperLayer("M50,52 V88", 0xFF3FA34D, depth = 2f, stroke = 5f),
                PaperLayer("M50,74 Q66,60 74,66 Q66,80 50,74Z", 0xFF6CCB5F, depth = 2f),
                PaperLayer("M40,25A10,10 0 1,0 60,25A10,10 0 1,0 40,25Z", 0xFFFF9DB0, depth = 2f),
                PaperLayer(
                    "M52.36,33.98A10,10 0 1,0 72.36,33.98A10,10 0 1,0 52.36,33.98Z",
                    0xFFFF9DB0,
                    depth = 2f,
                ),
                PaperLayer(
                    "M47.64,48.52A10,10 0 1,0 67.64,48.52A10,10 0 1,0 47.64,48.52Z",
                    0xFFFF9DB0,
                    depth = 2f,
                ),
                PaperLayer(
                    "M32.36,48.52A10,10 0 1,0 52.36,48.52A10,10 0 1,0 32.36,48.52Z",
                    0xFFFF9DB0,
                    depth = 2f,
                ),
                PaperLayer(
                    "M27.64,33.98A10,10 0 1,0 47.64,33.98A10,10 0 1,0 27.64,33.98Z",
                    0xFFFF9DB0,
                    depth = 2f,
                ),
                PaperLayer("M42,38A8,8 0 1,0 58,38A8,8 0 1,0 42,38Z", 0xFFFFD45C, depth = 1.5f),
            )
        RoutineIcon.LEAF ->
            listOf(
                PaperLayer("M18,82 C18,44 44,18 84,16 C84,56 58,82 18,82Z", 0xFF6CCB5F, depth = 4f),
                PaperLayer("M18,82 C34,58 54,38 84,16 C84,56 58,82 18,82Z", 0xFF3FA34D),
                PaperLayer("M18,82 Q50,50 78,22", 0xFFB9EBA0, stroke = 3f),
            )
        RoutineIcon.HEART ->
            listOf(
                PaperLayer(
                    "M50,80 C30,66 18,54 18,40 A15,15 0 0,1 50,33 A15,15 0 0,1 82,40 C82,54 70,66 50,80Z",
                    0xFFF25C7A,
                    depth = 4f,
                ),
                PaperLayer(
                    "M50,67 C38,59 31,52 31,45 A8.5,8.5 0 0,1 50,41 A8.5,8.5 0 0,1 69,45 C69,52 62,59 50,67Z",
                    0xFFFF9DB0,
                    depth = 2f,
                ),
                PaperLayer("M32.6,42A3.4,3.4 0 1,0 39.4,42A3.4,3.4 0 1,0 32.6,42Z", 0xFFFFFFFF),
            )
        RoutineIcon.BRAIN ->
            listOf(
                PaperLayer(
                    "M50,22 C38,16 24,22 24,34 C14,38 14,54 22,60 C20,72 32,80 42,76 C46,82 50,80 50,78 Z",
                    0xFFFF9DB0,
                    depth = 4f,
                ),
                PaperLayer(
                    "M50,22 C62,16 76,22 76,34 C86,38 86,54 78,60 C80,72 68,80 58,76 C54,82 50,80 50,78 Z",
                    0xFFFFC2D1,
                    depth = 4f,
                ),
                PaperLayer(
                    "M50,24V78M34,32 Q42,36 38,44M26,50 Q36,48 40,56M36,68 Q42,62 48,66M66,32 Q58,36 62,44M74,50 Q64,48 60,56M64,68 Q58,62 52,66",
                    0xFFF25C7A,
                    stroke = 2.6f,
                ),
            )
        RoutineIcon.SMILE ->
            listOf(
                PaperLayer("M16,50A34,34 0 1,0 84,50A34,34 0 1,0 16,50Z", 0xFFFFD45C, depth = 4f),
                PaperLayer("M34,44A4,6 0 1,0 42,44A4,6 0 1,0 34,44Z", 0xFF2B3A2C),
                PaperLayer("M58,44A4,6 0 1,0 66,44A4,6 0 1,0 58,44Z", 0xFF2B3A2C),
                PaperLayer("M38,42A1.5,1.5 0 1,0 41,42A1.5,1.5 0 1,0 38,42Z", 0xFFFFFFFF),
                PaperLayer("M62,42A1.5,1.5 0 1,0 65,42A1.5,1.5 0 1,0 62,42Z", 0xFFFFFFFF),
                PaperLayer("M22,58A6,3.5 0 1,0 34,58A6,3.5 0 1,0 22,58Z", 0xFFFF9DB0),
                PaperLayer("M66,58A6,3.5 0 1,0 78,58A6,3.5 0 1,0 66,58Z", 0xFFFF9DB0),
                PaperLayer("M38,62 Q50,74 62,62", 0xFF2B3A2C, stroke = 4f),
            )
        RoutineIcon.PHONE ->
            listOf(
                PaperLayer(
                    "M39,12H61Q70,12 70,21V79Q70,88 61,88H39Q30,88 30,79V21Q30,12 39,12Z",
                    0xFF455A64,
                    depth = 4f,
                ),
                PaperLayer(
                    "M38,20H62Q66,20 66,24V72Q66,76 62,76H38Q34,76 34,72V24Q34,20 38,20Z",
                    0xFFA9DDF8,
                ),
                PaperLayer("M47.4,82A2.6,2.6 0 1,0 52.6,82A2.6,2.6 0 1,0 47.4,82Z", 0xFF78909C),
                PaperLayer(
                    "M40,34 H60 Q63,34 63,37 V47 Q63,50 60,50 H48 L43,55 V50 H40 Q37,50 37,47 V37 Q37,34 40,34Z",
                    0xFFFFFFFF,
                    depth = 2f,
                ),
                PaperLayer("M57,62A5,5 0 1,0 67,62A5,5 0 1,0 57,62Z", 0xFFE5533D, depth = 1f),
            )
        RoutineIcon.PEOPLE ->
            listOf(
                PaperLayer("M42,86 Q42,58 64,58 Q86,58 86,86 Z", 0xFF4FB3F0, depth = 3f),
                PaperLayer("M53,44A11,11 0 1,0 75,44A11,11 0 1,0 53,44Z", 0xFFF1C7A0, depth = 3f),
                PaperLayer("M14,88 Q14,62 36,62 Q58,62 58,88 Z", 0xFFFF9F43, depth = 4f),
                PaperLayer("M24,46A12,12 0 1,0 48,46A12,12 0 1,0 24,46Z", 0xFFF1C7A0, depth = 3f),
            )
        RoutineIcon.PAW ->
            listOf(
                PaperLayer(
                    "M50,50 C66,50 74,66 72,74 C70,84 58,82 50,82 C42,82 30,84 28,74 C26,66 34,50 50,50Z",
                    0xFFA0704C,
                    depth = 4f,
                ),
                PaperLayer("M18,46A8,10 0 1,0 34,46A8,10 0 1,0 18,46Z", 0xFFA0704C, depth = 3f),
                PaperLayer("M34,30A8,10 0 1,0 50,30A8,10 0 1,0 34,30Z", 0xFFA0704C, depth = 3f),
                PaperLayer("M50,30A8,10 0 1,0 66,30A8,10 0 1,0 50,30Z", 0xFFA0704C, depth = 3f),
                PaperLayer("M66,46A8,10 0 1,0 82,46A8,10 0 1,0 66,46Z", 0xFFA0704C, depth = 3f),
                PaperLayer("M39,70A11,7 0 1,0 61,70A11,7 0 1,0 39,70Z", 0xFFD2A26E),
            )
        RoutineIcon.SAVINGS ->
            listOf(
                PaperLayer("M42,20A10,10 0 1,0 62,20A10,10 0 1,0 42,20Z", 0xFFFFD45C, depth = 3f),
                PaperLayer("M48,20H56", 0xFFF2B935, stroke = 2.4f),
                PaperLayer("M32,74V84M64,74V84", 0xFFF25C7A, depth = 2f, stroke = 7f),
                PaperLayer("M30,40L34,28L42,38Z", 0xFFF25C7A, depth = 2f),
                PaperLayer("M16,58A32,22 0 1,0 80,58A32,22 0 1,0 16,58Z", 0xFFFF9DB0, depth = 4f),
                PaperLayer("M73,58A7,8 0 1,0 87,58A7,8 0 1,0 73,58Z", 0xFFF25C7A, depth = 2f),
                PaperLayer("M76.6,56A1.4,1.4 0 1,0 79.4,56A1.4,1.4 0 1,0 76.6,56Z", 0xFF2B3A2C),
                PaperLayer("M80.6,60A1.4,1.4 0 1,0 83.4,60A1.4,1.4 0 1,0 80.6,60Z", 0xFF2B3A2C),
                PaperLayer("M61.4,50A2.6,2.6 0 1,0 66.6,50A2.6,2.6 0 1,0 61.4,50Z", 0xFF2B3A2C),
                PaperLayer("M40,40H54", 0xFFF25C7A, stroke = 3f),
            )
        RoutineIcon.LAPTOP ->
            listOf(
                PaperLayer(
                    "M25,20H75Q80,20 80,25V57Q80,62 75,62H25Q20,62 20,57V25Q20,20 25,20Z",
                    0xFF455A64,
                    depth = 4f,
                ),
                PaperLayer(
                    "M27,25H73Q75,25 75,27V55Q75,57 73,57H27Q25,57 25,55V27Q25,25 27,25Z",
                    0xFFA9DDF8,
                ),
                PaperLayer("M32,34H56M32,42H62M32,50H48", 0xFFFFFFFF, stroke = 3f),
                PaperLayer(
                    "M10,66 H90 L84,78 Q83,80 80,80 H20 Q17,80 16,78 Z",
                    0xFF78909C,
                    depth = 3f,
                ),
            )
        RoutineIcon.PHONE_OFF ->
            listOf(
                PaperLayer(
                    "M39,12H61Q70,12 70,21V79Q70,88 61,88H39Q30,88 30,79V21Q30,12 39,12Z",
                    0xFF455A64,
                    depth = 4f,
                ),
                PaperLayer(
                    "M38,20H62Q66,20 66,24V72Q66,76 62,76H38Q34,76 34,72V24Q34,20 38,20Z",
                    0xFF2F5A88,
                ),
                PaperLayer("M56,32 A14,14 0 1,0 64,56 A11,11 0 1,1 56,32Z", 0xFFFFD45C, depth = 2f),
                PaperLayer(
                    "M44,30L44.85,33.15L48,34L44.85,34.85L44,38L43.15,34.85L40,34L43.15,33.15Z",
                    0xFFFFF3C4,
                ),
                PaperLayer("M47.4,82A2.6,2.6 0 1,0 52.6,82A2.6,2.6 0 1,0 47.4,82Z", 0xFF78909C),
            )
        RoutineIcon.STAR ->
            listOf(
                PaperLayer(
                    "M50,14L59.99,38.25L86.14,40.26L66.17,57.25L72.34,82.74L50,69L27.66,82.74L33.83,57.25L13.86,40.26L40.01,38.25Z",
                    0xFFFFD45C,
                    depth = 4f,
                ),
                PaperLayer(
                    "M50,32L55.29,44.72L69.02,45.82L58.56,54.78L61.76,68.18L50,61L38.24,68.18L41.44,54.78L30.98,45.82L44.71,44.72Z",
                    0xFFFFF3C4,
                    depth = 2f,
                ),
            )
        RoutineIcon.TARGET ->
            listOf(
                PaperLayer("M14,54A32,32 0 1,0 78,54A32,32 0 1,0 14,54Z", 0xFFE5533D, depth = 4f),
                PaperLayer("M23,54A23,23 0 1,0 69,54A23,23 0 1,0 23,54Z", 0xFFFFFFFF, depth = 1.5f),
                PaperLayer("M32,54A14,14 0 1,0 60,54A14,14 0 1,0 32,54Z", 0xFFE5533D, depth = 1.5f),
                PaperLayer("M40,54A6,6 0 1,0 52,54A6,6 0 1,0 40,54Z", 0xFFFFFFFF, depth = 1f),
                PaperLayer("M46,54 L80,20", 0xFFA0704C, depth = 2f, stroke = 4f),
                PaperLayer("M76,16L86,12L84,22L80,24L76,20Z", 0xFF3FA34D, depth = 1f),
            )
        RoutineIcon.CHECK ->
            listOf(
                PaperLayer("M16,50A34,34 0 1,0 84,50A34,34 0 1,0 16,50Z", 0xFF6CCB5F, depth = 4f),
                PaperLayer("M32,52 L45,64 L68,38", 0xFFFFFFFF, depth = 2f, stroke = 9f),
            )
    }
