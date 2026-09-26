package it.palsoftware.pastiera.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ForkUpdateReleaseTest {

    private fun release(tag: String, draft: Boolean = false) = GitHubRelease(
        tagName = tag,
        name = "Flux Keyboard ${tag.removePrefix("flux/v")}",
        prerelease = false,
        draft = draft,
        htmlUrl = "https://example.com/$tag",
        downloadUrl = "https://example.com/$tag.apk"
    )

    @Test
    fun offersTheNewestNewerFluxBuild() {
        val found = findNewerForkRelease(
            listOf(
                release("flux/v0.86-flux.202609260938"),
                release("flux/v0.86-flux.202609261200"),
                release("flux/v0.86-flux.202609250100")
            ),
            current = "0.86-flux.202609260938"
        )

        requireNotNull(found)
        assertEquals("flux/v0.86-flux.202609261200", found.tagName)
        assertEquals("https://example.com/flux/v0.86-flux.202609261200.apk", found.downloadUrl)
    }

    @Test
    fun stableChannelOffersFullReleasesOnly() {
        val releases = listOf(release("flux/v0.91"), release("flux/v0.92-flux.202610011200"))
        assertEquals("flux/v0.91", findNewerForkRelease(releases, "0.90-flux.202609261943", includeDev = false)?.tagName)
        assertEquals("flux/v0.92-flux.202610011200", findNewerForkRelease(releases, "0.90-flux.202609261943", includeDev = true)?.tagName)
        // On 0.91, only a newer full release is offered on Stable
        assertNull(findNewerForkRelease(releases, "0.91", includeDev = false))
    }

    @Test
    fun nothingWhenAlreadyOnTheLatestBuild() {
        assertNull(findNewerForkRelease(listOf(release("flux/v0.86-flux.202609260938")), "0.86-flux.202609260938"))
    }

    @Test
    fun ignoresDraftsAndOtherTags() {
        val releases = listOf(
            release("flux/v0.86-flux.202609270000", draft = true),
            release("nightly/v0.87-nightly.20260926.120000"),
            release("v0.90")
        )
        assertNull(findNewerForkRelease(releases, "0.86-flux.202609260938"))
    }

    @Test
    fun onlyNewerFluxTagsCountAsUpdates() {
        assertTrue(forkReleaseIsNewer("flux/v0.86-flux.202609261002", "0.86-flux.202609260957"))
        assertFalse(forkReleaseIsNewer("flux/v0.86-flux.202609261002", "0.86-flux.202609261002"))
        assertFalse(forkReleaseIsNewer("flux/v0.86-flux.202609260957", "0.86-flux.202609261002"))
        assertFalse(forkReleaseIsNewer("v0.90", "0.86-flux.202609261002"))
    }

    @Test
    fun aNewerBaseVersionWins() {
        val found = findNewerForkRelease(listOf(release("flux/v0.87-flux.202610010000")), "0.86-flux.202609260938")
        assertEquals("flux/v0.87-flux.202610010000", found?.tagName)
    }
}
