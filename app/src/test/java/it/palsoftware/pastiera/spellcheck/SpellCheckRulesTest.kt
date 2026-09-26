package it.palsoftware.pastiera.spellcheck

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpellCheckRulesTest {
    @Test
    fun ordinaryWordsAreChecked() {
        listOf("hello", "Helo", "don't", "caffè", "end.").forEach { assertTrue(it, SpellCheckRules.shouldCheck(it)) }
    }

    @Test
    fun codesLinksAndNamesAreLeftAlone() {
        listOf("a", "B2B", "NASA", "@pastiera", "#tbt", "example.com", "http://x", "snake_case", "42", "--")
            .forEach { assertFalse(it, SpellCheckRules.shouldCheck(it)) }
    }

    @Test
    fun everyBundledDictionaryIsALanguage() {
        val bundled = java.io.File("src/main/assets/common/dictionaries_serialized").list().orEmpty()
            .map { it.substringBefore('_') }.toSet()
        assertTrue(bundled.isNotEmpty())
        org.junit.Assert.assertEquals(bundled, SpellCheckRules.LANGUAGES)
    }
}
