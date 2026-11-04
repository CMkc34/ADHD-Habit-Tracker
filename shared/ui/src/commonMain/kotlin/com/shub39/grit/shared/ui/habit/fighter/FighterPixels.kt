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
import com.shub39.grit.core.fighter.FighterItems
import com.shub39.grit.core.fighter.FighterLook
import com.shub39.grit.core.fighter.FighterMood
import com.shub39.grit.core.fighter.FighterRank

/** What the fighter is doing. A [FighterMood.TIRED] fighter always sits, whatever the pose. */
enum class FighterPose {
    /** Fighting stance with a light idle bounce. */
    STANCE,
    /** Gathering ki: a glowing aura and a ki ball between the hands. */
    CHARGE,
    /** Firing the ki blast, the special attack in the arena. */
    BLAST,
}

/** A finished pixel picture, row by row; each pixel is an ARGB colour, 0 is transparent. */
class PixelImage(val width: Int, val height: Int, internal val argb: IntArray) {
    operator fun get(x: Int, y: Int): Int = argb[y * width + x]

    val isBlank: Boolean
        get() = argb.all { it == 0 }
}

/**
 * Builds the fighter's pixel art (#6) from [FighterSprites]: picks the pose, puts the head, the
 * face, the rank upgrades and the gear on their anchors, adds the auras and resolves every colour
 * slot. Pure Kotlin, so it is unit tested and [FighterAvatar] only paints the result.
 */
object FighterPixels {
    const val SIZE = FighterSprites.SIZE

    /** Animation frames in one loop; [FighterAvatar] shows one every 100 ms. */
    const val FRAMES = 12

    fun hasWristWraps(rank: FighterRank) = rank >= FighterRank.BRAWLER

    fun hasHeadband(rank: FighterRank) = rank >= FighterRank.CHALLENGER

    fun hasEmblem(rank: FighterRank) = rank >= FighterRank.CHAMPION

    fun hasGoldenAura(rank: FighterRank) = rank == FighterRank.LEGEND

    fun render(
        look: FighterLook,
        rank: FighterRank,
        mood: FighterMood,
        pose: FighterPose = FighterPose.STANCE,
        outfit: String = FighterItems.DEFAULT_OUTFIT,
        gear: Set<String> = emptySet(),
        frame: Int = 0,
    ): PixelImage {
        val tired = mood == FighterMood.TIRED
        val p =
            when {
                tired -> FighterSprites.tired
                pose == FighterPose.CHARGE -> FighterSprites.charge
                pose == FighterPose.BLAST -> FighterSprites.blast
                else -> FighterSprites.stance
            }
        val f = frame.mod(FRAMES)
        val bounce = if (!tired && pose == FighterPose.STANCE && f >= FRAMES / 2) 1 else 0
        val golden = hasGoldenAura(rank)
        val canvas = Canvas()

        if (FighterItems.CAPE in gear) canvas.put(p.cape, p.capeAt.x, p.capeAt.y + bounce)

        // the legs stay on the ground, the upper body bounces
        canvas.put(p.body, p.bodyAt.x, p.bodyAt.y, rows = p.hipRow until p.body.height)
        canvas.put(p.body, p.bodyAt.x, p.bodyAt.y + bounce, rows = 0 until p.hipRow)
        canvas.beltStripes(rank.belt.goldStripes)

        val head = if (look == FighterLook.MIRA) FighterSprites.miraHead else FighterSprites.kaiHead
        val fx = p.headAt.x
        val fy = p.headAt.y + bounce
        canvas.put(head.sprite, fx - head.faceX, fy)
        with(FighterSprites) {
            val eye = if (tired) eyeDroopy else eyeOpen
            val eyeDy = if (tired) 1 else 0
            canvas.put(eye, fx + leftEye.x, fy + leftEye.y + eyeDy)
            canvas.put(eye, fx + rightEye.x, fy + rightEye.y + eyeDy)
            canvas.put(
                if (tired) sleepyMouth else smile,
                fx + mouthAt.x + if (tired) 1 else 0,
                fy + mouthAt.y,
            )
            if (hasHeadband(rank)) canvas.put(headband, fx + headbandAt.x, fy + headbandAt.y)
            if (FighterItems.SUNGLASSES in gear) {
                canvas.put(sunglasses, fx + sunglassesAt.x, fy + sunglassesAt.y)
            }
            if (FighterItems.CROWN in gear) canvas.put(crown, fx + crownAt.x, fy + crownAt.y)

            if (hasEmblem(rank)) canvas.put(emblem, p.emblemAt.x, p.emblemAt.y + bounce)
            if (FighterItems.CHAMPION_BELT in gear) {
                canvas.put(sash, p.sashAt.x, p.sashAt.y + bounce)
            }

            p.kiAt?.let { at ->
                val ball =
                    when {
                        pose == FighterPose.BLAST -> if (f % 2 == 0) kiBurst else kiBig
                        f % 4 < 2 -> kiSmall
                        else -> kiBig
                    }
                canvas.put(ball, at.x - ball.width / 2, at.y - ball.height / 2)
            }
        }

        when {
            !tired && pose == FighterPose.CHARGE -> canvas.kiFlames(f)
            golden -> canvas.goldenOutline(f)
        }

        if (tired) {
            val z = FighterSprites.sleepyZ
            canvas.put(z, fx + FighterSprites.sleepyZAt.x, fy + FighterSprites.sleepyZAt.y - f / 4)
        }

        val colors = Colors(look, rank, outfit, gear, golden)
        return PixelImage(SIZE, SIZE, IntArray(SIZE * SIZE) { colors.of(canvas.cells[it]) })
    }

