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
package com.shub39.grit.shared.ui.habit.avatar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.shub39.grit.core.avatar.AvatarData
import com.shub39.grit.core.avatar.AvatarSpecies
import com.shub39.grit.core.avatar.AvatarState
import com.shub39.grit.core.avatar.PlantMood
import com.shub39.grit.core.avatar.PlantStage
import com.shub39.grit.core.avatar.PlantState
import com.shub39.grit.shared.ui.GritPreviewWrapper
import com.shub39.grit.shared.ui.components.GritDialog
import com.shub39.grit.shared.ui.theme.flexFontEmphasis
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.avatar_cancel
import grit.shared.ui.generated.resources.avatar_default_name
import grit.shared.ui.generated.resources.avatar_default_name_cherry
import grit.shared.ui.generated.resources.avatar_new_stage_ok
import grit.shared.ui.generated.resources.avatar_new_stage_text
import grit.shared.ui.generated.resources.avatar_new_stage_title
import grit.shared.ui.generated.resources.avatar_rename_hint
import grit.shared.ui.generated.resources.avatar_rename_title
import grit.shared.ui.generated.resources.avatar_save
import grit.shared.ui.generated.resources.avatar_stage_1
import grit.shared.ui.generated.resources.avatar_stage_10
import grit.shared.ui.generated.resources.avatar_stage_11
import grit.shared.ui.generated.resources.avatar_stage_12
import grit.shared.ui.generated.resources.avatar_stage_2
import grit.shared.ui.generated.resources.avatar_stage_3
import grit.shared.ui.generated.resources.avatar_stage_4
import grit.shared.ui.generated.resources.avatar_stage_5
import grit.shared.ui.generated.resources.avatar_stage_6
import grit.shared.ui.generated.resources.avatar_stage_7
import grit.shared.ui.generated.resources.avatar_stage_8
import grit.shared.ui.generated.resources.avatar_stage_9
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

// the longest name that still fits under the plant
private const val MAX_NAME_LENGTH = 24

private fun PlantStage.nameRes(): StringResource =
    when (this) {
        PlantStage.SEED -> Res.string.avatar_stage_1
        PlantStage.GERM -> Res.string.avatar_stage_2
        PlantStage.SEEDLING -> Res.string.avatar_stage_3
        PlantStage.SPROUT -> Res.string.avatar_stage_4
        PlantStage.PLANTLET -> Res.string.avatar_stage_5
        PlantStage.YOUNG_PLANT -> Res.string.avatar_stage_6
        PlantStage.SAPLING -> Res.string.avatar_stage_7
        PlantStage.YOUNG_TREE -> Res.string.avatar_stage_8
        PlantStage.TREE -> Res.string.avatar_stage_9
        PlantStage.BIG_TREE -> Res.string.avatar_stage_10
        PlantStage.BLOSSOMING_TREE -> Res.string.avatar_stage_11
        PlantStage.MIGHTY_TREE -> Res.string.avatar_stage_12
    }

@Composable fun stageName(stage: PlantStage): String = stringResource(stage.nameRes())

/** The name a plant of [species] has until the user names it: "Sprout" or "Kirsche" (#140). */
@Composable
fun defaultPlantName(species: AvatarSpecies): String =
    stringResource(
        when (species) {
            AvatarSpecies.SPROUT -> Res.string.avatar_default_name
            AvatarSpecies.CHERRY -> Res.string.avatar_default_name_cherry
        }
    )

/** The stored name, or the species' default name while the user has not named the plant. */
@Composable
fun plantName(data: AvatarData): String = data.name.ifBlank { defaultPlantName(data.species) }

/** Asks for a new name; an empty name brings back the default. */
@Composable
fun RenameAvatarDialog(
    currentName: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
    species: AvatarSpecies = AvatarSpecies.SPROUT,
) {
    var name by remember { mutableStateOf(currentName) }

    GritDialog(onDismissRequest = onDismiss) {
        Text(
            text = stringResource(Res.string.avatar_rename_title),
            style = MaterialTheme.typography.titleLarge,
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it.take(MAX_NAME_LENGTH) },
            singleLine = true,
            placeholder = { Text(defaultPlantName(species)) },
            supportingText = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(Res.string.avatar_rename_hint),
                        modifier = Modifier.weight(1f),
                    )
                    Text(text = "${name.length}/$MAX_NAME_LENGTH")
                }
            },
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.avatar_cancel)) }
            TextButton(onClick = { onSave(name) }) { Text(stringResource(Res.string.avatar_save)) }
        }
    }
}

/** Shown once when the plant reached a stage the user has not seen yet. */
@Composable
fun NewStageDialog(avatar: AvatarState, stage: PlantStage, onDismiss: () -> Unit) {
    GritDialog(onDismissRequest = onDismiss) {
        PlantAvatar(
            stage = stage,
            mood = avatar.plant.mood,
            potColor = avatar.data.potColor,
            accessories = avatar.data.accessories,
            species = avatar.data.species,
            modifier = Modifier.widthIn(max = 160.dp).fillMaxWidth(),
        )
        Text(
            text = stringResource(Res.string.avatar_new_stage_title),
            style = MaterialTheme.typography.titleLarge.copy(fontFamily = flexFontEmphasis()),
            textAlign = TextAlign.Center,
        )
        Text(
            text =
                stringResource(
                    Res.string.avatar_new_stage_text,
                    plantName(avatar.data),
                    stage.number,
                    stageName(stage),
                ),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        TextButton(onClick = onDismiss) { Text(stringResource(Res.string.avatar_new_stage_ok)) }
    }
}

private val previewAvatar =
    AvatarState(
        plant = PlantState(activeDays = 25, stage = PlantStage.PLANTLET, mood = PlantMood.HAPPY),
        energy = 12,
        data =
            AvatarData(
                potColor = "pot_blue",
                accessories = setOf("acc_ladybug"),
                decorations = setOf("deco_mushrooms", "deco_rabbit"),
            ),
    )

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun NewStageDialogPreview() {
    NewStageDialog(avatar = previewAvatar, stage = PlantStage.PLANTLET, onDismiss = {})
}
