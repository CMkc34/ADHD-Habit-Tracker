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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shub39.grit.core.avatar.AvatarState
import com.shub39.grit.core.fighter.FighterCharacter
import com.shub39.grit.core.fighter.FighterColors
import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.core.minigame.clutter.ClutterEvent
import com.shub39.grit.core.minigame.clutter.ClutterSmash
import com.shub39.grit.core.minigame.clutter.ClutterSmashConfig
import com.shub39.grit.core.minigame.clutter.EndReason
import com.shub39.grit.core.minigame.clutter.JunkKind
import com.shub39.grit.shared.ui.habit.fighter.FighterPixels
import com.shub39.grit.shared.ui.habit.fighter.FighterPose
import com.shub39.grit.shared.ui.habit.fighter.PixelImage
import com.shub39.grit.shared.ui.habit.fighter.drawPixels
import com.shub39.grit.shared.ui.habit.fighter.rememberPixelFrame
import com.shub39.grit.shared.ui.theme.flexFontEmphasis
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.close
import grit.shared.ui.generated.resources.cluttersmash_combo
import grit.shared.ui.generated.resources.cluttersmash_hint
import grit.shared.ui.generated.resources.cluttersmash_ki
import grit.shared.ui.generated.resources.cluttersmash_ki_bar
import grit.shared.ui.generated.resources.cluttersmash_ki_wave
import grit.shared.ui.generated.resources.cluttersmash_smashed
import grit.shared.ui.generated.resources.cluttersmash_tap_me
import grit.shared.ui.generated.resources.cluttersmash_time
import grit.shared.ui.generated.resources.cluttersmash_time_up
import grit.shared.ui.generated.resources.cluttersmash_too_high
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
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private const val BANNER_SECONDS = 1.1f
private const val POP_SECONDS = 0.85f
private const val STRIKE_SECONDS = 0.22f
private const val BOLT_SECONDS = 0.14f
private const val WAVE_SECONDS = 0.45f
private const val CRACK_SHAKE_SECONDS = 0.12f

private val ROOM_EDGE = Color(0xFF1D2430)
private val RED_LINE = Color(0xFFE8405A)
private val RED_LINE_BRIGHT = Color(0xFFFF7A8A)
private val TEXT_SHADOW = Shadow(Color(0xFF1E1628), Offset(3f, 3f), 3f)

/**
 * The full-screen run of Clutter Smash (#43): the [ClutterSmash] simulation steps every frame on
 * the Laundry Room stage. The fighter stands on the left while junk falls into three columns on the
 * right and piles up toward the dashed red line. Tapping a piece of junk sends a small ki bolt that
 * cracks or smashes it into pixel bits; quick smashes build a combo (`score ×combo`). Each smash
 * fills the ki bar; once full, "TAP ME!" on the fighter unleashes a Ki Wave across the room.
 */
