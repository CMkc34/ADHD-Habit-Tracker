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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.Alignment
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
import com.shub39.grit.core.avatar.UserName
import com.shub39.grit.core.fighter.FighterCharacter
import com.shub39.grit.core.fighter.FighterColors
import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.shared.ui.GritPreviewWrapper
import com.shub39.grit.shared.ui.habit.HabitsAction
import com.shub39.grit.shared.ui.habit.fighter.FighterAvatar
import com.shub39.grit.shared.ui.habit.fighter.FighterPicker
import com.shub39.grit.shared.ui.habit.fighter.FighterPose
import com.shub39.grit.shared.ui.theme.flexFontEmphasis
import com.shub39.grit.shared.ui.viewmodel.HabitViewModel
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.greeting_header_welcome
import grit.shared.ui.generated.resources.setup_fighter_choose
import grit.shared.ui.generated.resources.setup_fighter_confirm_back
import grit.shared.ui.generated.resources.setup_fighter_confirm_text
import grit.shared.ui.generated.resources.setup_fighter_confirm_title
import grit.shared.ui.generated.resources.setup_fighter_confirm_yes
import grit.shared.ui.generated.resources.setup_fighter_final
import grit.shared.ui.generated.resources.setup_fighter_title
import grit.shared.ui.generated.resources.setup_name_hint
import grit.shared.ui.generated.resources.setup_name_label
import grit.shared.ui.generated.resources.setup_name_next
import grit.shared.ui.generated.resources.setup_name_skip
import grit.shared.ui.generated.resources.setup_name_text
import grit.shared.ui.generated.resources.setup_name_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Shows the first start setup (#139, #26) app-wide like the [DailyGreetingHost]. The greeting is
 * asked for by the greeting host and waits until this is finished.
 */
@Composable
fun NameSetupHost() {
    val viewModel = koinViewModel<HabitViewModel>()
    val shown by viewModel.nameSetup.collectAsStateWithLifecycle()

    if (!shown) return
    NameSetupStage(
        onChooseFighter = { character, colors ->
            viewModel.onAction(HabitsAction.ChooseFighter(character, colors))
        },
        onFinish = { name -> viewModel.onAction(HabitsAction.FinishNameSetup(name)) },
    )
}

/**
 * Full screen stage on the very first start, in two steps. First the user picks their fighter and
 * its colours (#26); a confirm dialog says the choice is final, then [onChooseFighter] stores it.
 * Then the fighter asks for the user's name (#139); [onFinish] gets the name, or an empty one when
 * skipped. The back key does nothing while picking, so the choice is never made by accident, and
 * skips the name on the second step.
 */
@Composable
fun NameSetupStage(
    onChooseFighter: (FighterCharacter, FighterColors) -> Unit,
    onFinish: (String) -> Unit,
    modifier: Modifier = Modifier,
    startWithName: Boolean = false,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var picked by rememberSaveable { mutableStateOf(startWithName) }
    var confirming by rememberSaveable { mutableStateOf(false) }
    var characterId by rememberSaveable { mutableStateOf(FighterCharacter.DEFAULT.id) }
    val character = FighterCharacter.fromId(characterId)
    var colorsCode by rememberSaveable { mutableStateOf(character.defaultColors.encode()) }
    val colors = FighterColors.decode(colorsCode, character.defaultColors)

    CelebrationStage(
        colors = CheckInStageColors,
        onDismiss = { if (picked) onFinish("") },
        tapToDismiss = false,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (picked) {
                NameStep(
                    character = character,
                    colors = colors,
                    name = name,
                    onNameChange = { name = it.take(UserName.MAX_LENGTH) },
                    onNext = { onFinish(name) },
                    onSkip = { onFinish("") },
                )
            } else {
                FighterStep(
                    character = character,
                    colors = colors,
                    onCharacterChange = {
                        // a new fighter starts in its own colours
                        characterId = it.id
                        colorsCode = it.defaultColors.encode()
                    },
                    onColorsChange = { colorsCode = it.encode() },
                    onChoose = { confirming = true },
                )
            }
        }
    }

    if (confirming) {
        AlertDialog(
            onDismissRequest = { confirming = false },
            title = {
                Text(stringResource(Res.string.setup_fighter_confirm_title, character.defaultName))
            },
            text = { Text(stringResource(Res.string.setup_fighter_confirm_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirming = false
                        picked = true
                        onChooseFighter(character, colors)
                    }
                ) {
                    Text(stringResource(Res.string.setup_fighter_confirm_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirming = false }) {
                    Text(stringResource(Res.string.setup_fighter_confirm_back))
                }
            },
        )
    }
}

/** The first step: "Who trains with you?", the picker and a note that the choice is final. */
@Composable
private fun FighterStep(
    character: FighterCharacter,
    colors: FighterColors,
    onCharacterChange: (FighterCharacter) -> Unit,
    onColorsChange: (FighterColors) -> Unit,
    onChoose: () -> Unit,
) {
    Text(
        text = stringResource(Res.string.greeting_header_welcome).uppercase(),
        style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 3.sp),
        color = StageYellow,
        textAlign = TextAlign.Center,
    )
    Text(
        text = stringResource(Res.string.setup_fighter_title),
        style =
            MaterialTheme.typography.headlineMedium.copy(
                fontFamily = flexFontEmphasis(),
                fontWeight = FontWeight.Black,
            ),
        color = Color.White,
        textAlign = TextAlign.Center,
    )
    FighterPicker(
        character = character,
        colors = colors,
        onCharacterChange = onCharacterChange,
        onColorsChange = onColorsChange,
        contentColor = Color.White,
        selectedColor = StageYellow,
        modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth(),
    )
    Text(
        text = stringResource(Res.string.setup_fighter_final),
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White.copy(alpha = 0.85f),
        textAlign = TextAlign.Center,
        modifier = Modifier.widthIn(max = 360.dp),
    )
    Button(
        onClick = onChoose,
        colors = ButtonDefaults.buttonColors(containerColor = StageYellow, contentColor = StageInk),
        modifier = Modifier.padding(top = 4.dp),
    ) {
        Text(
            text = stringResource(Res.string.setup_fighter_choose, character.defaultName),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
        )
    }
}

