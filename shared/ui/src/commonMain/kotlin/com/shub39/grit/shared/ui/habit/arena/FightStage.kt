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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shub39.grit.core.arena.Opponent
import com.shub39.grit.core.arena.Side
import com.shub39.grit.core.fighter.FighterCharacter
import com.shub39.grit.core.fighter.FighterColors
import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.shared.ui.habit.fighter.FighterPixels
import com.shub39.grit.shared.ui.habit.fighter.FighterPose
import com.shub39.grit.shared.ui.habit.fighter.PixelImage
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter
import com.shub39.grit.shared.ui.habit.fighter.drawPixels
import com.shub39.grit.shared.ui.habit.fighter.rememberPixelFrame
import com.shub39.grit.shared.ui.theme.flexFontEmphasis
import grit.shared.ui.generated.resources.Res
import grit.shared.ui.generated.resources.arena_float_blocked
import grit.shared.ui.generated.resources.arena_float_crit
import grit.shared.ui.generated.resources.arena_float_dodge
import grit.shared.ui.generated.resources.arena_float_guard
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import org.jetbrains.compose.resources.stringResource

/** A big line over the stage: "ROUND 2", "FIGHT!", "ROUND WON!"; [small] goes under it. */
data class StageBanner(val text: String, val small: String? = null)

/**
 * The pictures of one fight, built once per pose and frame and then only painted, plus the
 * [geometry] measured from them.
 */
internal class FightSprites(
    private val character: FighterCharacter,
    private val rank: FighterRank,
    private val colors: FighterColors,
    private val outfit: String?,
    private val gear: Set<String>,
    private val opponent: Opponent,
) {
    private val fighters = HashMap<Triple<FighterPose, FighterMood, Int>, PixelImage>()
    private val opponents = HashMap<Pair<OpponentPose, Int>, PixelImage>()
    private val shots = HashMap<Pair<Int, Boolean>, PixelImage?>()
    private val blasts = HashMap<Int, PixelImage>()
    private val whites = HashMap<PixelImage, PixelImage>()

    fun fighter(pose: FighterPose, mood: FighterMood, frame: Int): PixelImage =
        fighters.getOrPut(Triple(pose, mood, frame)) {
            FighterPixels.render(character, rank, mood, colors, pose, gear, frame, outfit)
        }

    fun opponent(pose: OpponentPose, frame: Int): PixelImage =
        opponents.getOrPut(pose to frame) { OpponentPixels.render(opponent, pose, frame) }

    fun shot(frame: Int, special: Boolean): PixelImage? =
        shots.getOrPut(frame % OpponentAttack.FRAMES to special) {
            OpponentAttack.render(opponent, frame, special)
        }

    fun blast(frame: Int): PixelImage =
        blasts.getOrPut(frame % 2) { FighterPixels.renderKiBlast(character, rank, frame) }

    /** [image] washed almost white, for the flash of a blow landing. */
    fun white(image: PixelImage): PixelImage = whites.getOrPut(image) { image.whitened() }

    val geometry: FightGeometry =
        FightGeometry.of(
            punch = fighter(FighterPose.PUNCH, FighterMood.READY, 0),
            kick = fighter(FighterPose.KICK, FighterMood.READY, 0),
            stance = fighter(FighterPose.STANCE, FighterMood.READY, 0),
            idle = opponent(OpponentPose.IDLE, 0),
            attack = opponent(OpponentPose.ATTACK, 0),
            launch = OpponentPixels.launchPoint(opponent).let { StagePoint(it.x, it.y) },
        )
}

private fun PixelImage.whitened(): PixelImage {
    val argb =
        IntArray(width * height) { i ->
            val c = this[i % width, i / width]
            if (c == 0) 0 else PixelPainter.mix(c, WHITE, 0.7f)
        }
    return PixelImage(width, height, argb)
}

/**
 * The stage with both fighters and everything that flies, sparks and floats, as [scene] says,
 * shaking as blows land. [turn] is shown small at the top, [banner] big across the middle.
 */
