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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shub39.grit.core.avatar.AvatarState
import com.shub39.grit.core.avatar.Weather
import com.shub39.grit.core.fighter.FighterCharacter
import com.shub39.grit.core.fighter.FighterColors
import com.shub39.grit.core.fighter.FighterItems
import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.core.minigame.boardbreak.BoardBreak
import com.shub39.grit.core.minigame.boardbreak.BoardBreakConfig
import com.shub39.grit.core.minigame.boardbreak.BoardBreakEvent
import com.shub39.grit.core.minigame.boardbreak.BoardBreakPhase
import com.shub39.grit.core.minigame.boardbreak.StrikeMaterial
import com.shub39.grit.core.minigame.boardbreak.StrikeOutcome
import com.shub39.grit.core.minigame.boardbreak.StrikeRating
import com.shub39.grit.shared.ui.habit.fighter.FighterPixels
import com.shub39.grit.shared.ui.habit.fighter.FighterPose
import com.shub39.grit.shared.ui.habit.fighter.PixelImage
import com.shub39.grit.shared.ui.habit.fighter.TrainingGroundPixels
import com.shub39.grit.shared.ui.habit.fighter.drawPixels
import com.shub39.grit.shared.ui.habit.fighter.rememberPixelFrame
import com.shub39.grit.shared.ui.theme.flexFontEmphasis
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.boardbreak_boards
import grit.shared.ui.generated.resources.boardbreak_bricks
import grit.shared.ui.generated.resources.boardbreak_broken
import grit.shared.ui.generated.resources.boardbreak_good
import grit.shared.ui.generated.resources.boardbreak_great
import grit.shared.ui.generated.resources.boardbreak_hint
import grit.shared.ui.generated.resources.boardbreak_hold
import grit.shared.ui.generated.resources.boardbreak_ice
import grit.shared.ui.generated.resources.boardbreak_meter
import grit.shared.ui.generated.resources.boardbreak_miss
import grit.shared.ui.generated.resources.boardbreak_perfect
import grit.shared.ui.generated.resources.boardbreak_release
import grit.shared.ui.generated.resources.boardbreak_strike
import grit.shared.ui.generated.resources.boardbreak_total_broken
import grit.shared.ui.generated.resources.close
import grit.shared.ui.generated.resources.kibarrage_back
import grit.shared.ui.generated.resources.kibarrage_best
import grit.shared.ui.generated.resources.kibarrage_end
import grit.shared.ui.generated.resources.kibarrage_end_text
import grit.shared.ui.generated.resources.kibarrage_end_title
import grit.shared.ui.generated.resources.kibarrage_keep_playing
import grit.shared.ui.generated.resources.kibarrage_new_best
import grit.shared.ui.generated.resources.kibarrage_over
import grit.shared.ui.generated.resources.kibarrage_points
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private const val DASH_SECONDS = 0.10f
private const val IMPACT_FLASH_SECONDS = 0.24f

private val DOJO_EDGE = Color(0xFF1A222D)
private val METER_BLACK = Color(0xFF141822)
private val METER_YELLOW = Color(0xFFF2B830)
private val METER_GREEN = Color(0xFF38C864)
private val METER_BRIGHT = Color(0xFFE8FF9E)
private val METER_BORDER = Color(0xFF384256)
private val TEXT_SHADOW = Shadow(Color(0xFF1E1628), Offset(3f, 3f), 3f)

/**
 * The full-screen run of Board Break (#44): the [BoardBreak] simulation runs on the Training Ground
 * stage ([TrainingGroundPixels]) with a breaking stand next to the fighter. A run has 3 strikes:
 * boards, bricks, then ice. Holding anywhere puts the fighter in the `CHARGE` pose while a needle
 * sweeps across the timing meter (black / yellow / green / bright center); releasing dashes the
 * fighter in with a punch, rates the strike (`PERFECT` / `GREAT` / `GOOD` / `MISS`), splits the
 * broken layers apart, and shows `"n / m broken"`.
 */
