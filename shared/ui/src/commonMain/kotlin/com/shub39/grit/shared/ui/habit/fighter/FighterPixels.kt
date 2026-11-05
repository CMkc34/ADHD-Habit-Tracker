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
package com.shub39.grit.shared.ui.habit.fighter

import com.shub39.grit.core.fighter.Belt
import com.shub39.grit.core.fighter.FighterCharacter
import com.shub39.grit.core.fighter.FighterColors
import com.shub39.grit.core.fighter.FighterItems
import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.core.fighter.FighterRank
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter.Companion.INK
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter.Companion.argb
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter.Companion.mix
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter.Companion.shade
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/** What the fighter is doing. A [FighterMood.TIRED] fighter always sits, whatever the pose. */
enum class FighterPose {
    /** Fighting stance with a light idle bounce. */
    STANCE,
    /** Gathering ki: a glowing, flickering aura and a ki ball at the hip. */
    CHARGE,
    /** Firing the ki blast, the special attack in the arena. */
    BLAST,
    /** A straight punch, one of the two looks of a strike in the arena (#14). */
    PUNCH,
    /** A side kick, the other look of a strike in the arena (#14). */
    KICK,
    /** Arms crossed to block, with a short shimmer of ki around the body (#14). */
    GUARD,
}

/** A finished pixel picture, row by row; each pixel is an ARGB colour, 0 is transparent. */
class PixelImage(val width: Int, val height: Int, internal val argb: IntArray) {
    operator fun get(x: Int, y: Int): Int = argb[y * width + x]

    val isBlank: Boolean
        get() = argb.all { it == 0 }
}

/**
 * Builds the fighter's pixel art (#6): one of the [FighterCharacter]s in the user's
 * [FighterColors], drawn on the [Skeleton] of the pose with the rank upgrades, the gear, the face
 * and the ki effects. Pure Kotlin, so it is unit tested and [FighterAvatar] only paints the result.
 */
object FighterPixels {
    const val SIZE = Skeleton.SIZE

    /** Animation frames in one loop; [FighterAvatar] shows one every 100 ms. */
    const val FRAMES = 12

    fun hasWristWraps(rank: FighterRank) = rank >= FighterRank.BRAWLER

    fun hasHeadband(rank: FighterRank) = rank >= FighterRank.CHALLENGER

    fun hasEmblem(rank: FighterRank) = rank >= FighterRank.CHAMPION

    fun hasGoldenAura(rank: FighterRank) = rank == FighterRank.LEGEND

    /** Every fighter has its own ki colour; the Legend's ki turns gold. */
    fun kiColor(character: FighterCharacter, rank: FighterRank = FighterRank.ROOKIE): Int =
        if (hasGoldenAura(rank)) GOLD
        else
            when (character) {
                FighterCharacter.WIND_MONK -> argb(0xFF9A3C)
                FighterCharacter.STREET_BRAWLER -> argb(0x4FD1B0)
                FighterCharacter.SHADOW_RUNNER -> argb(0xB06CFF)
                FighterCharacter.SPARK_PRODIGY -> argb(0x58D7F0)
                FighterCharacter.KARATEKA -> argb(0xE0405A)
            }

