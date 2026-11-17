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
import androidx.compose.foundation.layout.size
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
import com.shub39.grit.core.arena.Opponent
import com.shub39.grit.core.avatar.AvatarState
import com.shub39.grit.core.fighter.FighterCharacter
import com.shub39.grit.core.fighter.FighterColors
import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.core.minigame.kibarrage.Boss
import com.shub39.grit.core.minigame.kibarrage.KiBarrage
import com.shub39.grit.core.minigame.kibarrage.KiBarrageConfig
import com.shub39.grit.core.minigame.kibarrage.KiBarrageEvent
import com.shub39.grit.core.minigame.kibarrage.MinionKind
import com.shub39.grit.shared.ui.habit.arena.OpponentPixels
import com.shub39.grit.shared.ui.habit.arena.OpponentPose
import com.shub39.grit.shared.ui.habit.arena.label
import com.shub39.grit.shared.ui.habit.fighter.FighterPixels
import com.shub39.grit.shared.ui.habit.fighter.FighterPose
import com.shub39.grit.shared.ui.habit.fighter.PixelImage
import com.shub39.grit.shared.ui.habit.fighter.drawPixels
import com.shub39.grit.shared.ui.theme.flexFontEmphasis
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.close
import grit.shared.ui.generated.resources.kibarrage_back
import grit.shared.ui.generated.resources.kibarrage_best
import grit.shared.ui.generated.resources.kibarrage_boss
import grit.shared.ui.generated.resources.kibarrage_end
import grit.shared.ui.generated.resources.kibarrage_end_text
import grit.shared.ui.generated.resources.kibarrage_end_title
import grit.shared.ui.generated.resources.kibarrage_hearts
import grit.shared.ui.generated.resources.kibarrage_hint
import grit.shared.ui.generated.resources.kibarrage_keep_playing
import grit.shared.ui.generated.resources.kibarrage_ko
import grit.shared.ui.generated.resources.kibarrage_new_best
import grit.shared.ui.generated.resources.kibarrage_over
import grit.shared.ui.generated.resources.kibarrage_points
import grit.shared.ui.generated.resources.kibarrage_wave
import grit.shared.ui.generated.resources.kibarrage_wave_reached
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.random.Random
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** How long a wave or boss banner stays, in game seconds. */
private const val BANNER_SECONDS = 1.4f

/** How long the bits of a popped minion and its points fly. */
private const val POP_SECONDS = 0.9f

/** How long the fighter shows the hurt pose after a hit. */
private const val HURT_SECONDS = 0.4f

/** Visual scale of the fighter sprite relative to its 48x48 pixel art, for arcade proportions. */
private const val FIGHTER_DRAW_SCALE = 2f / 3f

/** Night blue around the field, so the pixel picture has no hard edge on tall phones. */
private val FIELD_EDGE = Color(0xFF110F24)

/**
 * The full-screen run of Ki Barrage (#42): the [KiBarrage] simulation steps every frame and is
 * drawn in pixel art on the Screen World stage at night. A drag anywhere on the field moves the
 * fighter, its ki fires by itself. Score, best and hearts sit above the field, a boss shows its
 * name and HP bar. When the last heart is gone (or the user ends the run) the score goes to the
 * view model with [MiniGamesAction.Finish], and the end card shows it with a possible new best.
 */
