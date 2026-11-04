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
package com.shub39.grit.shared.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

// Paper cut look for the whole app (#128): warm paper colours,
// two pale paper hills at the bottom and cards that lie on the page like sheets of paper.

/** [base] with the surfaces and neutral text colours swapped for warm paper. */
fun paperColorScheme(base: ColorScheme, isDark: Boolean): ColorScheme =
    if (isDark) {
        base.copy(
            background = Color(0xFF211F1A),
            onBackground = Color(0xFFEEE8DA),
            surface = Color(0xFF211F1A),
            onSurface = Color(0xFFEEE8DA),
            surfaceVariant = Color(0xFF423D33),
            onSurfaceVariant = Color(0xFFB3AB98),
            surfaceDim = Color(0xFF1B1915),
            surfaceBright = Color(0xFF3A362E),
            surfaceContainerLowest = Color(0xFF1B1915),
            surfaceContainerLow = Color(0xFF282520),
            surfaceContainer = Color(0xFF2E2B24),
            surfaceContainerHigh = Color(0xFF37332B),
            surfaceContainerHighest = Color(0xFF423D33),
            secondaryContainer = Color(0xFF454036),
            onSecondaryContainer = Color(0xFFEEE8DA),
            outline = Color(0xFF8F887A),
            outlineVariant = Color(0xFF423D33),
        )
    } else {
        base.copy(
            background = Color(0xFFF6F1E6),
            onBackground = Color(0xFF2B2A25),
            surface = Color(0xFFF6F1E6),
            onSurface = Color(0xFF2B2A25),
            surfaceVariant = Color(0xFFE9E1CF),
            onSurfaceVariant = Color(0xFF6E6858),
            surfaceDim = Color(0xFFE6DFCF),
            surfaceBright = Color(0xFFFBF7EE),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFFBF7EE),
            surfaceContainer = Color(0xFFFFFDF7),
            surfaceContainerHigh = Color(0xFFEFE8D8),
            surfaceContainerHighest = Color(0xFFE9E1CF),
            secondaryContainer = Color(0xFFE2D8C1),
            onSecondaryContainer = Color(0xFF2B2A25),
            outline = Color(0xFF8E8774),
            outlineVariant = Color(0xFFE3DCCB),
        )
    }

private val shadowInk = Color(0xFF1B2A1C)

/**
 * The page background: the theme's background colour and two pale paper hills along the bottom
 * behind the content. No paper grain here, on whole pages it looked dirty (#130).
 */
@Composable
fun Modifier.paperBackground(): Modifier {
    val background = MaterialTheme.colorScheme.background
    val isDark = background.luminance() < 0.5f
    val back = if (isDark) Color(0xFF2C3A2A) else Color(0xFFDCEBCF)
    val front = if (isDark) Color(0xFF33452F) else Color(0xFFC9E2B4)
    return drawWithCache {
        val w = size.width
        val h = size.height
        fun hill(top: Float, dip: Float, rise: Float) =
            Path().apply {
                moveTo(0f, h - top)
                quadraticTo(w * 0.27f, h - top - rise, w * 0.53f, h - top - dip)
                quadraticTo(w * 0.78f, h - top + dip * 0.4f, w, h - top - dip * 0.8f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
        val backHill = hill(top = 140.dp.toPx(), dip = 10.dp.toPx(), rise = 50.dp.toPx())
        val frontHill = hill(top = 90.dp.toPx(), dip = 4.dp.toPx(), rise = 40.dp.toPx())
        val drop = 2.4.dp.toPx()
        onDrawBehind {
            drawRect(background)
            pageShadow(backHill, drop, isDark)
            drawPath(backHill, back)
            pageShadow(frontHill, drop, isDark)
            drawPath(frontHill, front)
        }
    }
}

/** A soft paper shadow under a card of [shape], falling down and to the right. */
@Composable
fun Modifier.paperShadow(shape: Shape): Modifier {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return drawBehind {
        val outline = shape.createOutline(size, layoutDirection, this)
        val d = 2.4.dp.toPx()
        val (near, far) = shadowAlphas(isDark)
        translate(left = d * 0.25f, top = d * 0.55f) {
            drawOutline(outline, shadowInk.copy(alpha = near))
        }
        translate(left = d * 0.45f, top = d) { drawOutline(outline, shadowInk.copy(alpha = far)) }
    }
}

private fun shadowAlphas(isDark: Boolean) = if (isDark) 0.35f to 0.3f else 0.14f to 0.12f

private fun DrawScope.pageShadow(path: Path, d: Float, isDark: Boolean) {
    val (near, far) = shadowAlphas(isDark)
    translate(left = d * 0.25f, top = -d * 0.55f) { drawPath(path, shadowInk.copy(alpha = near)) }
    translate(left = d * 0.45f, top = -d) { drawPath(path, shadowInk.copy(alpha = far)) }
}