@Composable
fun ClutterSmashScreen(
    run: RunState,
    avatar: AvatarState,
    best: Int,
    onAction: (MiniGamesAction) -> Unit,
) {
    val act by rememberUpdatedState(onAction)
    val haptics = LocalHapticFeedback.current
    val game = remember(run.started) { ClutterSmash(ClutterSmashConfig.of(run.started.stats)) }
    val data = avatar.data
    val rank = avatar.fighter.rank
    val sprites =
        remember(data.character, rank, data.colors, data.outfit, data.accessories) {
            ClutterSmashSprites(data.character, rank, data.colors, data.outfit, data.accessories)
        }
    val pixelFrame = rememberPixelFrame()

    var clock by remember { mutableFloatStateOf(0f) }
    var hud by remember { mutableStateOf(ClutterHud.of(game)) }
    var banner by remember { mutableStateOf<ClutterBanner?>(null) }
    val pops = remember { mutableStateListOf<ClutterPop>() }
    var animationsEnabled by remember { mutableStateOf(true) }
    var askingEnd by remember { mutableStateOf(false) }
    var ended by remember { mutableStateOf(false) }
    val over = hud.over || ended

    fun handleEvents(events: List<ClutterEvent>) {
        for (event in events) {
            when (event) {
                is ClutterEvent.JunkCracked ->
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                is ClutterEvent.JunkSmashed -> {
                    pops +=
                        ClutterPop(
                            x = event.x,
                            y = event.y,
                            at = game.time,
                            color = ClutterSmashPixels.color(event.kind),
                            points = event.points,
                        )
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                ClutterEvent.KiReady -> Unit
                is ClutterEvent.KiWaveCleared -> {
                    banner = ClutterBanner(ClutterBannerKind.KI_WAVE, game.time)
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                is ClutterEvent.GameOver -> {
                    val kind =
                        when (event.reason) {
                            EndReason.TIME_UP -> ClutterBannerKind.TIME_UP
                            EndReason.TOO_HIGH -> ClutterBannerKind.TOO_HIGH
                        }
                    banner = ClutterBanner(kind, game.time)
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }
        }
        clock = game.time
        hud = ClutterHud.of(game)
    }

    LaunchedEffect(game) {
        val scale = coroutineContext[MotionDurationScale]?.scaleFactor ?: 1f
        animationsEnabled = scale > 0f
        var last = -1L
        while (!game.isOver && !ended) {
            withFrameNanos { now ->
                val seconds = if (last < 0) 0f else (now - last) / 1_000_000_000f
                last = now
                if (askingEnd) return@withFrameNanos
                game.step(seconds)
                handleEvents(game.events)
                pops.removeAll { game.time - it.at > POP_SECONDS }
                if (banner?.let { game.time - it.at > BANNER_SECONDS } == true) banner = null
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
        Surface(modifier = Modifier.fillMaxSize(), color = ROOM_EDGE) {
            Column(
                modifier = Modifier.fillMaxSize().padding(safeArea),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ClutterHudRow(
                    hud = hud,
                    best = best,
                    kiColor = Color(sprites.kiColor),
                    onKiWave = { if (game.fireKiWave()) handleEvents(game.events) },
                    onEnd = { if (over) act(MiniGamesAction.Close) else askingEnd = true },
                )
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    ClutterField(
                        game = game,
                        sprites = sprites,
                        clock = { clock },
                        idleFrame = pixelFrame,
                        animate = animationsEnabled,
                        pops = pops,
                        onTap = { x, y -> if (game.tap(x, y)) handleEvents(game.events) },
                        modifier = Modifier.aspectRatio(ClutterSmash.WIDTH / ClutterSmash.HEIGHT),
                        overlay = {
                            if (hud.danger && !hud.over) {
                                DangerWarning(animate = animationsEnabled, clock = clock)
                            }
                            if (hud.kiReady && !hud.over) {
                                TapMeBadge(
                                    onClick = { if (game.fireKiWave()) handleEvents(game.events) },
                                    modifier =
                                        Modifier.align(Alignment.BottomStart)
                                            .padding(start = 12.dp, bottom = 72.dp),
                                )
                            }
                            banner?.let { ClutterBannerText(it) }
                        },
                    )
                }
                Text(
                    text = stringResource(Res.string.cluttersmash_hint),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            if (over) {
                ClutterEndOverlay(
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

private data class ClutterHud(
    val score: Int,
    val smashed: Int,
    val secondsLeft: Int,
    val combo: Int,
    val kiShare: Float,
    val kiReady: Boolean,
    val danger: Boolean,
    val over: Boolean,
    val reason: EndReason?,
) {
    companion object {
        fun of(game: ClutterSmash): ClutterHud =
            ClutterHud(
                score = game.score,
                smashed = game.smashedCount,
                secondsLeft = ceil(game.timeLeft).toInt(),
                combo = game.combo,
                kiShare = game.kiShare,
                kiReady = game.kiReady,
                danger = game.isDanger,
                over = game.isOver,
                reason = game.endReason,
            )
    }
}

private enum class ClutterBannerKind {
    KI_WAVE,
    TIME_UP,
    TOO_HIGH,
}

private data class ClutterBanner(val kind: ClutterBannerKind, val at: Float)

private data class ClutterPop(
    val x: Float,
    val y: Float,
    val at: Float,
    val color: Int,
    val points: Int,
)

@Composable
private fun ClutterHudRow(
    hud: ClutterHud,
    best: Int,
    kiColor: Color,
    onKiWave: () -> Unit,
    onEnd: () -> Unit,
) {
    val kiPercent = (hud.kiShare * 100).roundToInt()
    val kiBarDescription = stringResource(Res.string.cluttersmash_ki_bar, kiPercent)

    Row(
        modifier = Modifier.widthIn(max = 640.dp).fillMaxWidth().padding(start = 16.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = hud.score.toString().padStart(5, '0'),
                    style =
                        MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = flexFontEmphasis()
                        ),
                    color = Color(0xFFFFF0C8),
                )
                if (hud.combo >= 2) {
                    Text(
                        text = stringResource(Res.string.cluttersmash_combo, hud.combo),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFD65A),
                    )
                }
                Box(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(Res.string.cluttersmash_time, hud.secondsLeft),
                    style =
                        MaterialTheme.typography.titleMedium.copy(fontFamily = flexFontEmphasis()),
                    color = if (hud.secondsLeft <= 10) Color(0xFFFF7A8A) else Color(0xFFE8F0FF),
                )
                Text(
                    text = stringResource(Res.string.kibarrage_best, maxOf(best, hud.score)),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFBEBEDC),
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier =
                    Modifier.semantics { contentDescription = kiBarDescription }
                        .clickable(enabled = hud.kiReady, onClick = onKiWave),
            ) {
                Text(
                    text = stringResource(Res.string.cluttersmash_ki),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (hud.kiReady) Color(0xFFFFD65A) else Color(0xFFBEBEDC),
                )
                Box(
                    modifier =
                        Modifier.weight(1f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF121620))
                ) {
                    Box(
                        modifier =
                            Modifier.fillMaxHeight()
                                .fillMaxWidth(hud.kiShare.coerceIn(0f, 1f))
                                .background(if (hud.kiReady) Color(0xFFFFD65A) else kiColor)
                    )
                }
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
private fun ClutterField(
    game: ClutterSmash,
    sprites: ClutterSmashSprites,
    clock: () -> Float,
    idleFrame: State<Int>,
    animate: Boolean,
    pops: List<ClutterPop>,
    onTap: (Float, Float) -> Unit,
    modifier: Modifier = Modifier,
    overlay: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit,
) {
    val measurer = rememberTextMeasurer()
    val popStyle =
        TextStyle(
            color = Color(0xFFFFEC78),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            shadow = Shadow(Color(0xFF1E1628), Offset(2f, 2f), 2f),
        )
    Box(modifier = modifier.clipToBounds().tapToSmash(onTap)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val time = clock()
            val px = size.width / ClutterSmash.WIDTH
            val frame = if (animate) idleFrame.value else 0
            fun at(x: Float, y: Float) = Offset(floor(x) * px, floor(y) * px)

            drawPixels(sprites.backdrop, Offset.Zero, px)
            drawRedLine(
                game.config.lineY,
                game.isDanger && animate && (time * 6).toInt() % 2 == 0,
                px,
            )

            for (item in game.junk) {
                val shake =
                    if (animate && time - item.hitAt < CRACK_SHAKE_SECONDS) {
                        if ((time * 40).toInt() % 2 == 0) -1f else 1f
                    } else 0f
                drawPixels(sprites.junk(item.kind, item.isDamaged), at(item.x + shake, item.y), px)
            }

            val pose =
                when {
                    time - game.lastWaveAt < WAVE_SECONDS -> FighterPose.BLAST
                    time - game.lastStrikeAt < STRIKE_SECONDS ->
                        if (game.lastStrikeKick) FighterPose.KICK else FighterPose.PUNCH
                    game.kiReady -> FighterPose.CHARGE
                    else -> FighterPose.STANCE
                }
            drawPixels(
                sprites.fighter(pose, frame),
                at(ClutterSmash.FIGHTER_LEFT, ClutterSmash.FIGHTER_TOP),
                px,
            )

            game.lastBolt?.let { bolt ->
                val age = time - bolt.at
                if (age in 0f..BOLT_SECONDS) {
                    val t = if (animate) (age / BOLT_SECONDS).coerceIn(0f, 1f) else 1f
                    val fromX = ClutterSmash.FIGHTER_LEFT + 38f
                    val fromY = ClutterSmash.FIGHTER_TOP + 22f
                    val bx = fromX + (bolt.toX - fromX) * t - sprites.bolt.width / 2f
                    val by = fromY + (bolt.toY - fromY) * t - sprites.bolt.height / 2f
                    drawPixels(sprites.bolt, at(bx, by), px)
                }
            }

            val waveAge = time - game.lastWaveAt
            if (waveAge in 0f..WAVE_SECONDS) {
                val t = if (animate) (waveAge / WAVE_SECONDS).coerceIn(0f, 1f) else 0.5f
                val startX = ClutterSmash.FIGHTER_LEFT + 38f
                val wx = startX + (ClutterSmash.WIDTH - startX) * t
                val wy = ClutterSmash.FLOOR_Y - sprites.wave.height
                drawPixels(sprites.wave, at(wx, wy), px)
            }

            for (pop in pops) {
                val age = time - pop.at
                if (age < 0f || age > POP_SECONDS) continue
                if (animate) burst(pop, age, px)
                val rise = if (animate) 14f * age else 4f
                val text = measurer.measure("+${pop.points}", popStyle)
                drawText(
                    textLayoutResult = text,
                    topLeft =
                        Offset(
                            pop.x * px - text.size.width / 2f,
                            (pop.y - 4f - rise) * px - text.size.height,
                        ),
                    alpha = (1f - age / POP_SECONDS).coerceIn(0f, 1f),
                )
            }
        }
        overlay()
    }
}

private fun DrawScope.drawRedLine(lineY: Float, bright: Boolean, px: Float) {
    val color = if (bright) RED_LINE_BRIGHT else RED_LINE
    val y = floor(lineY) * px
    var x = ClutterSmash.COLUMNS_LEFT - 2f
    while (x < ClutterSmash.WIDTH - 4f) {
        val seg = minOf(4f, ClutterSmash.WIDTH - 4f - x)
        drawRect(color = color, topLeft = Offset(floor(x) * px, y), size = Size(seg * px, 2f * px))
        x += 7f
    }
}

private fun Modifier.tapToSmash(onTap: (Float, Float) -> Unit): Modifier =
    pointerInput(onTap) {
        awaitEachGesture {
            val down = awaitFirstDown()
            val fx = down.position.x / size.width * ClutterSmash.WIDTH
            val fy = down.position.y / size.height * ClutterSmash.HEIGHT
            onTap(fx, fy)
        }
    }

private fun DrawScope.burst(pop: ClutterPop, age: Float, px: Float) {
    val random = Random(pop.x.toInt() * 31 + (pop.at * 100).toInt())
    val color = Color(pop.color)
    repeat(8) {
        val angle = random.nextFloat() * 2 * PI.toFloat()
        val speed = 10f + random.nextFloat() * 14f
        val x = pop.x + cos(angle) * speed * age
        val y = pop.y + sin(angle) * speed * age + 16f * age * age
        drawRect(color, Offset(floor(x) * px, floor(y) * px), Size(2 * px, 2 * px))
    }
}

@Composable
private fun DangerWarning(animate: Boolean, clock: Float) {
    if (animate && (clock * 5).toInt() % 2 == 1) return
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, end = 12.dp),
        contentAlignment = Alignment.TopEnd,
    ) {
        Text(
            text = stringResource(Res.string.cluttersmash_too_high),
            style =
                MaterialTheme.typography.titleMedium.copy(
                    fontFamily = flexFontEmphasis(),
                    fontWeight = FontWeight.Black,
                    shadow = TEXT_SHADOW,
                ),
            color = RED_LINE_BRIGHT,
        )
    }
}

@Composable
private fun TapMeBadge(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        color = Color(0xFFFFD65A),
        contentColor = Color(0xFF231B2E),
        shape = RoundedCornerShape(10.dp),
        shadowElevation = 4.dp,
        modifier = modifier,
    ) {
        Text(
            text = stringResource(Res.string.cluttersmash_tap_me),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun ClutterBannerText(banner: ClutterBanner) {
    val text =
        when (banner.kind) {
            ClutterBannerKind.KI_WAVE -> stringResource(Res.string.cluttersmash_ki_wave)
            ClutterBannerKind.TIME_UP -> stringResource(Res.string.cluttersmash_time_up)
            ClutterBannerKind.TOO_HIGH -> stringResource(Res.string.cluttersmash_too_high)
        }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style =
                MaterialTheme.typography.displaySmall.copy(
                    fontFamily = flexFontEmphasis(),
                    shadow = TEXT_SHADOW,
                ),
            color = Color(0xFFFFD678),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ClutterEndOverlay(hud: ClutterHud, result: RunResult?, onClose: () -> Unit) {
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
                val headline =
                    when (hud.reason) {
                        EndReason.TIME_UP -> stringResource(Res.string.cluttersmash_time_up)
                        EndReason.TOO_HIGH -> stringResource(Res.string.cluttersmash_too_high)
                        null -> stringResource(Res.string.kibarrage_over)
                    }
                Text(
                    text = headline,
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
                    text = stringResource(Res.string.cluttersmash_smashed, hud.smashed),
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

/** The pictures of one Clutter Smash run, cached per pose, frame and damage state. */
private class ClutterSmashSprites(
    private val character: FighterCharacter,
    private val rank: FighterRank,
    private val colors: FighterColors,
    private val outfit: String?,
    private val gear: Set<String>,
) {
    private val fighters = HashMap<Pair<FighterPose, Int>, PixelImage>()
    private val junkSprites = HashMap<Pair<JunkKind, Boolean>, PixelImage>()

    val kiColor: Int = FighterPixels.kiColor(character, rank)
    val backdrop: PixelImage = ClutterSmashPixels.backdrop()
    val bolt: PixelImage = ClutterSmashPixels.bolt(kiColor)
    val wave: PixelImage = ClutterSmashPixels.wave(kiColor)

    fun fighter(pose: FighterPose, frame: Int): PixelImage {
        val f = frame.mod(FighterPixels.FRAMES)
        return fighters.getOrPut(pose to f) {
            FighterPixels.render(character, rank, FighterMood.READY, colors, pose, gear, f, outfit)
        }
    }

    fun junk(kind: JunkKind, damaged: Boolean): PixelImage =
        junkSprites.getOrPut(kind to damaged) { ClutterSmashPixels.junk(kind, damaged) }
}
