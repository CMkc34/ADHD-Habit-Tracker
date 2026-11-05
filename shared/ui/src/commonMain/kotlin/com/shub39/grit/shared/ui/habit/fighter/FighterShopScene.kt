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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.shub39.grit.core.avatar.AvatarData
import com.shub39.grit.core.fighter.FighterCharacter
import com.shub39.grit.core.fighter.FighterItems
import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.shared.ui.GritPreviewWrapper

/**
 * The fighter as the shop shows it (#7): wearing the outfit and gear of [data], with the shown
 * training-ground props below. Until the character and colours are stored (#26), the fighter is the
 * one that replaces the old look ([FighterCharacter.of]) in its default colours. The full training
 * ground scene follows in #8.
 */
@Composable
fun FighterShopScene(
    data: AvatarData,
    rank: FighterRank,
    mood: FighterMood,
    modifier: Modifier = Modifier,
    animate: Boolean = true,
) {
    val props = FighterItems.props.filter { it in data.decorations }
    Column(
        modifier =
            modifier
                .background(
                    MaterialTheme.colorScheme.surfaceContainerHigh,
                    MaterialTheme.shapes.large,
                )
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FighterAvatar(
            character = FighterCharacter.of(data.look),
            rank = rank,
            mood = mood,
            outfit = data.outfit,
            gear = data.accessories,
            animate = animate,
            modifier = Modifier.widthIn(max = 200.dp).fillMaxWidth(),
        )
        if (props.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                props.forEach { PropIcon(it, Modifier.size(40.dp)) }
            }
        }
    }
}

/** The pixel icon of the training-ground prop [id]; draws nothing for other ids. */
@Composable
fun PropIcon(id: String, modifier: Modifier = Modifier) {
    val image = remember(id) { PropPixels.render(id) } ?: return
    Canvas(modifier = modifier.aspectRatio(1f)) { drawPixels(image) }
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview(widthDp = 360, heightDp = 360)
@Composable
private fun FighterShopScenePreview() {
    FighterShopScene(
        data =
            AvatarData(
                outfit = FighterItems.GI_BLUE,
                accessories = setOf(FighterItems.SWEATBAND, FighterItems.GLOVES),
                decorations = FighterItems.props.toSet(),
            ),
        rank = FighterRank.FIGHTER,
        mood = FighterMood.READY,
        animate = false,
    )
}
