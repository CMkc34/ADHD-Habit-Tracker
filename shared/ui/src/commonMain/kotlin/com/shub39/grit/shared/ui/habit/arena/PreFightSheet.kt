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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shub39.grit.core.arena.ArenaState
import com.shub39.grit.core.arena.Opponent
import com.shub39.grit.core.avatar.AvatarState
import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.shared.ui.components.GritBottomSheet
import com.shub39.grit.shared.ui.habit.fighter.FighterAvatar
import com.shub39.grit.shared.ui.theme.flexFontEmphasis
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.arena_attack
import grit.shared.ui.generated.resources.arena_fight
import grit.shared.ui.generated.resources.arena_fight_no_ticket
import grit.shared.ui.generated.resources.arena_focus
import grit.shared.ui.generated.resources.arena_hp
import grit.shared.ui.generated.resources.arena_speed
import grit.shared.ui.generated.resources.arena_stronger
import grit.shared.ui.generated.resources.arena_vs
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The pre-fight sheet (#14): the fighter and [opponent] face each other, their HP, Attack, Speed
 * and Focus below with a green arrow at the stronger value of each row, and the Fight button that
 * uses one ticket. Without a ticket the button is disabled.
 */
@Composable
fun PreFightSheet(
    arena: ArenaState,
    avatar: AvatarState,
    opponent: Opponent,
    onFight: () -> Unit,
    onDismiss: () -> Unit,
) {
    val player = arena.player
    val them = opponent.combatant
    val canStart = arena.canStart(opponent)

    GritBottomSheet(onDismissRequest = onDismiss, padding = 24.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FighterAvatar(
                    character = avatar.data.character,
                    colors = avatar.data.colors,
                    rank = avatar.fighter.rank,
                    mood = FighterMood.READY,
                    outfit = avatar.data.outfit,
                    gear = avatar.data.accessories,
                    modifier = Modifier.size(112.dp),
                )
                Text(player.name, style = MaterialTheme.typography.titleSmall)
            }
            Text(
                text = stringResource(Res.string.arena_vs),
                style =
                    MaterialTheme.typography.headlineMedium.copy(fontFamily = flexFontEmphasis()),
                color = MaterialTheme.colorScheme.primary,
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OpponentAvatar(opponent, OpponentPose.IDLE, Modifier.size(112.dp))
                Text(opponent.label(), style = MaterialTheme.typography.titleSmall)
            }
        }

        StatRow(Res.string.arena_hp, player.maxHp, them.maxHp)
        StatRow(Res.string.arena_attack, player.attack, them.attack)
        StatRow(Res.string.arena_speed, player.speed, them.speed)
        StatRow(Res.string.arena_focus, player.focus, them.focus)

        Button(
            onClick = onFight,
            enabled = canStart,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) {
            Text(
                text =
                    stringResource(
                        if (arena.tickets.available > 0) Res.string.arena_fight
                        else Res.string.arena_fight_no_ticket
                    ),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

/** One value of both sides; the stronger one gets a green arrow, a tie none. */
@Composable
private fun StatRow(label: StringResource, mine: Int, theirs: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatValue(mine, stronger = mine > theirs, modifier = Modifier.weight(1f))
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(96.dp),
        )
        StatValue(theirs, stronger = theirs > mine, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatValue(value: Int, stronger: Boolean, modifier: Modifier = Modifier) {
    val arrow = if (stronger) " ▲" else ""
    val description = if (stronger) stringResource(Res.string.arena_stronger) else null
    Text(
        text = "$value$arrow",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = if (stronger) FontWeight.Bold else FontWeight.Normal,
        color = if (stronger) StrongerGreen else MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier =
            if (description == null) modifier
            else modifier.semantics { contentDescription = "$value, $description" },
    )
}

/** Green of the "stronger" arrow; readable on light and dark sheets. */
private val StrongerGreen = Color(0xFF3FA34D)
