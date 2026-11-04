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

/**
 * A hand-drawn pixel grid. Every character is one pixel, `.` is transparent; the other characters
 * name a colour slot that [FighterPixels] resolves (skin, gi, belt, gear and so on). Keeping the
 * slots instead of colours lets one grid serve both looks, every outfit and every rank.
 *
 * Slots used in the grids:
 * - `O` ink outline, `S`/`s` skin and its shade, `R` blush, `H`/`h` hair, `T` hair tie
 * - `G`/`g` gi and its shade, `B`/`b` belt and its shade
 * - `F`/`f` fist (gloves), `X` wrist (wraps, sweatband), `P`/`p` foot (sneakers)
 * - overlays: `E` eye, `W` eye shine, `M` mouth, `L`/`l` lens, `D`/`d` headband, `C`/`c` cape,
 *   `Y`/`y` gold, `N` red gem, `K`/`k`/`q` ki energy, `A` golden aura, `Z` the sleepy "z"
 */
internal class Sprite(val rows: List<String>) {
    val width: Int = rows.maxOf { it.length }
    val height: Int = rows.size

    init {
        require(rows.all { it.length == width }) { "all rows of a sprite need the same width" }
    }

    operator fun get(x: Int, y: Int): Char = rows[y][x]
}

/** A pixel position in the 40 x 40 fighter canvas. */
internal data class Px(val x: Int, val y: Int)

/**
 * One body pose with its fixed anchor points. The anchors are where the head, the cape, the
 * champion belt and the rank emblem go; hands, wrists and feet are marked in the grid itself, so
 * gear on them fits by construction.
 */
internal class Pose(
    val body: Sprite,
    val bodyAt: Px,
    val headAt: Px,
    /** Rows of [body] from here on (the legs) stay put while the upper body bounces. */
    val hipRow: Int,
    val cape: Sprite,
    val capeAt: Px,
    val sashAt: Px,
    val emblemAt: Px,
    /** Centre of the ki ball, for the poses that hold one. */
    val kiAt: Px? = null,
)

internal object FighterSprites {
    /** Width and height of the canvas everything is drawn into. */
    const val SIZE = 40

    private val legs =
        listOf(
            ".....OGGGBObBGGGGO......",
            "....OGGGGBOOBGGGGGO.....",
            "...OGGGGGOO.OOGGGGO.....",
            "..OGGGGgO.....OGGGGO....",
            ".OGGGGgO.......OGGGGO...",
            ".OPPPPO........OOPPPPO..",
            "OpppppO........OppppppO.",
            "OOOOOOO........OOOOOOOO.",
        )

    /** Fighting stance, fists up, turned a little to the right. */
    private val stanceBody =
        Sprite(
            listOf(
                "..........OOOO.....OOOO.",
                "........OOGSSGOO..OFFFFO",
                "......OOGGgSSgGGOOOFfFfO",
                ".....OGGGGGgSGGGGGOOXXO.",
                "....OGGGGGGgGGGGGOGGGGO.",
                "....OGGGGGGgGGGGOGGGGGO.",
                "...OGGgOOOOOgGGOGGGGgO..",
                "...OGgOXFfFfOGGGOgggO...",
                "...OGgOXFFFFOGGGOOOO....",
                "...OgggOOOOOggggO.......",
                "....OOOBBBBBBBBBO.......",
                "......ObbbbBbbbbO.......",
            ) + legs
        )

    /** Both hands cupped at the hip around a growing ki ball. */
    private val chargeBody =
        Sprite(
            listOf(
                "..........OOOO..........",
                "........OOGSSGOO........",
                "......OOGGgSSgGGOO......",
                ".....OGGGGGgSGGGGGO.....",
                "....OGGGGGGgGGGGGGGO....",
                "...OGGgGGGGgGGOOGGGO....",
                "..OGGgOGGGGgGOGGGGO.....",
                ".OFFOOGGGGGgOGGGGO......",
                ".OFfFOGGGGGgOOOOO.......",
                "..OXXOgggggggggO........",
                "...OOOOBBBBBBBBBO.......",
                "......ObbbbBbbbbO.......",
            ) + legs
        )

