package it.palsoftware.pastiera.data.symbols

import android.graphics.Paint
import java.util.Locale

/**
 * Finds symbols by their Unicode names ("degree", "euro", "right arrow", "check mark"), for
 * anything not already on the SYM pages. Built from the symbol and punctuation blocks, keeping
 * only characters the phone's fonts can draw. Nothing is downloaded.
 */
object SymbolSearch {
    data class Entry(val symbol: String, val name: String)

    // Unicode blocks with symbols worth typing (letters and scripts are left to the keyboard)
    private val BLOCKS = listOf(
        0x00A1..0x00BF, // Latin-1 punctuation and symbols
        0x00D7..0x00D7, 0x00F7..0x00F7, // × ÷
        0x0391..0x03C9, // Greek letters
        0x2010..0x205E, // General punctuation
        0x2070..0x209C, // Superscripts and subscripts
        0x20A0..0x20C0, // Currency
        0x2100..0x214F, // Letterlike
        0x2150..0x218B, // Number forms
        0x2190..0x21FF, // Arrows
        0x2200..0x22FF, // Mathematical operators
        0x2300..0x23FF, // Miscellaneous technical
        0x2460..0x24FF, // Enclosed alphanumerics
        0x2500..0x259F, // Box drawing, block elements
        0x25A0..0x25FF, // Geometric shapes
        0x2600..0x26FF, // Miscellaneous symbols
        0x2700..0x27BF, // Dingbats
        0x27C0..0x27EF, // Miscellaneous mathematical symbols A
        0x27F0..0x27FF, // Supplemental arrows A
        0x2900..0x297F, // Supplemental arrows B
        0x2980..0x29FF, // Miscellaneous mathematical symbols B
        0x2A00..0x2AFF, // Supplemental mathematical operators
        0x2B00..0x2BFF  // Miscellaneous symbols and arrows
    )

    @Volatile
    private var index: List<Entry>? = null

    /** Every searchable symbol the phone's fonts can draw, built once. */
    fun entries(): List<Entry> = index ?: buildEntries(::fontHasGlyph).also { index = it }

    /** Builds the list; [hasGlyph] decides which characters can be drawn. */
    fun buildEntries(hasGlyph: (String) -> Boolean): List<Entry> =
        BLOCKS.asSequence()
            .flatMap { it.asSequence() }
            .filter { Character.isDefined(it) && !Character.isISOControl(it) && !Character.isWhitespace(it) }
            .mapNotNull { codePoint ->
                val name = Character.getName(codePoint) ?: return@mapNotNull null
                val symbol = String(Character.toChars(codePoint))
                if (hasGlyph(symbol)) Entry(symbol, name.lowercase(Locale.ROOT)) else null
            }
            .toList()

    // Words that pad Unicode names without saying what a symbol is ("greek small letter pi")
    private val FILLER = setOf("sign", "greek", "small", "capital", "letter", "with", "symbol", "mark", "of", "the", "and")

    // Everyday words for symbols whose Unicode names differ (™ is "trade mark sign", ¶ "pilcrow")
    private val ALIASES = mapOf(
        "trademark" to "trade mark", "tm" to "trade mark", "paragraph" to "pilcrow",
        "approx" to "almost equal", "approximately" to "almost equal", "tick" to "check mark",
        "times" to "multiplication sign", "sqrt" to "square root", "root" to "square root",
        "sum" to "summation", "deg" to "degree"
    )

    /**
     * Symbols whose name contains every word of [query], or its everyday alias, or that are the
     * query itself. Best matches first: whole words over word starts, relative to how long the
     * name is (filler words aside). A blank query returns everything.
     */
    fun search(query: String, entries: List<Entry> = entries(), limit: Int = 400): List<Entry> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return entries
        val lower = trimmed.lowercase(Locale.ROOT)
        val queries = listOfNotNull(lower, ALIASES[lower]).map { it.split(Regex("\\s+")) }
        return entries.asSequence()
            .mapNotNull { entry ->
                if (entry.symbol == trimmed) return@mapNotNull entry to Double.MAX_VALUE
                val best = queries
                    .filter { words -> words.all { entry.name.contains(it) } }
                    .maxOfOrNull { words -> score(entry.name, words) }
                    ?: return@mapNotNull null
                entry to best
            }
            .sortedWith(compareByDescending<Pair<Entry, Double>> { it.second }.thenBy { it.first.name.length })
            .take(limit)
            .map { it.first }
            .toList()
    }

    private fun score(name: String, words: List<String>): Double {
        val nameWords = name.split(' ', '-').filter { it.isNotEmpty() }
        val points = words.sumOf { word ->
            when {
                word in nameWords -> 2
                nameWords.any { it.startsWith(word) } -> 1
                else -> 0
            }.toInt()
        }
        return points.toDouble() / maxOf(1, nameWords.count { it !in FILLER })
    }

    private val glyphPaint by lazy { Paint() }

    private fun fontHasGlyph(symbol: String): Boolean = glyphPaint.hasGlyph(symbol)
}
