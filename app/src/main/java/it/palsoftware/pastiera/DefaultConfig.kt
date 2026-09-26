package it.palsoftware.pastiera

import android.content.Context
import android.util.Log
import it.palsoftware.pastiera.backup.RestoreManager
import it.palsoftware.pastiera.backup.RestoreResult
import it.palsoftware.pastiera.inputmethod.DeviceSpecific
import java.io.File

/**
 * The default configuration: a Pastiera backup bundled in assets/common/default_config (made on
 * a Titan 2 Elite), applied through the normal restore on a fresh install, and on request from
 * Privacy & system > Backup. Only settings the backup contract allows are applied. On other
 * phones the device-specific settings are left to that phone, as when restoring a backup from
 * another device.
 */
object DefaultConfig {
    private const val TAG = "DefaultConfig"
    private const val ASSET_DIR = "common/default_config"
    const val PREF_APPLIED = "default_config_applied"

    /** On a fresh install (no settings yet), applies the default configuration. */
    fun applyIfFreshInstall(context: Context): Boolean {
        // Unit tests start every app from scratch and expect Pastiera's own defaults
        if (android.os.Build.FINGERPRINT == "robolectric") return false
        val prefs = SettingsManager.getPreferences(context)
        if (prefs.all.isNotEmpty()) return false
        return apply(context)
    }

    fun apply(context: Context): Boolean {
        val dir = File(context.cacheDir, "default_config_${System.currentTimeMillis()}")
        return try {
            copyAssets(context, ASSET_DIR, dir)
            val mode = if (DeviceSpecific.isTitan2EliteDevice()) {
                RestoreManager.ImportMode.UNCHANGED
            } else {
                RestoreManager.ImportMode.ADAPT_TO_CURRENT_DEVICE
            }
            val result = RestoreManager.restoreExtracted(context, dir, mode)
            val ok = result is RestoreResult.Success
            if (ok) SettingsManager.getPreferences(context).edit().putBoolean(PREF_APPLIED, true).apply()
            else Log.w(TAG, "Default configuration not applied: $result")
            ok
        } catch (e: Exception) {
            Log.e(TAG, "Default configuration failed", e)
            false
        } finally {
            dir.deleteRecursively()
        }
    }

    private fun copyAssets(context: Context, assetPath: String, target: File) {
        val children = context.assets.list(assetPath).orEmpty()
        if (children.isEmpty()) {
            target.parentFile?.mkdirs()
            context.assets.open(assetPath).use { input -> target.outputStream().use { input.copyTo(it) } }
            return
        }
        target.mkdirs()
        children.forEach { child -> copyAssets(context, "$assetPath/$child", File(target, child)) }
    }
}
