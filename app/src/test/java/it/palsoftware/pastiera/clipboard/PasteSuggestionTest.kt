package it.palsoftware.pastiera.clipboard

import org.junit.Assert.assertEquals
import org.junit.Test

class PasteSuggestionTest {
    @Test
    fun shortTextIsShownWhole() {
        assertEquals("📋 hello world", PasteSuggestion.label("  hello\n world "))
    }

    @Test
    fun longTextIsCutOnOneLine() {
        val label = PasteSuggestion.label("https://example.com/a/very/long/path/that/goes/on")
        assertEquals("📋 https://example.com/a/v…", label)
    }
}
