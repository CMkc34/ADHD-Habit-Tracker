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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.shub39.grit.core.arena.Opponent
import com.shub39.grit.shared.ui.GritPreviewWrapper
import com.shub39.grit.shared.ui.habit.fighter.PixelImage
import com.shub39.grit.shared.ui.habit.fighter.drawPixels
import com.shub39.grit.shared.ui.habit.fighter.rememberPixelFrame
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.arena_opponent_hit
import grit.shared.ui.generated.resources.arena_opponent_idle
import grit.shared.ui.generated.resources.arena_opponent_ko
import grit.shared.ui.generated.resources.arena_opponent_locked
import org.jetbrains.compose.resources.stringResource

/**
 * One of the 10 ladder opponents (#13) in pixel art, facing left towards the fighter, in the
 * [pose]. Idle it bobs and does its own little move, knocked out the dizzy stars circle; the motion
 * follows the system's animation setting and stops completely when [animate] is false.
 */
@Composable
fun OpponentAvatar(
    opponent: Opponent,
    pose: OpponentPose,
    modifier: Modifier = Modifier,
    animate: Boolean = true,
) {
    val frame = if (animate && pose != OpponentPose.HIT) rememberPixelFrame() else null
    // each frame is built once and then only painted
    val frames = remember(opponent, pose) { arrayOfNulls<PixelImage>(OpponentPixels.FRAMES) }
    val description =
        stringResource(
            when (pose) {
                OpponentPose.IDLE -> Res.string.arena_opponent_idle
                OpponentPose.HIT -> Res.string.arena_opponent_hit
                OpponentPose.KO -> Res.string.arena_opponent_ko
            },
            opponent.label(),
        )

    Canvas(modifier = modifier.aspectRatio(1f).semantics { contentDescription = description }) {
        val f = frame?.value ?: 0
        val image = frames[f] ?: OpponentPixels.render(opponent, pose, f).also { frames[f] = it }
        drawPixels(image)
    }
}

/**
 * A ladder opponent that is still locked: its flat shape with a question mark, in the theme's
 * outline colour, so it fits light and dark mode and gives nothing away but the size.
 */
@Composable
fun LockedOpponent(opponent: Opponent, modifier: Modifier = Modifier) {
    val fill = MaterialTheme.colorScheme.outline.toArgb()
    val mark = MaterialTheme.colorScheme.surface.toArgb()
    val image = remember(opponent, fill, mark) { OpponentPixels.silhouette(opponent, fill, mark) }
    val description = stringResource(Res.string.arena_opponent_locked)

    Canvas(modifier = modifier.aspectRatio(1f).semantics { contentDescription = description }) {
        drawPixels(image)
    }
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview(widthDp = 400, heightDp = 820)
@Composable
private fun OpponentsPreview() {
    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Opponent.entries.forEach { opponent ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OpponentPose.entries.forEach { pose ->
                    OpponentAvatar(opponent, pose, Modifier.size(76.dp), animate = false)
                }
                LockedOpponent(opponent, Modifier.size(76.dp))
            }
        }
    }
}