    fun render(
        character: FighterCharacter,
        rank: FighterRank,
        mood: FighterMood,
        colors: FighterColors = character.defaultColors,
        pose: FighterPose = FighterPose.STANCE,
        gear: Set<String> = emptySet(),
        frame: Int = 0,
        outfit: String? = null,
    ): PixelImage {
        val tired = mood == FighterMood.TIRED
        val f = frame.mod(FRAMES)
        val base =
            when {
                tired -> Skeleton.sitting
                pose == FighterPose.CHARGE -> Skeleton.charge
                pose == FighterPose.BLAST -> Skeleton.blast
                pose == FighterPose.PUNCH -> Skeleton.punch
                pose == FighterPose.KICK -> Skeleton.kick
                pose == FighterPose.GUARD -> Skeleton.guard
                else -> Skeleton.stance
            }
        val bounce = !tired && pose == FighterPose.STANCE && f >= FRAMES / 2
        val s = base.bounced(if (bounce) 1f else 0f)
        val main = outfitColor(outfit) ?: colors.mainColor
        val drawing = Drawing(character, colors, main, rank, gear, s, tired)
        drawing.draw()

        val ki = kiColor(character, rank)
        val charging = !tired && pose == FighterPose.CHARGE
        val guarding = !tired && pose == FighterPose.GUARD
        val golden = hasGoldenAura(rank)
        val p = drawing.painter
        val at = s.ki
        if (at != null && !tired) {
            val pulse = if (pose == FighterPose.BLAST) f % 2 else f % 4 / 2
            kiBall(p, at, s.kiRadius + 0.8f * pulse, ki, f)
        }
        if (tired) {
            p.effect(
                s.head.px + 9,
                s.head.py - 12 - f / 4,
                listOf("ZZZ", ".Z.", "ZZZ"),
                mapOf('Z' to SLEEPY_Z),
            )
        }
        val argb =
            when {
                charging ->
                    p.render(halo = mix(ki, WHITE, 0.35f), beforeEffects = { kiFlames(it, ki, f) })
                // a thin, sparkling ki skin that shimmers as the light runs over it
                guarding ->
                    p.render(
                        halo = mix(ki, WHITE, 0.5f),
                        haloLit = { x, y -> (x + y + 2 * f) % 4 != 0 },
                    )
                golden -> p.render(halo = GOLD, haloLit = { x, y -> (x + 2 * y + f) % 6 != 0 })
                else -> p.render()
            }
        return PixelImage(SIZE, SIZE, argb)
    }

    /** The flying ki blast of [character] for the arena, 24 x 12 with a flickering tail. */
    fun renderKiBlast(
        character: FighterCharacter,
        rank: FighterRank = FighterRank.ROOKIE,
        frame: Int = 0,
    ): PixelImage {
        val ki = kiColor(character, rank)
        val core = mix(ki, WHITE, 0.85f)
        val light = mix(ki, WHITE, 0.45f)
        val flicker = frame.mod(2) == 1
        val width = kiBlast.first().length
        val argb =
            IntArray(width * kiBlast.size) { i ->
                val x = i % width
                when (kiBlast[i / width][x]) {
                    'K' -> core
                    'k' -> light
                    'q' -> if (flicker && x < 3) 0 else ki
                    else -> 0
                }
            }
        return PixelImage(width, kiBlast.size, argb)
    }

    /** The main colour of a shop outfit, `null` for no or an unknown outfit. */
    fun outfitColor(outfit: String?): Int? =
        when (outfit) {
            FighterItems.GI_WHITE -> argb(0xF4F1EA)
            FighterItems.GI_BLUE -> argb(0x4A78C8)
            FighterItems.GI_RED -> argb(0xD9524A)
            FighterItems.GI_BLACK -> argb(0x3A3B4E)
            FighterItems.GI_PURPLE -> argb(0x8E62C4)
            FighterItems.GI_GOLD -> argb(0xE8BE4A)
            else -> null
        }

    /** Fill colour of each belt, also used by the unit tests to keep them apart. */
    fun beltColor(belt: Belt): Int = beltColors.getValue(belt)

    private fun kiBall(p: PixelPainter, c: Pt, r: Float, ki: Int, frame: Int) {
        val core = mix(ki, WHITE, 0.85f)
        val light = mix(ki, WHITE, 0.45f)
        val edge = shade(ki)
        for (y in 0 until SIZE) for (x in 0 until SIZE) {
            val d = hypot(x + 0.5f - c.x, y + 0.5f - c.y)
            val color =
                when {
                    d < r * 0.45f -> core
                    d < r * 0.72f -> light
                    d < r -> ki
                    d < r + 1.2f && (x + y + frame) % 2 == 0 -> edge
                    else -> null
                }
            if (color != null) p.effectPixel(x, y, color)
        }
        repeat(4) { i ->
            val angle = (i * 0.5 + frame * 0.13) * PI
            val dist = r + 2.5f
            p.effectPixel(
                (c.x + cos(angle) * dist).toInt(),
                (c.y + sin(angle) * dist).toInt(),
                light,
            )
        }
    }

