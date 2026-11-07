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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shub39.grit.core.arena.ArenaRules
import com.shub39.grit.core.arena.BattleAction
import com.shub39.grit.core.arena.BattleEvent
import com.shub39.grit.core.arena.Outcome
import com.shub39.grit.core.arena.Side
import com.shub39.grit.core.avatar.AvatarState
import com.shub39.grit.shared.ui.habit.ui.component.ShakeText
import com.shub39.grit.shared.ui.theme.flexFontEmphasis
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.arena_banner_fight
import grit.shared.ui.generated.resources.arena_banner_round
import grit.shared.ui.generated.resources.arena_banner_round_lost
import grit.shared.ui.generated.resources.arena_banner_round_won
import grit.shared.ui.generated.resources.arena_banner_time_up
import grit.shared.ui.generated.resources.arena_best_of_three
import grit.shared.ui.generated.resources.arena_charge
import grit.shared.ui.generated.resources.arena_draw_text
import grit.shared.ui.generated.resources.arena_draw_title
import grit.shared.ui.generated.resources.arena_give_up
import grit.shared.ui.generated.resources.arena_give_up_text
import grit.shared.ui.generated.resources.arena_give_up_title
import grit.shared.ui.generated.resources.arena_guard
import grit.shared.ui.generated.resources.arena_guard_hint
import grit.shared.ui.generated.resources.arena_keep_fighting
import grit.shared.ui.generated.resources.arena_log_crit
import grit.shared.ui.generated.resources.arena_log_dodge
import grit.shared.ui.generated.resources.arena_log_guard
import grit.shared.ui.generated.resources.arena_log_guarded
import grit.shared.ui.generated.resources.arena_log_hit
import grit.shared.ui.generated.resources.arena_log_ko
import grit.shared.ui.generated.resources.arena_log_special
import grit.shared.ui.generated.resources.arena_log_time_up
import grit.shared.ui.generated.resources.arena_loss_text
import grit.shared.ui.generated.resources.arena_loss_title
import grit.shared.ui.generated.resources.arena_match_round
import grit.shared.ui.generated.resources.arena_result_ok
import grit.shared.ui.generated.resources.arena_reward
import grit.shared.ui.generated.resources.arena_round
import grit.shared.ui.generated.resources.arena_rounds_won
import grit.shared.ui.generated.resources.arena_special
import grit.shared.ui.generated.resources.arena_special_hint
import grit.shared.ui.generated.resources.arena_special_locked
import grit.shared.ui.generated.resources.arena_strike
import grit.shared.ui.generated.resources.arena_strike_hint
import grit.shared.ui.generated.resources.arena_turn_opponent
import grit.shared.ui.generated.resources.arena_turn_player
import grit.shared.ui.generated.resources.arena_unlocked
import grit.shared.ui.generated.resources.arena_win_text
import grit.shared.ui.generated.resources.arena_win_title
import grit.shared.ui.generated.resources.close
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** How long the "Round n, Fight!" banner shows; with animations off it is skipped. */
private const val INTRO_MILLIS = 1400

/** How long the end of a round shows before the next one starts. */
private const val ROUND_END_MILLIS = 1800

/**
 * The full-screen fight (#14): the fighter on the left, the opponent on the right on their
 * [ArenaStage], each with an HP bar, 3 charge pips and their won rounds of the best of three, the
 * last log lines, and the three moves. The event that is playing is animated for
 * [FightScene.durationOf] and then reported back with [ArenaAction.EventPlayed]; the phone vibrates
 * as a blow lands. Each round opens with a banner and ends with one. Back asks "Give up?"; at the
 * end the result covers the screen.
 */