@Composable
fun BoardBreakScreen(
    run: RunState,
    avatar: AvatarState,
    best: Int,
    onAction: (MiniGamesAction) -> Unit,
) {
    val act by rememberUpdatedState(onAction)
    val haptics = LocalHapticFeedback.current
    val game = remember(run.started) { BoardBreak(BoardBreakConfig.of(run.started.stats)) }
    val data = avatar.data
    val rank = avatar.fighter.rank
    val sprites =
        remember(data.character, rank, data.colors, data.outfit, data.accessories) {
            BoardBreakSprites(data.character, rank, data.colors, data.outfit, data.accessories)
        }
    val pixelFrame = rememberPixelFrame()

    var clock by remember { mutableFloatStateOf(0f) }
    var hud by remember { mutableStateOf(BoardBreakHud.of(game)) }
    var animationsEnabled by remember { mutableStateOf(true) }
    var askingEnd by remember { mutableStateOf(false) }
    var ended by remember { mutableStateOf(false) }
    val over = hud.over || ended

    fun handleEvents(events: List<BoardBreakEvent>) {
        for (event in events) {
            when (event) {
                BoardBreakEvent.ChargeStarted ->
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                is BoardBreakEvent.StrikeLanded ->
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                is BoardBreakEvent.NextStrike,
                is BoardBreakEvent.RunComplete -> Unit
            }
        }
        clock = game.time
        hud = BoardBreakHud.of(game)
    }

    LaunchedEffect(game) {
        val scale = coroutineContext[MotionDurationScale]?.scaleFactor ?: 1f
        animationsEnabled = scale > 0f
        var last = -1L
        while (!game.isOver && !ended) {
            withFrameNanos { now ->
                val seconds =
                    if (last < 0) 0f else ((now - last) / 1_000_000_000f).coerceAtMost(0.25f)
                last = now
                if (askingEnd) return@withFrameNanos
                game.step(seconds)
                handleEvents(game.events)
            }
        }
    }
    LaunchedEffect(over) { if (over) act(MiniGamesAction.Finish(game.score)) }

    val safeArea = WindowInsets.safeDrawing.asPaddingValues()

    Dialog(
        onDismissRequest = { if (over) act(MiniGamesAction.Close) else askingEnd = true },
        properties =
            DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false),
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = DOJO_EDGE) {
            Column(
                modifier =
                    Modifier.fillMaxSize()
                        .padding(safeArea)
                        .holdToCharge(
                            onPress = { if (game.startCharge()) handleEvents(game.events) },
                            onRelease = {
                                if (game.releaseCharge() != null) handleEvents(game.events)
                            },
                        ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BoardBreakHudRow(
                    hud = hud,
                    best = best,
                    onEnd = { if (over) act(MiniGamesAction.Close) else askingEnd = true },
                )
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    BoardBreakField(
                        game = game,
                        sprites = sprites,
                        clock = { clock },
                        idleFrame = pixelFrame,
                        animate = animationsEnabled,
                        modifier = Modifier.aspectRatio(BoardBreak.WIDTH / BoardBreak.HEIGHT),
                        overlay = { BoardBreakOverlay(hud = hud) },
                    )
                }
                BoardBreakMeter(
                    config = game.config,
                    needle = hud.needle,
                    phase = hud.phase,
                    modifier =
                        Modifier.widthIn(max = 520.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                )
                Text(
                    text = stringResource(Res.string.boardbreak_hint),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            if (over) {
                BoardBreakEndOverlay(
                    hud = hud,
                    result = run.result,
                    onClose = { act(MiniGamesAction.Close) },
                )
            }
        }

        if (askingEnd && !over) {
            AlertDialog(
                onDismissRequest = { askingEnd = false },
                title = { Text(stringResource(Res.string.kibarrage_end_title)) },
                text = { Text(stringResource(Res.string.kibarrage_end_text)) },
                confirmButton = {
                    TextButton(onClick = { askingEnd = false }) {
                        Text(stringResource(Res.string.kibarrage_keep_playing))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            askingEnd = false
                            ended = true
                        }
                    ) {
                        Text(stringResource(Res.string.kibarrage_end))
                    }
                },
            )
        }
    }
}

