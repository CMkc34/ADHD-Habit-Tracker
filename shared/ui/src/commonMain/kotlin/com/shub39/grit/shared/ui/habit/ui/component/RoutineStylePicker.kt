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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shub39.grit.core.habits.style.RoutineColor
import com.shub39.grit.core.habits.style.RoutineIcon
import com.shub39.grit.core.habits.style.RoutineStyle
import com.shub39.grit.core.theme.AppTheme
import com.shub39.grit.core.theme.Theme
import com.shub39.grit.shared.ui.components.GritDialog
import com.shub39.grit.shared.ui.habit.style.RoutinePaperIcon
import com.shub39.grit.shared.ui.habit.style.color
import com.shub39.grit.shared.ui.habit.style.contentColor
import com.shub39.grit.shared.ui.habit.style.label
import com.shub39.grit.shared.ui.theme.GritTheme
import com.shub39.grit.shared.ui.theme.flexFontEmphasis
import com.shub39.grit.shared.ui.theme.flexFontRounded
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.avatar_cancel
import grit.shared.ui.generated.resources.check
import grit.shared.ui.generated.resources.routine_style_color
import grit.shared.ui.generated.resources.routine_style_done
import grit.shared.ui.generated.resources.routine_style_icon
import grit.shared.ui.generated.resources.routine_style_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

private const val COLUMNS = 6

/**
 * The routine's paper icon on a light tile in the routine's colour, as on the routine card (#126).
 * Clickable when [onClick] is set.
 */
@Composable
fun RoutineStyleTile(
    style: RoutineStyle,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    onClick: (() -> Unit)? = null,
) {
    val description = "${style.icon.label()}, ${style.color.label()}"
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .size(size)
                .then(
                    if (onClick != null) {
                        Modifier.clip(RoundedCornerShape(size * 0.28f)).clickable(onClick = onClick)
                    } else {
                        Modifier
                    }
                )
                .semantics { contentDescription = description },
    ) {
        RoutinePaperIcon(style = style, modifier = Modifier.fillMaxSize())
    }
}

/**
 * Dialog to choose colour (24, grid of 6 × 4) and icon (36, grid of 6 × 6). The choice only counts
 * after [onConfirm]; dismissing keeps the previous style.
 */
@Composable
fun RoutineStylePicker(
    initial: RoutineStyle,
    onConfirm: (RoutineStyle) -> Unit,
    onDismiss: () -> Unit,
) {
    var style by remember(initial) { mutableStateOf(initial) }

    GritDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RoutineStyleTile(style = style, size = 64.dp)

            Text(
                text = stringResource(Res.string.routine_style_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontFamily = flexFontEmphasis()),
            )

            SectionTitle(stringResource(Res.string.routine_style_color))
            Grid(RoutineColor.entries) { color ->
                ColorSwatch(
                    color = color,
                    selected = color == style.color,
                    onClick = { style = style.copy(color = color) },
                )
            }

            SectionTitle(stringResource(Res.string.routine_style_icon))
            Grid(RoutineIcon.entries) { icon ->
                IconChoice(
                    icon = icon,
                    color = style.color,
                    selected = icon == style.icon,
                    onClick = { style = style.copy(icon = icon) },
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text(stringResource(Res.string.avatar_cancel)) }
                TextButton(onClick = { onConfirm(style) }) {
                    Text(stringResource(Res.string.routine_style_done))
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontFamily = flexFontRounded(),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun <T> Grid(items: List<T>, cell: @Composable (T) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.chunked(COLUMNS).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { Box(modifier = Modifier.weight(1f)) { cell(it) } }
            }
        }
    }
}

@Composable
private fun ColorSwatch(color: RoutineColor, selected: Boolean, onClick: () -> Unit) {
    val label = color.label()
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier.fillMaxWidth()
                .aspectRatio(1f)
                .clip(CircleShape)
                .background(color.color)
                .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
                .semantics { contentDescription = label },
    ) {
        if (selected) {
            Icon(
                imageVector = vectorResource(Res.drawable.check),
                contentDescription = null,
                tint = color.contentColor,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun IconChoice(
    icon: RoutineIcon,
    color: RoutineColor,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val label = icon.label()
    val shape = RoundedCornerShape(12.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier.fillMaxWidth()
                .aspectRatio(1f)
                .then(
                    if (selected) {
                        Modifier.border(
                                BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface),
                                shape,
                            )
                            .padding(4.dp)
                    } else {
                        Modifier
                    }
                )
                .clip(shape)
                .background(color.color.copy(alpha = if (selected) 0.45f else 0.18f))
                .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
                .semantics { contentDescription = label },
    ) {
        RoutinePaperIcon(
            style = RoutineStyle(color, icon),
            withTile = false,
            modifier = Modifier.size(34.dp),
        )
    }
}

@Preview
@Composable
private fun RoutineStylePickerLightPreview() {
    GritTheme(theme = Theme(appTheme = AppTheme.LIGHT)) {
        Surface {
            RoutineStylePicker(
                initial = RoutineStyle(RoutineColor.CORAL, RoutineIcon.RUN),
                onConfirm = {},
                onDismiss = {},
            )
        }
    }
}

@Preview
@Composable
private fun RoutineStylePickerDarkPreview() {
    GritTheme(theme = Theme(appTheme = AppTheme.DARK)) {
        Surface {
            RoutineStylePicker(initial = RoutineStyle.DEFAULT, onConfirm = {}, onDismiss = {})
        }
    }
}