@Composable
internal fun FightStage(
    fight: FightState,
    scene: FightScene,
    sprites: FightSprites,
    millis: Int,
    turn: String?,
    banner: StageBanner?,
) {
    val backdrop =
        remember(fight.stage, fight.lighting) {
            ArenaStagePixels.render(fight.stage, fight.lighting)
        }
    val frame = rememberPixelFrame()
    val ratio = ArenaStagePixels.WIDTH.toFloat() / ArenaStagePixels.HEIGHT
    val shakeX = (scene.shake * sin(millis * 0.09f)).roundToInt()
    val shakeY = (scene.shake * 0.5f * cos(millis * 0.13f)).roundToInt()

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth().aspectRatio(ratio).clip(RoundedCornerShape(16.dp))
    ) {
        val px = maxWidth / ArenaStagePixels.WIDTH
        Box(
            modifier =
                Modifier.fillMaxSize().graphicsLayer {
                    translationX = shakeX * px.toPx()
                    translationY = shakeY * px.toPx()
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val p = size.width / ArenaStagePixels.WIDTH
                drawPixels(backdrop, Offset.Zero, p)
                drawFighters(scene, sprites, frame.value, p)
                drawEffects(scene, sprites, frame.value, p)
                if (scene.flash > 0f) drawRect(Color.White.copy(alpha = scene.flash * 0.6f))
            }
            scene.float?.let { float ->
                val left =
                    if (float.over == Side.PLAYER) ArenaStagePixels.PLAYER_LEFT + scene.playerDx
                    else ArenaStagePixels.OPPONENT_LEFT + scene.opponentDx
                val width = if (float.over == Side.PLAYER) 48 else OpponentPixels.SIZE
                val top =
                    if (float.over == Side.PLAYER) FightGeometry.PLAYER_TOP
                    else FightGeometry.OPPONENT_TOP
                FloatingText(
                    float = float,
                    age = scene.floatAge.coerceIn(0f, 1f),
                    modifier =
                        Modifier.offset(x = px * left, y = px * (top - 4f - 10f * scene.floatAge))
                            .width(px * width),
                )
            }
        }
        turn?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (scene.opponentTurn) Color(0xFFFF8A80) else Color(0xFF9DF0CF),
                modifier =
                    Modifier.align(Alignment.TopCenter)
                        .padding(top = 6.dp)
                        .background(Color(0x99231B2E), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
            )
        }
        banner?.let { Banner(it, Modifier.align(Alignment.Center)) }
    }
}

private fun DrawScope.drawFighters(scene: FightScene, sprites: FightSprites, frame: Int, p: Float) {
    val opponentTop = FightGeometry.OPPONENT_TOP
    val playerTop = FightGeometry.PLAYER_TOP
    scene.opponentGhost?.let { dx ->
        val ghost = sprites.opponent(OpponentPose.IDLE, 0)
        drawSprite(ghost, ArenaStagePixels.OPPONENT_LEFT + dx, opponentTop.toFloat(), p, 0.35f)
    }
    val opponent = sprites.opponent(scene.opponentPose, frame)
    drawSprite(
        if (scene.opponentFlash) sprites.white(opponent) else opponent,
        ArenaStagePixels.OPPONENT_LEFT + scene.opponentDx,
        opponentTop + scene.opponentDy,
        p,
    )
    scene.playerGhost?.let { dx ->
        val ghost = sprites.fighter(FighterPose.STANCE, FighterMood.READY, 0)
        drawSprite(ghost, ArenaStagePixels.PLAYER_LEFT + dx, playerTop.toFloat(), p, 0.35f)
    }
    val fighter = sprites.fighter(scene.playerPose, scene.playerMood, frame)
    drawSprite(
        if (scene.playerFlash) sprites.white(fighter) else fighter,
        ArenaStagePixels.PLAYER_LEFT + scene.playerDx,
        playerTop + scene.playerDy,
        p,
    )
}

private fun DrawScope.drawEffects(scene: FightScene, sprites: FightSprites, frame: Int, p: Float) {
    val g = sprites.geometry
    scene.blast?.let { t ->
        val from = ArenaStagePixels.PLAYER_LEFT + 36f
        val to = g.punchAt.x - 16f
        drawSprite(sprites.blast(frame), from + (to - from) * t, FightGeometry.PLAYER_TOP + 17f, p)
    }
    scene.shot?.let { shot ->
        shot.chainFrom?.let { from ->
            val links = ((from.x - shot.at.x) / 2f).toInt().coerceAtLeast(1)
            for (i in 0..links step 2) {
                val k = i / links.toFloat()
                val x = from.x + (shot.at.x - from.x) * k
                val y = from.y + (shot.at.y - from.y) * k + sin(k * PI).toFloat() * 3f
                dot(x, y, OpponentAttack.CHAIN_COLOR, p, shot.alpha)
            }
        }
        sprites.shot(frame, shot.special)?.let { image ->
            val top = if (shot.trail) shot.at.y - image.height else shot.at.y - image.height / 2f
            if (shot.trail) {
                // spikes keep breaking out behind the front, smaller and fading
                for (i in 2 downTo 1) {
                    val x = shot.at.x + i * (image.width + 3)
                    if (x < g.launch.x) drawSprite(image, x, top, p, shot.alpha * (0.8f - i * 0.2f))
                }
            }
            drawSprite(image, shot.at.x - image.width / 2f, top, p, shot.alpha)
        }
    }
    scene.whiff?.let { at ->
        // a swoosh where the blow cut the air
        for (k in 0..8) {
            val a = -PI / 2 + k * PI / 8
            dot(at.x + 2 + cos(a).toFloat() * 6f, at.y + sin(a).toFloat() * 6f, WHITE, p, 0.7f)
        }
    }
    scene.spark?.let { drawSpark(it, p) }
}

