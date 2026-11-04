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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shub39.grit.core.avatar.AvatarData
import com.shub39.grit.core.avatar.AvatarState
import com.shub39.grit.core.avatar.PlantMood
import com.shub39.grit.core.avatar.PlantStage
import com.shub39.grit.core.avatar.PlantState
import com.shub39.grit.core.motivation.DayTime
import com.shub39.grit.core.motivation.GreetingKind
import com.shub39.grit.shared.ui.GritPreviewWrapper
import com.shub39.grit.shared.ui.habit.HabitsAction
import com.shub39.grit.shared.ui.habit.avatar.MeadowScene
import com.shub39.grit.shared.ui.habit.avatar.plantName
import com.shub39.grit.shared.ui.theme.flexFontEmphasis
import com.shub39.grit.shared.ui.viewmodel.HabitViewModel
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.greeting_first_day_text
import grit.shared.ui.generated.resources.greeting_first_day_title
import grit.shared.ui.generated.resources.greeting_growing_last
import grit.shared.ui.generated.resources.greeting_growing_next
import grit.shared.ui.generated.resources.greeting_growing_title
import grit.shared.ui.generated.resources.greeting_header_day
import grit.shared.ui.generated.resources.greeting_header_day_named
import grit.shared.ui.generated.resources.greeting_header_evening
import grit.shared.ui.generated.resources.greeting_header_evening_named
import grit.shared.ui.generated.resources.greeting_header_morning
import grit.shared.ui.generated.resources.greeting_header_morning_named
import grit.shared.ui.generated.resources.greeting_header_welcome
import grit.shared.ui.generated.resources.greeting_start
import grit.shared.ui.generated.resources.greeting_tired_text
import grit.shared.ui.generated.resources.greeting_tired_title
import grit.shared.ui.generated.resources.greeting_water
import grit.shared.ui.generated.resources.greeting_welcome_text
import grit.shared.ui.generated.resources.greeting_welcome_title
import grit.shared.ui.generated.resources.greeting_welcome_title_named
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

// the same plant size as on the avatar card
private const val GREETING_PLANT_SCALE = 1.3f

/**
 * Asks for the daily greeting whenever the app comes to the front and shows it app-wide. Waits
 * while the freeze notice or a motivation popup is open, so only one stage shows at a time. Shares
 * the [HabitViewModel] instance with the habit screens.
 */
@Composable
fun DailyGreetingHost() {
    val viewModel = koinViewModel<HabitViewModel>()
    val greeting by viewModel.dailyGreeting.collectAsStateWithLifecycle()
    val freezeNotice by viewModel.freezeNotice.collectAsStateWithLifecycle()
    val popup by viewModel.motivationPopup.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.onAction(HabitsAction.CheckDailyGreeting)
        onPauseOrDispose {}
    }

    val shown = greeting ?: return
    if (freezeNotice != null || popup != null) return
    DailyGreetingStage(
        greeting = shown,
        onDismiss = { viewModel.onAction(HabitsAction.DismissDailyGreeting) },
    )
}

/**
 * Full screen greeting on the first app start of a day: the plant on its meadow exactly as it is
 * now, a line fitting its state and the water balance. Closes by the button, tapping anywhere or
 * the back key.
 */
