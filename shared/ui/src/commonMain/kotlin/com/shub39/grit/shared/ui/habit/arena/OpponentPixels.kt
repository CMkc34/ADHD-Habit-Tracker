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

import com.shub39.grit.core.arena.Opponent
import com.shub39.grit.shared.ui.habit.fighter.Paint
import com.shub39.grit.shared.ui.habit.fighter.PixelImage
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter.Companion.INK
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter.Companion.argb
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter.Companion.mix
import com.shub39.grit.shared.ui.habit.fighter.Pt
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/** What an arena opponent is doing. */
enum class OpponentPose {
    /** Waiting for the fighter, with a gentle bob and one small move of its own. */
    IDLE,
    /** A short flinch: leans back, eyes squeezed shut, a white impact star. Never hurt. */
    HIT,
    /** Knocked out: slumped, spiral eyes and little stars circling the head. Dizzy, not hurt. */
    KO,
    /** Gathering for its own attack: leans back, the glowing eyes fixed on the fighter. */
    WINDUP,
    /** Its own attack leaves it (see [OpponentAttack]): leans in with the mouth wide open. */
    ATTACK,
    /** Braced behind a pale blue shimmer; the next strike only does half damage. */
    GUARD,
}

/**
 * Builds the pixel art of the 10 ladder opponents (#13) in the fighter's style: simple shapes,
 * shaded and outlined by [PixelPainter]. Every opponent has its own fixed colours and faces left,
 * towards the fighter. Small opponents stay small, the big ones fill the canvas. Pure Kotlin, so it
 * is unit tested and [OpponentAvatar] only paints the result.
 *
 * The designs are made on a 48 x 48 grid like the fighter and drawn [SCALE] times larger (decided
 * 2026-11-07: the enemies 20 % bigger than the fighter), on a [SIZE] x [SIZE] picture.
 */
object OpponentPixels {
    const val SIZE = 62

    /** How much larger than the 48 px design grid the opponents are drawn. */
    const val SCALE = 1.2f

    /** Animation frames in one loop, the same clock as the fighter (one every 100 ms). */
    const val FRAMES = 12

    // the shapes are drawn on a larger canvas, so leaning and tilting never cuts anything off;
    // the finished picture is then cropped back to SIZE
    private const val PAD = 8
    private const val CANVAS = SIZE + 2 * PAD
    private const val GROUND = 46.5f

    /** Pale blue of the guard shimmer, like the fighter's "GUARD" text. */
    private val GUARD_SHIMMER = argb(0x8FD8FF)

    /**
     * Where the opponent's own attack leaves it in the [OpponentPose.ATTACK] picture (its mouth,
     * hand or front), in pixels of the picture.
     */
    internal fun launchPoint(opponent: Opponent): Pt {
        val sketch = Sketch(opponent, OpponentPose.ATTACK, 0, plain = false)
        sketch.draw()
        sketch.finish()
        return sketch.launchInPicture()
    }

    fun render(
        opponent: Opponent,
        pose: OpponentPose = OpponentPose.IDLE,
        frame: Int = 0,
    ): PixelImage {
        val sketch = Sketch(opponent, pose, frame.mod(FRAMES), plain = false)
        sketch.draw()
        return sketch.finish()
    }

    /**
     * The locked opponent for the ladder list: its flat shape in [fill] with a big question mark in
     * [mark]. The colours come from the app theme, so it fits light and dark mode.
     */
    fun silhouette(opponent: Opponent, fill: Int, mark: Int): PixelImage {
        val image = Sketch(opponent, OpponentPose.IDLE, 0, plain = true).also { it.draw() }.finish()
        val argb = IntArray(SIZE * SIZE) { if (image.argb[it] != 0) fill else 0 }
        val filled = argb.indices.filter { argb[it] != 0 }
        val left = filled.minOf { it % SIZE }
        val right = filled.maxOf { it % SIZE }
        val top = filled.minOf { it / SIZE }
        val bottom = filled.maxOf { it / SIZE }
        val scale = if (right - left >= 20 && bottom - top >= 24) 2 else 1
        val w = QUESTION.first().length * scale
        val h = QUESTION.size * scale
        val x0 = (left + right + 1 - w) / 2
        val y0 = (top + bottom + 1 - h) / 2
        for (y in 0 until h) for (x in 0 until w) {
            if (QUESTION[y / scale][x / scale] == 'M') {
                val px = x0 + x
                val py = y0 + y
                if (px in 0 until SIZE && py in 0 until SIZE) argb[py * SIZE + px] = mark
            }
        }
        return PixelImage(SIZE, SIZE, argb)
    }