@Composable
fun FightScreen(fight: FightState, avatar: AvatarState, onAction: (ArenaAction) -> Unit) {
    val act by rememberUpdatedState(onAction)
    val haptics = LocalHapticFeedback.current
    val index = fight.playback.playingIndex
    val event = fight.playback.playing
    val round = fight.match.round
    // a fresh clock per event, so a new event never shows a frame of the last one's end
    val clock = remember(index, round) { Animatable(0f) }
    LaunchedEffect(index, round, fight.intro) {
        if (index < 0 || fight.intro) return@LaunchedEffect
        val duration = FightScene.durationOf(event)
        val impact = FightScene.impactAt(event)
        if (impact != null) {
            clock.animateTo(impact.toFloat(), tween(impact, easing = LinearEasing))
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        val left = duration - (impact ?: 0)
        clock.animateTo(duration.toFloat(), tween(left, easing = LinearEasing))
        act(ArenaAction.EventPlayed(index))
    }

    // the round banners: "Round n, Fight!" at the start, won or lost at the end
    val banner = remember { Animatable(0f) }
    LaunchedEffect(round, fight.intro) {
        if (!fight.intro) return@LaunchedEffect
        banner.snapTo(0f)
        banner.animateTo(1f, tween(INTRO_MILLIS, easing = LinearEasing))
        act(ArenaAction.IntroShown)
    }
    LaunchedEffect(round, fight.roundOver, fight.askingGiveUp) {
        if (!fight.roundOver || fight.askingGiveUp) return@LaunchedEffect
        banner.snapTo(0f)
        banner.animateTo(1f, tween(ROUND_END_MILLIS, easing = LinearEasing))
        act(ArenaAction.NextRound)
    }

    val data = avatar.data
    val rank = avatar.fighter.rank
    val sprites =
        remember(data.character, rank, data.colors, data.outfit, data.accessories, fight.opponent) {
            FightSprites(
                data.character,
                rank,
                data.colors,
                data.outfit,
                data.accessories,
                fight.opponent,
            )
        }
    val millis = if (index < 0 || fight.intro) 0 else clock.value.toInt()
    val scene = FightScene.of(fight, millis, sprites.geometry)
    val playerName = fight.started.player.name
    val opponentName = fight.opponent.label()
    val turn =
        when {
            scene.opponentTurn -> stringResource(Res.string.arena_turn_opponent)
            fight.canAct -> stringResource(Res.string.arena_turn_player)
            else -> null
        }
    val stageBanner =
        when {
            fight.intro ->
                if (banner.value < 0.6f) {
                    StageBanner(
                        stringResource(Res.string.arena_banner_round, round),
                        stringResource(Res.string.arena_best_of_three),
                    )
                } else StageBanner(stringResource(Res.string.arena_banner_fight))
            fight.roundOver ->
                when {
                    fight.roundWinner == Side.PLAYER ->
                        StageBanner(stringResource(Res.string.arena_banner_round_won))
                    fight.battle.log.lastOrNull() == BattleEvent.TimeUp && banner.value < 0.5f ->
                        StageBanner(stringResource(Res.string.arena_banner_time_up))
                    else -> StageBanner(stringResource(Res.string.arena_banner_round_lost))
                }
            else -> null
        }

    // read in the app window: the full-screen dialog draws under the status bar but does not get
    // the insets itself, so safeDrawingPadding inside it was 0 on the Pixel (#18)
    val safeArea = WindowInsets.safeDrawing.asPaddingValues()

    Dialog(
        onDismissRequest = { act(ArenaAction.AskGiveUp) },
        properties =
            DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false),
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                Column(
                    modifier =
                        Modifier.widthIn(max = 640.dp)
                            .fillMaxSize()
                            .padding(safeArea)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(Res.string.arena_match_round, round),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text =
                                    stringResource(
                                        Res.string.arena_round,
                                        fight.battle.round,
                                        ArenaRules.MAX_ROUNDS,
                                    ),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = { act(ArenaAction.AskGiveUp) }) {
                            Icon(
                                painter = painterResource(Res.drawable.close),
                                contentDescription = stringResource(Res.string.arena_give_up),
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        CornerPanel(
                            name = playerName,
                            hp = fight.hp(Side.PLAYER),
                            maxHp = fight.battle.player.combatant.maxHp,
                            charge = fight.charge(Side.PLAYER),
                            wins = fight.wins(Side.PLAYER),
                            alignEnd = false,
                            modifier = Modifier.weight(1f),
                        )
                        CornerPanel(
                            name = opponentName,
                            hp = fight.hp(Side.OPPONENT),
                            maxHp = fight.battle.opponent.combatant.maxHp,
                            charge = fight.charge(Side.OPPONENT),
                            wins = fight.wins(Side.OPPONENT),
                            alignEnd = true,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    FightStage(
                        fight = fight,
                        scene = scene,
                        sprites = sprites,
                        millis = millis,
                        turn = turn,
                        banner = stageBanner,
                    )

                    LogBox(fight.logLines, playerName, opponentName)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MoveButton(
                            title = Res.string.arena_strike,
                            hint = Res.string.arena_strike_hint,
                            enabled = fight.canAct,
                            onClick = { act(ArenaAction.Act(BattleAction.STRIKE)) },
                            modifier = Modifier.weight(1f),
                        )
                        MoveButton(
                            title = Res.string.arena_guard,
                            hint = Res.string.arena_guard_hint,
                            enabled = fight.canAct,
                            onClick = { act(ArenaAction.Act(BattleAction.GUARD)) },
                            modifier = Modifier.weight(1f),
                        )
                        MoveButton(
                            title = Res.string.arena_special,
                            hint =
                                if (fight.battle.player.specialReady) Res.string.arena_special_hint
                                else Res.string.arena_special_locked,
                            enabled = fight.canSpecial,
                            onClick = { act(ArenaAction.Act(BattleAction.SPECIAL)) },
                            modifier = Modifier.weight(1f),
                            special = true,
                        )
                    }
                }

                fight.result?.let { result ->
                    ResultOverlay(
                        result = result,
                        opponentName = opponentName,
                        onClose = { act(ArenaAction.CloseFight) },
                    )
                }
            }
        }

        if (fight.askingGiveUp) {
            AlertDialog(
                onDismissRequest = { act(ArenaAction.KeepFighting) },
                title = { Text(stringResource(Res.string.arena_give_up_title)) },
                text = { Text(stringResource(Res.string.arena_give_up_text)) },
                confirmButton = {
                    TextButton(onClick = { act(ArenaAction.KeepFighting) }) {
                        Text(stringResource(Res.string.arena_keep_fighting))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { act(ArenaAction.GiveUp) }) {
                        Text(stringResource(Res.string.arena_give_up))
                    }
                },
            )
        }
    }
}

