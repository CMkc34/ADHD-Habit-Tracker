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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.shub39.grit.shared.ui.GritPreviewWrapper
import com.shub39.grit.shared.ui.theme.flexFontRounded
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.energy_header_desc
import grit.shared.ui.generated.resources.energy_header_line
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** „🔥 12 Tage aktiv · 💧 14“ in the habits header; tapping it opens the energy page. */
@Composable
fun ActivityHeaderLine(
    activeDays: Int,
    energy: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WaterText(
        text = pluralStringResource(Res.plurals.energy_header_line, activeDays, activeDays, energy),
        fontFamily = flexFontRounded(),
        color = MaterialTheme.colorScheme.primary,
        modifier =
            modifier
                .clip(MaterialTheme.shapes.small)
                .clickable(
                    onClickLabel = stringResource(Res.string.energy_header_desc),
                    role = Role.Button,
                    onClick = onClick,
                )
                .padding(vertical = 2.dp),
    )
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun ActivityHeaderLinePreview() {
    ActivityHeaderLine(activeDays = 12, energy = 14, onClick = {})
}
