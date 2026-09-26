package it.palsoftware.pastiera

import it.palsoftware.pastiera.update.bundledReleaseNotes
import it.palsoftware.pastiera.update.friendlyVersion
import it.palsoftware.pastiera.update.shortVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** The fork's own What's new ships with the app. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class BundledReleaseNotesTest {
    @Test
    fun forkNotesAreBundled() {
        val notes = assertNotNullAndGet(bundledReleaseNotes(RuntimeEnvironment.getApplication(), "0.86-flux.1"))
        assertTrue(notes.title.startsWith("Flux Keyboard"))
        assertTrue(notes.highlights.size >= 3)
        assertTrue(notes.docsUrl.endsWith("FORK_CHANGES.md"))
    }

    @Test
    fun notesSayWhatTheyCoverAndKeepThePastieraTeamsWorkApart() {
        val notes = assertNotNullAndGet(bundledReleaseNotes(RuntimeEnvironment.getApplication(), "0.86-flux.1"))
        assertTrue(notes.intro.orEmpty().contains("Pastiera 0.85"))
        assertEquals("Added by Flux Keyboard", notes.sectionTitle)
        assertTrue(notes.upstreamTitle.orEmpty().startsWith("From the Pastiera team"))
        assertTrue(notes.upstreamChanges.size >= 5)
    }

    @Test
    fun forkVersionsReadAsADate() {
        assertEquals("0.86 · 26 Sep 2026, 04:16", friendlyVersion("0.86-flux.202609260416", java.util.Locale.US))
        assertEquals("0.86", shortVersion("0.86-flux.202609260416"))
        assertEquals("0.85", friendlyVersion("0.85", java.util.Locale.UK))
        assertEquals("Flux Keyboard 0.86", bundledReleaseNotes(RuntimeEnvironment.getApplication(), "0.86-flux.202609260416")?.title)
    }

    private fun <T> assertNotNullAndGet(value: T?): T { assertNotNull(value); return value!! }
}