@Composable
fun KiBarrageScreen(
    run: RunState,
    avatar: AvatarState,
    best: Int,
    onAction: (MiniGamesAction) -> Unit,
) {
    val act by rememberUpdatedState(onAction)
    val haptics = LocalHapticFeedback.current
    val game =
        remember(run.started) {
            KiBarrage(KiBarrageConfig.of(run.started.stats), run.started.opponents)
        }
    val data = avatar.data
    val rank = avatar.fighter.rank
    val sprites =
        remember(data.character, rank, data.colors, data.outfit, data.accessories) {
            KiBarrageSprites(data.character, rank, data.colors, data.outfit, data.accessories)
        }

    // the game time of the last frame; the field reads it while drawing, so only drawing repeats
    var clock by remember { mutableFloatStateOf(0f) }
    var hud by remember { mutableStateOf(Hud.of(game)) }
    var banner by remember { mutableStateOf<Banner?>(null) }
    val pops = remember { mutableStateListOf<Pop>() }
    var askingEnd by remember { mutableStateOf(false) }
    var ended by remember { mutableStateOf(false) }
    val over = hud.over || ended

    LaunchedEffect(game) {
        var last = -1L
        while (!game.isOver && !ended) {
            withFrameNanos { now ->
                val seconds = if (last < 0) 0f else (now - last) / 1_000_000_000f
                last = now
                if (askingEnd) return@withFrameNanos
                game.step(seconds)
                for (event in game.events) {
                    when (event) {
                        is KiBarrageEvent.WaveStarted ->
                            banner =
                                Banner(
                                    if (event.boss != null) BannerKind.BOSS else BannerKind.WAVE,
                                    event.wave,
                                    game.time,
                                )
                        is KiBarrageEvent.MinionPopped ->
                            pops +=
                                Pop(
                                    event.x,
                                    event.y,
                                    game.time,
                                    KiBarragePixels.color(event.kind),
                                    event.points,
                                )
                        is KiBarrageEvent.BossHit ->
                            pops += Pop(event.x, event.y, game.time, BOSS_HIT_COLOR, event.points)
                        is KiBarrageEvent.BossKnockedOut -> {
                            banner = Banner(BannerKind.KO, game.wave, game.time)
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        KiBarrageEvent.PlayerHit ->
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        KiBarrageEvent.GameOver -> Unit
                    }
                }
                pops.removeAll { game.time - it.at > POP_SECONDS }
                if (banner?.let { game.time - it.at > BANNER_SECONDS } == true) banner = null
                clock = game.time
                hud = Hud.of(game)
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
        Surface(modifier = Modifier.fillMaxSize(), color = FIELD_EDGE) {
            Column(
                modifier = Modifier.fillMaxSize().padding(safeArea),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                HudRow(
                    hud = hud,
                    best = best,
                    maxHearts = game.config.hearts,
                    onEnd = { if (over) act(MiniGamesAction.Close) else askingEnd = true },
                )
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Field(
                        game = game,
                        sprites = sprites,
                        clock = { clock },
                        pops = pops,
                        modifier = Modifier.aspectRatio(KiBarrage.WIDTH / KiBarrage.HEIGHT),
                        overlay = {
                            hud.boss?.let { BossBar(it, hud.bossShare) }
                            banner?.let { BannerText(it) }
                        },
                    )
                }
                Text(
                    text = stringResource(Res.string.kibarrage_hint),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(8.dp),
                )
            }

            if (over) {
                EndOverlay(
                    score = hud.score,
                    wave = hud.wave,
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

/** What the text above and on the field shows; changes only a few times per wave. */
private data class Hud(
    val score: Int,
    val hearts: Int,
    val wave: Int,
    val boss: Opponent?,
    val bossShare: Float,
    val over: Boolean,
) {
    companion object {
        fun of(game: KiBarrage): Hud {
            val boss = game.boss?.takeIf { it.koAt == null }
            return Hud(
                score = game.score,
                hearts = game.hearts,
                wave = game.wave,
                boss = boss?.opponent,
                bossShare = boss?.let { it.hp / it.maxHp.toFloat() } ?: 0f,
                over = game.isOver,
            )
        }
    }
}

private enum class BannerKind {
    WAVE,
    BOSS,
    KO,
}

private data class Banner(val kind: BannerKind, val wave: Int, val at: Float)

/** A popped minion or a blow on the boss: flying bits and the points, from game time [at]. */
private data class Pop(val x: Float, val y: Float, val at: Float, val color: Int, val points: Int)

private val BOSS_HIT_COLOR = 0xFFFFFFFF.toInt()

@Composable
private fun HudRow(hud: Hud, best: Int, maxHearts: Int, onEnd: () -> Unit) {
    val heartsText = stringResource(Res.string.kibarrage_hearts, hud.hearts)
    Row(
        modifier = Modifier.widthIn(max = 640.dp).fillMaxWidth().padding(start = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = hud.score.toString().padStart(5, '0'),
                    style =
                        MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = flexFontEmphasis()
                        ),
                    color = Color(0xFFFFF0C8),
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(Res.string.kibarrage_best, maxOf(best, hud.score)),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFBEBEDC),
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.semantics { contentDescription = heartsText },
            ) {
                repeat(maxHearts) { i ->
                    val heart = remember(i < hud.hearts) { KiBarragePixels.heart(i < hud.hearts) }
                    Canvas(modifier = Modifier.size(width = 21.dp, height = 18.dp)) {
                        drawPixels(heart)
                    }
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

/** The pixel field with everything that moves, and [overlay] (boss bar, banner) on top. */
@Composable
private fun Field(
    game: KiBarrage,
    sprites: KiBarrageSprites,
    clock: () -> Float,
    pops: List<Pop>,
    modifier: Modifier = Modifier,
    overlay: @Composable () -> Unit,
) {
    val measurer = rememberTextMeasurer()
    val popStyle =
        TextStyle(
            color = Color(0xFFFFEC78),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            shadow = Shadow(Color(0xFF1E1628), Offset(2f, 2f), 2f),
        )
    Box(modifier = modifier.clipToBounds().dragToMove(game)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val time = clock()
            val px = size.width / KiBarrage.WIDTH
            val frame = (time * 10).toInt()
            fun at(x: Float, y: Float) = Offset(floor(x) * px, floor(y) * px)

            drawPixels(sprites.backdrop, Offset.Zero, px)
            for (m in game.minions) {
                if (!m.alive || m.y < -KiBarrage.MINION_SIZE) continue
                drawPixels(sprites.minion(m.kind, frame / 3), at(m.x, m.y), px)
            }
            game.boss?.let { drawPixels(sprites.boss(it, time, frame), at(it.x, it.y), px) }
            for (shot in game.shots) {
                drawPixels(KiBarragePixels.shot, at(shot.x - 2, shot.y - 2), px)
            }
            for (bolt in game.bolts) drawPixels(sprites.bolt, at(bolt.x - 1, bolt.y), px)

            // blinks while it cannot be hit again
            val blink = game.isInvulnerable && frame % 2 == 1 && !game.isOver
            if (!blink) {
                val pose =
                    if (time - game.hurtAt < HURT_SECONDS) FighterPose.HURT else FighterPose.CHARGE
                val drawnSize = KiBarrage.FIGHTER_SIZE * FIGHTER_DRAW_SCALE
                val left = game.fighterX - drawnSize / 2f
                val top = KiBarrage.FIGHTER_TOP + (KiBarrage.FIGHTER_SIZE - drawnSize) - 2f
                drawPixels(sprites.fighter(pose, frame), at(left, top), px * FIGHTER_DRAW_SCALE)
            }

            for (pop in pops) {
                val age = time - pop.at
                if (age < 0f || age > POP_SECONDS) continue
                burst(pop, age, px)
                val text = measurer.measure("+${pop.points}", popStyle)
                drawText(
                    textLayoutResult = text,
                    topLeft =
                        Offset(
                            pop.x * px - text.size.width / 2f,
                            (pop.y - 6f - 15f * age) * px - text.size.height,
                        ),
                    alpha = (1f - age / POP_SECONDS).coerceIn(0f, 1f),
                )
            }
        }
        overlay()
    }
}

/** A finger anywhere on the field pulls the fighter to its x; it does not have to touch it. */
private fun Modifier.dragToMove(game: KiBarrage): Modifier =
    pointerInput(game) {
        awaitEachGesture {
            val down = awaitFirstDown()
            game.target = down.position.x / size.width * KiBarrage.WIDTH
            do {
                val event = awaitPointerEvent()
                for (change in event.changes) {
                    if (!change.pressed) continue
                    game.target = change.position.x / size.width * KiBarrage.WIDTH
                    change.consume()
                }
            } while (event.changes.any { it.pressed })
        }
    }

/** The bits of a popped minion, flying out and falling, like the preview. */
private fun DrawScope.burst(pop: Pop, age: Float, px: Float) {
    val random = Random(pop.x.toInt() * 31 + (pop.at * 100).toInt())
    val color = Color(pop.color)
    repeat(8) {
        val angle = random.nextFloat() * 2 * PI.toFloat()
        val speed = 10f + random.nextFloat() * 12f
        val x = pop.x + cos(angle) * speed * age
        val y = pop.y + sin(angle) * speed * age + 15f * age * age
        drawRect(color, Offset(floor(x) * px, floor(y) * px), Size(2 * px, 2 * px))
    }
}

@Composable
private fun BossBar(boss: Opponent, share: Float) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF1E1628))
        ) {
            Box(
                modifier =
                    Modifier.fillMaxHeight()
                        .fillMaxWidth(share.coerceIn(0f, 1f))
                        .background(Color(0xFFE8405A))
            )
        }
        Text(
            text = boss.label(),
            style = MaterialTheme.typography.labelLarge.copy(shadow = TEXT_SHADOW),
            color = Color.White,
        )
    }
}

@Composable
private fun BannerText(banner: Banner) {
    val text =
        when (banner.kind) {
            BannerKind.WAVE -> stringResource(Res.string.kibarrage_wave, banner.wave)
            BannerKind.BOSS -> stringResource(Res.string.kibarrage_boss)
            BannerKind.KO -> stringResource(Res.string.kibarrage_ko)
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

private val TEXT_SHADOW = Shadow(Color(0xFF1E1628), Offset(3f, 3f), 3f)

@Composable
private fun EndOverlay(score: Int, wave: Int, result: RunResult?, onClose: () -> Unit) {
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
                    text = stringResource(Res.string.kibarrage_points, score),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(Res.string.kibarrage_wave_reached, wave),
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

/** The pictures of one run, built once per pose and frame and then only painted. */
private class KiBarrageSprites(
    private val character: FighterCharacter,
    private val rank: FighterRank,
    private val colors: FighterColors,
    private val outfit: String?,
    private val gear: Set<String>,
) {
    private val fighters = HashMap<Pair<FighterPose, Int>, PixelImage>()
    private val minions = HashMap<Pair<MinionKind, Int>, PixelImage>()
    private val bosses = HashMap<Triple<Opponent, OpponentPose, Int>, PixelImage>()

    val backdrop: PixelImage = KiBarragePixels.backdrop()
    val bolt: PixelImage = KiBarragePixels.bolt(FighterPixels.kiColor(character, rank))

    fun fighter(pose: FighterPose, frame: Int): PixelImage {
        val f = frame.mod(FighterPixels.FRAMES)
        return fighters.getOrPut(pose to f) {
            FighterPixels.render(character, rank, FighterMood.READY, colors, pose, gear, f, outfit)
        }
    }

    fun minion(kind: MinionKind, frame: Int): PixelImage {
        val f = frame.mod(KiBarragePixels.FRAMES)
        return minions.getOrPut(kind to f) { KiBarragePixels.minion(kind, f) }
    }

    /** The boss flinches when hit, winds up when it throws, and is dizzy once knocked out. */
    fun boss(boss: Boss, time: Float, frame: Int): PixelImage {
        val pose =
            when {
                boss.koAt != null -> OpponentPose.KO
                time - boss.hitAt < 0.15f -> OpponentPose.HIT
                time - boss.firedAt < 0.25f -> OpponentPose.ATTACK
                else -> OpponentPose.IDLE
            }
        val f = frame.mod(OpponentPixels.FRAMES)
        return bosses.getOrPut(Triple(boss.opponent, pose, f)) {
            OpponentPixels.render(boss.opponent, pose, f)
        }
    }
}