private data class BoardBreakHud(
    val score: Int,
    val maxPossible: Int,
    val strikeNumber: Int,
    val material: StrikeMaterial,
    val totalLayers: Int,
    val phase: BoardBreakPhase,
    val needle: Float,
    val lastOutcome: StrikeOutcome?,
    val over: Boolean,
) {
    companion object {
        fun of(game: BoardBreak): BoardBreakHud =
            BoardBreakHud(
                score = game.score,
                maxPossible = game.config.maxPossibleScore,
                strikeNumber = game.strikeNumber,
                material = game.currentMaterial,
                totalLayers = game.currentTotalLayers,
                phase = game.phase,
                needle = game.needle,
                lastOutcome = game.lastOutcome,
                over = game.isOver,
            )
    }
}

@Composable
private fun BoardBreakHudRow(hud: BoardBreakHud, best: Int, onEnd: () -> Unit) {
    Row(
        modifier = Modifier.widthIn(max = 640.dp).fillMaxWidth().padding(start = 16.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = hud.score.toString().padStart(2, '0'),
                    style =
                        MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = flexFontEmphasis()
                        ),
                    color = Color(0xFFFFF0C8),
                )
                Text(
                    text =
                        stringResource(
                            Res.string.boardbreak_strike,
                            hud.strikeNumber,
                            BoardBreak.STRIKES_PER_RUN,
                        ) + " · " + stringResource(hud.material.label),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE8F0FF),
                )
                Box(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(Res.string.kibarrage_best, maxOf(best, hud.score)),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFBEBEDC),
                )
            }
        }
        IconButton(onClick = onEnd) {
            Icon(
                painter = painterResource(Res.drawable.close),
                contentDescription = stringResource(Res.string.kibarrage_end),
                tint = Color.White,
            )
        }
    }
}