/** Name with the won rounds, HP bar with "hp / max" and the charge pips of one side. */
@Composable
private fun CornerPanel(
    name: String,
    hp: Int,
    maxHp: Int,
    charge: Int,
    wins: Int,
    alignEnd: Boolean,
    modifier: Modifier = Modifier,
) {
    val share by animateFloatAsState(hp / maxHp.toFloat(), label = "hp")
    val barColor =
        when {
            share > 0.5f -> Color(0xFF4FB35A)
            share > 0.25f -> Color(0xFFF5C542)
            else -> Color(0xFFE0504A)
        }
    val chargeText = stringResource(Res.string.arena_charge, charge, ArenaRules.MAX_CHARGE)
    val winsText = stringResource(Res.string.arena_rounds_won, wins)

    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val marks: @Composable () -> Unit = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.semantics { contentDescription = winsText },
                ) {
                    repeat(ArenaRules.ROUNDS_TO_WIN) { i ->
                        Box(
                            modifier =
                                Modifier.size(10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (i < wins) Color(0xFFFFD34A)
                                        else MaterialTheme.colorScheme.surfaceContainerHighest
                                    )
                        )
                    }
                }
            }
            if (alignEnd) marks()
            Text(
                name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (!alignEnd) marks()
        }
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            Box(
                modifier =
                    Modifier.fillMaxHeight()
                        .fillMaxWidth(share.coerceIn(0f, 1f))
                        .align(if (alignEnd) Alignment.CenterEnd else Alignment.CenterStart)
                        .background(barColor)
            )
        }
        Text(
            text = "$hp / $maxHp",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.semantics { contentDescription = chargeText },
        ) {
            repeat(ArenaRules.MAX_CHARGE) { i ->
                Box(
                    modifier =
                        Modifier.size(12.dp)
                            .background(
                                if (i < charge) MaterialTheme.colorScheme.tertiary
                                else MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                )
            }
        }
    }
}