    /** Draws one opponent in one pose and frame. */
    private class Sketch(
        val opponent: Opponent,
        val pose: OpponentPose,
        val f: Int,
        /** Only the shape, without the little idle effects (for the locked silhouette). */
        val plain: Boolean,
    ) {
        val p = PixelPainter(CANVAS)
        private val idle = pose == OpponentPose.IDLE
        private val effects = !plain
        private val hit = pose == OpponentPose.HIT
        private val ko = pose == OpponentPose.KO
        private val guard = pose == OpponentPose.GUARD

        /** Idle bob in pixels: the top of the figure goes down by one in the second half. */
        private val bob = if (idle && f >= FRAMES / 2) 1f else 0f

        /** Sideways shake while idle (only the alarm clock uses it). */
        private var shake = 0f

        /** Where the impact star of [OpponentPose.HIT] lands, in sketch coordinates. */
        private var impact = Pt(14, 30)

        /** Where its own attack leaves it, in sketch coordinates; the front ([impact]) if unset. */
        private var launch: Pt? = null

        /** Top left of the finished picture on the canvas, set by [finish]. */
        private var cropX = PAD
        private var cropY = PAD

        /** Top of the head, the dizzy stars of [OpponentPose.KO] circle above it. */
        private var headTop = Pt(24, 18)

        /** How far the knocked out opponent tilts back (radians). */
        private var koTilt = 0.42f

        /** The x the knocked out opponent tilts around (its back foot). */
        private var koPivot = 30f

        /**
         * While true, shapes lie on the ground and do not tilt when knocked out (cushion, props).
         */
        private var grounded = false

        /** Colour of eye and mouth lines; on a dark mask the eyes are drawn light. */
        private var eyeInk = INK

        // -- transform ----------------------------------------------------------------------------

        /** Moves a point of the opponent's design to the canvas for the current pose and frame. */
        private fun t(q: Pt): Pt {
            val moved =
                when (pose) {
                    OpponentPose.IDLE -> {
                        // the top moves, the feet stay on the ground
                        val lift = ((GROUND - q.y) / 8f).coerceIn(0f, 1f)
                        Pt(q.x + shake, q.y + bob * lift)
                    }
                    OpponentPose.HIT -> {
                        val up = (GROUND - q.y).coerceAtLeast(0f)
                        Pt(q.x + 1.5f + up * 0.13f, q.y + up * 0.04f)
                    }
                    OpponentPose.WINDUP,
                    OpponentPose.GUARD -> {
                        val up = (GROUND - q.y).coerceAtLeast(0f)
                        Pt(q.x + 0.8f + up * 0.07f, q.y)
                    }
                    OpponentPose.ATTACK -> {
                        val up = (GROUND - q.y).coerceAtLeast(0f)
                        Pt(q.x - 1.5f - up * 0.12f, q.y)
                    }
                    OpponentPose.KO ->
                        if (grounded) q
                        else {
                            val rx = q.x - koPivot
                            val ry = (q.y - GROUND) * 0.86f
                            val c = cos(koTilt)
                            val s = sin(koTilt)
                            Pt(koPivot + rx * c - ry * s, GROUND + rx * s + ry * c)
                        }
                }
            // scaled up around the middle of the ground line, which ends on the picture's last row
            val x = (moved.x - 24f) * SCALE + SIZE / 2f
            val y = (moved.y - GROUND) * SCALE + GROUND + (SIZE - 48)
            return Pt(x + PAD, y + PAD)
        }

        private fun pt(x: Number, y: Number) = Pt(x, y)

        /** A shape colour, graded darker and colder so the opponents look mean, not cute. */
        private fun paint(color: Int, group: String = color.toString()) =
            Paint(mix(color, DUSK, 0.24f), group)

        /** White claws in a row, centred on [c]. */
        private fun claws(c: Pt, rows: List<String> = listOf("W.W.W")) =
            face(c, rows, mapOf('W' to CLAW))

        // -- shapes, all in design coordinates ----------------------------------------------------

        fun ellipse(c: Pt, rx: Float, ry: Float, paint: Paint) {
            val n = 28
            p.poly(
                (0 until n).map { i ->
                    val a = 2 * PI * i / n
                    t(Pt(c.x + rx * cos(a).toFloat(), c.y + ry * sin(a).toFloat()))
                },
                paint,
            )
        }

        fun circle(c: Pt, r: Float, paint: Paint) = ellipse(c, r, r, paint)

        fun poly(points: List<Pt>, paint: Paint) = p.poly(points.map(::t), paint)

        fun rect(x0: Number, y0: Number, x1: Number, y1: Number, paint: Paint) =
            poly(listOf(pt(x0, y0), pt(x1, y0), pt(x1, y1), pt(x0, y1)), paint)

        fun capsule(a: Pt, b: Pt, ra: Float, paint: Paint, rb: Float = ra) =
            p.capsule(t(a), t(b), ra * SCALE, paint, rb * SCALE)

        /** Hand-placed pixels centred on [c]. */
        fun face(c: Pt, rows: List<String>, colors: Map<Char, Int>) {
            val at = t(c)
            val x = (at.x - rows.first().length / 2f).roundToInt()
            val y = (at.y - rows.size / 2f).roundToInt()
            p.face(x, y, rows, colors)
        }

        fun effect(c: Pt, rows: List<String>, colors: Map<Char, Int>, move: Boolean = true) {
            if (!effects) return
            val at = if (move) t(c) else Pt(c.x + PAD, c.y + PAD)
            val x = (at.x - rows.first().length / 2f).roundToInt()
            val y = (at.y - rows.size / 2f).roundToInt()
            p.effect(x, y, rows, colors)
        }

        /**
         * Two eyes centred on [l] and [r]. Idle they use [open]; hit they are squeezed shut,
         * knocked out they turn into spirals. [lid] is an optional colour for heavy eyelids (`L`).
         */
        fun eyes(l: Pt, r: Pt, open: List<String> = BEAN, lid: Int? = null) {
            val colors = buildMap {
                put('O', eyeInk)
                put('W', WHITE)
                if (lid != null) put('L', lid)
            }
            when (pose) {
                OpponentPose.IDLE,
                OpponentPose.WINDUP,
                OpponentPose.ATTACK,
                OpponentPose.GUARD -> {
                    // mean look: narrow glowing eyes under lids that slant down to the middle
                    val glowing = colors + ('G' to EYE_GLOW)
                    val small = open.size <= 2
                    face(l, if (small) SLIT_LEFT_SMALL else SLIT_LEFT, glowing)
                    face(r, if (small) SLIT_RIGHT_SMALL else SLIT_RIGHT, glowing)
                }
                OpponentPose.HIT -> {
                    face(l, SQUEEZE_LEFT, colors)
                    face(r, SQUEEZE_RIGHT, colors)
                }
                OpponentPose.KO -> {
                    face(l, SPIRAL, colors)
                    face(r, SPIRAL, colors)
                }
            }
        }

        /**
         * A mouth centred on [c]: [open] idle, a small "o" when hit, a wobbly line when knocked
         * out.
         */
        fun mouth(c: Pt, open: List<String> = SMILE) {
            val colors =
                mapOf('O' to if (eyeInk == INK) MOUTH else eyeInk, 'W' to CLAW, 'M' to THROAT)
            when (pose) {
                OpponentPose.IDLE,
                OpponentPose.GUARD,
                OpponentPose.WINDUP -> face(c, snarl(open.first().length), colors)
                OpponentPose.ATTACK -> face(c, roar(open.first().length), colors)
                OpponentPose.HIT -> face(c, OOH, colors)
                OpponentPose.KO -> face(c, WOBBLE, colors)
            }
        }

        /** The mouth torn wide open for the attack: fangs top and bottom around a dark throat. */
        private fun roar(width: Int): List<String> {
            val w = (width + 1).coerceAtLeast(4)
            val fangs = CharArray(w) { if (it % 2 == 0) 'W' else 'O' }.concatToString()
            val throat = "O" + "M".repeat(w - 2) + "O"
            return listOf("O".repeat(w), fangs, throat, throat, fangs.reversed(), "O".repeat(w))
        }

        /** A wide grin of jagged teeth, at least [width] wide. */
        private fun snarl(width: Int): List<String> {
            val w = (width + 1).coerceAtLeast(4)
            val teeth = CharArray(w) { if (it % 2 == 0) 'W' else 'O' }.concatToString()
            val fangs = CharArray(w) { if (it == 0 || it == w - 1) 'W' else '.' }.concatToString()
            return listOf("O".repeat(w), teeth, fangs)
        }

        // -- the opponents ------------------------------------------------------------------------

        fun draw() {
            when (opponent) {
                Opponent.SNOOZE_BUTTON -> snoozeButton()
                Opponent.COUCH_GREMLIN -> couchGremlin()
                Opponent.DOOMSCROLL_IMP -> doomscrollImp()
                Opponent.LAUNDRY_MOUNTAIN -> laundryMountain()
                Opponent.INBOX_HYDRA -> inboxHydra()
                Opponent.DISTRACTION_SWARM -> distractionSwarm()
                Opponent.TIME_THIEF -> timeThief()
                Opponent.DEADLINE_GOLEM -> deadlineGolem()
                Opponent.OVERWHELM_OGRE -> overwhelmOgre()
                Opponent.PROCRASTINATION_DRAGON -> procrastinationDragon()
            }
        }

        /** A small round alarm clock with a big snooze button nose; it rings now and then. */
        private fun snoozeButton() {
            val ringing = idle && effects && f < 4
            if (ringing) shake = if (f % 2 == 0) -1f else 1f
            impact = pt(14, 32)
            headTop = pt(24, 19)
            koTilt = 0.55f
            koPivot = 29f
            val body = paint(CLOCK_RED, "body")
            val bell = paint(GOLD, "bell")
            val metal = paint(METAL, "metal")
            val feet = paint(INK_SOFT, "feet")

            capsule(pt(20, 40), pt(18.5, 44.5), 1.1f, feet)
            capsule(pt(28, 40), pt(29.5, 44.5), 1.1f, feet)
            ellipse(pt(18, 45), 2.6f, 1.5f, feet)
            ellipse(pt(30, 45), 2.6f, 1.5f, feet)
            circle(pt(16.5, 24.5), 3.6f, bell)
            circle(pt(31.5, 24.5), 3.6f, bell)
            capsule(pt(24, 23), pt(24, 20.5), 0.9f, metal)
            circle(pt(24, 20), 1.6f, metal)
            capsule(pt(15, 35), pt(11.5, 38), 1.3f, body)
            capsule(pt(33, 35), pt(36.5, 38), 1.3f, body)
            circle(pt(24, 33), 10f, body)
            circle(pt(23, 33.5), 7.4f, paint(CREAM, "dial"))
            face(pt(23, 27.5), listOf("O"), mapOf('O' to INK_SOFT))
            face(pt(29.5, 33.5), listOf("O"), mapOf('O' to INK_SOFT))
            circle(pt(21.5, 35.5), 2.3f, paint(BUTTON_PINK, "button"))
            eyes(pt(18, 30.5), pt(25.5, 30.5), open = SLEEPY, lid = CLOCK_RED)
            mouth(pt(22, 39.5), open = listOf("O..O", ".OO."))
            if (ringing) {
                val ring = mapOf('Y' to GOLD)
                effect(pt(11, 20), listOf("Y.", ".Y", "Y."), ring)
                effect(pt(37, 20), listOf(".Y", "Y.", ".Y"), ring)
            }
        }

        /** A small fuzzy creature on a cushion; its ear twitches. */
        private fun couchGremlin() {
            impact = pt(12, 32)
            headTop = pt(24, 22)
            koPivot = 32f
            val fur = paint(GREMLIN, "fur")
            val ear = paint(GREMLIN, "ear")
            val cushion = paint(CUSHION, "cushion")
            val tassel = paint(GOLD, "tassel")

            grounded = true
            circle(pt(8.5, 43.5), 1.6f, tassel)
            circle(pt(39.5, 43.5), 1.6f, tassel)
            ellipse(pt(24, 42.5), 15f, 4f, cushion)
            grounded = false
            val twitch = idle && f in 4..5
            poly(listOf(pt(16, 27.5), pt(6, 21), pt(19, 24)), ear)
            poly(listOf(pt(30, 24), if (twitch) pt(41, 22.5) else pt(41, 20), pt(32.5, 28)), ear)
            ellipse(pt(24, 33), 9.5f, 8.5f, fur)
            // fuzzy tufts on the head
            for (deg in listOf(200, 225, 250, 275, 300, 325)) {
                val a = deg * PI / 180
                circle(pt(24 + 9 * cos(a), 33 + 8 * sin(a)), 1.8f, fur)
            }
            ellipse(pt(23, 37), 5f, 3.6f, paint(GREMLIN_BELLY, "belly"))
            ellipse(pt(16.5, 40.5), 2.4f, 1.8f, fur)
            ellipse(pt(31.5, 40.5), 2.4f, 1.8f, fur)
            claws(pt(15.5, 42))
            claws(pt(32.5, 42))
            face(pt(9.5, 22.5), listOf("P."), mapOf('P' to EAR_PINK))
            face(pt(38.5, if (twitch) 23.5f else 22f), listOf(".P"), mapOf('P' to EAR_PINK))
            eyes(pt(19, 30), pt(26, 30), open = BIG)
            mouth(pt(22.5, 34.5), open = listOf("O.WO", ".OO."))
        }

        /** A little imp glued to its phone; the screen scrolls and the tail sways. */
        private fun doomscrollImp() {
            impact = pt(17, 34)
            headTop = pt(23, 15)
            koPivot = 30f
            val imp = paint(IMP, "imp")
            val horn = paint(HORN, "horn")
            val sway = if (idle && f / 3 % 2 == 1) 1.5f else 0f

            capsule(pt(30, 39), pt(37, 37), 1.0f, imp)
            capsule(pt(37, 37), pt(39 + sway, 31), 1.0f, imp)
            poly(listOf(pt(37 + sway, 31.5), pt(42 + sway, 31), pt(40 + sway, 27)), imp)
            capsule(pt(21, 39), pt(20, 44.5), 1.6f, imp)
            capsule(pt(27, 39), pt(28, 44.5), 1.6f, imp)
            ellipse(pt(19, 45), 2.4f, 1.5f, imp)
            ellipse(pt(28.5, 45), 2.4f, 1.5f, imp)
            capsule(pt(28, 34), pt(31.5, 37.5), 1.3f, imp)
            ellipse(pt(24, 35.5), 6f, 5.5f, imp)
            poly(listOf(pt(17.5, 20), pt(14, 13.5), pt(20.5, 18)), horn)
            poly(listOf(pt(26, 18), pt(30.5, 13), pt(29.5, 20.5)), horn)
            poly(listOf(pt(16, 25.5), pt(10.5, 21.5), pt(16.5, 22)), imp)
            ellipse(pt(23, 25), 7.5f, 6.5f, imp)
            capsule(pt(20, 33), pt(15, 32), 1.4f, imp)
            rect(9, 27, 15, 36, paint(PHONE, "phone"))
            rect(10, 28, 14, 34.5, paint(SCREEN, "screen"))
            ellipse(pt(15, 33), 1.9f, 1.7f, imp)
            if (!ko) {
                // feed lines scrolling up the screen
                val line = mapOf('L' to SCREEN_LINE)
                for (k in 0 until 2) {
                    val y = 29 + (k * 3 + (if (idle) 5 - f % 6 else 0)) % 6
                    face(pt(12, y), listOf("LLL"), line)
                }
            }
            eyes(pt(19, 24.5), pt(24.5, 24.5))
            mouth(pt(21, 28.5), open = listOf("O...O", ".OOO."))
        }

        /** A pile of clothes with eyes; the sock on top flaps. */
        private fun laundryMountain() {
            impact = pt(9, 33)
            headTop = pt(22, 19)
            koTilt = 0.3f
            koPivot = 30f
            val flap = idle && f / 3 % 2 == 1

            ellipse(pt(13, 41.5), 9f, 5f, paint(JEANS, "jeans"))
            ellipse(pt(35, 41.5), 9.5f, 5f, paint(SHIRT_RED, "shirt"))
            ellipse(pt(24, 42.5), 8.5f, 4f, paint(TOWEL, "towel"))
            capsule(pt(11, 32), pt(6.5, 39), 2f, paint(HOODIE, "hoodie"))
            ellipse(pt(19, 33), 9.5f, 6.5f, paint(HOODIE, "hoodie"))
            ellipse(pt(31, 32.5), 8.5f, 6.5f, paint(TEE, "tee"))
            capsule(pt(37, 33), pt(41.5, 37), 1.8f, paint(TEE, "tee"))
            val sock = paint(SOCK, "sock")
            capsule(pt(24, 22), pt(28, 16), 1.8f, sock)
            val toe = if (flap) pt(31.5, 16.5) else pt(30, 13.5)
            capsule(pt(28, 16), toe, 1.8f, sock)
            ellipse(toe, 2.2f, 1.9f, sock)
            ellipse(pt(23, 25.5), 7.5f, 5f, paint(SWEATER, "sweater"))
            face(pt(26, 19), listOf("S"), mapOf('S' to SOCK_STRIPE))
            face(pt(27, 17.5), listOf("S"), mapOf('S' to SOCK_STRIPE))
            face(pt(23, 26), listOf("D.D.D.D"), mapOf('D' to mix(SWEATER, WHITE, 0.4f)))
            eyes(pt(18, 31.5), pt(25, 31.5), open = BIG)
            mouth(pt(21.5, 36.5))
        }

        /** Three envelope heads on long necks; they bob one after another. */
        private fun inboxHydra() {
            impact = pt(8, 30)
            headTop = pt(24, 9)
            koTilt = 0.32f
            koPivot = 32f
            val body = paint(HYDRA, "hydra")

            capsule(pt(36, 40), pt(43, 35), 1.6f, body, 1.0f)
            ellipse(pt(19, 45), 3f, 1.6f, body)
            ellipse(pt(33, 45), 3f, 1.6f, body)
            ellipse(pt(26, 39), 11f, 6.5f, body)
            ellipse(pt(25, 41), 6.5f, 3.2f, paint(HYDRA_BELLY, "belly"))
            val heads = listOf(pt(10, 22), pt(24, 13.5), pt(38, 21))
            val roots = listOf(pt(19, 35), pt(26, 33), pt(33, 35))
            val bends = listOf(pt(12, 31), pt(26.5, 24), pt(39, 30))
            heads.forEachIndexed { i, head ->
                val dy = if (idle && (f / 2 + i * 2) % 6 >= 3) 1 else 0
                val h = head + pt(0, dy)
                val neck = paint(HYDRA, "neck$i")
                capsule(roots[i], bends[i], 2.8f, neck, 2.4f)
                capsule(bends[i], h + pt(0, 3), 2.4f, neck, 2.0f)
                rect(h.x - 6f, h.y - 4f, h.x + 6f, h.y + 4f, paint(ENVELOPE, "env$i"))
                val fold = mapOf('F' to ENVELOPE_FOLD)
                face(
                    h + pt(0, -1.5),
                    listOf("F.........F", ".FF.....FF.", "...FF.FF...", ".....F....."),
                    fold,
                )
                face(h + pt(4, 2.5), listOf("RR", "RR"), mapOf('R' to STAMP))
                eyes(h + pt(-3, 1.5), h + pt(0.5, 1.5), open = DOT)
            }
            // only the middle head talks
            mouth(
                heads[1] +
                    pt(-1, 3.5) +
                    pt(0, if (idle && f / 2 % 6 >= 1 && f / 2 % 6 < 4) 1 else 0),
                open = listOf("OO"),
            )
        }

        /** A buzzing cluster of notification bubbles; their badges blink. */
        private fun distractionSwarm() {
            impact = pt(10, 28)
            headTop = pt(24, 12)
            koTilt = 0.25f
            koPivot = 30f
            fun jitter(i: Int) =
                if (idle) pt((f + i) % 3 - 1, if ((f + 2 * i) % 3 == 0) -1 else 0) else pt(0, 0)

            val small =
                listOf(
                    Triple(pt(12, 20), 5f, BUBBLE_ORANGE),
                    Triple(pt(36, 19), 5.5f, BUBBLE_BLUE),
                    Triple(pt(11, 35), 4.5f, BUBBLE_PURPLE),
                    Triple(pt(37, 35), 5f, BUBBLE_GREEN),
                    Triple(pt(25, 13), 4f, BUBBLE_YELLOW),
                )
            small.forEachIndexed { i, (c0, r, color) ->
                val c = c0 + jitter(i)
                val paint = paint(color, "bubble$i")
                poly(
                    listOf(
                        c + pt(-r * 0.2f, r * 0.6f),
                        c + pt(-r * 0.9f, r * 1.3f),
                        c + pt(-r * 0.7f, r * 0.2f),
                    ),
                    paint,
                )
                circle(c, r, paint)
                val white = mapOf('W' to WHITE)
                val blink = idle && (f / 2 + i) % 3 == 0
                when (i % 3) {
                    0 -> face(c, listOf("W", "W", ".", "W"), white)
                    1 -> face(c, listOf("W.W.W"), white)
                    else -> face(c, listOf("WW", ".W", ".W"), white)
                }
                if (!blink && i != 4)
                    face(c + pt(r * 0.7f, -r * 0.7f), listOf("RR", "RR"), mapOf('R' to BADGE))
            }
            val main = paint(BUBBLE_RED, "main")
            val mc = pt(24, 28) + jitter(5)
            poly(listOf(mc + pt(2, 6), mc + pt(6, 12), mc + pt(6, 5)), main)
            circle(mc, 8.5f, main)
            eyes(mc + pt(-4.5, -1), mc + pt(1.5, -1), open = BIG)
            mouth(mc + pt(-1.5, 4), open = listOf("O..O", ".OO."))
            if (idle) {
                // buzz lines
                val buzz = mapOf('B' to BUZZ)
                val side = f % 2 == 0
                effect(pt(if (side) 3 else 4, 27), listOf("B", ".", "B"), buzz, move = false)
                effect(pt(if (side) 45 else 44, 26), listOf("B", ".", "B"), buzz, move = false)
            }
        }

        /** A sneaky masked figure on tiptoes with a melting pocket watch. */
        private fun timeThief() {
            impact = pt(15, 28)
            headTop = pt(23, 12)
            koPivot = 30f
            val cloak = paint(CLOAK, "cloak")
            val hood = paint(HOOD, "hood")
            val skin = paint(THIEF_SKIN, "skin")
            val dark = paint(INK_SOFT, "legs")
            val gold = paint(GOLD, "watch")
            val flutter = if (idle && f % 4 >= 2) 1f else 0f

            capsule(pt(21, 37), pt(17, 44), 1.4f, dark)
            capsule(pt(28, 37), pt(31, 43), 1.4f, dark)
            ellipse(pt(16, 44.8), 2.4f, 1.3f, dark)
            ellipse(pt(32.5, 43.8), 2.4f, 1.3f, dark)
            capsule(pt(29, 27), pt(33, 31), 1.4f, cloak)
            poly(
                listOf(
                    pt(18, 23),
                    pt(29, 23),
                    pt(34, 40),
                    pt(31, 38.5 + flutter),
                    pt(27.5, 40.5),
                    pt(24, 38.5 + flutter),
                    pt(20.5, 40.5),
                    pt(17, 38.5 + flutter),
                    pt(14, 40),
                ),
                cloak,
            )
            ellipse(pt(23.5, 18.5), 7.5f, 7f, hood)
            poly(listOf(pt(27, 13), pt(33, 10 + flutter), pt(30, 17)), hood)
            ellipse(pt(21.5, 20.5), 5.2f, 4.4f, skin)
            rect(16.5, 17.5, 27, 21, paint(MASK, "mask"))
            launch = pt(7, 25)
            when (pose) {
                // the pocket watch swings up behind the hood
                OpponentPose.WINDUP -> {
                    capsule(pt(25, 24), pt(30, 15), 1.5f, cloak)
                    ellipse(pt(30.5, 14), 1.8f, 1.7f, skin)
                    capsule(pt(30.5, 14), pt(33, 9), 0.5f, gold)
                    circle(pt(34, 7), 3.8f, gold)
                    circle(pt(33.8, 6.8), 2.6f, paint(CREAM, "dial"))
                }
                // thrown: the watch flies on its chain (OpponentAttack), the hand stays out
                OpponentPose.ATTACK -> {
                    capsule(pt(19, 26), pt(8, 25), 1.5f, cloak)
                    ellipse(pt(7, 25), 1.8f, 1.7f, skin)
                }
                // the watch held up like a small shield
                OpponentPose.GUARD -> {
                    capsule(pt(19, 27), pt(14, 21), 1.5f, cloak)
                    ellipse(pt(13.5, 20.5), 1.8f, 1.7f, skin)
                    circle(pt(11, 24), 4.6f, gold)
                    circle(pt(10.8, 23.8), 3.3f, paint(CREAM, "dial"))
                    face(pt(11, 23), listOf("O.", "O.", ".O"), mapOf('O' to INK_SOFT))
                }
                else -> {
                    capsule(pt(19, 27), pt(13, 30), 1.5f, cloak)
                    ellipse(pt(12.5, 31), 1.8f, 1.7f, skin)
                    capsule(pt(12.5, 31), pt(10, 31.5), 0.5f, gold)
                    circle(pt(9, 34.5), 3.8f, gold)
                    circle(pt(8.8, 34.3), 2.6f, paint(CREAM, "dial"))
                    val drip = if (idle) f / 4 else 1
                    capsule(pt(8, 37.5), pt(8, 39f + drip), 1.0f, gold)
                    face(pt(9, 33.5), listOf("O.", "O.", ".O"), mapOf('O' to INK_SOFT))
                }
            }
            eyeInk = WHITE
            eyes(pt(19, 19), pt(24.5, 19), open = SNEAKY)
            eyeInk = INK
            mouth(pt(21, 23), open = listOf("OOO.", "...O"))
        }

        /** A heavy stone block with a calendar page; the circled deadline blinks. */
        private fun deadlineGolem() {
            impact = pt(6, 30)
            headTop = pt(24, 11)
            koTilt = 0.3f
            koPivot = 33f
            val stone = paint(STONE, "stone")
            val arm = paint(STONE, "arm")
            val leg = paint(STONE_DARK, "leg")

            rect(14, 39, 21, 46.3, leg)
            rect(27, 39, 34, 46.3, leg)
            rect(10, 12, 38, 40.5, stone)
            rect(4.5, 23, 10.5, 36, arm)
            rect(37.5, 23, 43.5, 36, arm)
            ellipse(pt(7.5, 37), 3.5f, 3f, arm)
            ellipse(pt(40.5, 37), 3.5f, 3f, arm)
            rect(14, 16, 34, 36, paint(PAPER, "page"))
            rect(14, 16, 34, 21, paint(CALENDAR_RED, "header"))
            val ring = mapOf('O' to METAL)
            face(pt(18.5, 15.5), listOf("O", "O"), ring)
            face(pt(29.5, 15.5), listOf("O", "O"), ring)
            face(pt(24, 18.5), listOf("W.WWW.W"), mapOf('W' to mix(CALENDAR_RED, WHITE, 0.6f)))
            val grid = mapOf('G' to GRID)
            for (y in listOf(30, 34)) face(pt(24, y), listOf("G.G.G.G.G.G.G"), grid)
            // the deadline: a date with a red ring that blinks
            face(pt(29.5, 32.5), listOf("O"), mapOf('O' to INK_SOFT))
            val blink = idle && effects && f / 3 % 2 == 1
            if (!blink) {
                face(
                    pt(29.5, 32.5),
                    listOf(".RRR.", "R...R", "R...R", "R...R", ".RRR."),
                    mapOf('R' to CALENDAR_RED),
                )
            }
            face(pt(12, 26), listOf("C", ".C", "C"), mapOf('C' to STONE_CRACK))
            face(pt(36, 15), listOf("CC", ".C"), mapOf('C' to STONE_CRACK))
            eyes(pt(19.5, 24.5), pt(27, 24.5), open = listOf("OOO", "OWO", "OOO"))
            mouth(pt(23, 28), open = listOf("OOOO"))
        }

        /** A big ogre juggling far too many things. */
        private fun overwhelmOgre() {
            impact = pt(9, 32)
            headTop = pt(22, 13)
            koTilt = 0.28f
            koPivot = 32f
            val ogre = paint(OGRE, "ogre")
            val armPaint = paint(OGRE, "arm")

            capsule(pt(18, 38), pt(16.5, 44), 3f, ogre)
            capsule(pt(30, 38), pt(31.5, 44), 3f, ogre)
            ellipse(pt(15.5, 45), 4f, 1.7f, ogre)
            ellipse(pt(32.5, 45), 4f, 1.7f, ogre)
            ellipse(pt(24, 31), 12f, 9.5f, ogre)
            ellipse(pt(23, 33.5), 7f, 5.5f, paint(OGRE_BELLY, "belly"))
            poly(
                listOf(pt(12.5, 37), pt(35.5, 37), pt(33.5, 41.5), pt(14.5, 41.5)),
                paint(LOINCLOTH, "cloth"),
            )
            ellipse(pt(14.5, 20.5), 2f, 2.6f, paint(OGRE, "ear"))
            ellipse(pt(29.5, 20.5), 2f, 2.6f, paint(OGRE, "ear"))
            ellipse(pt(22, 20.5), 7f, 6.5f, ogre)
            val armsUp = !ko
            if (armsUp) {
                capsule(pt(14, 27), pt(7, 22), 2.6f, armPaint)
                capsule(pt(7, 22), pt(8, 15), 2.6f, armPaint, 2.2f)
                ellipse(pt(8, 14), 2.6f, 2.4f, armPaint)
                capsule(pt(34, 27), pt(41, 22), 2.6f, armPaint)
                capsule(pt(41, 22), pt(40, 15), 2.6f, armPaint, 2.2f)
                ellipse(pt(40, 14), 2.6f, 2.4f, armPaint)
                claws(pt(8, 11))
                claws(pt(40, 11))
            } else {
                capsule(pt(14, 27), pt(9, 36), 2.6f, armPaint)
                ellipse(pt(9, 37), 2.6f, 2.4f, armPaint)
                capsule(pt(34, 27), pt(39, 36), 2.6f, armPaint)
                ellipse(pt(39, 37), 2.6f, 2.4f, armPaint)
            }
            eyes(pt(18.5, 19), pt(24.5, 19))
            mouth(pt(21.5, 24), open = listOf("W...W", ".OOO."))
            // the juggled things; knocked out, they lie on the ground
            val things = listOf(::ball, ::book, ::mug, ::phone)
            if (ko) {
                grounded = true
                listOf(pt(5, 44), pt(43, 44), pt(10, 44.5), pt(37, 44.5)).forEachIndexed { i, at ->
                    things[i](at)
                }
                grounded = false
            } else {
                val turn = if (idle) f else 2
                things.forEachIndexed { i, draw ->
                    val a = 2 * PI * (i / 4.0 + turn / FRAMES.toDouble())
                    draw(pt(24 + 14 * cos(a), 7.5 + 4 * sin(a)))
                }
            }
        }

        private fun ball(c: Pt) = circle(c, 2.2f, paint(BALL_RED, "ball"))

        private fun book(c: Pt) {
            rect(c.x - 2.5f, c.y - 2f, c.x + 2.5f, c.y + 2f, paint(BOOK_BLUE, "book"))
            face(c + pt(0, 0), listOf("WWW"), mapOf('W' to WHITE))
        }

        private fun mug(c: Pt) {
            capsule(c + pt(2, -0.5), c + pt(2.5, 1), 0.9f, paint(MUG, "mug"))
            rect(c.x - 2f, c.y - 2f, c.x + 1.5f, c.y + 2f, paint(MUG, "mug"))
        }

        private fun phone(c: Pt) {
            rect(c.x - 1.5f, c.y - 2.5f, c.x + 1.5f, c.y + 2.5f, paint(PHONE, "phone"))
            face(c, listOf("S", "S"), mapOf('S' to SCREEN))
        }

        /** A lazy dragon lying on a "tomorrow" pillow, puffing little smoke rings. */
        private fun procrastinationDragon() {
            impact = pt(6, 31)
            headTop = pt(12, 25)
            koTilt = 0f
            koPivot = 24f
            val dragon = paint(DRAGON, "dragon")
            val head = paint(DRAGON, "head")
            val sway = if (idle) listOf(0f, 0.5f, 1f, 1.5f, 1f, 0.5f)[f % 6] else 0f

            grounded = true
            circle(pt(4, 44), 1.6f, paint(GOLD, "tassel"))
            circle(pt(44, 44), 1.6f, paint(GOLD, "tassel"))
            ellipse(pt(24, 41.5), 21f, 5f, paint(PILLOW, "pillow"))
            face(
                pt(31, 42),
                CALENDAR_PLUS_ONE,
                mapOf('R' to CALENDAR_RED, 'W' to WHITE, 'O' to INK),
            )
            grounded = false
            capsule(pt(36, 34), pt(41.5, 29), 2.4f, dragon, 1.8f)
            capsule(pt(41.5, 29), pt(43 + sway, 22.5), 1.8f, dragon, 1.2f)
            poly(
                listOf(pt(41 + sway, 23), pt(43.5 + sway, 18), pt(46 + sway, 22.5)),
                paint(SPIKE, "tip"),
            )
            val spike = paint(SPIKE, "spike")
            for (x in listOf(19f, 24f, 29f, 34f)) {
                poly(listOf(pt(x - 2, 29), pt(x, 25.5), pt(x + 2, 29)), spike)
            }
            ellipse(pt(27, 33), 11f, 5.5f, dragon)
            ellipse(pt(26, 36.5), 8f, 2.2f, paint(DRAGON_BELLY, "belly"))
            poly(listOf(pt(25, 30), pt(32, 23), pt(35, 30)), paint(WING, "wing"))
            poly(listOf(pt(10, 28), pt(12.5, 22.5), pt(14, 27.5)), paint(HORN, "horn"))
            poly(listOf(pt(14.5, 28), pt(18, 23.5), pt(18, 29)), paint(HORN, "horn"))
            ellipse(pt(12.5, 32), 7f, 5.5f, head)
            ellipse(pt(6.5, 34.5), 4.2f, 3.2f, head)
            ellipse(pt(8, 38.5), 3f, 1.8f, paint(DRAGON, "paw"))
            ellipse(pt(15, 38.8), 3f, 1.8f, paint(DRAGON, "paw"))
            claws(pt(5, 39), listOf("W", ".", "W"))
            claws(pt(12, 39.5), listOf("W", ".", "W"))
            face(pt(3.5, 33.5), listOf("O.O"), mapOf('O' to INK))
            eyes(pt(9, 30.5), pt(14.5, 30.5), open = SLEEPY, lid = DRAGON)
            mouth(pt(7, 36.5), open = listOf("OOOO."))
            if (idle) {
                val puff = mapOf('S' to SMOKE)
                val rise = f / 3
                effect(pt(2, 30 - rise), listOf(".S.", "S.S", ".S."), puff, move = false)
            }
        }

        // -- finishing ----------------------------------------------------------------------------

        fun finish(): PixelImage {
            if (hit && effects) {
                effect(impact + pt(-2, 0), IMPACT, mapOf('W' to WHITE, 'Y' to IMPACT_CORE))
            }
            if (ko && effects) {
                repeat(3) { i ->
                    val a = 2 * PI * i / 3 + f * PI / 6
                    val at = t(headTop) + Pt(7 * cos(a).toFloat(), -3f + 2 * sin(a).toFloat())
                    val x = at.x.roundToInt() - 1
                    val y = at.y.roundToInt() - 1
                    p.effect(x, y, STAR, mapOf('Y' to STAR_YELLOW, 'W' to WHITE))
                }
            }
            val big =
                if (guard && effects)
                    p.render(halo = GUARD_SHIMMER, haloLit = { x, y -> (x + y + f) % 3 != 0 })
                else p.render()
            val filled = big.indices.filter { big[it] != 0 }
            val left = filled.minOf { it % CANVAS }
            val right = filled.maxOf { it % CANVAS }
            val bottom = filled.maxOf { it / CANVAS }
            var x0 = PAD
            if (left < x0) x0 = left
            if (right > x0 + SIZE - 1) x0 = right - SIZE + 1
            // keep a free column on the right where there is room, so the guard shimmer fits
            if (right >= x0 + SIZE - 1 && left > x0) x0 = minOf(left, right - SIZE + 2)
            var y0 = PAD
            // knocked out opponents sink to the ground; a lean never pushes anyone below it
            if (ko || bottom > y0 + SIZE - 1) y0 = bottom - SIZE + 1
            cropX = x0
            cropY = y0
            val argb =
                IntArray(SIZE * SIZE) { i ->
                    val x = i % SIZE + x0
                    val y = i / SIZE + y0
                    if (x in 0 until CANVAS && y in 0 until CANVAS) big[y * CANVAS + x] else 0
                }
            return PixelImage(SIZE, SIZE, argb)
        }

        /** [launch] in pixels of the finished picture; call after [finish]. */
        fun launchInPicture(): Pt {
            val at = t(launch ?: impact)
            return Pt(at.x - cropX, at.y - cropY)
        }
    }