    /** The flying ki blast for the arena, [FighterSprites.kiBlast] with a flickering tail. */
    fun renderKiBlast(golden: Boolean = false, frame: Int = 0): PixelImage {
        val sprite = FighterSprites.kiBlast
        val flicker = frame.mod(2) == 1
        val ki = if (golden) goldKi else tealKi
        val argb =
            IntArray(sprite.width * sprite.height) { i ->
                val x = i % sprite.width
                val c = sprite[x, i / sprite.width]
                when {
                    flicker && x < 3 && c == 'q' -> 0
                    c == 'K' -> ki[0]
                    c == 'k' -> ki[1]
                    c == 'q' -> ki[2]
                    else -> 0
                }
            }
        return PixelImage(sprite.width, sprite.height, argb)
    }

    /** Fill colour of each belt, also used by the unit tests to keep them apart. */
    fun beltColor(belt: Belt): Int = beltColors.getValue(belt).first

    /** The lower row of the belt, which keeps a belt apart from a gi of the same colour. */
    fun beltShade(belt: Belt): Int = beltColors.getValue(belt).second

    fun outfitColor(outfit: String): Int = outfitColors(outfit).first

    private class Canvas {
        val cells = CharArray(SIZE * SIZE) { EMPTY }

        operator fun get(x: Int, y: Int): Char =
            if (x in 0 until SIZE && y in 0 until SIZE) cells[y * SIZE + x] else EMPTY

        operator fun set(x: Int, y: Int, c: Char) {
            if (x in 0 until SIZE && y in 0 until SIZE) cells[y * SIZE + x] = c
        }

        fun put(sprite: Sprite, x: Int, y: Int, rows: IntRange = 0 until sprite.height) {
            for (row in rows) for (col in 0 until sprite.width) {
                val c = sprite[col, row]
                if (c != EMPTY) this[x + col, y + row] = c
            }
        }

        /** Gold stripes near the end of the belt (Master, Grandmaster). */
        fun beltStripes(count: Int) {
            if (count == 0) return
            val top = (0 until SIZE).firstOrNull { y -> (0 until SIZE).any { this[it, y] == 'B' } }
            if (top == null) return
            val end = (0 until SIZE).last { this[it, top] == 'B' }
            repeat(count) { i ->
                val x = end - 1 - 2 * i
                this[x, top] = 'Y'
                this[x, top + 1] = 'y'
            }
        }

        private fun filled(x: Int, y: Int) = this[x, y] != EMPTY

        private fun border(): List<Pair<Int, Int>> = buildList {
            for (y in 0 until SIZE) for (x in 0 until SIZE) {
                if (!filled(x, y) && NEIGHBOURS.any { (dx, dy) -> filled(x + dx, y + dy) }) {
                    add(x to y)
                }
            }
        }

        /** One pixel of shimmering gold around the Legend. */
        fun goldenOutline(frame: Int) {
            for ((x, y) in border()) if ((x + 2 * y + frame) % 6 != 0) this[x, y] = 'A'
        }

        /** A ki aura: a glowing outline with flames that lick upwards and flicker per frame. */
        fun kiFlames(frame: Int) {
            for ((x, y) in border()) this[x, y] = 'k'
            for (x in 0 until SIZE) {
                val top = (0 until SIZE).firstOrNull { filled(x, it) } ?: continue
                val height = ((x * 7 + frame * 5 + x * x) % 5).coerceAtMost(3)
                for (i in 1..height) this[x, top - i] = if (i == height) 'q' else 'k'
            }
            for ((x, y) in border()) if ((x + y + frame) % 3 == 0) this[x, y] = 'q'
        }
    }

