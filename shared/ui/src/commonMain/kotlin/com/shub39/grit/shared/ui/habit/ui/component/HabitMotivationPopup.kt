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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import com.shub39.grit.core.avatar.AvatarData
import com.shub39.grit.core.avatar.AvatarState
import com.shub39.grit.core.fighter.FighterCharacter
import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.core.fighter.FighterStat
import com.shub39.grit.core.fighter.FighterState
import com.shub39.grit.core.motivation.MotivationQuote
import com.shub39.grit.core.motivation.QuoteTone
import com.shub39.grit.core.motivation.StreakTier
import com.shub39.grit.shared.ui.GritPreviewWrapper
import com.shub39.grit.shared.ui.habit.fighter.FighterAvatar
import com.shub39.grit.shared.ui.habit.fighter.FighterPose
import com.shub39.grit.shared.ui.habit.fighter.StatIcon
import com.shub39.grit.shared.ui.habit.fighter.label
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
import grit.shared.ui.generated.resources.fighter_new_stat_level
import grit.shared.ui.generated.resources.fighter_popup_ticket
import grit.shared.ui.generated.resources.fighter_popup_xp
import grit.shared.ui.generated.resources.motivation_header_motivating
import grit.shared.ui.generated.resources.motivation_header_motivating_named
import grit.shared.ui.generated.resources.motivation_header_sarcastic
import grit.shared.ui.generated.resources.motivation_header_sarcastic_named
import grit.shared.ui.generated.resources.motivation_streak_unit
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Full screen celebration after a habit check-in: the user's pixel fighter, the streak counting up
 * and the quote. Closes by tapping anywhere or the back key. [activityStreak] adds the "+1 💧" pill
 * for the first check-in of the day; the daily bonus gets its own [BonusPopup] afterwards. The
 * training pills follow (#15): "+1 Strength XP" for [statGain], "Strength level 4!" for
 * [newStatLevel] (the fighter then gathers ki) and "+1 🎟 fight ticket" for [ticketEarned]. Without
 * [avatar] a rookie as the default fighter is shown.
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
    statGain: FighterStat? = null,
    newStatLevel: Int? = null,
    ticketEarned: Boolean = false,
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

        // the pixel fighter; it gathers ki when the check-in raised a stat level, and it shrinks a
        // little when many pills follow, so everything fits on small phones
        val pills =
            listOf(activityStreak != null, statGain != null, newStatLevel != null, ticketEarned)
                .count { it }
        val fighterSize = if (pills > 2) 140.dp else 170.dp
        val fighterScale = rememberPopIn(delayMillis = 150)
        FighterAvatar(
            character = avatar?.data?.character ?: FighterCharacter.DEFAULT,
            colors = avatar?.data?.colors ?: FighterCharacter.DEFAULT.defaultColors,
            rank = avatar?.fighter?.rank ?: FighterRank.ROOKIE,
            mood = FighterMood.READY,
            outfit = avatar?.data?.outfit,
            gear = avatar?.data?.accessories.orEmpty(),
            pose = if (newStatLevel != null) FighterPose.CHARGE else FighterPose.STANCE,
            modifier =
                Modifier.widthIn(max = fighterSize).fillMaxWidth().graphicsLayer {
                    scaleX = fighterScale
                    scaleY = fighterScale
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
        // the reward pills pop in one after another, each a little later
        var pillDelay = 1000L
        activityStreak?.let {
            RewardPill(
                text = pluralStringResource(Res.plurals.energy_popup_line, it, it),
                delayMillis = pillDelay,
            )
            pillDelay += PILL_STEP_MILLIS
        }
        statGain?.let { stat ->
            RewardPill(
                text = stringResource(Res.string.fighter_popup_xp, stat.label()),
                delayMillis = pillDelay,
                stat = stat,
            )
            pillDelay += PILL_STEP_MILLIS
            newStatLevel?.let { level ->
                RewardPill(
                    text = stringResource(Res.string.fighter_new_stat_level, stat.label(), level),
                    delayMillis = pillDelay,
                    stat = stat,
                )
                pillDelay += PILL_STEP_MILLIS
            }
        }
        if (ticketEarned) {
            RewardPill(
                text = stringResource(Res.string.fighter_popup_ticket),
                delayMillis = pillDelay,
            )
        }
    }
}

private const val PILL_STEP_MILLIS = 250L

/** A [StagePill] that pops in after [delayMillis], with the pixel icon of [stat] if given. */
@Composable
private fun RewardPill(text: String, delayMillis: Long, stat: FighterStat? = null) {
    val scale = rememberPopIn(delayMillis = delayMillis)
    StagePill(
        text = text,
        icon = stat?.let { { StatIcon(stat = it, modifier = Modifier.size(20.dp)) } },
        modifier =
            Modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
    )
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
private fun RewardPreview() {
    HabitMotivationPopup(
        habitTitle = "Push-ups",
        activeDays = 4,
        quote =
            MotivationQuote("quote_starter_motivating_1", QuoteTone.MOTIVATING, StreakTier.STARTER),
        activityStreak = 13,
        avatar =
            AvatarState(
                fighter =
                    FighterState(
                        activeDays = 14,
                        rank = FighterRank.BRAWLER,
                        mood = FighterMood.READY,
                    ),
                energy = 0,
                data = AvatarData(name = "Kai"),
            ),
        statGain = FighterStat.STRENGTH,
        newStatLevel = 4,
        ticketEarned = true,
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
