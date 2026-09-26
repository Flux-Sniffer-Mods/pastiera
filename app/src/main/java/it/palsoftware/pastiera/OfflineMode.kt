package it.palsoftware.pastiera

import android.content.Context

/**
 * Offline mode: nothing in Pastiera goes online. GIF search (KLIPY) disappears, and dictionary
 * and layout downloads, update checks, release notes and the downloadable emoji font are all
 * skipped. Loaded when the app starts and updated by its setting, so code without a Context
 * (the download managers) can check it too.
 */
object OfflineMode {
    @Volatile
    var enabled: Boolean = false
        private set

    fun load(context: Context) {
        enabled = SettingsManager.isOfflineMode(context)
    }

    internal fun update(value: Boolean) {
        enabled = value
    }
}