    /** Both arms pushed forward, palms open: the ki blast leaves the hands. */
    private val blastBody =
        Sprite(
            listOf(
                "..........OOOO...............",
                "........OOGSSGOOOOOOOOOO.....",
                "......OOGGgSSgGGGGGGGGGGOOOO.",
                ".....OGGGGGgSGGGGgggggGGOXFFO",
                "....OGGGGGGgGGGGOOOOOOOOOXFFO",
                "....OGGGGGGgGGGGGGGGGGGGOXFFO",
                "....OGGGGGGgGGGGgggggGGGOXFfO",
                "....OGGGGGGgGGGGOOOOOOOOOOOO.",
                "....OGGGGGGgGGGGO............",
                "....OgggggggggggO............",
                "....OOOBBBBBBBBBO............",
                "......ObbbbBbbbbO............",
                ".....OGGGBObBGGGGO...........",
                "....OGGGGBOOBGGGGGO..........",
                "...OGGGGGOO.OOGGGGGO.........",
                "..OGGGGgO.....OGGGGGO........",
                ".OGGGGgO.......OGGGGGO.......",
                ".OPPPPO.........OOPPPPO......",
                "OpppppO.........OppppppO.....",
                "OOOOOOO.........OOOOOOOO.....",
            )
        )

    /** Sitting cross-legged with the hands on the knees: a tired fighter takes a break. */
    private val tiredBody =
        Sprite(
            listOf(
                ".........OOOO.........",
                ".......OOGSSGOO.......",
                ".....OOGGgSSgGGOO.....",
                "....OGGGGGgSGGGGGO....",
                "...OGGOGGGgGGGOGGGO...",
                "...OGGOGGGgGGGOGGGO...",
                "..OGGgOGGGgGGGOgGGGO..",
                "..OGGgOGGGgGGGOgGGGO..",
                "..OGGOOBBBBBBBBOGGGO..",
                "..OOXXObbbbBbbbOXXOO..",
                ".OGOFFOGGBObBGGOFFOGO.",
                "OGGGOOGGGGBOBGGGOOGGGO",
                "OGGGGGGgGGGGGgGGGGGGGO",
                "OPPPPOOOOOOOOOOOOPPPPO",
                ".OOOO............OOOO.",
            )
        )

    /** Hangs from the shoulders; drawn behind the body, so only its edges show. */
    private val standingCape =
        Sprite(
            listOf(
                "....OOOOOOOOOO",
                "...OCCCCCCCCCO",
                "..OCCCCCCCCCCO",
                "..OCcCCCCCCCCO",
                ".OCCcCCCCCCCO.",
                ".OCCcCCCCCCCO.",
                ".OCcCCCCCCCO..",
                "OCCcCCCCCCCO..",
                "OCcCCCCCCCO...",
                "OCcCCCCCCO....",
                "OCCcCCCCO.....",
                "OCcCcCCO......",
                "OOCOOCO.......",
                "..O..O........",
            )
        )

    /** Spread behind a sitting fighter. */
    private val sittingCape =
        Sprite(
            listOf(
                "...OOOOOOOOOOOOOO...",
                "..OCCCCCCCCCCCCCCO..",
                ".OCcCCCCCCCCCCCCcCO.",
                ".OCcCCCCCCCCCCCCcCO.",
                "OCCcCCCCCCCCCCCCcCCO",
                "OCcCCCCCCCCCCCCCCcCO",
                "OCcCCCCCCCCCCCCCCcCO",
                "OOOOOOOOOOOOOOOOOOOO",
            )
        )

    val stance =
        Pose(
            body = stanceBody,
            bodyAt = Px(8, 19),
            headAt = Px(13, 6),
            hipRow = 12,
            cape = standingCape,
            capeAt = Px(7, 21),
            sashAt = Px(15, 20),
            emblemAt = Px(13, 22),
        )

    val charge =
        Pose(
            body = chargeBody,
            bodyAt = Px(8, 19),
            headAt = Px(13, 6),
            hipRow = 12,
            cape = standingCape,
            capeAt = Px(7, 21),
            sashAt = Px(15, 20),
            emblemAt = Px(13, 22),
            kiAt = Px(9, 27),
        )

    val blast =
        Pose(
            body = blastBody,
            bodyAt = Px(4, 19),
            headAt = Px(9, 6),
            hipRow = 12,
            cape = standingCape,
            capeAt = Px(3, 21),
            sashAt = Px(11, 20),
            emblemAt = Px(9, 22),
            kiAt = Px(35, 24),
        )

