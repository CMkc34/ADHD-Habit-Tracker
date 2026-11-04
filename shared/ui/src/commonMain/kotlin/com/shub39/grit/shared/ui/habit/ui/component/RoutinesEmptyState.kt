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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.shub39.grit.core.avatar.AvatarSpecies
import com.shub39.grit.core.avatar.AvatarState
import com.shub39.grit.core.avatar.PlantMood
import com.shub39.grit.core.avatar.PlantStage
import com.shub39.grit.core.avatar.ShopCatalog
import com.shub39.grit.shared.ui.GritPreviewWrapper
import com.shub39.grit.shared.ui.habit.avatar.PlantAvatar
import com.shub39.grit.shared.ui.habit.avatar.defaultPlantName
import com.shub39.grit.shared.ui.habit.avatar.plantName
import com.shub39.grit.shared.ui.theme.flexFontEmphasis
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.greeting_empty_button
import grit.shared.ui.generated.resources.greeting_empty_text
import grit.shared.ui.generated.resources.greeting_empty_title
import org.jetbrains.compose.resources.stringResource

/**
 * Shown on the routine page while there is no routine: the user's plant sleeps and waits for the
 * first one. Replaces the plain "add something" hint (fork).
 */
@Composable
fun RoutinesEmptyState(
    avatar: AvatarState?,
    onAddRoutine: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val name = avatar?.let { plantName(it.data) } ?: defaultPlantName(AvatarSpecies.SPROUT)

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PlantAvatar(
            stage = avatar?.plant?.stage ?: PlantStage.SEED,
            mood = PlantMood.TIRED,
            potColor = avatar?.data?.potColor ?: ShopCatalog.DEFAULT_POT_COLOR,
            accessories = avatar?.data?.accessories.orEmpty(),
            species = avatar?.data?.species ?: AvatarSpecies.SPROUT,
            modifier = Modifier.size(150.dp),
        )
        Text(
            text = stringResource(Res.string.greeting_empty_title, name),
            style =
                MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = flexFontEmphasis(),
                    fontWeight = FontWeight.Bold,
                ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.greeting_empty_text, name),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onAddRoutine, modifier = Modifier.padding(top = 8.dp)) {
            Text(text = stringResource(Res.string.greeting_empty_button))
        }
    }
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun RoutinesEmptyStatePreview() {
    RoutinesEmptyState(avatar = null, onAddRoutine = {})
}
