package it.palsoftware.pastiera.data.symbols

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SymbolSearchTest {
    // Every character counts as drawable here; on the phone, the fonts decide
    private val entries = SymbolSearch.buildEntries { true }

    private fun first(query: String): String = SymbolSearch.search(query, entries).first().symbol

    @Test
    fun findsSymbolsByTheirNames() {
        assertEquals("°", first("degree"))
        assertEquals("€", first("euro"))
        assertEquals("✓", first("check mark"))
        assertEquals("π", first("pi"))
        assertEquals("×", first("multiplication"))
    }

    @Test
    fun ranksTheClosestNameFirst() {
        // "rightwards arrow" beats "left right arrow" and longer arrow names
        assertEquals("→", first("right arrow"))
        assertEquals("←", first("left arrow"))
    }

    @Test
    fun everydayWordsFindSymbolsWithOtherUnicodeNames() {
        assertEquals("™", first("trademark"))
        assertEquals("¶", first("paragraph"))
        assertEquals("≈", first("approx"))
        assertEquals("✓", first("tick"))
    }

    @Test
    fun theSymbolItselfAndABlankQueryWork() {
        assertEquals("€", first("€"))
        assertEquals(entries.size, SymbolSearch.search("  ", entries).size)
        assertTrue(entries.size > 1000)
    }
}