@Composable
private fun BoardBreakField(
    game: BoardBreak,
    sprites: BoardBreakSprites,
    clock: () -> Float,
    idleFrame: State<Int>,
    animate: Boolean,
    modifier: Modifier = Modifier,
    overlay: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit,
) {
    Box(modifier = modifier.clipToBounds()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val time = clock()
            val px = size.width / BoardBreak.WIDTH
            val frame = if (animate) idleFrame.value else 0
            fun at(x: Float, y: Float) = Offset(floor(x) * px, floor(y) * px)

            val skyFrame =
                if (animate) TrainingGroundPixels.SkyFrame.at(time)
                else TrainingGroundPixels.SkyFrame.STILL
            drawPixels(sprites.sky(skyFrame), Offset.Zero, px)
            drawPixels(
                sprites.ground,
                at(0f, BoardBreak.HEIGHT - TrainingGroundPixels.GROUND_HEIGHT),
                px,
            )

            // Twin-post breaking stand on the dojo floor
            val standTop = BoardBreak.FLOOR_Y - BoardBreakPixels.STAND_BASE_HEIGHT
            drawPixels(sprites.stand, at(BoardBreak.STAND_X, standTop), px)

            val material = game.currentMaterial
            val total = game.currentTotalLayers
            val stackX =
                BoardBreak.STAND_X +
                    (BoardBreakPixels.STAND_WIDTH - BoardBreakPixels.LAYER_WIDTH) / 2f
            val stackBottomY = BoardBreak.FLOOR_Y - 13f

            val striking =
                game.phase == BoardBreakPhase.STRIKING || game.phase == BoardBreakPhase.OVER
            val outcome = game.lastOutcome
            val intactCount =
                if (striking && outcome != null) (outcome.total - outcome.broken).coerceAtLeast(0)
                else total

            val layerSprite = sprites.layer(material)
            for (i in 0 until intactCount) {
                val ly =
                    stackBottomY - i * BoardBreakPixels.LAYER_STEP_Y - BoardBreakPixels.LAYER_HEIGHT
                drawPixels(layerSprite, at(stackX, ly), px)
            }

            if (striking && outcome != null && outcome.broken > 0) {
                val age = (time - game.strikeStartedAt).coerceAtLeast(0f)
                val t =
                    if (animate) (age / BoardBreak.STRIKE_SETTLE_SECONDS).coerceIn(0f, 1f)
                    else 0.35f
                val leftHalf = sprites.halfLayer(material, rightHalf = false)
                val rightHalf = sprites.halfLayer(material, rightHalf = true)
                for (b in 0 until outcome.broken) {
                    val layerIdx = intactCount + b
                    val baseY =
                        stackBottomY -
                            layerIdx * BoardBreakPixels.LAYER_STEP_Y -
                            BoardBreakPixels.LAYER_HEIGHT
                    val spread = (8f + (b + 1) * 3.5f) * t
                    val drop = -8f * t + 26f * t * t
                    drawPixels(leftHalf, at(stackX - spread, baseY + drop), px)
                    drawPixels(
                        rightHalf,
                        at(
                            stackX + BoardBreakPixels.LAYER_WIDTH - rightHalf.width + spread,
                            baseY + drop,
                        ),
                        px,
                    )
                }

                val impactCenterX = stackX + BoardBreakPixels.LAYER_WIDTH / 2f
                val impactCenterY =
                    stackBottomY -
                        (intactCount + outcome.broken / 2f) * BoardBreakPixels.LAYER_STEP_Y
                if (age <= IMPACT_FLASH_SECONDS || !animate) {
                    drawPixels(
                        sprites.impactBurst,
                        at(
                            impactCenterX - sprites.impactBurst.width / 2f,
                            impactCenterY - sprites.impactBurst.height / 2f,
                        ),
                        px,
                    )
                }
                if (animate) {
                    drawSplinters(
                        centerX = impactCenterX,
                        centerY = impactCenterY,
                        color = Color(BoardBreakPixels.color(material)),
                        age = age,
                        seed = outcome.strikeNumber * 37 + outcome.broken,
                        px = px,
                    )
                }
            }

            val pose =
                when (game.phase) {
                    BoardBreakPhase.READY -> FighterPose.STANCE
                    BoardBreakPhase.CHARGING -> FighterPose.CHARGE
                    BoardBreakPhase.STRIKING,
                    BoardBreakPhase.OVER -> FighterPose.PUNCH
                }
            val fighterX =
                when (game.phase) {
                    BoardBreakPhase.READY,
                    BoardBreakPhase.CHARGING -> BoardBreak.FIGHTER_REST_X
                    BoardBreakPhase.STRIKING,
                    BoardBreakPhase.OVER -> {
                        val age = (time - game.strikeStartedAt).coerceAtLeast(0f)
                        val dash = if (animate) (age / DASH_SECONDS).coerceIn(0f, 1f) else 1f
                        BoardBreak.FIGHTER_REST_X +
                            (BoardBreak.FIGHTER_STRIKE_X - BoardBreak.FIGHTER_REST_X) * dash
                    }
                }
            drawPixels(sprites.fighter(pose, frame), at(fighterX, BoardBreak.FIGHTER_TOP), px)
        }
        overlay()
    }
}

private fun DrawScope.drawSplinters(
    centerX: Float,
    centerY: Float,
    color: Color,
    age: Float,
    seed: Int,
    px: Float,
) {
    val random = Random(seed)
    repeat(10) {
        val angle = random.nextFloat() * 2f * PI.toFloat()
        val speed = 12f + random.nextFloat() * 18f
        val x = centerX + cos(angle) * speed * age
        val y = centerY + sin(angle) * speed * age + 18f * age * age
        drawRect(color, Offset(floor(x) * px, floor(y) * px), Size(2f * px, 2f * px))
    }
}

