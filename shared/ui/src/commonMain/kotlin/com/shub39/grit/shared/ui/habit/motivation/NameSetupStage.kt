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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shub39.grit.core.avatar.AvatarSpecies
import com.shub39.grit.core.avatar.PlantMood
import com.shub39.grit.core.avatar.PlantStage
import com.shub39.grit.core.avatar.UserName
import com.shub39.grit.shared.ui.GritPreviewWrapper
import com.shub39.grit.shared.ui.habit.HabitsAction
import com.shub39.grit.shared.ui.habit.avatar.PlantAvatar
import com.shub39.grit.shared.ui.habit.avatar.SpeciesChoice
import com.shub39.grit.shared.ui.habit.avatar.SpeciesChoiceColors
import com.shub39.grit.shared.ui.theme.flexFontEmphasis
import com.shub39.grit.shared.ui.viewmodel.HabitViewModel
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.greeting_header_welcome
import grit.shared.ui.generated.resources.setup_name_hint
import grit.shared.ui.generated.resources.setup_name_label
import grit.shared.ui.generated.resources.setup_name_next
import grit.shared.ui.generated.resources.setup_name_skip
import grit.shared.ui.generated.resources.setup_name_text
import grit.shared.ui.generated.resources.setup_name_title
import grit.shared.ui.generated.resources.setup_species_header
import grit.shared.ui.generated.resources.setup_species_header_named
import grit.shared.ui.generated.resources.setup_species_later
import grit.shared.ui.generated.resources.setup_species_start
import grit.shared.ui.generated.resources.setup_species_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Shows the name setup on the very first start (#139), app-wide like the [DailyGreetingHost]. The
 * greeting is asked for by the greeting host and waits until this is finished or skipped.
 */
@Composable
fun NameSetupHost() {
    val viewModel = koinViewModel<HabitViewModel>()
    val shown by viewModel.nameSetup.collectAsStateWithLifecycle()

    if (!shown) return
    NameSetupStage(
        onFinish = { name, species ->
            viewModel.onAction(HabitsAction.FinishNameSetup(name, species))
        }
    )
}

/**
 * Full screen stage on the very first start, in two steps: the seed asks for the user's name, then
 * the user picks which plant grows with them (#140). [onFinish] gets the name, or an empty one when
 * skipped, and the chosen species. The back key skips the name, and on the second step keeps the
 * plant that is selected.
 */
@Composable
fun NameSetupStage(
    onFinish: (String, AvatarSpecies) -> Unit,
    modifier: Modifier = Modifier,
    startWithSpecies: Boolean = false,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var choosing by rememberSaveable { mutableStateOf(startWithSpecies) }
    var species by rememberSaveable { mutableStateOf(AvatarSpecies.SPROUT) }

    CelebrationStage(
        colors = CheckInStageColors,
        onDismiss = {
            if (choosing) onFinish(name, species)
            else {
                name = ""
                choosing = true
            }
        },
        tapToDismiss = false,
        modifier = modifier,
    ) {
        if (choosing) {
            SpeciesStep(
                userName = UserName.clean(name),
                species = species,
                onSelect = { species = it },
                onStart = { onFinish(name, species) },
            )
            return@CelebrationStage
        }

        Text(
            text = stringResource(Res.string.greeting_header_welcome).uppercase(),
            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 3.sp),
            color = StageYellow,
            textAlign = TextAlign.Center,
        )

        val plantScale = rememberPopIn(delayMillis = 150)
        PlantAvatar(
            stage = PlantStage.SEED,
            mood = PlantMood.HAPPY,
            modifier =
                Modifier.widthIn(max = 160.dp).fillMaxWidth().graphicsLayer {
                    scaleX = plantScale
                    scaleY = plantScale
                },
        )

        Text(
            text = stringResource(Res.string.setup_name_title),
            style =
                MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = flexFontEmphasis(),
                    fontWeight = FontWeight.Black,
                ),
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.setup_name_text),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it.take(UserName.MAX_LENGTH) },
            singleLine = true,
            label = { Text(stringResource(Res.string.setup_name_label)) },
            supportingText = { Text(stringResource(Res.string.setup_name_hint)) },
            keyboardOptions =
                KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done,
                ),
            keyboardActions = KeyboardActions(onDone = { choosing = true }),
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = StageYellow,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                    focusedLabelColor = StageYellow,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
                    cursorColor = StageYellow,
                    focusedSupportingTextColor = Color.White.copy(alpha = 0.8f),
                    unfocusedSupportingTextColor = Color.White.copy(alpha = 0.8f),
                ),
            modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth().padding(top = 8.dp),
        )

        Button(
            onClick = { choosing = true },
            colors =
                ButtonDefaults.buttonColors(containerColor = StageYellow, contentColor = StageInk),
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Text(
                text = stringResource(Res.string.setup_name_next),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
            )
        }
        TextButton(
            onClick = {
                name = ""
                choosing = true
            }
        ) {
            Text(text = stringResource(Res.string.setup_name_skip), color = Color.White)
        }
    }
}

/** The second step: two cards to pick the plant, and a button to start. */
@Composable
private fun SpeciesStep(
    userName: String,
    species: AvatarSpecies,
    onSelect: (AvatarSpecies) -> Unit,
    onStart: () -> Unit,
) {
    Text(
        text =
            if (userName.isBlank()) stringResource(Res.string.setup_species_header)
            else stringResource(Res.string.setup_species_header_named, userName),
        style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp),
        color = StageYellow,
        textAlign = TextAlign.Center,
    )
    Text(
        text = stringResource(Res.string.setup_species_title),
        style =
            MaterialTheme.typography.headlineMedium.copy(
                fontFamily = flexFontEmphasis(),
                fontWeight = FontWeight.Black,
            ),
        color = Color.White,
        textAlign = TextAlign.Center,
    )
    SpeciesChoice(
        selected = species,
        onSelect = onSelect,
        colors =
            SpeciesChoiceColors(
                card = Color.White.copy(alpha = 0.14f),
                content = Color.White,
                selected = StageYellow,
                onSelected = StageInk,
            ),
        modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth().padding(top = 8.dp),
    )
    Text(
        text = stringResource(Res.string.setup_species_later),
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White.copy(alpha = 0.85f),
        textAlign = TextAlign.Center,
    )
    Button(
        onClick = onStart,
        colors = ButtonDefaults.buttonColors(containerColor = StageYellow, contentColor = StageInk),
        modifier = Modifier.padding(top = 8.dp),
    ) {
        Text(
            text = stringResource(Res.string.setup_species_start),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
        )
    }
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun NameSetupStagePreview() {
    NameSetupStage(onFinish = { _, _ -> })
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun SpeciesStepPreview() {
    NameSetupStage(onFinish = { _, _ -> }, startWithSpecies = true)
}
