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
import com.shub39.grit.shared.ui.habit.fighter.PixelImage
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter.Companion.INK
import com.shub39.grit.shared.ui.habit.fighter.PixelPainter.Companion.argb

/** How an opponent's own attack crosses the stage to the fighter. */
enum class AttackPath {
    /** Flies straight from the opponent to the fighter. */
    SHOT,
    /** Thrown in an arc. */
    THROW,
    /** Thrown in an arc on a chain that stays in the opponent's hand. */
    CHAIN,
    /** Runs along the floor as a row of rock spikes. */
    GROUND,
    /** The opponent itself dashes over and hits up close; nothing flies. */
    RUSH,
}

/**
 * Every ladder opponent's own attack (decided 2026-11-07): what flies at the fighter and how. The
 * special is the same attack, twice as big. Pixel patterns in the opponents' colours, facing left
 * like the opponents; pure Kotlin, so it is unit tested and the fight screen only paints it.
 */
object OpponentAttack {

    /** Frames of the flicker loop, one every 100 ms like the fighters. */
    const val FRAMES = 2

    fun path(opponent: Opponent): AttackPath =
        when (opponent) {
            Opponent.SNOOZE_BUTTON -> AttackPath.SHOT
            Opponent.COUCH_GREMLIN -> AttackPath.THROW
            Opponent.DOOMSCROLL_IMP -> AttackPath.SHOT
            Opponent.LAUNDRY_MOUNTAIN -> AttackPath.THROW
            Opponent.INBOX_HYDRA -> AttackPath.SHOT
            Opponent.DISTRACTION_SWARM -> AttackPath.RUSH
            Opponent.TIME_THIEF -> AttackPath.CHAIN
            Opponent.DEADLINE_GOLEM -> AttackPath.GROUND
            Opponent.OVERWHELM_OGRE -> AttackPath.THROW
            Opponent.PROCRASTINATION_DRAGON -> AttackPath.SHOT
        }

    /**
     * The flying part of [opponent]'s attack in [frame], twice as big for the special; `null` for a
     * [AttackPath.RUSH], where the opponent hits with its own body.
     */
    fun render(opponent: Opponent, frame: Int = 0, special: Boolean = false): PixelImage? {
        val (rows, colors) = pattern(opponent, frame.mod(FRAMES)) ?: return null
        val scale = if (special) 2 else 1
        val width = rows.maxOf { it.length } * scale
        val height = rows.size * scale
        val argb =
            IntArray(width * height) { i ->
                val row = rows[i / width / scale]
                val c = row.getOrElse(i % width / scale) { '.' }
                if (c == '.') 0 else colors.getValue(c)
            }
        return PixelImage(width, height, argb)
    }

    /** The chain of [AttackPath.CHAIN], one link every other pixel. */
    val CHAIN_COLOR: Int = argb(0xC9A040)

