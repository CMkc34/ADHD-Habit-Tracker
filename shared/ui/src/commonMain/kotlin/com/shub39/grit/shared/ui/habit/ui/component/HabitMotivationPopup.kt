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

import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shub39.grit.core.avatar.AvatarSpecies
import com.shub39.grit.core.avatar.AvatarState
import com.shub39.grit.core.avatar.PlantMood
import com.shub39.grit.core.avatar.PlantStage
import com.shub39.grit.core.avatar.ShopCatalog
import com.shub39.grit.core.motivation.MotivationQuote
import com.shub39.grit.core.motivation.QuoteTone
import com.shub39.grit.core.motivation.StreakTier
import com.shub39.grit.shared.ui.GritPreviewWrapper
import com.shub39.grit.shared.ui.habit.avatar.PlantAvatar
import com.shub39.grit.shared.ui.habit.motivation.CelebrationStage
import com.shub39.grit.shared.ui.habit.motivation.CheckInStageColors
import com.shub39.grit.shared.ui.habit.motivation.StagePill
import com.shub39.grit.shared.ui.habit.motivation.StageYellow
import com.shub39.grit.shared.ui.habit.motivation.rememberCountUp
import com.shub39.grit.shared.ui.habit.motivation.rememberPopIn
import com.shub39.grit.shared.ui.habit.motivation.textResource
import com.shub39.grit.shared.ui.theme.flexFontEmphasis
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.energy_popup_line
import grit.shared.ui.generated.resources.motivation_header_motivating
import grit.shared.ui.generated.resources.motivation_header_motivating_named
import grit.shared.ui.generated.resources.motivation_header_sarcastic
import grit.shared.ui.generated.resources.motivation_header_sarcastic_named
import grit.shared.ui.generated.resources.motivation_streak_unit
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Full screen celebration after a habit check-in: the user's plant, the streak counting up and the
 * quote. Closes by tapping anywhere or the back key. [activityStreak] adds the "+1 💧" pill for the
 * first check-in of the day; the daily bonus gets its own [BonusPopup] afterwards. Without [avatar]
 * a seed is shown.
 */
@Composable
fun HabitMotivationPopup(
    habitTitle: String,
    activeDays: Int,
    quote: MotivationQuote,
    activityStreak: Int?,
    avatar: AvatarState?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CelebrationStage(colors = CheckInStageColors, onDismiss = onDismiss, modifier = modifier) {
        // the user's name in the header when they gave one (#139)
        val userName = avatar?.data?.userName.orEmpty()
        Text(
            text =
                if (userName.isBlank()) {
                        stringResource(
                            when (quote.tone) {
                                QuoteTone.MOTIVATING -> Res.string.motivation_header_motivating
                                QuoteTone.SARCASTIC -> Res.string.motivation_header_sarcastic
                            }
                        )
                    } else {
                        stringResource(
                            when (quote.tone) {
                                QuoteTone.MOTIVATING ->
                                    Res.string.motivation_header_motivating_named
                                QuoteTone.SARCASTIC -> Res.string.motivation_header_sarcastic_named
                            },
                            userName,
                        )
                    }
                    .uppercase(),
            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 3.sp),
            color = StageYellow,
            textAlign = TextAlign.Center,
        )

        val plantScale = rememberPopIn(delayMillis = 150)
        PlantAvatar(
            stage = avatar?.plant?.stage ?: PlantStage.SEED,
            mood = PlantMood.HAPPY,
            potColor = avatar?.data?.potColor ?: ShopCatalog.DEFAULT_POT_COLOR,
            accessories = avatar?.data?.accessories.orEmpty(),
            species = avatar?.data?.species ?: AvatarSpecies.SPROUT,
            modifier =
                Modifier.size(170.dp).graphicsLayer {
                    scaleX = plantScale
                    scaleY = plantScale
                },
        )

        Text(
            text = rememberCountUp(activeDays).toString(),
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
            text = pluralStringResource(Res.plurals.motivation_streak_unit, activeDays, habitTitle),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = stringResource(quote.textResource(), activeDays),
            style =
                MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = flexFontEmphasis(),
                    fontWeight = FontWeight.Bold,
                ),
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        activityStreak?.let {
            val pillScale = rememberPopIn(delayMillis = 1000)
            StagePill(
                text = pluralStringResource(Res.plurals.energy_popup_line, it, it),
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
private fun MotivatingPreview() {
    HabitMotivationPopup(
        habitTitle = "Meditate",
        activeDays = 1,
        quote =
            MotivationQuote("quote_starter_motivating_1", QuoteTone.MOTIVATING, StreakTier.STARTER),
        activityStreak = 13,
        avatar = null,
        onDismiss = {},
    )
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun SarcasticPreview() {
    HabitMotivationPopup(
        habitTitle = "Learn Spanish for 20 minutes every morning",
        activeDays = 12,
        quote =
            MotivationQuote(
                "quote_established_sarcastic_1",
                QuoteTone.SARCASTIC,
                StreakTier.ESTABLISHED,
            ),
        activityStreak = null,
        avatar = null,
        onDismiss = {},
    )
}
