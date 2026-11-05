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

/** An arm or a leg: from the [root] (shoulder, hip) over the [joint] (elbow, knee) to the [end]. */
internal class Limb(val root: Pt, val joint: Pt, val end: Pt) {
    fun shift(d: Pt) = Limb(root + d, joint + d, end + d)
}

/**
 * The fixed anchor points of one pose. Every character is drawn on these points, so all of them
 * share the poses, and gear and rank upgrades fit every character by construction.
 *
 * "Back" is the limb on the viewer's left, "front" the one on the right; the fighters face right.
 */
internal class Skeleton(
    val head: Pt,
    /** Left shoulder, right shoulder, then down the right side, along the hips and up again. */
    val torso: List<Pt>,
    val beltLeft: Pt,
    val beltRight: Pt,
    val backArm: Limb,
    val frontArm: Limb,
    val backLeg: Limb,
    val frontLeg: Limb,
    val backFoot: Pt,
    val frontFoot: Pt,
    val sitting: Boolean = false,
    /** Centre of the ki ball, for the ki poses. */
    val ki: Pt? = null,
    val kiRadius: Float = 4f,
) {
    val leftShoulder: Pt
        get() = torso[0]

    val rightShoulder: Pt
        get() = torso[1]

    /** The head, torso, belt and arms moved by [dy]; the legs stay on the ground. */
    fun bounced(dy: Float): Skeleton {
        if (dy == 0f) return this
        val d = Pt(0f, dy)
        return Skeleton(
            head = head + d,
            torso = torso.map { it + d },
            beltLeft = beltLeft + d,
            beltRight = beltRight + d,
            backArm = backArm.shift(d),
            frontArm = frontArm.shift(d),
            backLeg = backLeg,
            frontLeg = frontLeg,
            backFoot = backFoot,
            frontFoot = frontFoot,
            sitting = sitting,
            ki = ki,
            kiRadius = kiRadius,
        )
    }

    companion object {
        const val SIZE = 48

        private fun torsoAt(dx: Float, dy: Float = 0f) =
            listOf(Pt(17, 22), Pt(31, 22), Pt(30, 30), Pt(28, 34), Pt(20, 34), Pt(18, 30)).map {
                it + Pt(dx, dy)
            }

        /** Fighting stance, turned a little to the right, the front fist up beside the face. */
        val stance =
            Skeleton(
                head = Pt(25, 14),
                torso = torsoAt(0f),
                beltLeft = Pt(18, 30),
                beltRight = Pt(30, 30),
                backArm = Limb(Pt(18, 24), Pt(14, 29), Pt(19.5f, 30f)),
                frontArm = Limb(Pt(30, 24), Pt(35, 27), Pt(36.2f, 19.6f)),
                backLeg = Limb(Pt(21, 33), Pt(16, 39), Pt(14, 44)),
                frontLeg = Limb(Pt(27, 33), Pt(32, 39), Pt(35, 44)),
                backFoot = Pt(13f, 45.2f),
                frontFoot = Pt(37f, 45.2f),
            )

        /** Both hands pulled back to the hip around a growing ki ball. */
        val charge =
            Skeleton(
                head = Pt(25, 14),
                torso = torsoAt(0f),
                beltLeft = Pt(18, 30),
                beltRight = Pt(30, 30),
                backArm = Limb(Pt(18, 24), Pt(14, 28), Pt(12, 32)),
                frontArm = Limb(Pt(30, 24), Pt(27, 30), Pt(15, 33)),
                backLeg = Limb(Pt(21, 33), Pt(16, 39), Pt(14, 44)),
                frontLeg = Limb(Pt(27, 33), Pt(32, 39), Pt(35, 44)),
                backFoot = Pt(13f, 45.2f),
                frontFoot = Pt(37f, 45.2f),
                ki = Pt(8.5f, 31f),
                kiRadius = 4f,
            )

        /** Lunging forward with both palms out: the ki blast leaves the hands. */
        val blast =
            Skeleton(
                head = Pt(21, 14),
                torso = torsoAt(-4f),
                beltLeft = Pt(14, 30),
                beltRight = Pt(26, 30),
                backArm = Limb(Pt(15, 24), Pt(24, 26), Pt(33, 25)),
                frontArm = Limb(Pt(26, 24), Pt(33, 23), Pt(38, 23)),
                backLeg = Limb(Pt(17, 33), Pt(12, 39), Pt(10, 44)),
                frontLeg = Limb(Pt(23, 33), Pt(30, 38), Pt(33, 44)),
                backFoot = Pt(9f, 45.2f),
                frontFoot = Pt(35f, 45.2f),
                ki = Pt(43f, 23.5f),
                kiRadius = 5f,
            )

        /** A straight punch: the front fist shoots out at shoulder height, the back one at the hip. */
        val punch =
            Skeleton(
                head = Pt(26, 14),
                torso = torsoAt(1f),
                beltLeft = Pt(19, 30),
                beltRight = Pt(31, 30),
                backArm = Limb(Pt(19, 24), Pt(15, 29), Pt(19.5f, 31f)),
                frontArm = Limb(Pt(31, 24), Pt(37, 23), Pt(43.5f, 22f)),
                backLeg = Limb(Pt(22, 33), Pt(16, 39), Pt(12, 44)),
                frontLeg = Limb(Pt(28, 33), Pt(34, 38), Pt(37, 44)),
                backFoot = Pt(11f, 45.2f),
                frontFoot = Pt(39f, 45.2f),
            )

        /** A side kick: leaning back on the back leg, the front foot thrusts out at belt height. */
        val kick =
            Skeleton(
                head = Pt(20, 13),
                torso = torsoAt(-5f, -1f),
                beltLeft = Pt(13, 29),
                beltRight = Pt(25, 29),
                backArm = Limb(Pt(13, 23), Pt(8, 26), Pt(5, 30)),
                frontArm = Limb(Pt(25, 23), Pt(29, 26), Pt(28.5f, 19f)),
                backLeg = Limb(Pt(17, 32), Pt(16, 38), Pt(16, 44)),
                frontLeg = Limb(Pt(23, 31), Pt(31, 29), Pt(39, 27.5f)),
                backFoot = Pt(16f, 45.2f),
                frontFoot = Pt(42f, 27f),
            )

        /** Guarding: low and wide, both forearms crossed in front of the chin. */
        val guard =
            Skeleton(
                head = Pt(23, 15),
                torso = torsoAt(-2f, 1f),
                beltLeft = Pt(16, 31),
                beltRight = Pt(28, 31),
                backArm = Limb(Pt(16, 25), Pt(30, 32), Pt(40.5f, 17f)),
                frontArm = Limb(Pt(28, 25), Pt(41, 31), Pt(34f, 16.5f)),
                backLeg = Limb(Pt(20, 34), Pt(14, 39), Pt(12, 44)),
                frontLeg = Limb(Pt(26, 34), Pt(33, 39), Pt(36, 44)),
                backFoot = Pt(11f, 45.2f),
                frontFoot = Pt(38f, 45.2f),
            )

        /** Sitting cross-legged, the hands on the knees: a tired fighter takes a break. */
        val sitting =
            Skeleton(
                head = Pt(24, 21),
                torso =
                    listOf(Pt(16, 29), Pt(32, 29), Pt(31, 36), Pt(29, 40), Pt(19, 40), Pt(17, 36)),
                beltLeft = Pt(17, 36),
                beltRight = Pt(31, 36),
                backArm = Limb(Pt(17, 31), Pt(13, 36), Pt(14, 40)),
                frontArm = Limb(Pt(31, 31), Pt(35, 36), Pt(34, 40)),
                backLeg = Limb(Pt(20, 40), Pt(9, 42), Pt(20, 45)),
                frontLeg = Limb(Pt(28, 40), Pt(39, 42), Pt(28, 45)),
                backFoot = Pt(20f, 45.4f),
                frontFoot = Pt(28f, 45.4f),
                sitting = true,
            )
    }
}