/** The second step: the picked fighter asks for the user's name. */
@Composable
private fun NameStep(
    character: FighterCharacter,
    colors: FighterColors,
    name: String,
    onNameChange: (String) -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit,
) {
    Text(
        text = stringResource(Res.string.greeting_header_welcome).uppercase(),
        style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 3.sp),
        color = StageYellow,
        textAlign = TextAlign.Center,
    )

    val fighterScale = rememberPopIn(delayMillis = 150)
    FighterAvatar(
        character = character,
        rank = FighterRank.ROOKIE,
        mood = FighterMood.READY,
        colors = colors,
        pose = FighterPose.CHARGE,
        modifier =
            Modifier.widthIn(max = 160.dp).fillMaxWidth().graphicsLayer {
                scaleX = fighterScale
                scaleY = fighterScale
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
        text = stringResource(Res.string.setup_name_text, character.defaultName),
        style = MaterialTheme.typography.titleMedium,
        color = Color.White.copy(alpha = 0.9f),
        textAlign = TextAlign.Center,
    )

    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        singleLine = true,
        label = { Text(stringResource(Res.string.setup_name_label)) },
        supportingText = { Text(stringResource(Res.string.setup_name_hint)) },
        keyboardOptions =
            KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done,
            ),
        keyboardActions = KeyboardActions(onDone = { onNext() }),
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
        onClick = onNext,
        colors = ButtonDefaults.buttonColors(containerColor = StageYellow, contentColor = StageInk),
        modifier = Modifier.padding(top = 8.dp),
    ) {
        Text(
            text = stringResource(Res.string.setup_name_next),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
        )
    }
    TextButton(onClick = onSkip) {
        Text(text = stringResource(Res.string.setup_name_skip), color = Color.White)
    }
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun FighterStepPreview() {
    NameSetupStage(onChooseFighter = { _, _ -> }, onFinish = {})
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun NameStepPreview() {
    NameSetupStage(onChooseFighter = { _, _ -> }, onFinish = {}, startWithName = true)
}
