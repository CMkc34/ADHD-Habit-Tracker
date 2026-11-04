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
package com.shub39.grit.shared.ui

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.w3c.dom.Document
import org.w3c.dom.Element

class ForkTranslationTest {

    private val resources = File("src/commonMain/composeResources")

    // the fork's own string files, kept apart from the upstream strings.xml
    private val files =
        listOf(
            "motivation_strings.xml",
            "energy_strings.xml",
            "stats_strings.xml",
            "avatar_strings.xml",
        )

    private fun forkStrings(valuesDir: String): List<Document> =
        files.map { name ->
            val file = resources.resolve("$valuesDir/$name")
            assertTrue(file.exists(), "missing ${file.path}")
            DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        }

    private fun Document.elements(tag: String): List<Element> {
        val nodes = getElementsByTagName(tag)
        return (0 until nodes.length).map { nodes.item(it) as Element }
    }

    private fun stringNames(valuesDir: String): Set<String> =
        forkStrings(valuesDir)
            .flatMap { it.elements("string") }
            .map { it.getAttribute("name") }
            .toSet()

    /** Plural name to the quantities it defines. */
    private fun plurals(valuesDir: String): Map<String, Set<String>> =
        forkStrings(valuesDir)
            .flatMap { it.elements("plurals") }
            .associate { plural ->
                val items = plural.getElementsByTagName("item")
                plural.getAttribute("name") to
                    (0 until items.length)
                        .map { (items.item(it) as Element).getAttribute("quantity") }
                        .toSet()
            }

    @Test
    fun `german and english offer the same strings`() {
        assertEquals(stringNames("values"), stringNames("values-de"))
    }

    @Test
    fun `german and english offer the same plurals`() {
        assertEquals(plurals("values"), plurals("values-de"))
    }
}
