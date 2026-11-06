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

import com.shub39.grit.core.avatar.ShopCatalog
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
            "fighter_strings.xml",
            "greeting_strings.xml",
            "arena_strings.xml",
            "label_strings.xml",
            "date_strings.xml",
            "branding_strings.xml",
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

    /** Every text the user sees: strings and plural items. */
    private fun texts(valuesDir: String): List<String> =
        forkStrings(valuesDir).flatMap { doc ->
            (doc.elements("string") + doc.elements("item")).map { it.textContent }
        }

    @Test
    fun `no text talks about the plant any more`() {
        // since #16 the fighter speaks
        val plantWords =
            mapOf(
                "values" to
                    Regex(
                        """\b(plants?|trees?|seeds?|sprouts?|meadows?|blossoms?)\b""",
                        RegexOption.IGNORE_CASE,
                    ),
                "values-de" to
                    Regex(
                        """\b(pflanze\w*|b[aä]um\w*|samen|wiese|spross|blüte\w*)\b""",
                        RegexOption.IGNORE_CASE,
                    ),
            )
        plantWords.forEach { (dir, words) ->
            val found = texts(dir).filter { words.containsMatchIn(it) }
            assertTrue(found.isEmpty(), "$dir still mentions the plant: $found")
        }
    }

    @Test
    fun `the app is called Daily Dojo in every language`() {
        // renamed from Sprout in #18
        listOf("values", "values-de").forEach { dir ->
            val names =
                forkStrings(dir)
                    .flatMap { it.elements("string") }
                    .associate { it.getAttribute("name") to it.textContent }
            assertEquals("Daily Dojo", names["app_display_name"], dir)
            assertEquals("Daily Dojo %1\$s", names["app_version_label"], dir)
        }
    }

    @Test
    fun `the currency is protein shakes, not water`() {
        // since #17; the shop's water bottle is an item, not the currency
        val waterWords =
            mapOf(
                "values" to Regex("""\bwater\b(?! bottle)""", RegexOption.IGNORE_CASE),
                "values-de" to Regex("""\bwasser\b""", RegexOption.IGNORE_CASE),
            )
        waterWords.forEach { (dir, words) ->
            val found = texts(dir).filter { words.containsMatchIn(it) || "💧" in it }
            assertTrue(found.isEmpty(), "$dir still talks about water: $found")
        }
    }

    @Test
    fun `every shop item has a name in german and english`() {
        listOf("values", "values-de").forEach { dir ->
            val names = stringNames(dir)
            val missing = ShopCatalog.items.map { "shop_item_${it.id}" }.filterNot { it in names }
            assertTrue(missing.isEmpty(), "$dir is missing $missing")
        }
    }
}
