package it.palsoftware.pastiera

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.widget.Toast

/**
 * Android's restricted settings: an app installed from a file (like Flux Keyboard's releases)
 * can't have its accessibility service or notification access switched on until the person
 * allows it in App info (⋮ > Allow restricted settings). Features that need either service
 * send people to App info first while Android still blocks it, then to the service's own page.
 */
object RestrictedSettings {
    private const val OP_ACCESS_RESTRICTED_SETTINGS = "android:access_restricted_settings"

    /** Android still blocks this app's accessibility service and notification access. */
    fun blocked(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
        val appOps = context.getSystemService(AppOpsManager::class.java) ?: return false
        val mode = runCatching {
            appOps.unsafeCheckOpNoThrow(OP_ACCESS_RESTRICTED_SETTINGS, Process.myUid(), context.packageName)
        }.getOrNull() ?: return false
        return mode == AppOpsManager.MODE_ERRORED || mode == AppOpsManager.MODE_IGNORED
    }

    /** App info, where ⋮ > Allow restricted settings lifts the block. */
    fun openAppInfo(context: Context) {
        Toast.makeText(context, R.string.restricted_settings_toast, Toast.LENGTH_LONG).show()
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    /** Opens [action]'s page, or App info first while restricted settings block it. */
    fun openServicePage(context: Context, action: String) {
        if (blocked(context)) {
            openAppInfo(context)
            return
        }
        runCatching { context.startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    fun openAccessibility(context: Context) = openServicePage(context, Settings.ACTION_ACCESSIBILITY_SETTINGS)

    fun openNotificationAccess(context: Context) = openServicePage(context, Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
}
