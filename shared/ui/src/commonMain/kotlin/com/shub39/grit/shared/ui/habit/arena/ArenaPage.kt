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
package com.shub39.grit.shared.ui.habit.arena

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.shub39.grit.core.avatar.AvatarData
import com.shub39.grit.core.avatar.AvatarState
import com.shub39.grit.core.fighter.FighterCharacter
import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.core.fighter.FighterState
import com.shub39.grit.shared.ui.GritPreviewWrapper
import com.shub39.grit.shared.ui.habit.fighter.FighterAvatar
import com.shub39.grit.shared.ui.habit.fighter.FighterPose
import com.shub39.grit.shared.ui.theme.flexFontEmphasis
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.arena_soon_text
import grit.shared.ui.generated.resources.arena_soon_title
import org.jetbrains.compose.resources.stringResource

/** The Arena tab. Until the fights arrive (#14) it only shows the fighter gathering ki. */
@Composable
fun ArenaPage(avatar: AvatarState?, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.widthIn(max = 360.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (avatar != null) {
                FighterAvatar(
                    character = FighterCharacter.of(avatar.data.look),
                    rank = avatar.fighter.rank,
                    // the arena is about getting ready, so the fighter never dozes here
                    mood = FighterMood.READY,
                    outfit = avatar.data.outfit,
                    gear = avatar.data.accessories,
                    pose = FighterPose.CHARGE,
                    modifier = Modifier.widthIn(max = 192.dp).fillMaxWidth(),
                )
            }
            Text(
                text = stringResource(Res.string.arena_soon_title),
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = flexFontEmphasis()),
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(Res.string.arena_soon_text),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun ArenaPagePreview() {
    ArenaPage(
        avatar =
            AvatarState(
                fighter =
                    FighterState(activeDays = 20, rank = FighterRank.BRAWLER, mood = FighterMood.READY),
                energy = 0,
                data = AvatarData(),
            )
    )
}
