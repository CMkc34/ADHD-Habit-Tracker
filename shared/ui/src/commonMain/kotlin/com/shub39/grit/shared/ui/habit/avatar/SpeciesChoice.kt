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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.shub39.grit.core.avatar.AvatarSpecies
import com.shub39.grit.core.avatar.PlantMood
import com.shub39.grit.core.avatar.PlantStage
import com.shub39.grit.core.avatar.ShopCatalog
import com.shub39.grit.shared.ui.GritPreviewWrapper
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.avatar_species_cherry_desc
import grit.shared.ui.generated.resources.avatar_species_sprout_desc
import grit.shared.ui.generated.resources.check
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Colors of a [SpeciesChoice] card, so it fits on the setup stage and in the settings. */
data class SpeciesChoiceColors(
    val card: Color,
    val content: Color,
    val selected: Color,
    val onSelected: Color,
)

/** The short line under a species' name. */
@Composable
fun speciesDescription(species: AvatarSpecies): String =
    stringResource(
        when (species) {
            AvatarSpecies.SPROUT -> Res.string.avatar_species_sprout_desc
            AvatarSpecies.CHERRY -> Res.string.avatar_species_cherry_desc
        }
    )

/**
 * One card per [AvatarSpecies] side by side, each showing that plant at [stage] with the user's
 * [potColor] and [accessories]; the [selected] one is framed and ticked (#140).
 */
@Composable
fun SpeciesChoice(
    selected: AvatarSpecies,
    onSelect: (AvatarSpecies) -> Unit,
    colors: SpeciesChoiceColors,
    modifier: Modifier = Modifier,
    stage: PlantStage = PlantStage.YOUNG_TREE,
    potColor: String = ShopCatalog.DEFAULT_POT_COLOR,
    accessories: Set<String> = emptySet(),
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AvatarSpecies.entries.forEach { species ->
            val isSelected = species == selected
            Surface(
                color = colors.card,
                contentColor = colors.content,
                shape = RoundedCornerShape(18.dp),
                border = if (isSelected) BorderStroke(2.5.dp, colors.selected) else null,
                modifier =
                    Modifier.weight(1f)
                        .selectable(
                            selected = isSelected,
                            onClick = { onSelect(species) },
                            role = Role.RadioButton,
                        ),
            ) {
                Box {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                    ) {
                        PlantAvatar(
                            stage = stage,
                            mood = PlantMood.HAPPY,
                            species = species,
                            potColor = potColor,
                            accessories = accessories,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            text = defaultPlantName(species),
                            style =
                                MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black
                                ),
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = speciesDescription(species),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.content.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                        )
                    }
                    if (isSelected) {
                        Surface(
                            color = colors.selected,
                            contentColor = colors.onSelected,
                            shape = CircleShape,
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(24.dp),
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.check),
                                contentDescription = null,
                                modifier = Modifier.padding(4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun SpeciesChoicePreview() {
    SpeciesChoice(
        selected = AvatarSpecies.CHERRY,
        onSelect = {},
        colors =
            SpeciesChoiceColors(
                card = MaterialTheme.colorScheme.surfaceContainerHigh,
                content = MaterialTheme.colorScheme.onSurface,
                selected = MaterialTheme.colorScheme.primary,
                onSelected = MaterialTheme.colorScheme.onPrimary,
            ),
        modifier = Modifier.padding(16.dp),
    )
}
