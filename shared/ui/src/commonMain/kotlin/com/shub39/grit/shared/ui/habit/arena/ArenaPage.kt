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

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.shub39.grit.core.arena.ArenaRules
import com.shub39.grit.core.arena.ArenaState
import com.shub39.grit.core.arena.Combatant
import com.shub39.grit.core.arena.LadderEntry
import com.shub39.grit.core.arena.LadderStatus
import com.shub39.grit.core.arena.Opponent
import com.shub39.grit.core.arena.TicketStatus
import com.shub39.grit.core.avatar.AvatarData
import com.shub39.grit.core.avatar.AvatarState
import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.core.fighter.FighterState
import com.shub39.grit.shared.ui.GritPreviewWrapper
import com.shub39.grit.shared.ui.theme.flexFontEmphasis
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.arena_fight_no_ticket
import grit.shared.ui.generated.resources.arena_ladder_complete
import grit.shared.ui.generated.resources.arena_level
import grit.shared.ui.generated.resources.arena_next_ticket_hint
import grit.shared.ui.generated.resources.arena_no_tickets
import grit.shared.ui.generated.resources.arena_status_beaten
import grit.shared.ui.generated.resources.arena_status_next
import grit.shared.ui.generated.resources.arena_tab
import grit.shared.ui.generated.resources.arena_tickets
import grit.shared.ui.generated.resources.arena_to_routines
import grit.shared.ui.generated.resources.check_circle
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * The Arena tab (#14): today's tickets on top, then the ladder of 10 opponents from 1 to 10. Beaten
 * opponents can be fought again, the next one is framed and bobs, locked ones are silhouettes.
 * Tapping an opponent opens the pre-fight sheet; a started fight covers the whole screen. All state
 * comes from `ArenaViewModel`, every tap goes back to it as an [ArenaAction].
 */
@Composable
fun ArenaPage(
    state: ArenaPageState,
    onAction: (ArenaAction) -> Unit,
    onOpenRoutines: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val arena = state.arena
    val avatar = state.avatar
    if (arena == null || avatar == null) {
        Box(modifier = modifier.fillMaxSize())
        return
    }

    val listState = rememberLazyListState()
    val nextIndex = arena.ladder.indexOfFirst { it.status == LadderStatus.NEXT }
    // the header is item 0, so this shows the opponent before the next one, and the next below it
    LaunchedEffect(nextIndex) { if (nextIndex > 0) listState.animateScrollToItem(nextIndex) }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            state = listState,
            modifier = Modifier.widthIn(max = 600.dp).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "header") { TicketHeader(arena.tickets, onOpenRoutines = onOpenRoutines) }
            items(arena.ladder, key = { it.opponent.id }) { entry ->
                LadderCard(entry, onClick = { onAction(ArenaAction.Select(entry.opponent)) })
            }
            if (arena.isComplete) {
                item(key = "complete") {
                    Text(
                        text = stringResource(Res.string.arena_ladder_complete),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                    )
                }
            }
        }
    }

    state.selected?.let { opponent ->
        PreFightSheet(
            arena = arena,
            avatar = avatar,
            opponent = opponent,
            onFight = { onAction(ArenaAction.StartFight) },
            onDismiss = { onAction(ArenaAction.CloseSheet) },
        )
    }

    state.fight?.let { fight -> FightScreen(fight = fight, avatar = avatar, onAction = onAction) }
}

@Composable
private fun TicketHeader(tickets: TicketStatus, onOpenRoutines: () -> Unit) {
    Card(
        colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(Res.string.arena_tab),
                style = MaterialTheme.typography.headlineSmall.copy(fontFamily = flexFontEmphasis()),
            )
            Text(
                text =
                    stringResource(
                        Res.string.arena_tickets,
                        tickets.available,
                        ArenaRules.TICKETS_PER_DAY,
                    ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            when {
                tickets.available == 0 && tickets.showNextTicketHint -> {
                    Text(
                        text = stringResource(Res.string.arena_no_tickets),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                    OutlinedButton(onClick = onOpenRoutines) {
                        Text(stringResource(Res.string.arena_to_routines))
                    }
                }
                tickets.available == 0 ->
                    Text(
                        text = stringResource(Res.string.arena_fight_no_ticket),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                tickets.showNextTicketHint ->
                    Text(
                        text = stringResource(Res.string.arena_next_ticket_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
            }
        }
    }
}

@Composable
private fun LadderCard(entry: LadderEntry, onClick: () -> Unit) {
    val opponent = entry.opponent
    val next = entry.status == LadderStatus.NEXT
    val bob =
        if (next) {
            val transition = rememberInfiniteTransition(label = "next")
            val y by
                transition.animateFloat(
                    initialValue = 0f,
                    targetValue = -4f,
                    animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
                    label = "bob",
                )
            y
        } else 0f

    Card(
        onClick = onClick,
        enabled = entry.canFight,
        border = if (next) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (next) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.surfaceContainer,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "${opponent.number}",
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = flexFontEmphasis()),
                modifier = Modifier.widthIn(min = 28.dp),
                textAlign = TextAlign.Center,
            )
            if (entry.status == LadderStatus.LOCKED) {
                LockedOpponent(opponent, Modifier.size(64.dp))
            } else {
                OpponentAvatar(
                    opponent = opponent,
                    pose = OpponentPose.IDLE,
                    animate = next,
                    modifier = Modifier.size(64.dp).graphicsLayer { translationY = bob * density },
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text =
                        if (entry.status == LadderStatus.LOCKED) LOCKED_NAME else opponent.label(),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text =
                        when (entry.status) {
                            LadderStatus.BEATEN -> stringResource(Res.string.arena_status_beaten)
                            LadderStatus.NEXT -> stringResource(Res.string.arena_status_next)
                            LadderStatus.LOCKED ->
                                stringResource(Res.string.arena_level, opponent.level)
                        },
                    style = MaterialTheme.typography.bodySmall,
                    color =
                        if (next) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (entry.status == LadderStatus.BEATEN) {
                Icon(
                    painter = painterResource(Res.drawable.check_circle),
                    contentDescription = stringResource(Res.string.arena_status_beaten),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

private fun previewArena(available: Int, beaten: Int): ArenaState =
    ArenaState(
        ladder = ArenaState.ladder(Opponent.entries.take(beaten).toSet()),
        tickets = TicketStatus(earned = maxOf(available, 1), available = available),
        player = Combatant("Kai", maxHp = 90, attack = 12, speed = 3, focus = 3),
        today = LocalDate(2026, 11, 5),
    )

private val previewAvatar =
    AvatarState(
        fighter =
            FighterState(activeDays = 20, rank = FighterRank.BRAWLER, mood = FighterMood.READY),
        energy = 0,
        data = AvatarData(),
    )

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun ArenaPagePreview() {
    ArenaPage(
        state = ArenaPageState(arena = previewArena(2, 3), avatar = previewAvatar),
        onAction = {},
        onOpenRoutines = {},
    )
}

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun ArenaPageNoTicketsPreview() {
    ArenaPage(
        state = ArenaPageState(arena = previewArena(0, 0), avatar = previewAvatar),
        onAction = {},
        onOpenRoutines = {},
    )
}

// the same in every language, and kept out of the string resources, where Android reads a
// leading "?" as a theme attribute
private const val LOCKED_NAME = "???"
