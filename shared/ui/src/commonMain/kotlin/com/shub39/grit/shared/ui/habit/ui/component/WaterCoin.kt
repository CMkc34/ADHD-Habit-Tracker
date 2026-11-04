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
package com.shub39.grit.shared.ui.habit.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.shub39.grit.shared.ui.GritPreviewWrapper

/** Marker for the water currency in strings; [WaterText] draws it as the [WaterCoin]. */
const val WATER_MARK = "💧"

private const val WATER_COIN_ID = "waterCoin"

private val CoinBlue = Color(0xFF3FA9F5)
private val CoinRing = Color(0xFFBFE6FF)
private val CoinInk = Color(0xFF1F3A5F)

/** Blue coin with two waves: the symbol of the water currency (#119). */
val WaterCoinIcon: ImageVector by lazy {
    ImageVector.Builder(
            name = "WaterCoin",
            defaultWidth = 48.dp,
            defaultHeight = 48.dp,
            viewportWidth = 48f,
            viewportHeight = 48f,
        )
        .apply {
            // paper cut look (#123): a soft shadow under the coin instead of an outline
            path(fill = SolidColor(CoinInk), fillAlpha = 0.16f) {
                moveTo(45.5f, 26.5f)
                arcTo(20f, 20f, 0f, isMoreThanHalf = true, isPositiveArc = true, 5.5f, 26.5f)
                arcTo(20f, 20f, 0f, isMoreThanHalf = true, isPositiveArc = true, 45.5f, 26.5f)
                close()
            }
            path(fill = SolidColor(CoinInk), fillAlpha = 0.14f) {
                moveTo(44.8f, 25.3f)
                arcTo(20f, 20f, 0f, isMoreThanHalf = true, isPositiveArc = true, 4.8f, 25.3f)
                arcTo(20f, 20f, 0f, isMoreThanHalf = true, isPositiveArc = true, 44.8f, 25.3f)
                close()
            }
            // coin
            path(fill = SolidColor(CoinBlue)) {
                moveTo(44f, 24f)
                arcTo(20f, 20f, 0f, isMoreThanHalf = true, isPositiveArc = true, 4f, 24f)
                arcTo(20f, 20f, 0f, isMoreThanHalf = true, isPositiveArc = true, 44f, 24f)
                close()
            }
            // inner ring
            path(stroke = SolidColor(CoinRing), strokeLineWidth = 1.8f) {
                moveTo(39f, 24f)
                arcTo(15f, 15f, 0f, isMoreThanHalf = true, isPositiveArc = true, 9f, 24f)
                arcTo(15f, 15f, 0f, isMoreThanHalf = true, isPositiveArc = true, 39f, 24f)
                close()
            }
            // big wave
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 3.2f,
                strokeLineCap = StrokeCap.Round,
            ) {
                moveTo(11f, 26f)
                curveTo(14.5f, 22f, 17.5f, 22f, 21f, 26f)
                reflectiveCurveTo(27.5f, 30f, 31f, 26f)
                reflectiveCurveTo(37f, 22f, 37f, 24f)
            }
            // small wave
            path(
                stroke = SolidColor(Color.White),
                strokeAlpha = 0.75f,
                strokeLineWidth = 2.4f,
                strokeLineCap = StrokeCap.Round,
            ) {
                moveTo(13f, 32f)
                curveTo(16f, 29f, 19f, 29f, 22f, 32f)
                reflectiveCurveTo(28f, 35f, 31f, 32f)
                reflectiveCurveTo(35f, 30f, 35f, 30.5f)
            }
            // shine
            path(
                stroke = SolidColor(Color.White),
                strokeAlpha = 0.8f,
                strokeLineWidth = 2.6f,
                strokeLineCap = StrokeCap.Round,
            ) {
                moveTo(17f, 12.5f)
                arcTo(13f, 13f, 0f, isMoreThanHalf = false, isPositiveArc = true, 27f, 10.5f)
            }
        }
        .build()
}

@Composable
fun WaterCoin(modifier: Modifier = Modifier) {
    Image(
        painter = rememberVectorPainter(WaterCoinIcon),
        contentDescription = null,
        modifier = modifier,
    )
}

private val waterInlineContent =
    mapOf(
        WATER_COIN_ID to
            InlineTextContent(Placeholder(1.15.em, 1.15.em, PlaceholderVerticalAlign.TextCenter)) {
                WaterCoin(Modifier.fillMaxSize())
            }
    )

/** [Text] that draws every [WATER_MARK] in [text] as the [WaterCoin]. */
@Composable
fun WaterText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontFamily: FontFamily? = null,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    style: TextStyle = LocalTextStyle.current,
) {
    val annotated =
        remember(text) {
            buildAnnotatedString {
                text.split(WATER_MARK).forEachIndexed { index, part ->
                    if (index > 0) appendInlineContent(WATER_COIN_ID, WATER_MARK)
                    append(part)
                }
            }
        }
    Text(
        text = annotated,
        modifier = modifier,
        color = color,
        fontFamily = fontFamily,
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines,
        style = style,
        inlineContent = waterInlineContent,
    )
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun WaterCoinPreview() {
    WaterCoin(Modifier.size(64.dp))
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun WaterTextPreview() {
    WaterText(text = "🔥 12 Tage aktiv · $WATER_MARK 14")
}
