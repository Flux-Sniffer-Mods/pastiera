package it.palsoftware.pastiera.otp

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import it.palsoftware.pastiera.SettingsManager

/**
 * Reads new notifications for one-time codes, only while "One-time codes" is on and Android's
 * notification access is granted to the app. Only the code itself is kept (see [OneTimeCodes]).
 */
class OneTimeCodeListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notification = sbn?.notification ?: return
        if (sbn.packageName == packageName || !SettingsManager.getOneTimeCodesEnabled(this)) return
        val extras = notification.extras ?: return
        val text = listOfNotNull(
            extras.getCharSequence(Notification.EXTRA_TITLE),
            extras.getCharSequence(Notification.EXTRA_TEXT),
            extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
        ).joinToString(" ")
        if (text.isBlank()) return
        OneTimeCodes.extract(text)?.let { OneTimeCodes.offer(it) }
    }
}