/** The last [FightState.LOG_LINES] events, newest at the bottom. */
@Composable
private fun LogBox(lines: List<BattleEvent>, playerName: String, opponentName: String) {
    Card(
        colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Bottom),
        ) {
            lines.forEachIndexed { i, event ->
                val newest = i == lines.lastIndex
                Text(
                    text = logLine(event, playerName, opponentName),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (newest) FontWeight.Bold else FontWeight.Normal,
                    color =
                        if (newest) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** One line of the fight log, e.g. "Crit! Kai deals 12 damage" or "Dodged! Kai misses". */
@Composable
fun logLine(event: BattleEvent, playerName: String, opponentName: String): String {
    fun name(side: Side) = if (side == Side.PLAYER) playerName else opponentName
    return when (event) {
        is BattleEvent.Hit ->
            when {
                event.critical ->
                    stringResource(Res.string.arena_log_crit, name(event.actor), event.damage)
                event.guarded ->
                    stringResource(Res.string.arena_log_guarded, name(event.actor), event.damage)
                else -> stringResource(Res.string.arena_log_hit, name(event.actor), event.damage)
            }
        is BattleEvent.Dodge -> stringResource(Res.string.arena_log_dodge, name(event.actor))
        is BattleEvent.Guard -> stringResource(Res.string.arena_log_guard, name(event.actor))
        is BattleEvent.Special ->
            stringResource(Res.string.arena_log_special, name(event.actor), event.damage)
        // the actor knocked the other side out
        is BattleEvent.Ko -> stringResource(Res.string.arena_log_ko, name(event.actor.other))
        BattleEvent.TimeUp -> stringResource(Res.string.arena_log_time_up)
    }
}

@Composable
private fun MoveButton(
    title: StringResource,
    hint: StringResource,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    special: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors =
            if (special)
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                )
            else ButtonDefaults.buttonColors(),
        modifier = modifier.heightIn(min = 88.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(hint),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** The result over the fight: confetti for a win, warm words for a loss or a draw. */
@Composable
private fun ResultOverlay(result: FightResult, opponentName: String, onClose: () -> Unit) {
    Box(
        modifier =
            Modifier.fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
        contentAlignment = Alignment.Center,
    ) {
        if (result.outcome == Outcome.WIN) Confetti()
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.padding(24.dp).widthIn(max = 400.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val (title, text) =
                    when (result.outcome) {
                        Outcome.WIN ->
                            stringResource(Res.string.arena_win_title) to
                                stringResource(Res.string.arena_win_text, opponentName)
                        Outcome.LOSS ->
                            stringResource(Res.string.arena_loss_title) to
                                stringResource(Res.string.arena_loss_text)
                        Outcome.DRAW ->
                            stringResource(Res.string.arena_draw_title) to
                                stringResource(Res.string.arena_draw_text)
                    }
                Text(
                    text = title,
                    style =
                        MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = flexFontEmphasis()
                        ),
                    textAlign = TextAlign.Center,
                )
                Text(text = text, textAlign = TextAlign.Center)
                result.unlocked?.let {
                    Text(
                        text = stringResource(Res.string.arena_unlocked, it.label()),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                    )
                }
                if (result.shakes > 0) {
                    ShakeText(
                        text = stringResource(Res.string.arena_reward, result.shakes),
                        style =
                            MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                    )
                }
                Button(onClick = onClose, modifier = Modifier.padding(top = 8.dp)) {
                    Text(stringResource(Res.string.arena_result_ok))
                }
            }
        }
    }
}

/** Square pixel confetti in the theme's colours, falling and flipping. */
@Composable
private fun Confetti() {
    val colors =
        listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.tertiaryContainer,
        )
    val transition = rememberInfiniteTransition(label = "confetti")
    val time by
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
            label = "fall",
        )
    Canvas(modifier = Modifier.fillMaxSize()) {
        val piece = 6.dp.toPx()
        repeat(CONFETTI_PIECES) { i ->
            val speed = 0.7f + (i * 7 % 10) / 10f
            val x = (i * 37 % 100) / 100f * size.width
            val y = ((time * speed + i * 0.137f) % 1f) * (size.height + 2 * piece) - piece
            val flip = abs(cos((time * speed * 12f + i) * PI.toFloat()))
            drawRect(
                color = colors[i % colors.size],
                topLeft = Offset(x, y),
                size = Size(piece * (0.3f + 0.7f * flip), piece),
            )
        }
    }
}

private const val CONFETTI_PIECES = 60
