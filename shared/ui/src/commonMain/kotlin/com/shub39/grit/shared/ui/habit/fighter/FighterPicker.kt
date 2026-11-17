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
package com.shub39.grit.shared.ui.habit.fighter

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.shub39.grit.core.fighter.FighterCharacter
import com.shub39.grit.core.fighter.FighterColorSlot
import com.shub39.grit.core.fighter.FighterColors
import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.core.fighter.FighterSetup
import com.shub39.grit.shared.ui.GritPreviewWrapper
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.arrow_back
import grit.shared.ui.generated.resources.arrow_forward
import grit.shared.ui.generated.resources.fighter_picker_next
import grit.shared.ui.generated.resources.fighter_picker_previous
import grit.shared.ui.generated.resources.fighter_picker_random
import grit.shared.ui.generated.resources.fighter_picker_reset
import grit.shared.ui.generated.resources.fighter_picker_swatch
import grit.shared.ui.generated.resources.label_fighter_slot_main
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

// how far a swipe over the fighter has to go to flip to the next one
private const val SWIPE_THRESHOLD_PX = 60f

/**
 * Picks the fighter and its colours at setup (#26): a big animated fighter to swipe or flip through
 * the five [FighterCharacter]s, its name and kind below, then a compact segmented tab bar for
 * [FighterColorSlot] showing one active swatch row at a time with a live preview, and buttons for
 * random colours and the character's own. The picker only shows the choice; the caller keeps it and
 * makes it final.
 */
@Composable
fun FighterPicker(
    character: FighterCharacter,
    colors: FighterColors,
    onCharacterChange: (FighterCharacter) -> Unit,
    onColorsChange: (FighterColors) -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
) {
    val current by rememberUpdatedState(character)
    val onChange by rememberUpdatedState(onCharacterChange)
    var activeSlot by rememberSaveable { mutableStateOf(FighterColorSlot.SKIN) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onCharacterChange(FighterSetup.step(character, -1)) }) {
                Icon(
                    painter = painterResource(Res.drawable.arrow_back),
                    contentDescription = stringResource(Res.string.fighter_picker_previous),
                    tint = contentColor,
                )
            }
            Box(
                modifier =
                    Modifier.size(144.dp).pointerInput(Unit) {
                        var dragged = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { dragged = 0f },
                            onDragEnd = {
                                when {
                                    dragged > SWIPE_THRESHOLD_PX ->
                                        onChange(FighterSetup.step(current, -1))
                                    dragged < -SWIPE_THRESHOLD_PX ->
                                        onChange(FighterSetup.step(current, 1))
                                }
                            },
                            onHorizontalDrag = { change, amount ->
                                change.consume()
                                dragged += amount
                            },
                        )
                    }
            ) {
                FighterAvatar(
                    character = character,
                    rank = FighterRank.ROOKIE,
                    mood = FighterMood.READY,
                    colors = colors,
                    modifier = Modifier.size(144.dp),
                )
            }
            IconButton(onClick = { onCharacterChange(FighterSetup.step(character, 1)) }) {
                Icon(
                    painter = painterResource(Res.drawable.arrow_forward),
                    contentDescription = stringResource(Res.string.fighter_picker_next),
                    tint = contentColor,
                )
            }
        }

        // one dot per fighter, the shown one filled
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FighterCharacter.entries.forEach {
                Box(
                    modifier =
                        Modifier.size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (it == character) selectedColor
                                else contentColor.copy(alpha = 0.35f)
                            )
                )
            }
        }

        Text(
            text = character.defaultName,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
            color = contentColor,
        )
        Text(
            text = "${characterName(character)} · ${characterDescription(character)}",
            style = MaterialTheme.typography.bodySmall,
            color = contentColor.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
        )

        ColorSlotTabs(
            activeSlot = activeSlot,
            colors = colors,
            onSelectSlot = { activeSlot = it },
            contentColor = contentColor,
            selectedColor = selectedColor,
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        )

        SwatchRow(
            slot = activeSlot,
            selected = activeSlot.indexIn(colors),
            onSelect = { onColorsChange(activeSlot.set(colors, it)) },
            contentColor = contentColor,
            selectedColor = selectedColor,
            modifier = Modifier.padding(top = 6.dp),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(
                onClick = { onColorsChange(FighterSetup.randomColors()) },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Text(stringResource(Res.string.fighter_picker_random), color = contentColor)
            }
            TextButton(
                onClick = { onColorsChange(character.defaultColors) },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Text(stringResource(Res.string.fighter_picker_reset), color = contentColor)
            }
        }
    }
}

