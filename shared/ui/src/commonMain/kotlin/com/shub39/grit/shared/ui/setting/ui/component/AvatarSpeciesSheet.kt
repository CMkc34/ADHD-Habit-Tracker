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
package com.shub39.grit.shared.ui.setting.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.shub39.grit.core.avatar.AvatarData
import com.shub39.grit.core.avatar.AvatarSpecies
import com.shub39.grit.core.avatar.PlantStage
import com.shub39.grit.shared.ui.components.GritBottomSheet
import com.shub39.grit.shared.ui.habit.avatar.SpeciesChoice
import com.shub39.grit.shared.ui.habit.avatar.SpeciesChoiceColors
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.avatar_species_keep
import grit.shared.ui.generated.resources.avatar_species_setting
import org.jetbrains.compose.resources.stringResource

/**
 * Settings → "Dein Avatar" (#140): both plants side by side with the user's pot and accessories; a
 * tap switches right away. A plant still in its pot is shown as a young tree, so the two differ.
 */
@Composable
fun AvatarSpeciesSheet(
    avatar: AvatarData,
    stage: PlantStage,
    onSelect: (AvatarSpecies) -> Unit,
    onDismiss: () -> Unit,
) {
    GritBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = stringResource(Res.string.avatar_species_setting),
            style = MaterialTheme.typography.titleLarge,
        )
        SpeciesChoice(
            selected = avatar.species,
            onSelect = onSelect,
            colors =
                SpeciesChoiceColors(
                    card = MaterialTheme.colorScheme.surfaceContainerHigh,
                    content = MaterialTheme.colorScheme.onSurface,
                    selected = MaterialTheme.colorScheme.primary,
                    onSelected = MaterialTheme.colorScheme.onPrimary,
                ),
            stage = if (stage >= PlantStage.SAPLING) stage else PlantStage.YOUNG_TREE,
            potColor = avatar.potColor,
            accessories = avatar.accessories,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = stringResource(Res.string.avatar_species_keep),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
