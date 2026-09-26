package it.palsoftware.pastiera

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class HiddenKeyboardAppsTest {

    private val context get() = RuntimeEnvironment.getApplication()

    @Before
    fun reset() {
        SettingsManager.getPreferences(context).edit().clear().commit()
    }

    @Test
    fun parsesPackageNamesFromFreeTextAndDropsJunk() {
        assertEquals(
            listOf("com.termux.x11", "org.example.app"),
            SettingsManager.parsePackageList("com.termux.x11, org.example.app\nnotapackage 1bad.pkg com.termux.x11")
        )
    }

    @Test
    fun emptyByDefault() {
        assertTrue(SettingsManager.getHiddenKeyboardApps(context).isEmpty())
        assertFalse(SettingsManager.isKeyboardHiddenForApp(context, "com.termux.x11"))
    }

    @Test
    fun storedAppsAreHidden() {
        SettingsManager.setHiddenKeyboardApps(context, listOf("com.termux.x11"))

        assertTrue(SettingsManager.isKeyboardHiddenForApp(context, "com.termux.x11"))
        assertFalse(SettingsManager.isKeyboardHiddenForApp(context, "com.termux"))
        assertFalse(SettingsManager.isKeyboardHiddenForApp(context, null))
    }
}
