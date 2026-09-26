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
class Titan2EliteChromeSettingsTest {

    private val context get() = RuntimeEnvironment.getApplication()

    @Before
    fun reset() {
        SettingsManager.getPreferences(context).edit().clear().commit()
    }

    @Test
    fun defaultsKeepTheCurrentLook() {
        assertFalse(SettingsManager.getTitan2EliteFillCorners(context))
        assertFalse(SettingsManager.getTitan2EliteStraightOuterButtons(context))
        assertEquals(0, SettingsManager.getTitan2EliteStatusBarLiftDp(context))
        assertEquals(0, SettingsManager.getTitan2EliteStatusBarLiftPx(context))
    }

    @Test
    fun fillCornersIsStored() {
        SettingsManager.setTitan2EliteFillCorners(context, true)
        assertTrue(SettingsManager.getTitan2EliteFillCorners(context))
    }

    @Test
    fun straightOuterButtonsIsStored() {
        SettingsManager.setTitan2EliteStraightOuterButtons(context, true)
        assertTrue(SettingsManager.getTitan2EliteStraightOuterButtons(context))
        assertTrue(
            SettingsManager.getPreferences(context)
                .contains(SettingsManager.KEY_TITAN2_ELITE_STRAIGHT_OUTER_BUTTONS)
        )
    }

    @Test
    fun liftIsClampedAndConvertedToPixels() {
        SettingsManager.setTitan2EliteStatusBarLiftDp(context, 99)
        assertEquals(SettingsManager.TITAN2_ELITE_STATUS_BAR_LIFT_MAX_DP, SettingsManager.getTitan2EliteStatusBarLiftDp(context))

        SettingsManager.setTitan2EliteStatusBarLiftDp(context, -3)
        assertEquals(0, SettingsManager.getTitan2EliteStatusBarLiftDp(context))

        SettingsManager.setTitan2EliteStatusBarLiftDp(context, 8)
        val density = context.resources.displayMetrics.density
        assertEquals(Math.round(8 * density), SettingsManager.getTitan2EliteStatusBarLiftPx(context))
    }
}