@Composable
private fun androidx.compose.foundation.layout.BoxScope.BoardBreakOverlay(hud: BoardBreakHud) {
    when (hud.phase) {
        BoardBreakPhase.READY,
        BoardBreakPhase.CHARGING -> {
            val charging = hud.phase == BoardBreakPhase.CHARGING
            Surface(
                color = if (charging) Color(0xFF38C864) else Color(0xFFFFD65A),
                contentColor = Color(0xFF1A222D),
                shape = RoundedCornerShape(10.dp),
                shadowElevation = 4.dp,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 10.dp),
            ) {
                Text(
                    text =
                        stringResource(
                            if (charging) Res.string.boardbreak_release
                            else Res.string.boardbreak_hold
                        ),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }
        }
        BoardBreakPhase.STRIKING,
        BoardBreakPhase.OVER -> {
            val outcome = hud.lastOutcome ?: return
            Column(
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(outcome.rating.label),
                    style =
                        MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = flexFontEmphasis(),
                            fontWeight = FontWeight.Black,
                            shadow = TEXT_SHADOW,
                        ),
                    color = outcome.rating.color,
                )
                Text(
                    text =
                        stringResource(Res.string.boardbreak_broken, outcome.broken, outcome.total),
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            shadow = TEXT_SHADOW,
                        ),
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun BoardBreakMeter(
    config: BoardBreakConfig,
    needle: Float,
    phase: BoardBreakPhase,
    modifier: Modifier = Modifier,
) {
    val needlePercent = (needle.coerceIn(0f, 1f) * 100).roundToInt()
    val description = stringResource(Res.string.boardbreak_meter, needlePercent)

    Canvas(
        modifier =
            modifier.height(28.dp).clip(RoundedCornerShape(8.dp)).semantics {
                contentDescription = description
            }
    ) {
        val w = size.width
        val h = size.height
        val center = w * BoardBreak.METER_CENTER

        // Outer black background (MISS)
        drawRoundRect(color = METER_BLACK, size = size, cornerRadius = CornerRadius(8.dp.toPx()))

        // Yellow zone (GOOD)
        val yellowW = w * config.yellowHalfWidth * 2f
        drawRect(
            color = METER_YELLOW,
            topLeft = Offset(center - yellowW / 2f, 0f),
            size = Size(yellowW, h),
        )

        // Green zone (GREAT)
        val greenW = w * config.greenHalfWidth * 2f
        drawRect(
            color = METER_GREEN,
            topLeft = Offset(center - greenW / 2f, 0f),
            size = Size(greenW, h),
        )

        // Bright center (PERFECT)
        val perfectW = w * config.perfectHalfWidth * 2f
        drawRect(
            color = METER_BRIGHT,
            topLeft = Offset(center - perfectW / 2f, 0f),
            size = Size(perfectW, h),
        )

        // Border around the meter
        drawRoundRect(
            color = METER_BORDER,
            size = size,
            cornerRadius = CornerRadius(8.dp.toPx()),
            style = Stroke(width = 2.dp.toPx()),
        )

        // Sweeping needle
        val nx = (w * needle.coerceIn(0f, 1f)).coerceIn(3.dp.toPx(), w - 3.dp.toPx())
        val needleColor = if (phase == BoardBreakPhase.CHARGING) Color.White else Color(0xFFFFD65A)
        drawRect(
            color = Color(0xFF141822),
            topLeft = Offset(nx - 3.dp.toPx(), 0f),
            size = Size(6.dp.toPx(), h),
        )
        drawRect(
            color = needleColor,
            topLeft = Offset(nx - 1.5.dp.toPx(), 0f),
            size = Size(3.dp.toPx(), h),
        )
    }
}

private fun Modifier.holdToCharge(onPress: () -> Unit, onRelease: () -> Unit): Modifier =
    pointerInput(onPress, onRelease) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)
            onPress()
            waitForUpOrCancellation()
            onRelease()
        }
    }