    val tired =
        Pose(
            body = tiredBody,
            bodyAt = Px(9, 24),
            headAt = Px(13, 11),
            hipRow = 0,
            cape = sittingCape,
            capeAt = Px(10, 27),
            sashAt = Px(19, 25),
            emblemAt = Px(14, 27),
        )

    /** A head and where its 14 x 14 face sits inside the grid (the ponytail sticks out left). */
    class Head(val sprite: Sprite, val faceX: Int)

    /** Short spiky dark hair. */
    val kaiHead =
        Head(
            Sprite(
                listOf(
                    "....O.OO.O....",
                    "...OHOHHOHO...",
                    "..OHHHHHHHHOO.",
                    ".OHHhhHHHHHHHO",
                    ".OHhHHHHHHHHHO",
                    "OHHHHHHHHHHHHO",
                    "OHHHSHHHSHHSHO",
                    "OHSSSSSSSSSSSO",
                    "OSSSSSSSSSSSSO",
                    "OSSSSSSSSSSSSO",
                    "OSRSSSSSSSSRSO",
                    ".OSSSSSSSSSSO.",
                    "..OSSSSSSSSO..",
                    "...OOOOOOOO...",
                )
            ),
            faceX = 0,
        )

    /** Rounder hair with side-swept bangs and a ponytail with a hair tie. */
    val miraHead =
        Head(
            Sprite(
                listOf(
                    "........OOOOOO....",
                    "......OOHHHHHHOO..",
                    ".....OHHhhHHHHHHO.",
                    "..OOOHHhHHHHHHHHHO",
                    ".OHHTTHHHHHHHHHHHO",
                    "OHHhTTHHHHHHHHHHHO",
                    "OHHHOOHHHHSSHHHHSO",
                    "OHHO.OHSSSSSSSSSSO",
                    "OHhO.OHSSSSSSSSSSO",
                    ".OHHO.OSSSSSSSSSSO",
                    ".OHO..OSRSSSSSSRSO",
                    "..OHO..OSSSSSSSSO.",
                    "...O....OSSSSSSO..",
                    ".........OOOOOO...",
                )
            ),
            faceX = 4,
        )

    // overlays, positioned relative to the face (x) and the top of the head (y)
    val eyeOpen = Sprite(listOf("EW", "EE"))
    val eyeDroopy = Sprite(listOf("OO"))
    val leftEye = Px(5, 8)
    val rightEye = Px(9, 8)
    val smile = Sprite(listOf("OO"))
    val sleepyMouth = Sprite(listOf("M"))
    val mouthAt = Px(7, 11)

    /** From rank 7: a long headband knotted at the back, the tails flying to the left. */
    val headband =
        Sprite(
            listOf(
                "..dODDDDDDDDDDDDO",
                ".dD..............",
                "dD...............",
            )
        )
    val headbandAt = Px(-3, 5)

    val sunglasses = Sprite(listOf("OlLOOlLO", ".LL..LL."))
    val sunglassesAt = Px(4, 8)

    val crown = Sprite(listOf("Y.YY.Y", "YNYYNY", "OyyyyO"))
    val crownAt = Px(4, -2)

    val sleepyZ = Sprite(listOf("ZZZ", ".Z.", "ZZZ"))
    val sleepyZAt = Px(15, -1)

    /** The champion belt is worn over the shoulder, so the rank belt stays visible. */
    val sash =
        Sprite(
            listOf(
                ".....yY.",
                "..OOOOO.",
                ".OYYNYYO",
                ".OyYYYyO",
                "..OOOOO.",
                ".yY.....",
            )
        )

    /** From rank 9: a small gold emblem on the chest. */
    val emblem = Sprite(listOf(".Y.", "YNY", ".Y."))

    /** Ki balls; the small one is the first beat of the pulse. */
    val kiSmall = Sprite(listOf(".q.", "qKq", ".q."))
    val kiBig = Sprite(listOf("..q..", ".qkq.", "qkKkq", ".qkq.", "..q.."))
    val kiBurst =
        Sprite(
            listOf(
                "..q..q..",
                "...qq...",
                ".qkkkkq.",
                "qqkKKkqq",
                "qqkKKkqq",
                ".qkkkkq.",
                "...qq...",
                "..q..q..",
            )
        )

    /** The flying ki blast for the arena, 24 x 12, travelling to the right. */
    val kiBlast =
        Sprite(
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
        )
}
