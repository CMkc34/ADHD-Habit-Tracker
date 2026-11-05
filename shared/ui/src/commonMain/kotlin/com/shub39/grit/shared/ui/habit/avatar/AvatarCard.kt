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
import androidx.compose.ui.unit.dp
import com.shub39.grit.core.avatar.AvatarData
import com.shub39.grit.shared.ui.components.GritDialog
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.avatar_cancel
import grit.shared.ui.generated.resources.avatar_rename_hint
import grit.shared.ui.generated.resources.avatar_rename_title
import grit.shared.ui.generated.resources.avatar_save
import org.jetbrains.compose.resources.stringResource

// the longest name that still fits on the name plate
private const val MAX_NAME_LENGTH = 24

/** The stored name, or the fighter's default name while the user has not named it. */
fun fighterName(data: AvatarData): String = data.displayName

/** Asks for a new name; an empty name brings back the [defaultName]. */
@Composable
fun RenameAvatarDialog(
    currentName: String,
    defaultName: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
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
            placeholder = { Text(defaultName) },
            supportingText = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(Res.string.avatar_rename_hint, defaultName),
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