@Composable
private fun BoardBreakEndOverlay(hud: BoardBreakHud, result: RunResult?, onClose: () -> Unit) {
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
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.padding(24.dp).widthIn(max = 400.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(Res.string.kibarrage_over),
                    style =
                        MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = flexFontEmphasis()
                        ),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(Res.string.kibarrage_points, hud.score),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text =
                        stringResource(
                            Res.string.boardbreak_total_broken,
                            hud.score,
                            hud.maxPossible,
                        ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (result?.newBest == true) {
                    Text(
                        text = stringResource(Res.string.kibarrage_new_best),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Button(onClick = onClose, modifier = Modifier.padding(top = 8.dp)) {
                    Text(stringResource(Res.string.kibarrage_back))
                }
            }
        }
    }
}

private val StrikeMaterial.label: StringResource
    get() =
        when (this) {
            StrikeMaterial.BOARDS -> Res.string.boardbreak_boards
            StrikeMaterial.BRICKS -> Res.string.boardbreak_bricks
            StrikeMaterial.ICE -> Res.string.boardbreak_ice
        }

private val StrikeRating.label: StringResource
    get() =
        when (this) {
            StrikeRating.PERFECT -> Res.string.boardbreak_perfect
            StrikeRating.GREAT -> Res.string.boardbreak_great
            StrikeRating.GOOD -> Res.string.boardbreak_good
            StrikeRating.MISS -> Res.string.boardbreak_miss
        }

private val StrikeRating.color: Color
    get() =
        when (this) {
            StrikeRating.PERFECT -> Color(0xFFFFF078)
            StrikeRating.GREAT -> Color(0xFF68F088)
            StrikeRating.GOOD -> Color(0xFFFFD65A)
            StrikeRating.MISS -> Color(0xFFFF7A8A)
        }

/** The pictures of one Board Break run, cached per pose, frame and material. */
private class BoardBreakSprites(
    private val character: FighterCharacter,
    private val rank: FighterRank,
    private val colors: FighterColors,
    private val outfit: String?,
    private val gear: Set<String>,
) {
    private val fighters = HashMap<Pair<FighterPose, Int>, PixelImage>()
    private val layers = HashMap<StrikeMaterial, PixelImage>()
    private val halves = HashMap<Pair<StrikeMaterial, Boolean>, PixelImage>()
    private var lastSkyFrame: TrainingGroundPixels.SkyFrame? = null
    private var lastSkyImage: PixelImage? = null

    val kiColor: Int = FighterPixels.kiColor(character, rank)
    val ground: PixelImage =
        TrainingGroundPixels.renderGround(props = setOf(FighterItems.MAT), tired = false)
    val stand: PixelImage = BoardBreakPixels.stand()
    val impactBurst: PixelImage = BoardBreakPixels.impactBurst(kiColor)

    fun sky(frame: TrainingGroundPixels.SkyFrame): PixelImage {
        val cached = lastSkyImage
        if (cached != null && frame == lastSkyFrame) return cached
        return TrainingGroundPixels.renderSky(
                weather = Weather.SUNNY,
                height = BoardBreak.HEIGHT.toInt(),
                frame = frame,
            )
            .also {
                lastSkyFrame = frame
                lastSkyImage = it
            }
    }

    fun fighter(pose: FighterPose, frame: Int): PixelImage {
        val f = frame.mod(FighterPixels.FRAMES)
        return fighters.getOrPut(pose to f) {
            FighterPixels.render(character, rank, FighterMood.READY, colors, pose, gear, f, outfit)
        }
    }

    fun layer(material: StrikeMaterial): PixelImage =
        layers.getOrPut(material) { BoardBreakPixels.layer(material) }

    fun halfLayer(material: StrikeMaterial, rightHalf: Boolean): PixelImage =
        halves.getOrPut(material to rightHalf) { BoardBreakPixels.halfLayer(material, rightHalf) }
}