    // -- pixel patterns ---------------------------------------------------------------------------

    private val BEAN = listOf("OW", "OO", "OO")
    private val BIG = listOf(".OO.", "OOWO", "OOOO", ".OO.")
    private val DOT = listOf("OW", "OO")
    private val SLEEPY = listOf("LLL", "OOO", "OWO")
    private val SNEAKY = listOf("OW", "OW")
    private val SLIT_LEFT = listOf("OO..", "OOOO", "OGGO", ".OO.")
    private val SLIT_RIGHT = listOf("..OO", "OOOO", "OGGO", ".OO.")
    private val SLIT_LEFT_SMALL = listOf("O..", "OOO", "GGO")
    private val SLIT_RIGHT_SMALL = listOf("..O", "OOO", "OGG")
    private val SQUEEZE_LEFT = listOf("O..", ".OO", "O..")
    private val SQUEEZE_RIGHT = listOf("..O", "OO.", "..O")
    private val SPIRAL = listOf(".OO.", "O..O", "O.OO", ".O..")
    private val SMILE = listOf("O...O", ".OOO.")
    private val OOH = listOf(".O.", "O.O", ".O.")
    private val WOBBLE = listOf(".O.O.", "O.O.O")
    private val IMPACT = listOf("..W..", "W.W.W", ".WYW.", "WWYWW", ".WYW.", "W.W.W", "..W..")
    private val STAR = listOf(".Y.", "YWY", ".Y.")
    private val CALENDAR_PLUS_ONE = listOf("RRRRRRR", "WWOWWOW", "WOOOWOW", "WWOWWOW", "WWWWWWW")
    private val QUESTION = listOf(".MMM.", "MM.MM", "...MM", "..MM.", "..M..", ".....", "..M..")