    private fun pattern(opponent: Opponent, f: Int): Pair<List<String>, Map<Char, Int>>? =
        when (opponent) {
            // the alarm rings: sound waves roll over the stage
            Opponent.SNOOZE_BUTTON ->
                listOf(
                    "...Y...Y..",
                    "..Y...Y...",
                    ".Y...Y...Y",
                    ".Y...Y...Y",
                    "..Y...Y...",
                    "...Y...Y..",
                ) to mapOf('Y' to if (f == 0) RING else WHITE)
            // a sofa cushion, hurled
            Opponent.COUCH_GREMLIN ->
                (if (f == 0)
                    listOf(
                        "O........O",
                        ".OOOOOOOO.",
                        "OCCCCCCCCO",
                        "OCCcCCcCCO",
                        "OCCCCCCCCO",
                        "OCCcCCcCCO",
                        ".OOOOOOOO.",
                        "O........O",
                    )
                else
                    listOf(
                        ".O......O.",
                        "OOOOOOOOOO",
                        "OCCcCCcCCO",
                        "OCCCCCCCCO",
                        "OCCcCCcCCO",
                        "OOOOOOOOOO",
                        ".O......O.",
                    )) to mapOf('O' to INK, 'C' to CUSHION, 'c' to CUSHION_DARK)
            // a buzzing notification badge
            Opponent.DOOMSCROLL_IMP ->
                listOf(
                    "..OOOO..",
                    ".ORRRRO.",
                    "ORRWWRRO",
                    "ORRWWRRO",
                    "ORRWWRRO",
                    "ORRRRRRO",
                    "ORRWWRRO",
                    ".ORRRRO.",
                    "..OOOO..",
                ) to mapOf('O' to INK, 'R' to if (f == 0) BADGE else BADGE_GLOW, 'W' to WHITE)
            // a rolled-up sock, spinning
            Opponent.LAUNDRY_MOUNTAIN ->
                (if (f == 0)
                    listOf(
                        "..OOOO..",
                        ".OSSSSO.",
                        "OSRRRRSO",
                        "OSSSSSSO",
                        "OSRRRRSO",
                        ".OSSSSO.",
                        "..OOOO..",
                    )
                else
                    listOf(
                        "..OOOO..",
                        ".OSRSSO.",
                        "OSSRSRSO",
                        "OSRSRSSO",
                        "OSSRSRSO",
                        ".OSSRSO.",
                        "..OOOO..",
                    )) to mapOf('O' to INK, 'S' to SOCK, 'R' to SOCK_STRIPE)
            // an urgent letter
            Opponent.INBOX_HYDRA ->
                listOf(
                    "OOOOOOOOOO",
                    "OWOWWWWOWO",
                    "OWWOWWOWWO",
                    "OWWWOOWWWO",
                    "OWWWWWWRWO",
                    "OOOOOOOOOO",
                ) to mapOf('O' to INK, 'W' to PAPER, 'R' to if (f == 0) BADGE else PAPER)
            Opponent.DISTRACTION_SWARM -> null
            // the pocket watch on its chain
            Opponent.TIME_THIEF ->
                listOf(
                    "..OOOO..",
                    ".OGGGGO.",
                    "OGCCCCGO",
                    "OGCKCCGO",
                    "OGCKKCGO",
                    "OGCCCCGO",
                    ".OGGGGO.",
                    "..OOOO..",
                ) to
                    mapOf(
                        'O' to INK,
                        'G' to WATCH_GOLD,
                        'C' to if (f == 0) DIAL else WHITE,
                        'K' to INK,
                    )
            // a rock spike breaking out of the floor
            Opponent.DEADLINE_GOLEM ->
                listOf("...O...", "..OSO..", "..OSO..", ".OSSdO.", ".OSdSO.", "OSSSdSO") to
                    mapOf('O' to INK, 'S' to STONE, 'd' to if (f == 0) STONE_DARK else STONE)
            // a heavy book, tumbling
            Opponent.OVERWHELM_OGRE ->
                (if (f == 0) listOf("OOOOOOOOO", "OBBBBBBPO", "OBBYYBBPO", "OBBBBBBPO", "OOOOOOOOO")
                else listOf("..OOO..", ".OBBBO.", "OBBYBBO", "OBBBBBO", "OPPPPPO", ".OOOOO.")) to
                    mapOf('O' to INK, 'B' to BOOK, 'Y' to RING, 'P' to PAGES)
            // a ball of purple-orange fire, flickering at the tail
            Opponent.PROCRASTINATION_DRAGON ->
                listOf(
                    "...YYFF.....",
                    ".YYWWYFFF...",
                    "YWWWWYYFFFF.",
                    "YWWWWYYFFFFF",
                    ".YYWWYFFF...",
                    "...YYFF.....",
                ) to
                    mapOf('W' to WHITE, 'Y' to FLAME_CORE, 'F' to if (f == 0) FLAME else FLAME_EDGE)
        }

    private val WHITE = argb(0xFFFFFF)
    private val RING = argb(0xF2C14E)
    private val CUSHION = argb(0x8E6CC9)
    private val CUSHION_DARK = argb(0x6A4FA0)
    private val BADGE = argb(0xE0505A)
    private val BADGE_GLOW = argb(0xFF7A80)
    private val SOCK = argb(0xF4F1EA)
    private val SOCK_STRIPE = argb(0xE0605A)
    private val PAPER = argb(0xF7EEDC)
    private val WATCH_GOLD = argb(0xE0B040)
    private val DIAL = argb(0xFBF3E0)
    private val STONE = argb(0x9EA3AE)
    private val STONE_DARK = argb(0x6A6E7A)
    private val BOOK = argb(0x4A78C8)
    private val PAGES = argb(0xF4F0E6)
    private val FLAME_CORE = argb(0xFFD23C)
    private val FLAME = argb(0xB04CD9)
    private val FLAME_EDGE = argb(0xF08A4B)
}