@Composable
fun DailyGreetingStage(
    greeting: DailyGreetingState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val avatar = greeting.avatar
    val plant = avatar.plant
    val name = plantName(avatar.data)
    // the user's own name in the header and the welcome when they gave one (#139)
    val userName = avatar.data.userName

    CelebrationStage(colors = CheckInStageColors, onDismiss = onDismiss, modifier = modifier) {
        val header =
            // the welcome has the name in its title already
            if (greeting.kind == GreetingKind.WELCOME) {
                Res.string.greeting_header_welcome
            } else {
                when (greeting.dayTime) {
                    DayTime.MORNING ->
                        if (userName.isBlank()) Res.string.greeting_header_morning
                        else Res.string.greeting_header_morning_named
                    DayTime.DAY ->
                        if (userName.isBlank()) Res.string.greeting_header_day
                        else Res.string.greeting_header_day_named
                    DayTime.EVENING ->
                        if (userName.isBlank()) Res.string.greeting_header_evening
                        else Res.string.greeting_header_evening_named
                }
            }
        Text(
            text = stringResource(header, userName).uppercase(),
            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 3.sp),
            color = StageYellow,
            textAlign = TextAlign.Center,
        )

        val sceneScale = rememberPopIn(delayMillis = 150)
        MeadowScene(
            stage = plant.stage,
            // a plant without a single active day is new, not tired: it greets awake
            mood = if (plant.activeDays == 0) PlantMood.HAPPY else plant.mood,
            potColor = avatar.data.potColor,
            accessories = avatar.data.accessories,
            decorations = avatar.data.decorations,
            weather = plant.weather,
            species = avatar.data.species,
            plantScale = GREETING_PLANT_SCALE,
            modifier =
                Modifier.widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .graphicsLayer {
                        scaleX = sceneScale
                        scaleY = sceneScale
                    },
        )

        val title =
            when (greeting.kind) {
                GreetingKind.WELCOME ->
                    if (userName.isBlank()) stringResource(Res.string.greeting_welcome_title, name)
                    else stringResource(Res.string.greeting_welcome_title_named, userName, name)
                GreetingKind.FIRST_DAY -> stringResource(Res.string.greeting_first_day_title)
                GreetingKind.TIRED -> stringResource(Res.string.greeting_tired_title)
                GreetingKind.GROWING ->
                    pluralStringResource(
                        Res.plurals.greeting_growing_title,
                        greeting.activityStreak,
                        greeting.activityStreak,
                    )
            }
        val text =
            when (greeting.kind) {
                GreetingKind.WELCOME -> stringResource(Res.string.greeting_welcome_text)
                GreetingKind.FIRST_DAY -> stringResource(Res.string.greeting_first_day_text)
                GreetingKind.TIRED -> stringResource(Res.string.greeting_tired_text)
                GreetingKind.GROWING -> {
                    val missing = plant.daysToNextStage
                    val next = plant.stage.next
                    if (missing == null || next == null) {
                        stringResource(Res.string.greeting_growing_last)
                    } else {
                        pluralStringResource(
                            Res.plurals.greeting_growing_next,
                            missing,
                            missing,
                            next.number,
                        )
                    }
                }
            }

        Text(
            text = title,
            style =
                MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = flexFontEmphasis(),
                    fontWeight = FontWeight.Black,
                ),
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
        )

        if (greeting.kind != GreetingKind.WELCOME) {
            val pillScale = rememberPopIn(delayMillis = 700)
            StagePill(
                text = stringResource(Res.string.greeting_water, avatar.energy),
                modifier =
                    Modifier.graphicsLayer {
                        scaleX = pillScale
                        scaleY = pillScale
                    },
            )
        }

        Button(
            onClick = onDismiss,
            colors =
                ButtonDefaults.buttonColors(containerColor = StageYellow, contentColor = StageInk),
            modifier = Modifier.padding(top = 12.dp),
        ) {
            Text(
                text = stringResource(Res.string.greeting_start),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
            )
        }
    }
}

private fun previewGreeting(kind: GreetingKind, activeDays: Int, mood: PlantMood) =
    DailyGreetingState(
        kind = kind,
        dayTime = DayTime.MORNING,
        avatar =
            AvatarState(
                plant = PlantState(activeDays, PlantStage.forActiveDays(activeDays), mood),
                energy = 12,
                data = AvatarData(),
            ),
        activityStreak = if (mood == PlantMood.HAPPY) 6 else 0,
    )

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun WelcomePreview() {
    DailyGreetingStage(previewGreeting(GreetingKind.WELCOME, 0, PlantMood.TIRED), onDismiss = {})
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun GrowingPreview() {
    DailyGreetingStage(previewGreeting(GreetingKind.GROWING, 25, PlantMood.HAPPY), onDismiss = {})
}