    // -- colours: fixed, like the fighter's; the ink outline keeps them apart from any theme ----

    private val WHITE = argb(0xFFFFFF)
    private val CREAM = argb(0xFBF3E0)
    private val GOLD = argb(0xF2C14E)
    private val METAL = argb(0x9AA3B5)
    private val INK_SOFT = argb(0x3A3248)
    private val MOUTH = argb(0x7A2E35)
    private val THROAT = argb(0x3A1420)
    private val CLOCK_RED = argb(0xE8654F)
    private val BUTTON_PINK = argb(0xFF9EB8)
    private val GREMLIN = argb(0x8CC063)
    private val GREMLIN_BELLY = argb(0xD8EBA8)
    private val EAR_PINK = argb(0xF59AA0)
    private val CUSHION = argb(0x8E6CC9)
    private val IMP = argb(0xC46BD1)
    private val HORN = argb(0xF4E3C1)
    private val PHONE = argb(0x2C2C38)
    private val SCREEN = argb(0x58D7F0)
    private val SCREEN_LINE = argb(0xD8F7FF)
    private val JEANS = argb(0x4F78B8)
    private val SHIRT_RED = argb(0xE0605A)
    private val TOWEL = argb(0x5EC2A0)
    private val HOODIE = argb(0xF2C24A)
    private val TEE = argb(0x8FC7EE)
    private val SWEATER = argb(0xF08DB4)
    private val SOCK = argb(0xF4F1EA)
    private val SOCK_STRIPE = argb(0xE0605A)
    private val HYDRA = argb(0x4FA3A5)
    private val HYDRA_BELLY = argb(0xA9DCC9)
    private val ENVELOPE = argb(0xF7EEDC)
    private val ENVELOPE_FOLD = argb(0xC9B89A)
    private val STAMP = argb(0xE0605A)
    private val BUBBLE_RED = argb(0xEF5B5B)
    private val BUBBLE_ORANGE = argb(0xF59A3C)
    private val BUBBLE_BLUE = argb(0x4A8CE0)
    private val BUBBLE_PURPLE = argb(0x9B6CD9)
    private val BUBBLE_GREEN = argb(0x4FB878)
    private val BUBBLE_YELLOW = argb(0xF2C94A)
    private val BADGE = argb(0xD93B3B)
    private val BUZZ = argb(0xB0A8C8)
    private val CLOAK = argb(0x4B4E86)
    private val HOOD = argb(0x3C3F70)
    private val THIEF_SKIN = argb(0xE9CDB6)
    private val MASK = argb(0x2A2440)
    private val STONE = argb(0x9EA3AE)
    private val STONE_DARK = argb(0x7D828E)
    private val STONE_CRACK = argb(0x6A6E7A)
    private val PAPER = argb(0xF7F3EA)
    private val CALENDAR_RED = argb(0xE0505A)
    private val GRID = argb(0xC8C2B8)
    private val OGRE = argb(0x86B25E)
    private val OGRE_BELLY = argb(0xCFE0A0)
    private val LOINCLOTH = argb(0x9A6A44)
    private val BALL_RED = argb(0xEF5B5B)
    private val BOOK_BLUE = argb(0x4A78C8)
    private val MUG = argb(0xF2C94A)
    private val DRAGON = argb(0xF08A4B)
    private val DRAGON_BELLY = argb(0xFFE0A8)
    private val SPIKE = argb(0xF6C445)
    private val WING = argb(0xF7AE7C)
    private val PILLOW = argb(0x9CC8F0)
    private val SMOKE = argb(0xC8C4D4)
    private val EYE_GLOW = argb(0xFFD23C)
    private val CLAW = argb(0xF4F0E6)
    private val DUSK = argb(0x2B1D3A)
    private val IMPACT_CORE = argb(0xFFF2A8)
    private val STAR_YELLOW = argb(0xFFD34A)
}