    private class Colors(
        look: FighterLook,
        rank: FighterRank,
        outfit: String,
        gear: Set<String>,
        golden: Boolean,
    ) {
        private val skin = if (look == FighterLook.MIRA) miraSkin else kaiSkin
        private val hair = if (look == FighterLook.MIRA) miraHair else kaiHair
        private val gi = outfitColors(outfit)
        private val belt = beltColors.getValue(rank.belt)
        private val ki = if (golden) goldKi else tealKi
        private val gloves = FighterItems.GLOVES in gear
        private val sweatband = FighterItems.SWEATBAND in gear
        private val sneakers = FighterItems.SNEAKERS in gear
        private val wraps = hasWristWraps(rank)

        fun of(c: Char): Int =
            when (c) {
                EMPTY -> 0
                'O',
                'E' -> INK
                'W' -> WHITE
                'S' -> skin.first
                's' -> skin.second
                'R' -> BLUSH
                'H' -> hair.first
                'h' -> hair.second
                'T' -> HAIR_TIE
                'G' -> gi.first
                'g' -> gi.second
                'B' -> belt.first
                'b' -> belt.second
                'F' -> if (gloves) GLOVE else skin.first
                'f' ->
                    when {
                        gloves -> GLOVE_SHADE
                        wraps -> WRAP
                        else -> skin.second
                    }
                'X' ->
                    when {
                        sweatband -> SWEATBAND
                        wraps -> WRAP
                        else -> skin.first
                    }
                'P' -> if (sneakers) SNEAKER else skin.first
                'p' -> if (sneakers) SNEAKER_SOLE else skin.second
                'M' -> MOUTH
                'L' -> LENS
                'l' -> LENS_SHINE
                'D' -> HEADBAND
                'd' -> HEADBAND_SHADE
                'C' -> CAPE
                'c' -> CAPE_SHADE
                'Y',
                'A' -> GOLD
                'y' -> GOLD_SHADE
                'N' -> GEM
                'K' -> ki[0]
                'k' -> ki[1]
                'q' -> ki[2]
                'Z' -> SLEEPY_Z
                else -> error("unknown pixel '$c'")
            }
    }

    private const val EMPTY = '.'
    private val NEIGHBOURS = listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)

    // fixed palette: the fighter looks the same in light and dark theme, the ink outline keeps
    // every part apart
    private fun argb(rgb: Long): Int = (0xFF000000 or rgb).toInt()

    private val INK = argb(0x2B2235)
    private val WHITE = argb(0xFFFFFF)
    private val BLUSH = argb(0xFF8FA0)
    private val MOUTH = argb(0xA0323C)
    private val HAIR_TIE = argb(0xE0566E)
    private val GLOVE = argb(0xE04848)
    private val GLOVE_SHADE = argb(0xB03434)
    private val WRAP = argb(0xEFE9DA)
    private val SWEATBAND = argb(0x3FA9F5)
    private val SNEAKER = argb(0xE04848)
    private val SNEAKER_SOLE = argb(0xFFFFFF)
    private val LENS = argb(0x1E1A26)
    private val LENS_SHINE = argb(0x7A86A8)
    private val HEADBAND = argb(0x2FB5A0)
    private val HEADBAND_SHADE = argb(0x22877A)
    private val CAPE = argb(0x4D5BD6)
    private val CAPE_SHADE = argb(0x36419E)
    private val GOLD = argb(0xFFD34A)
    private val GOLD_SHADE = argb(0xC99A2A)
    private val GEM = argb(0xE0405A)
    private val SLEEPY_Z = argb(0xB8B0F0)

    private val kaiSkin = argb(0xE0A47A) to argb(0xC07F58)
    private val miraSkin = argb(0xF8D6BC) to argb(0xE2AF8E)
    private val kaiHair = argb(0x2E2A3A) to argb(0x57506E)
    private val miraHair = argb(0x6B3E2E) to argb(0x96614A)

    private val tealKi = listOf(argb(0xF4FFFB), argb(0x8FF3DA), argb(0x2FB5A0))
    private val goldKi = listOf(argb(0xFFFDE0), argb(0xFFE066), argb(0xF0A830))

    private val beltColors: Map<Belt, Pair<Int, Int>> =
        mapOf(
            Belt.WHITE to (argb(0xFAFAF7) to argb(0xC3C6D2)),
            Belt.YELLOW to (argb(0xF5D33F) to argb(0xC9A82A)),
            Belt.ORANGE to (argb(0xF28C28) to argb(0xC2691C)),
            Belt.GREEN to (argb(0x4FB35A) to argb(0x3A8A45)),
            Belt.BLUE to (argb(0x3F7FD9) to argb(0x2E5FA8)),
            Belt.PURPLE to (argb(0x9B5CD0) to argb(0x7642A3)),
            Belt.BROWN to (argb(0x8B5A3C) to argb(0x6A422B)),
            Belt.RED to (argb(0xD93B3B) to argb(0xA82C2C)),
            Belt.BLACK to (argb(0x26232E) to argb(0x15131A)),
            Belt.BLACK_ONE_STRIPE to (argb(0x26232E) to argb(0x15131A)),
            Belt.BLACK_TWO_STRIPES to (argb(0x26232E) to argb(0x15131A)),
            Belt.GOLD to (argb(0xD4A017) to argb(0xA87C0E)),
        )

    /** Unknown outfit ids fall back to the white gi. */
    private fun outfitColors(outfit: String): Pair<Int, Int> =
        when (outfit) {
            FighterItems.GI_BLUE -> argb(0x5B86D6) to argb(0x41679F)
            FighterItems.GI_RED -> argb(0xE0645A) to argb(0xAF463F)
            FighterItems.GI_BLACK -> argb(0x55566E) to argb(0x3C3D52)
            FighterItems.GI_PURPLE -> argb(0x9A72CC) to argb(0x7553A3)
            FighterItems.GI_GOLD -> argb(0xEBC65E) to argb(0xC39A3A)
            else -> argb(0xF4F3EE) to argb(0xC9CCD8)
        }
}
