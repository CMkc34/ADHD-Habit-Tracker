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

/** Marker for the shake currency in strings; [ShakeText] draws it as the [ShakeCoin]. */
const val SHAKE_MARK = "🥤"

private const val SHAKE_COIN_ID = "shakeCoin"

/**
 * The protein shake currency (#17) as 16 × 16 pixel art: a shaker cup with a teal lid, filled with
 * a chocolate shake. Every character other than `.` is a pixel of the colour in [ShakePixelColors].
 */
private val ShakePixels =
    listOf(
        "................",
        "......oooo......",
        ".....oTTTto.....",
        "...oooooooooo...",
        "...oTtTTTTTTo...",
        "...oTTTTTTTDo...",
        "...oooooooooo...",
        "...owhwwwwwwo...",
        "...oshssssSSo...",
        "...oshssssSSo...",
        "....ohsssSSo....",
        "....ohsssSSo....",
        "....ossssSSo....",
        "....ossssSSo....",
        "....oooooooo....",
        "................",
    )

private val ShakePixelColors =
    mapOf(
        'o' to Color(0xFF1F2A33), // outline
        'T' to Color(0xFF2BB3A3), // lid
        't' to Color(0xFF7FE6D8), // lid light
        'D' to Color(0xFF1C8478), // lid shadow
        'w' to Color(0xFFF3EBDD), // foam
        'h' to Color(0xFFFFF6E6), // shine
        's' to Color(0xFFC0865A), // shake
        'S' to Color(0xFF93603D), // shake shadow
    )

/** The shaker cup of the shake currency, drawn pixel by pixel. */
val ShakeCoinIcon: ImageVector by lazy {
    val size = ShakePixels.size.toFloat()
    ImageVector.Builder(
            name = "ShakeCoin",
            defaultWidth = 48.dp,
            defaultHeight = 48.dp,
            viewportWidth = size,
            viewportHeight = size,
        )
        .apply {
            ShakePixelColors.forEach { (key, color) ->
                // one path per colour, one rectangle per horizontal run of its pixels
                path(fill = SolidColor(color)) {
                    ShakePixels.forEachIndexed { y, row ->
                        var x = 0
                        while (x < row.length) {
                            if (row[x] != key) {
                                x++
                                continue
                            }
                            val runStart = x
                            while (x < row.length && row[x] == key) x++
                            moveTo(runStart.toFloat(), y.toFloat())
                            lineTo(x.toFloat(), y.toFloat())
                            lineTo(x.toFloat(), y + 1f)
                            lineTo(runStart.toFloat(), y + 1f)
                            close()
                        }
                    }
                }
            }
        }
        .build()
}

@Composable
fun ShakeCoin(modifier: Modifier = Modifier) {
    Image(
        painter = rememberVectorPainter(ShakeCoinIcon),
        contentDescription = null,
        modifier = modifier,
    )
}

private val shakeInlineContent =
    mapOf(
        SHAKE_COIN_ID to
            InlineTextContent(Placeholder(1.15.em, 1.15.em, PlaceholderVerticalAlign.TextCenter)) {
                ShakeCoin(Modifier.fillMaxSize())
            }
    )

/** [Text] that draws every [SHAKE_MARK] in [text] as the [ShakeCoin]. */
@Composable
fun ShakeText(
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
                text.split(SHAKE_MARK).forEachIndexed { index, part ->
                    if (index > 0) appendInlineContent(SHAKE_COIN_ID, SHAKE_MARK)
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
        inlineContent = shakeInlineContent,
    )
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun ShakeCoinPreview() {
    ShakeCoin(Modifier.size(64.dp))
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun ShakeTextPreview() {
    ShakeText(text = "🔥 12 Tage aktiv · $SHAKE_MARK 14")
}