    /** Flames that lick upwards from the top of the aura and flicker per frame. */
    private fun kiFlames(out: IntArray, ki: Int, frame: Int) {
        val light = mix(ki, WHITE, 0.35f)
        for (x in 0 until SIZE) {
            val top = (0 until SIZE).firstOrNull { out[it * SIZE + x] != 0 } ?: continue
            val height = ((x * 7 + frame * 5 + x * x) % 6).coerceAtMost(4)
            for (i in 1..height) {
                val y = top - i
                if (y >= 0) out[y * SIZE + x] = if (i == height) ki else light
            }
        }
    }

    /** Draws one character on one skeleton. */
    private class Drawing(
        val character: FighterCharacter,
        colors: FighterColors,
        mainColor: Int,
        val rank: FighterRank,
        val gear: Set<String>,
        val s: Skeleton,
        val tired: Boolean,
    ) {
        val painter = PixelPainter(SIZE)
        private val p = painter
        private val h = s.head

        private val skin = Paint(colors.skinColor, "skin")
        private val hair = Paint(colors.hairColor, "hair")
        private val main = Paint(mainColor, "main")
        private val accent = Paint(colors.accentColor, "accent")
        private val iris = mix(colors.accentColor, INK, 0.5f)
        private val belt = Paint(beltColors.getValue(rank.belt), "belt")
        private val wrap = Paint(WRAP, "wrap")
        private val karate = character == FighterCharacter.KARATEKA

        private fun at(dx: Number, dy: Number) = h + Pt(dx, dy)

        fun draw() {
            if (FighterItems.CAPE in gear) cape()
            behind()
            legs()
            feet()
            arm(s.backArm, front = false)
            torso()
            belt()
            if (FighterItems.CHAMPION_BELT in gear) sash()
            arm(s.frontArm, front = true)
            afterArms()
            p.ellipse(h, 8.4f, 8f, skin)
            head()
            if (hasHeadband(rank)) headband()
            face()
            if (hasEmblem(rank)) emblem()
            if (FighterItems.SUNGLASSES in gear) sunglasses()
            if (FighterItems.CROWN in gear) crown()
            if (rank.belt.goldStripes > 0) beltStripes(rank.belt.goldStripes)
        }

        private fun cape() {
            val l = s.leftShoulder
            val r = s.rightShoulder
            val paint = Paint(CAPE, "cape")
            val points =
                if (s.sitting) listOf(l + Pt(-1, 0), r + Pt(1, 0), r + Pt(4, 10), l + Pt(-4, 10))
                else listOf(l + Pt(1, 0), r + Pt(-2, 0), r + Pt(-5, 13), l + Pt(-7, 14))
            p.poly(points, paint)
        }

        /** Parts behind the body: the monk's scarf tail, the runner's headband tails. */
        private fun behind() {
            when (character) {
                FighterCharacter.WIND_MONK -> {
                    val root = s.leftShoulder + Pt(2, 0)
                    val mid = root + Pt(-11, 4)
                    p.capsule(root, mid, 2.2f, accent, 1.6f)
                    p.capsule(mid, root + Pt(-15, 9), 1.6f, accent, 1.2f)
                }
                FighterCharacter.SHADOW_RUNNER -> {
                    p.capsule(at(-8, -4), at(-17, -1), 1.6f, accent, 1.1f)
                    p.capsule(at(-8, -3), at(-18, 3), 1.5f, accent, 1.0f)
                }
                else -> Unit
            }
        }

        private fun pants(): Paint =
            when (character) {
                FighterCharacter.WIND_MONK -> Paint(MONK_PANTS, "pants")
                FighterCharacter.STREET_BRAWLER -> Paint(JOGGERS, "pants")
                FighterCharacter.SPARK_PRODIGY -> Paint(GI_WHITE, "pants")
                FighterCharacter.SHADOW_RUNNER,
                FighterCharacter.KARATEKA -> main
            }

        private fun legs() {
            val paint = pants()
            val (r1, r2, r3, r4) =
                when (character) {
                    FighterCharacter.WIND_MONK -> listOf(3.2f, 3.6f, 3.4f, 2.6f)
                    FighterCharacter.KARATEKA -> listOf(2.9f, 2.7f, 2.6f, 2.4f)
                    else -> listOf(2.6f, 2.4f, 2.2f, 2.0f)
                }
            for (leg in listOf(s.backLeg, s.frontLeg)) {
                p.capsule(leg.root, leg.joint, r1, paint, r2)
                p.capsule(leg.joint, leg.end, r3, paint, r4)
            }
        }

        private fun feet() {
            val sneakers = FighterItems.SNEAKERS in gear
            val paint =
                when {
                    sneakers -> Paint(SNEAKER, "shoe")
                    character == FighterCharacter.WIND_MONK -> Paint(SANDAL, "shoe")
                    character == FighterCharacter.STREET_BRAWLER -> Paint(GI_WHITE, "shoe")
                    karate -> skin
                    else -> Paint(DARK_SHOE, "shoe")
                }
            val rx = if (s.sitting) 2.6f else 3.3f
            for (foot in listOf(s.backFoot, s.frontFoot)) {
                p.ellipse(foot, rx, 1.8f, paint)
                val y = foot.py
                when {
                    sneakers -> p.face(foot.px - 3, y, listOf("WWWWWW"), mapOf('W' to WHITE))
                    character == FighterCharacter.STREET_BRAWLER ->
                        p.face(foot.px - 2, y, listOf("AAAA"), mapOf('A' to accent.color))
                }
            }
        }

        private fun arm(limb: Limb, front: Boolean) {
            val sleeve = if (character == FighterCharacter.WIND_MONK) skin else main
            fun along(t: Float) = limb.joint.lerp(limb.end, t)
            p.capsule(limb.root, limb.joint, if (karate) 2.5f else 2.3f, sleeve)
            p.capsule(limb.joint, limb.end, 2.0f, if (karate) skin else sleeve)
            // the karateka's rolled sleeves end just below the elbow
            if (karate) p.capsule(limb.joint, along(0.22f), 2.4f, main)
            when (character) {
                FighterCharacter.WIND_MONK -> p.capsule(along(0.35f), limb.end, 2.1f, wrap)
                FighterCharacter.SHADOW_RUNNER ->
                    p.capsule(along(0.3f), along(0.85f), 2.3f, Paint(GUARD, "guard"))
                else -> Unit
            }
            if (hasWristWraps(rank) && character != FighterCharacter.WIND_MONK) {
                p.capsule(along(0.6f), along(0.9f), 2.15f, wrap)
            }
            if (FighterItems.SWEATBAND in gear) {
                p.capsule(along(0.55f), along(0.75f), 2.4f, Paint(SWEATBAND, "sweatband"))
            }
            val hand =
                when {
                    FighterItems.GLOVES in gear -> Paint(GLOVE, "glove")
                    character == FighterCharacter.STREET_BRAWLER -> Paint(FINGERLESS, "glove")
                    else -> skin
                }
            if (front) p.ellipse(limb.end, 2.6f, 2.4f, hand)
            else p.ellipse(limb.end, 2.2f, 2.1f, hand)
        }

        private fun torso() {
            val l = s.leftShoulder
            val r = s.rightShoulder
            val height = s.beltLeft.y - l.y + 2
            fun strip(from: Float, to: Float) =
                listOf(l + Pt(from, 0f), l + Pt(to, 0f), l + Pt(to, height), l + Pt(from, height))
            when (character) {
                FighterCharacter.SPARK_PRODIGY -> {
                    p.poly(s.torso, Paint(GI_WHITE, "gi"))
                    p.poly(listOf(l, l + Pt(4, 0), l + Pt(4, 7), l + Pt(1, 7)), main)
                    p.poly(listOf(r + Pt(-4, 0), r, r + Pt(-1, 7), r + Pt(-4, 7)), main)
                }
                FighterCharacter.STREET_BRAWLER -> {
                    p.poly(s.torso, main)
                    p.poly(strip(5f, 9f), Paint(GI_WHITE, "shirt"))
                    p.poly(strip(3f, 5f), accent)
                }
                FighterCharacter.KARATEKA -> {
                    p.poly(s.torso, main)
                    p.poly(listOf(l + Pt(5, 0), l + Pt(9, 0), l + Pt(7, 5)), skin)
                    p.capsule(l + Pt(4, 0), l + Pt(8, 8), 0.6f, accent)
                    p.capsule(r + Pt(-4, 0), l + Pt(7, 6), 0.6f, accent)
                }
                else -> p.poly(s.torso, main)
            }
        }

        private fun belt() {
            val l = s.beltLeft
            val r = s.beltRight
            p.poly(listOf(l, r, r + Pt(-1f, 2.6f), l + Pt(1f, 2.6f)), belt)
            val knot = l.lerp(r, 0.55f)
            p.capsule(knot + Pt(0f, 2.5f), knot + Pt(-1f, 5.5f), 0.6f, belt)
            p.capsule(knot + Pt(1f, 2.5f), knot + Pt(2f, 5f), 0.6f, belt)
        }

        private fun beltStripes(count: Int) {
            repeat(count) { i ->
                val x = s.beltRight.px - 3 - 2 * i
                p.face(x, s.beltRight.py, listOf("Y", "Y", "Y"), mapOf('Y' to GOLD))
            }
        }

        private fun sash() {
            val gold = Paint(GOLD, "gold")
            val from = s.rightShoulder + Pt(-1, 0)
            val to = s.beltLeft + Pt(1, 0)
            p.capsule(from, to, 1.3f, gold)
            val plate = from.lerp(to, 0.45f)
            p.ellipse(plate, 2.6f, 2.0f, gold)
            p.face(plate.px, plate.py, listOf("N"), mapOf('N' to GEM))
        }

        /** Parts in front of both arms: the monk's scarf, the brawler's hood. */
        private fun afterArms() {
            val l = s.leftShoulder
            val r = s.rightShoulder
            when (character) {
                FighterCharacter.WIND_MONK -> p.capsule(l + Pt(2, 0), r + Pt(-1, 0), 2.2f, accent)
                FighterCharacter.STREET_BRAWLER ->
                    p.capsule(l + Pt(0, 1), r + Pt(0, 1), 1.8f, main, 2.0f)
                else -> Unit
            }
        }

        private fun head() {
            when (character) {
                FighterCharacter.WIND_MONK -> {
                    p.ellipse(at(0, -4.5), 8.2f, 4.6f, hair)
                    p.poly(listOf(at(-8.2, -2), at(-6, -6), at(-3, -3), at(-7, 1)), hair)
                    p.ellipse(at(0, -10.8), 2.6f, 2.4f, hair)
                    p.poly(listOf(at(-2, -8), at(2, -8), at(1, -10), at(-1, -10)), accent)
                }
                FighterCharacter.STREET_BRAWLER -> {
                    p.poly(BRAWLER_HAIR.map { (dx, dy) -> at(dx, dy) }, hair)
                    p.poly(listOf(at(5, -11.5), at(11, -13), at(8, -9)), hair)
                    p.poly(listOf(at(-7, -10), at(-11, -11), at(-9, -7)), hair)
                }
                FighterCharacter.SHADOW_RUNNER -> {
                    p.capsule(at(-8, 0), at(-11, 3), 1.8f, hair)
                    p.ellipse(at(0, -4), 8.6f, 6.0f, Paint(mix(main.color, INK, 0.2f), "hood"))
                    p.poly(
                        listOf(at(-8.4, 1), at(8.4, 1), at(8, 5), at(5, 8), at(-5, 8), at(-8, 5)),
                        Paint(mix(main.color, INK, 0.3f), "mask"),
                    )
                    p.poly(listOf(at(-8.4, -4), at(8.4, -4), at(8.4, -2), at(-8.4, -2)), accent)
                }
                FighterCharacter.SPARK_PRODIGY -> {
                    for ((dx, dy, r) in CLOUD) p.ellipse(at(dx, dy), r, r * 0.9f, hair)
                    p.poly(listOf(at(-8.4, -7), at(8.4, -7), at(8.4, -5), at(-8.4, -5)), accent)
                    val lens = Paint(LENS, "lens")
                    p.ellipse(at(-3, -6), 2.4f, 2.2f, lens)
                    p.ellipse(at(3, -6), 2.4f, 2.2f, lens)
                }
                FighterCharacter.KARATEKA -> {
                    p.ellipse(at(0, -4.5), 8.4f, 4.6f, hair)
                    p.poly(listOf(at(-6, -7), at(-4, -11.5), at(-1, -8)), hair)
                    p.poly(listOf(at(-2, -8), at(1.5, -12.5), at(3, -8)), hair)
                    p.poly(listOf(at(2, -8), at(6.5, -11), at(7, -6)), hair)
                    p.poly(KARATEKA_FRINGE.map { (dx, dy) -> at(dx, dy) }, hair)
                    p.poly(listOf(at(-8.4, -3), at(-6.5, -3), at(-7.5, 1)), hair)
                }
            }
        }

        private fun headband() {
            val band = Paint(HEADBAND, "band")
            p.capsule(at(-8.2, -2.6), at(8.2, -2.6), 0.9f, band)
            p.capsule(at(-8, -2.4), at(-12, 0.5), 0.9f, band, 0.6f)
        }

        private fun face() {
            val ex = h.px - 4
            val ey = h.py - 1
            val ink = mapOf('O' to INK)
            if (tired) {
                for (x in listOf(ex, ex + 6)) {
                    p.face(x, ey + 1, listOf("OOO"), ink)
                    p.face(x, ey - 2, listOf("OOO"), ink)
                }
            } else {
                val eye =
                    if (character == FighterCharacter.SHADOW_RUNNER) listOf("OOO", "WII")
                    else listOf("OOO", "OWI", "OII")
                val colors = mapOf('O' to INK, 'W' to WHITE, 'I' to iris)
                p.face(ex, ey, eye, colors)
                p.face(ex + 6, ey, eye, colors)
                p.face(ex - 1, ey - 2, listOf("OOO.", "O..."), ink)
                p.face(ex + 6, ey - 2, listOf(".OOO", "...O"), ink)
            }
            if (character == FighterCharacter.SHADOW_RUNNER) return
            if (tired) p.face(ex + 4, ey + 5, listOf("O"), mapOf('O' to MOUTH))
            else p.face(ex + 3, ey + 4, listOf("O..O", ".OO."), mapOf('O' to MOUTH))
            p.face(ex - 1, ey + 4, listOf("R"), mapOf('R' to BLUSH))
            p.face(ex + 9, ey + 4, listOf("R"), mapOf('R' to BLUSH))
        }

        private fun emblem() {
            val at = s.leftShoulder + Pt(3, 3)
            p.face(at.px, at.py, listOf(".Y.", "YNY", ".Y."), mapOf('Y' to GOLD, 'N' to GEM))
        }

        private fun sunglasses() {
            p.face(
                h.px - 5,
                h.py - 1,
                listOf("OLLLOOOLLLO", ".LlL...LlL."),
                mapOf('O' to INK, 'L' to LENS_DARK, 'l' to LENS_SHINE),
            )
        }

        private fun crown() {
            val top =
                when (character) {
                    FighterCharacter.WIND_MONK,
                    FighterCharacter.SPARK_PRODIGY -> -16
                    FighterCharacter.STREET_BRAWLER -> -15
                    FighterCharacter.SHADOW_RUNNER,
                    FighterCharacter.KARATEKA -> -12
                }
            p.face(
                h.px - 3,
                h.py + top,
                listOf("Y.YY.Y", "YNYYNY", "yyyyyy"),
                mapOf('Y' to GOLD, 'y' to GOLD_SHADE, 'N' to GEM),
            )
        }
    }

