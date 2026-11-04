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
package com.shub39.grit.core.habits.style

/**
 * The 36 icons a routine can have. [key] is stored and backed up, so it must never change; the
 * drawing for each icon is chosen in `shared:ui`.
 */
enum class RoutineIcon(val key: String) {
    BOLT("bolt"),
    WATER("water"),
    DUMBBELL("dumbbell"),
    RUN("run"),
    BIKE("bike"),
    SWIM("swim"),
    MEDITATION("meditation"),
    SLEEP("sleep"),
    MORNING("morning"),
    ALARM("alarm"),
    BOOK("book"),
    WRITE("write"),
    LEARN("learn"),
    MUSIC("music"),
    PAINT("paint"),
    COOK("cook"),
    APPLE("apple"),
    COFFEE("coffee"),
    PILL("pill"),
    TOOTH("tooth"),
    SHOWER("shower"),
    CLEAN("clean"),
    FLOWER("flower"),
    LEAF("leaf"),
    HEART("heart"),
    BRAIN("brain"),
    SMILE("smile"),
    PHONE("phone"),
    PEOPLE("people"),
    PAW("paw"),
    SAVINGS("savings"),
    LAPTOP("laptop"),
    PHONE_OFF("phone_off"),
    STAR("star"),
    TARGET("target"),
    CHECK("check");

    companion object {
        fun fromKey(key: String): RoutineIcon? = entries.firstOrNull { it.key == key }
    }
}
