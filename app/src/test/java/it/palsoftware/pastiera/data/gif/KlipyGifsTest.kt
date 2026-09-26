package it.palsoftware.pastiera.data.gif

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import it.palsoftware.pastiera.SettingsManager
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class KlipyGifsTest {

    private val sample = """
        {
          "results": [
            {
              "id": "abc123",
              "content_description": "Cat waving",
              "media_formats": {
                "gif": { "url": "https://static.klipy.com/full.gif", "dims": [480, 360] },
                "tinygif": { "url": "https://static.klipy.com/tiny.gif", "dims": [220, 165] }
              }
            },
            {
              "id": "no-https",
              "media_formats": { "gif": { "url": "http://insecure.example/x.gif" } }
            },
            {
              "id": "only-gif",
              "content_description": "",
              "media_formats": { "gif": { "url": "https://static.klipy.com/only.gif", "dims": [300, 200] } }
            }
          ],
          "next": "30"
        }
    """.trimIndent()

    @Test
    fun parsesTenorV2ResultsAndSkipsInsecureOnes() {
        val gifs = KlipyGifs.parse(sample)

        assertEquals(listOf("abc123", "only-gif"), gifs.map { it.id })
        val cat = gifs.first()
        assertEquals("Cat waving", cat.description)
        assertEquals("https://static.klipy.com/tiny.gif", cat.previewUrl)
        assertEquals("https://static.klipy.com/full.gif", cat.gifUrl)
        assertEquals(220, cat.previewWidth)
        assertEquals(165, cat.previewHeight)
        // No preview format: the full GIF doubles as the preview
        assertEquals("https://static.klipy.com/only.gif", gifs[1].previewUrl)
    }

    @Test
    fun emptyOrUnexpectedResponsesGiveNoResults() {
        assertTrue(KlipyGifs.parse("""{"results": []}""").isEmpty())
        assertTrue(KlipyGifs.parse("""{"error": "bad key"}""").isEmpty())
    }

    @Test
    fun blankQueryAsksForFeaturedAndTextSearches() {
        val featured = KlipyGifs.requestUrl("KEY", "  ", Locale.UK)
        assertEquals("/v2/featured", featured.encodedPath)
        assertNull(featured.queryParameter("q"))
        assertEquals("KEY", featured.queryParameter("key"))
        assertEquals("tinygif,gif", featured.queryParameter("media_filter"))
        assertEquals("en_GB", featured.queryParameter("locale"))
        assertEquals("GB", featured.queryParameter("country"))

        val search = KlipyGifs.requestUrl(" KEY ", " happy cat ", Locale.UK)
        assertEquals("api.klipy.com", search.host)
        assertEquals("/v2/search", search.encodedPath)
        assertEquals("happy cat", search.queryParameter("q"))
        assertEquals("KEY", search.queryParameter("key"))
    }

    @Test
    fun onlyFieldsTakingGifContentGetTheFile() {
        assertTrue(KlipyGifs.editorAcceptsGif(arrayOf("image/gif")))
        assertTrue(KlipyGifs.editorAcceptsGif(arrayOf("image/png", "image/*")))
        assertFalse(KlipyGifs.editorAcceptsGif(arrayOf("image/png")))
        assertFalse(KlipyGifs.editorAcceptsGif(emptyArray()))
    }

    @Test
    fun ownKeyReplacesTheBuiltInOneAndIsAllTheFieldShows() {
        val context = RuntimeEnvironment.getApplication()
        assertEquals("", SettingsManager.getUserKlipyApiKey(context))

        SettingsManager.setKlipyApiKey(context, "  mine  ")

        assertEquals("mine", SettingsManager.getUserKlipyApiKey(context))
        assertEquals("mine", SettingsManager.getKlipyApiKey(context))
    }
}