    private val CLOUD =
        listOf(
            Triple(-7f, -5f, 4f),
            Triple(-3f, -9f, 4.5f),
            Triple(3f, -9.5f, 4.6f),
            Triple(8f, -6f, 4f),
            Triple(0f, -7f, 5f),
            Triple(-9f, -1f, 2.6f),
            Triple(9f, -1.5f, 2.6f),
        )

    private val BRAWLER_HAIR =
        listOf(
            -9f to -1f,
            -10f to -7f,
            -6f to -11f,
            -1f to -12.5f,
            5f to -11.5f,
            9f to -7f,
            9f to -1f,
            6f to -5f,
            3f to -4f,
            0f to -7f,
            -4f to -4f,
            -6f to -5f,
        )

    private val KARATEKA_FRINGE =
        listOf(
            -8.4f to -4f,
            -1f to -4f,
            3f to -2.5f,
            5f to -4f,
            8.6f to -3.5f,
            8.6f to -6f,
            -8.6f to -6f,
        )

    private val kiBlast =
        listOf(
            "...............qqqq.....",
            ".............qqkkkkqq...",
            "..........qqqkkKKKKkkq..",
            ".......qqqkkkKKKKKKKkkq.",
            "...qqqqkkkkKKKKKKKKKKkq.",
            "qqqkkkkKKKKKKKKKKKKKKkkq",
            "qqqkkkkKKKKKKKKKKKKKKkkq",
            "...qqqqkkkkKKKKKKKKKKkq.",
            ".......qqqkkkKKKKKKKkkq.",
            "..........qqqkkKKKKkkq..",
            ".............qqkkkkqq...",
            "...............qqqq.....",
        )