/** A burst of rays and, at first, a star: yellow on a hit, pale blue and small on a guard. */
private fun DrawScope.drawSpark(spark: Spark, p: Float) {
    val color = if (spark.blocked) GUARD_BLUE else SPARK_YELLOW
    val alpha = (1f - spark.age).coerceIn(0f, 1f)
    val grow = ((if (spark.big) 6 else 3) * spark.age).roundToInt()
    for (d in 0 until 8) {
        val a = d * PI / 4
        for (r in (2 + grow)..(4 + grow * 2)) {
            dot(
                spark.at.x + cos(a).toFloat() * r,
                spark.at.y + sin(a).toFloat() * r,
                if (r % 2 == 0) color else WHITE,
                p,
                alpha,
            )
        }
    }
    if (spark.age < 0.5f) {
        val star = if (spark.big) BIG_STAR else SMALL_STAR
        for ((y, row) in star.withIndex()) {
            for ((x, c) in row.withIndex()) {
                val col =
                    when (c) {
                        'W' -> WHITE
                        'Y' -> color
                        else -> continue
                    }
                dot(spark.at.x - row.length / 2 + x, spark.at.y - star.size / 2 + y, col, p, 1f)
            }
        }
    }
}

/** One stage pixel at ([x], [y]). */
private fun DrawScope.dot(x: Float, y: Float, color: Int, p: Float, alpha: Float) {
    drawRect(
        color = Color(color).copy(alpha = alpha.coerceIn(0f, 1f)),
        topLeft = Offset(x.roundToInt() * p, y.roundToInt() * p),
        size = Size(p, p),
    )
}

/** Paints [image] with its top left corner at stage pixel ([x], [y]), snapped to the grid. */
private fun DrawScope.drawSprite(
    image: PixelImage,
    x: Float,
    y: Float,
    p: Float,
    alpha: Float = 1f,
) {
    if (alpha >= 1f) {
        drawPixels(image, Offset(x.roundToInt() * p, y.roundToInt() * p), p)
        return
    }
    if (alpha <= 0f) return
    val left = x.roundToInt()
    val top = y.roundToInt()
    for (row in 0 until image.height) for (col in 0 until image.width) {
        val c = image[col, row]
        if (c != 0) dot((left + col).toFloat(), (top + row).toFloat(), c, p, alpha)
    }
}

@Composable
private fun FloatingText(float: FloatText, age: Float, modifier: Modifier = Modifier) {
    val text =
        when (float.kind) {
            FloatKind.DAMAGE -> "-${float.damage}"
            FloatKind.CRIT -> "${stringResource(Res.string.arena_float_crit)} -${float.damage}"
            FloatKind.BLOCKED -> stringResource(Res.string.arena_float_blocked, float.damage)
            FloatKind.DODGE -> stringResource(Res.string.arena_float_dodge)
            FloatKind.GUARD -> stringResource(Res.string.arena_float_guard)
        }
    val color =
        when (float.kind) {
            FloatKind.CRIT -> Color(0xFFFFD34A)
            FloatKind.GUARD,
            FloatKind.BLOCKED -> Color(0xFF8FD8FF)
            else -> Color.White
        }
    // pops in a little bigger, then settles
    val pop = if (age < 0.15f) 1.35f - age * 2.3f else 1f
    Text(
        text = text,
        color = color,
        textAlign = TextAlign.Center,
        style =
            MaterialTheme.typography.titleLarge.copy(
                fontFamily = flexFontEmphasis(),
                fontWeight = FontWeight.Black,
                shadow = Shadow(color = Color(0xFF231B2E), offset = Offset(2f, 2f), blurRadius = 2f),
            ),
        modifier =
            modifier.graphicsLayer {
                alpha = (1.6f - 1.6f * age).coerceIn(0f, 1f)
                scaleX = pop
                scaleY = pop
            },
    )
}

@Composable
private fun Banner(banner: StageBanner, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            modifier.fillMaxWidth().background(Color(0x8C000000)).padding(vertical = 8.dp),
    ) {
        val shadow = Shadow(color = Color(0xFF231B2E), offset = Offset(3f, 3f), blurRadius = 2f)
        Text(
            text = banner.text,
            color = Color(0xFFFFD34A),
            style =
                MaterialTheme.typography.displaySmall.copy(
                    fontFamily = flexFontEmphasis(),
                    fontWeight = FontWeight.Black,
                    shadow = shadow,
                ),
        )
        banner.small?.let {
            Text(
                text = it,
                color = Color.White,
                fontSize = 14.sp,
                style = MaterialTheme.typography.labelLarge.copy(shadow = shadow),
            )
        }
    }
}

private val WHITE = 0xFFFFFFFF.toInt()
private val SPARK_YELLOW = 0xFFFFD34A.toInt()
private val GUARD_BLUE = 0xFF8FD8FF.toInt()
private val SMALL_STAR = listOf(".W.", "WYW", ".W.")
private val BIG_STAR =
    listOf("...W...", ".W.Y.W.", "..YYY..", "WYYWYYW", "..YYY..", ".W.Y.W.", "...W...")
