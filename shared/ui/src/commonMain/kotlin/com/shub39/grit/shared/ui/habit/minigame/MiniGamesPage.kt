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
package com.shub39.grit.shared.ui.habit.minigame

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.shub39.grit.core.avatar.AvatarData
import com.shub39.grit.core.avatar.AvatarState
import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.core.fighter.FighterState
import com.shub39.grit.core.minigame.GameStats
import com.shub39.grit.core.minigame.MiniGame
import com.shub39.grit.core.minigame.MiniGameEntry
import com.shub39.grit.core.minigame.MiniGameRules
import com.shub39.grit.core.minigame.MiniGameState
import com.shub39.grit.shared.ui.GritPreviewWrapper
import com.shub39.grit.shared.ui.LocalWindowSizeClass
import com.shub39.grit.shared.ui.WindowSize.Companion.isExpanded
import com.shub39.grit.shared.ui.habit.fighter.FighterAvatar
import com.shub39.grit.shared.ui.habit.fighter.FighterPose
import com.shub39.grit.shared.ui.habit.fighter.StatIcon
import com.shub39.grit.shared.ui.habit.fighter.label
import com.shub39.grit.shared.ui.theme.flexFontEmphasis
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.game_board_break
import grit.shared.ui.generated.resources.game_board_break_text
import grit.shared.ui.generated.resources.game_clutter_smash
import grit.shared.ui.generated.resources.game_clutter_smash_text
import grit.shared.ui.generated.resources.game_ki_barrage
import grit.shared.ui.generated.resources.game_ki_barrage_text
import grit.shared.ui.generated.resources.games_best
import grit.shared.ui.generated.resources.games_coming_soon
import grit.shared.ui.generated.resources.games_helped_by
import grit.shared.ui.generated.resources.games_intro
import grit.shared.ui.generated.resources.games_no_best
import grit.shared.ui.generated.resources.games_no_runs
import grit.shared.ui.generated.resources.games_play
import grit.shared.ui.generated.resources.games_runs_left
import grit.shared.ui.generated.resources.games_tab
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The Games tab (SPEC 10, #41): one card per mini-game with today's free runs, the best score and
 * the stats that help. All state comes from `MiniGamesViewModel`, every tap goes back to it as a
 * [MiniGamesAction]. A started run covers the whole screen with its game (Ki Barrage since #42).
 */
@Composable
fun MiniGamesPage(
    state: MiniGamesPageState,
    onAction: (MiniGamesAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val games = state.games
    val avatar = state.avatar
    if (games == null || avatar == null) {
        Box(modifier = modifier.fillMaxSize())
        return
    }

    // the tab draws edge to edge, so the header starts below the status bar, like the Arena tab
    val statusBar =
        if (LocalWindowSizeClass.current.isExpanded()) 0.dp
        else WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.widthIn(max = 600.dp).fillMaxSize(),
            contentPadding =
                PaddingValues(start = 16.dp, top = 16.dp + statusBar, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "header") { GamesHeader() }
            items(games.games, key = { it.game.id }) { entry ->
                GameCard(
                    entry = entry,
                    avatar = avatar,
                    playable = entry.game in PLAYABLE_GAMES,
                    onPlay = { onAction(MiniGamesAction.Start(entry.game)) },
                )
            }
        }
    }

    state.run?.let { run ->
        when (run.game) {
            MiniGame.KI_BARRAGE ->
                KiBarrageScreen(
                    run = run,
                    avatar = avatar,
                    // the best before this run, so the end card can tell a new one
                    best = remember(run.started) { games.entry(MiniGame.KI_BARRAGE).best },
                    onAction = onAction,
                )
            // no screen yet, so no run can start (PLAYABLE_GAMES)
            MiniGame.CLUTTER_SMASH,
            MiniGame.BOARD_BREAK -> Unit
        }
    }
}

@Composable
private fun GamesHeader() {
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
                text = stringResource(Res.string.games_tab),
                style = MaterialTheme.typography.headlineSmall.copy(fontFamily = flexFontEmphasis()),
            )
            Text(
                text = stringResource(Res.string.games_intro),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun GameCard(
    entry: MiniGameEntry,
    avatar: AvatarState,
    playable: Boolean,
    onPlay: () -> Unit,
) {
    val game = entry.game
    Card(
        colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FighterAvatar(
                    character = avatar.data.character,
                    colors = avatar.data.colors,
                    rank = avatar.fighter.rank,
                    mood = FighterMood.READY,
                    outfit = avatar.data.outfit,
                    gear = avatar.data.accessories,
                    pose = game.pose,
                    animate = false,
                    modifier = Modifier.size(72.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(game.title),
                        style =
                            MaterialTheme.typography.titleLarge.copy(
                                fontFamily = flexFontEmphasis()
                            ),
                    )
                    Text(
                        text = stringResource(game.text),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            FlowRow(
                itemVerticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(Res.string.games_helped_by),
                    style = MaterialTheme.typography.labelMedium,
                )
                game.stats.forEach { stat ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        StatIcon(stat, Modifier.size(20.dp))
                        Text(text = stat.label(), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val perDay = MiniGameRules.RUNS_PER_DAY
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text =
                            if (entry.canStart)
                                stringResource(Res.string.games_runs_left, entry.runsLeft, perDay)
                            else stringResource(Res.string.games_no_runs, perDay),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text =
                            if (entry.best > 0) stringResource(Res.string.games_best, entry.best)
                            else stringResource(Res.string.games_no_best),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (playable) {
                    Button(onClick = onPlay, enabled = entry.canStart) {
                        Text(stringResource(Res.string.games_play))
                    }
                } else {
                    OutlinedButton(onClick = {}, enabled = false) {
                        Text(stringResource(Res.string.games_coming_soon))
                    }
                }
            }
        }
    }
}

private val MiniGame.title: StringResource
    get() =
        when (this) {
            MiniGame.KI_BARRAGE -> Res.string.game_ki_barrage
            MiniGame.CLUTTER_SMASH -> Res.string.game_clutter_smash
            MiniGame.BOARD_BREAK -> Res.string.game_board_break
        }

private val MiniGame.text: StringResource
    get() =
        when (this) {
            MiniGame.KI_BARRAGE -> Res.string.game_ki_barrage_text
            MiniGame.CLUTTER_SMASH -> Res.string.game_clutter_smash_text
            MiniGame.BOARD_BREAK -> Res.string.game_board_break_text
        }

/** The fighter's pose on the card: firing ki, punching junk, gathering ki for the strike. */
private val MiniGame.pose: FighterPose
    get() =
        when (this) {
            MiniGame.KI_BARRAGE -> FighterPose.BLAST
            MiniGame.CLUTTER_SMASH -> FighterPose.PUNCH
            MiniGame.BOARD_BREAK -> FighterPose.CHARGE
        }

@PreviewWrapper(GritPreviewWrapper::class)
@Preview
@Composable
private fun MiniGamesPagePreview() {
    MiniGamesPage(
        state =
            MiniGamesPageState(
                games =
                    MiniGameState(
                        games =
                            listOf(
                                MiniGameEntry(MiniGame.KI_BARRAGE, runsLeft = 2, best = 750),
                                MiniGameEntry(MiniGame.CLUTTER_SMASH, runsLeft = 0, best = 830),
                                MiniGameEntry(MiniGame.BOARD_BREAK, runsLeft = 3, best = 0),
                            ),
                        stats = GameStats(),
                        today = LocalDate(2026, 11, 15),
                    ),
                avatar =
                    AvatarState(
                        fighter =
                            FighterState(
                                activeDays = 20,
                                rank = FighterRank.BRAWLER,
                                mood = FighterMood.READY,
                            ),
                        energy = 0,
                        data = AvatarData(),
                    ),
            ),
        onAction = {},
    )
}
