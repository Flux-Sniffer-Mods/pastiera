package it.palsoftware.pastiera.update

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import it.palsoftware.pastiera.BuildConfig
import it.palsoftware.pastiera.R
import it.palsoftware.pastiera.SettingsManager
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException

internal fun successorReleasesApiUrl(): String =
    "https://api.github.com/repos/${BuildConfig.SUCCESSOR_GITHUB_REPOSITORY}/releases?per_page=20"

internal fun successorReleasesPage(): String =
    "https://github.com/${BuildConfig.SUCCESSOR_GITHUB_REPOSITORY}/releases"

/** Flux Keyboard stable builds update from the fork's own releases rather than upstream's. */
internal fun forkUpdatesEnabled(): Boolean =
    BuildConfig.RELEASE_CHANNEL == "stable" && BuildConfig.FORK_GITHUB_REPOSITORY.isNotBlank()

private const val FORK_UPDATE_PREFS = "fork_update"
private const val KEY_ANNOUNCED_FORK_RELEASE = "announced_release"

/** Remembers the release a notification announced, so it can be cleared once installed. */
internal fun rememberAnnouncedForkRelease(context: Context, tag: String) {
    context.getSharedPreferences(FORK_UPDATE_PREFS, Context.MODE_PRIVATE).edit()
        .putString(KEY_ANNOUNCED_FORK_RELEASE, tag).apply()
}

/**
 * After an update, a notification announcing this build (or an older one) is stale: it's
 * cleared, so the new version never prompts for itself.
 */
fun clearStaleForkUpdateNotice(context: Context) {
    ForkUpdateInstaller.clearDownloads(context)
    val prefs = context.getSharedPreferences(FORK_UPDATE_PREFS, Context.MODE_PRIVATE)
    val tag = prefs.getString(KEY_ANNOUNCED_FORK_RELEASE, null) ?: return
    if (forkReleaseIsNewer(tag, BuildConfig.VERSION_NAME)) return
    it.palsoftware.pastiera.inputmethod.NotificationHelper.cancelUpdateNotification(context)
    prefs.edit().remove(KEY_ANNOUNCED_FORK_RELEASE).apply()
}

internal fun forkReleasesPage(): String =
    "https://github.com/${BuildConfig.FORK_GITHUB_REPOSITORY}/releases"

private val client = OkHttpClient()
private val mainHandler = Handler(Looper.getMainLooper())

internal data class UpdateCheckResult(
    val successful: Boolean,
    val hasAnnouncement: Boolean = false,
    val releaseTag: String? = null,
    val displayName: String? = null,
    val releasePageUrl: String? = null,
    val downloadUrl: String? = null,
    val isNightlyUpdate: Boolean = false,
    val isForkUpdate: Boolean = false
)

internal fun checkForUpdate(
    context: Context,
    releaseChannel: String,
    ignoreDismissedReleases: Boolean = true,
    callback: (UpdateCheckResult) -> Unit
) = checkRelease(context, releaseChannel, ignoreDismissedReleases, false, callback)


internal fun checkForNightlyUpdate(
    context: Context,
    ignoreDismissedReleases: Boolean = true,
    callback: (UpdateCheckResult) -> Unit
) {
    if (BuildConfig.RELEASE_CHANNEL != "nightly") {
        postResult(callback, UpdateCheckResult(successful = true))
        return
    }
    checkRelease(context, "nightly", ignoreDismissedReleases, true, callback)
}

internal fun checkForUpdateNotices(
    context: Context,
    releaseChannel: String,
    ignoreDismissedReleases: Boolean = true,
    callback: (UpdateCheckResult) -> Unit
) {
    if (BuildConfig.RELEASE_CHANNEL == "nightly") {
        checkForNightlyUpdate(context, ignoreDismissedReleases) { nightly ->
            if (nightly.hasAnnouncement) callback(nightly)
            else checkForUpdate(context, releaseChannel, ignoreDismissedReleases, callback)
        }
    } else checkForUpdate(context, releaseChannel, ignoreDismissedReleases, callback)
}