    // fixed colours; the fighter looks the same in light and dark theme, the ink outline keeps it
    // apart from any background
    private val WHITE = argb(0xFFFFFF)
    private val GOLD = argb(0xFFD34A)
    private val GOLD_SHADE = argb(0xC99A2A)
    private val GEM = argb(0xE0405A)
    private val MOUTH = argb(0x7A2E35)
    private val BLUSH = argb(0xF59AA0)
    private val SLEEPY_Z = argb(0xB8B0F0)
    private val WRAP = argb(0xEFE6D2)
    private val GLOVE = argb(0xE04848)
    private val FINGERLESS = argb(0x2C2C38)
    private val SWEATBAND = argb(0x3FA9F5)
    private val SNEAKER = argb(0xE04848)
    private val CAPE = argb(0x4D5BD6)
    private val HEADBAND = argb(0xB8323F)
    private val LENS = argb(0x58D7F0)
    private val LENS_DARK = argb(0x1E1A26)
    private val LENS_SHINE = argb(0x7A86A8)
    private val GUARD = argb(0x8A93A8)
    private val GI_WHITE = argb(0xF4F1EA)
    private val MONK_PANTS = argb(0x3E7F8C)
    private val JOGGERS = argb(0x3A3F55)
    private val SANDAL = argb(0x5A4636)
    private val DARK_SHOE = argb(0x1E2230)

    private val beltColors: Map<Belt, Int> =
        mapOf(
            Belt.WHITE to argb(0xFAFAF7),
            Belt.YELLOW to argb(0xF5D33F),
            Belt.ORANGE to argb(0xF28C28),
            Belt.GREEN to argb(0x4FB35A),
            Belt.BLUE to argb(0x3F7FD9),
            Belt.PURPLE to argb(0x9B5CD0),
            Belt.BROWN to argb(0x8B5A3C),
            Belt.RED to argb(0xD93B3B),
            Belt.BLACK to argb(0x26232E),
            Belt.BLACK_ONE_STRIPE to argb(0x26232E),
            Belt.BLACK_TWO_STRIPES to argb(0x26232E),
            Belt.GOLD to argb(0xD4A017),
        )
}
