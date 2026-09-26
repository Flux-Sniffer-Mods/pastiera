package it.palsoftware.pastiera.inputmethod

import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import it.palsoftware.pastiera.SettingsManager

/**
 * Filters only explicitly configured buttons of a supported Clicks Power Keyboard.
 *
 * Android handles Left Meta before an input method receives it. This service is therefore optional
 * and remains inert for other keyboards, other keys, and native button assignments.
 */
class ClicksLauncherButtonAccessibilityService : AccessibilityService() {
    private val mapper = ClicksLauncherAccessibilityKeyMapper()

    override fun onKeyEvent(event: KeyEvent): Boolean {
        // Hidden apps: keys for Pastiera's emoji/symbols panels go to Pastiera, not the app
        val taken = HiddenAppKeyObserver.intercept(event)
        HiddenAppKeyObserver.logKey(event, if (taken) "accessibility, to the keyboard" else "accessibility, to the app")
        if (taken) return true
        // Hidden apps with status LEDs: let the LEDs follow keys the app reads before any IME
        HiddenAppKeyObserver.observe(event)
        val device = event.device
        val result = mapper.map(
            event = event,
            isClicksPowerKeyboard = device?.let(DeviceSpecific::isClicksPowerKeyboard) == true,
            redButtonMode = SettingsManager.getClicksButtonMode(this),
            launcherButtonMode = SettingsManager.getClicksMetaButtonMode(this),
            altButtonMode = SettingsManager.getClicksAltButtonMode(this),
            microphoneButtonMode = SettingsManager.getClicksMicrophoneButtonMode(this)
        )
        result.directAction?.let { ClicksButtonDirectActionExecutor.execute(this, it) }
        val forwardedEvent = result.forwardedEvent
        if (forwardedEvent != null && !ClicksAccessibilityKeyBridge.dispatch(forwardedEvent)) {
            // Fail open when Pastiera has no active editor. Keeping the mapper's held-modifier
            // state would otherwise consume the following native key events as well.
            mapper.resetDevice(event.deviceId)
            return false
        }
        return result.consume
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        mapper.reset()
        super.onDestroy()
    }
}
