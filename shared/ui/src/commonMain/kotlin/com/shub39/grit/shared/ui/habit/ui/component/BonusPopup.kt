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

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shub39.grit.core.activity.DailyBonus
import com.shub39.grit.shared.ui.GritPreviewWrapper
import com.shub39.grit.shared.ui.habit.motivation.BonusStageColors
import com.shub39.grit.shared.ui.habit.motivation.CelebrationStage
import com.shub39.grit.shared.ui.habit.motivation.StagePill
import com.shub39.grit.shared.ui.habit.motivation.rememberPopIn
import com.shub39.grit.shared.ui.theme.flexFontEmphasis
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.bonus_popup_balance
import grit.shared.ui.generated.resources.bonus_popup_header
import grit.shared.ui.generated.resources.bonus_popup_reason
import grit.shared.ui.generated.resources.bonus_popup_text
import org.jetbrains.compose.resources.stringResource

/**
 * Full screen gold stage for the daily bonus: a big bouncing water coin, "+1", why it was earned
 * and the new water balance ([energy], left out when unknown). Closes like the motivation popup.
 */
@Composable
fun BonusPopup(energy: Int?, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    CelebrationStage(colors = BonusStageColors, onDismiss = onDismiss, modifier = modifier) {
        Text(
            text = stringResource(Res.string.bonus_popup_header).uppercase(),
            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 3.sp),
            color = Color.White,
            textAlign = TextAlign.Center,
        )

        val coinScale = rememberPopIn(delayMillis = 150)
        val bob by
            rememberInfiniteTransition(label = "coin")
                .animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
                    label = "bob",
                )
        WaterCoin(
            modifier =
                Modifier.size(150.dp).graphicsLayer {
                    scaleX = coinScale
                    scaleY = coinScale
                    translationY = -bob * 6.dp.toPx()
                    rotationZ = -bob * 4f
                }
        )

        Text(
            text = "+${DailyBonus.ENERGY}",
            style =
                MaterialTheme.typography.displayLarge.copy(
                    fontFamily = flexFontEmphasis(),
                    fontWeight = FontWeight.Black,
                    fontSize = 96.sp,
                    lineHeight = 96.sp,
                    shadow = Shadow(Color(0x40000000), Offset(0f, 8f)),
                ),
            color = Color.White,
        )
        Text(
            text = stringResource(Res.string.bonus_popup_reason, DailyBonus.MIN_ROUTINES),
            style =
                MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = flexFontEmphasis(),
                    fontWeight = FontWeight.Bold,
                ),
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.bonus_popup_text),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
        )
        energy?.let {
            val pillScale = rememberPopIn(delayMillis = 700)
            StagePill(
                text = stringResource(Res.string.bonus_popup_balance, it),
                modifier =
                    Modifier.graphicsLayer {
                        scaleX = pillScale
                        scaleY = pillScale
                    },
            )
        }
    }
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun BonusPopupPreview() {
    BonusPopup(energy = 23, onDismiss = {})
}