/** Segmented tab strip for switching between [FighterColorSlot]s, each with a live colour dot. */
@Composable
private fun ColorSlotTabs(
    activeSlot: FighterColorSlot,
    colors: FighterColors,
    onSelectSlot: (FighterColorSlot) -> Unit,
    contentColor: Color,
    selectedColor: Color,
    modifier: Modifier = Modifier,
) {
    val tabShape = RoundedCornerShape(10.dp)
    Row(
        modifier =
            modifier
                .clip(RoundedCornerShape(12.dp))
                .background(contentColor.copy(alpha = 0.1f))
                .border(1.dp, contentColor.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
                .padding(3.dp)
                .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        FighterColorSlot.entries.forEach { slot ->
            val isSelected = slot == activeSlot
            val label =
                if (slot == FighterColorSlot.MAIN)
                    stringResource(Res.string.label_fighter_slot_main)
                else colorSlotName(slot)
            val previewColor =
                Color(slot.presets.getOrElse(slot.indexIn(colors)) { slot.presets.first() })

            Row(
                modifier =
                    Modifier.weight(1f)
                        .clip(tabShape)
                        .then(
                            if (isSelected) {
                                Modifier.background(selectedColor.copy(alpha = 0.22f))
                                    .border(1.dp, selectedColor, tabShape)
                            } else {
                                Modifier
                            }
                        )
                        .selectable(
                            selected = isSelected,
                            onClick = { onSelectSlot(slot) },
                            role = Role.Tab,
                        )
                        .padding(horizontal = 6.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier.size(10.dp)
                            .clip(CircleShape)
                            .background(previewColor)
                            .border(1.dp, contentColor.copy(alpha = 0.55f), CircleShape)
                )
                Text(
                    text = label,
                    style =
                        MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                    color = if (isSelected) selectedColor else contentColor.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** One round swatch per preset of [slot]; the [selected] one has a thick ring. */
@Composable
private fun SwatchRow(
    slot: FighterColorSlot,
    selected: Int,
    onSelect: (Int) -> Unit,
    contentColor: Color,
    selectedColor: Color,
    modifier: Modifier = Modifier,
) {
    val label = colorSlotName(slot)
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        slot.presets.forEachIndexed { index, argb ->
            val isSelected = index == selected
            val description =
                stringResource(
                    Res.string.fighter_picker_swatch,
                    label,
                    index + 1,
                    slot.presets.size,
                )
            Box(
                modifier =
                    Modifier.size(28.dp)
                        .clip(CircleShape)
                        .background(Color(argb))
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color =
                                if (isSelected) selectedColor else contentColor.copy(alpha = 0.4f),
                            shape = CircleShape,
                        )
                        .selectable(
                            selected = isSelected,
                            onClick = { onSelect(index) },
                            role = Role.RadioButton,
                        )
                        .semantics { contentDescription = description }
            )
        }
    }
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun FighterPickerPreview() {
    FighterPicker(
        character = FighterCharacter.SHADOW_RUNNER,
        colors = FighterCharacter.SHADOW_RUNNER.defaultColors,
        onCharacterChange = {},
        onColorsChange = {},
        modifier = Modifier.padding(16.dp),
    )
}
