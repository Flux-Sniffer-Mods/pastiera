package it.palsoftware.pastiera.data.gif

import android.content.ClipDescription
import android.content.Context
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit

/** One GIF: a small preview for the grid and the full GIF that gets sent. */
data class GifResult(
    val id: String,
    val description: String,
    val previewUrl: String,
    val gifUrl: String,
    val previewWidth: Int,
    val previewHeight: Int
)

/**
 * GIF search through KLIPY (https://klipy.com). Google shut the Tenor API down on 30 June 2026;
 * KLIPY serves the same v2 endpoints and response format on its own host, so this speaks
 * Tenor v2. Requests need the user's own API key (free at [SIGNUP_URL]) and are only made
 * while GIF search is open.
 */
object KlipyGifs {
    const val BASE_URL = "https://api.klipy.com/v2"
    const val SIGNUP_URL = "https://partner.klipy.com"
    const val CACHE_FOLDER = "gifs"
    private const val PAGE_SIZE = 30
    private const val CACHE_MAX_AGE_MS = 24L * 60 * 60 * 1000

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    // Preview bytes by URL; each grid cell decodes its own animation from them
    private val previewCache = object : LruCache<String, ByteArray>(12 * 1024 * 1024) {
        override fun sizeOf(key: String, value: ByteArray): Int = value.size
    }

    /** Featured GIFs for a blank [query], search results otherwise. */
    fun requestUrl(apiKey: String, query: String?, locale: Locale = Locale.getDefault(), limit: Int = PAGE_SIZE): HttpUrl {
        val search = !query.isNullOrBlank()
        return BASE_URL.toHttpUrl().newBuilder()
            .addPathSegment(if (search) "search" else "featured")
            .apply { if (search) addQueryParameter("q", query!!.trim()) }
            .addQueryParameter("key", apiKey.trim())
            .addQueryParameter("client_key", "pastiera")
            .addQueryParameter("limit", limit.toString())
            .addQueryParameter("media_filter", "tinygif,gif")
            .addQueryParameter("contentfilter", "medium")
            .addQueryParameter("locale", locale.toString())
            .apply { locale.country.takeIf { it.isNotBlank() }?.let { addQueryParameter("country", it) } }
            .build()
    }

    /** Parses a v2 response; entries without an https GIF are skipped. */
    fun parse(json: String): List<GifResult> {
        val results = JSONObject(json).optJSONArray("results") ?: return emptyList()
        return (0 until results.length()).mapNotNull { index ->
            val item = results.optJSONObject(index) ?: return@mapNotNull null
            val formats = item.optJSONObject("media_formats") ?: return@mapNotNull null
            val gif = formats.optJSONObject("gif") ?: formats.optJSONObject("mediumgif") ?: return@mapNotNull null
            val gifUrl = gif.optString("url").takeIf { it.startsWith("https://") } ?: return@mapNotNull null
            val preview = formats.optJSONObject("tinygif") ?: formats.optJSONObject("nanogif") ?: gif
            val previewUrl = preview.optString("url").takeIf { it.startsWith("https://") } ?: gifUrl
            val dims = preview.optJSONArray("dims")
            GifResult(
                id = item.optString("id"),
                description = item.optString("content_description"),
                previewUrl = previewUrl,
                gifUrl = gifUrl,
                previewWidth = dims?.optInt(0) ?: 0,
                previewHeight = dims?.optInt(1) ?: 0
            )
        }
    }

    /** Throws on network or HTTP errors (a bad key is an HTTP error). */
    suspend fun find(apiKey: String, query: String?): List<GifResult> = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(requestUrl(apiKey, query)).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("KLIPY HTTP ${response.code}")
            parse(response.body?.string().orEmpty())
        }
    }

    suspend fun previewBytes(url: String): ByteArray = withContext(Dispatchers.IO) {
        previewCache.get(url) ?: download(url).also { previewCache.put(url, it) }
    }

    /** The full GIF in the cache folder the FileProvider shares; older files are removed. */
    suspend fun downloadToCache(context: Context, gif: GifResult): File = withContext(Dispatchers.IO) {
        val folder = File(context.cacheDir, CACHE_FOLDER).apply { mkdirs() }
        val now = System.currentTimeMillis()
        folder.listFiles()?.filter { now - it.lastModified() > CACHE_MAX_AGE_MS }?.forEach { it.delete() }
        val name = gif.id.filter { it.isLetterOrDigit() }.ifEmpty { gif.gifUrl.hashCode().toUInt().toString() }
        File(folder, "gif-$name.gif").also { file ->
            if (!file.exists() || file.length() == 0L) file.writeBytes(download(gif.gifUrl))
        }
    }

    /** Whether a field accepting these MIME types takes GIFs as content (not just text). */
    fun editorAcceptsGif(mimeTypes: Array<String>): Boolean =
        mimeTypes.any { ClipDescription.compareMimeTypes("image/gif", it) }

    private fun download(url: String): ByteArray {
        client.newCall(Request.Builder().url(url).build()).execute().use { response ->
            if (!response.isSuccessful) throw IOException("GIF HTTP ${response.code}")
            return response.body?.bytes() ?: throw IOException("Empty GIF")
        }
    }
}