private fun checkRelease(
    context: Context,
    releaseChannel: String,
    ignoreDismissedReleases: Boolean,
    nightly: Boolean,
    callback: (UpdateCheckResult) -> Unit
) {
    if (!shouldUseGithubUpdateChecks(context)) {
        postResult(callback, UpdateCheckResult(successful = true))
        return
    }

    val fork = !nightly && forkUpdatesEnabled()
    val request = Request.Builder()
        .url(when {
            nightly -> "https://api.github.com/repos/palsoftware/pastiera/releases?per_page=20"
            fork -> "https://api.github.com/repos/${BuildConfig.FORK_GITHUB_REPOSITORY}/releases?per_page=20"
            else -> successorReleasesApiUrl()
        })
        .header("Accept", "application/vnd.github+json")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            postResult(callback, UpdateCheckResult(successful = false))
        }

        override fun onResponse(call: Call, response: Response) {
            response.use { res ->
                if (!res.isSuccessful) {
                    postResult(callback, UpdateCheckResult(successful = false))
                    return
                }

                val body = res.body?.string().orEmpty()
                if (body.isBlank()) {
                    postResult(callback, UpdateCheckResult(successful = false))
                    return
                }

                val latestRelease = try {
                    val releases = parseGitHubReleases(JSONArray(body))
                    when {
                        nightly -> findNewerNightlyRelease(releases, BuildConfig.VERSION_NAME)
                        fork -> findNewerForkRelease(
                            releases, BuildConfig.VERSION_NAME,
                            includeDev = SettingsManager.getForkUpdateChannel(context) == SettingsManager.FORK_UPDATE_CHANNEL_DEV
                        )
                        else -> findLatestRelease(releases, releaseChannel)
                    }
                } catch (_: Exception) {
                    postResult(callback, UpdateCheckResult(successful = false))
                    return
                }
                if (latestRelease == null) {
                    postResult(callback, UpdateCheckResult(successful = true))
                    return
                }

                val releaseTag = latestRelease.tagName
                if (ignoreDismissedReleases) {
                    val isDismissed = SettingsManager.isReleaseDismissed(context, dismissKey(releaseTag, nightly, fork))
                    if (isDismissed) {
                        // Release was dismissed, don't show update
                        postResult(callback, UpdateCheckResult(successful = true))
                        return
                    }
                }
                
                postResult(
                    callback,
                    UpdateCheckResult(
                        successful = true,
                        hasAnnouncement = true,
                        releaseTag = releaseTag,
                        displayName = latestRelease.displayName,
                        releasePageUrl = latestRelease.releasePageUrl,
                        downloadUrl = latestRelease.downloadUrl,
                        isNightlyUpdate = nightly,
                        isForkUpdate = fork
                    )
                )
            }
        }
    })
}

private fun dismissKey(tag: String, nightly: Boolean, fork: Boolean): String = when {
    nightly -> "pastiera-nightly:$tag"
    fork -> "pastiera-flux:$tag"
    else -> tag
}

private fun postResult(
    callback: (UpdateCheckResult) -> Unit,
    result: UpdateCheckResult
) {
    mainHandler.post {
        callback(result)
    }
}

fun showUpdateDialog(
    context: Context,
    releaseTag: String,
    displayName: String,
    releasePageUrl: String?
) {
    AlertDialog.Builder(context)
        .setTitle(R.string.successor_dialog_title)
        .setMessage(context.getString(R.string.successor_dialog_message, displayName))
        .setPositiveButton(R.string.successor_dialog_open_release) { _, _ ->
            openUrl(context, releasePageUrl ?: successorReleasesPage())
        }
        .setNeutralButton(R.string.successor_dialog_later) { _, _ ->
            SettingsManager.addDismissedRelease(context, releaseTag)
        }
        .create()
        .show()
}

private fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

internal fun showReleaseNotice(context: Context, result: UpdateCheckResult) {
    val tag = result.releaseTag ?: return
    val name = result.displayName ?: return
    if (result.isForkUpdate) {
        // Checked again here: a result from before an update may name the installed build
        if (!forkReleaseIsNewer(tag, BuildConfig.VERSION_NAME)) return
        showForkUpdateDialog(context, tag, name, result.releasePageUrl, result.downloadUrl)
        return
    }
    if (!result.isNightlyUpdate) {
        showUpdateDialog(context, tag, name, result.releasePageUrl)
        return
    }
    val builder = AlertDialog.Builder(context)
        .setTitle(R.string.nightly_update_title)
        .setMessage(context.getString(R.string.nightly_update_message, name))
        .setPositiveButton(R.string.nightly_update_open) { _, _ ->
            openUrl(context, result.releasePageUrl ?: "https://github.com/palsoftware/pastiera/releases")
        }
        .setNeutralButton(R.string.successor_dialog_later) { _, _ ->
            SettingsManager.addDismissedRelease(context, "pastiera-nightly:$tag")
        }
    result.downloadUrl?.let { url ->
        builder.setNegativeButton(R.string.nightly_update_download) { _, _ -> openUrl(context, url) }
    }
    builder.show()
}

private fun showForkUpdateDialog(
    context: Context,
    tag: String,
    name: String,
    releasePageUrl: String?,
    downloadUrl: String?
) {
    val builder = AlertDialog.Builder(context)
        .setTitle(R.string.fork_update_title)
        .setMessage(context.getString(R.string.fork_update_message, name))
        .setPositiveButton(R.string.nightly_update_open) { _, _ ->
            openUrl(context, releasePageUrl ?: forkReleasesPage())
        }
        .setNeutralButton(R.string.successor_dialog_later) { _, _ ->
            SettingsManager.addDismissedRelease(context, "pastiera-flux:$tag")
        }
    downloadUrl?.let { url ->
        builder.setNegativeButton(R.string.fork_update_install) { _, _ ->
            ForkUpdateInstaller.downloadAndInstall(context, url, releasePageUrl)
        }
    }
    builder.show()
}
