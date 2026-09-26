package it.palsoftware.pastiera.clipboard

import org.junit.Assert.assertEquals
import org.junit.Test

class LinkCleanerTest {
    @Test
    fun trackingParametersGo() {
        assertEquals(
            "https://example.com/article?id=7",
            LinkCleaner.clean("https://example.com/article?utm_source=x&id=7&utm_medium=social&fbclid=abc")
        )
        assertEquals("https://example.com/a", LinkCleaner.clean("https://example.com/a?gclid=1&_ga=2"))
    }

    @Test
    fun siteShareIdsGoOnlyOnTheirSites() {
        assertEquals("https://youtu.be/dQw4w9WgXcQ", LinkCleaner.clean("https://youtu.be/dQw4w9WgXcQ?si=Ab12"))
        assertEquals(
            "https://open.spotify.com/track/1?go=1",
            LinkCleaner.clean("https://open.spotify.com/track/1?si=xyz&go=1")
        )
        // si means something else on other sites, so it stays
        assertEquals("https://example.com/?si=5", LinkCleaner.clean("https://example.com/?si=5"))
    }

    @Test
    fun mobileHostsBecomeTheFullSite() {
        assertEquals("https://youtube.com/watch?v=abc", LinkCleaner.clean("https://m.youtube.com/watch?v=abc&feature=share"))
        assertEquals(
            "https://en.wikipedia.org/wiki/Pasta#History",
            LinkCleaner.clean("https://en.m.wikipedia.org/wiki/Pasta#History")
        )
    }

    @Test
    fun textAroundLinksAndSentencePunctuationStay() {
        assertEquals(
            "Look: https://example.com/x?id=1. Nice!",
            LinkCleaner.clean("Look: https://example.com/x?id=1&utm_campaign=y. Nice!")
        )
        assertEquals("no links here", LinkCleaner.clean("no links here"))
    }
}
