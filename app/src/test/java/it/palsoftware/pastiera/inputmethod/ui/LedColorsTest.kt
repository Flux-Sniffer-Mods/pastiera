package it.palsoftware.pastiera.inputmethod.ui

import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LedColorsTest {
    private fun hsv(color: Int) = FloatArray(3).also { Color.colorToHSV(color, it) }

    @Test
    fun offIsDimActiveIsTheColourLockedIsBrighterAndMoreSaturated() {
        val base = Color.rgb(90, 140, 200)
        val off = LedColors.shade(base, LedColors.Level.OFF)
        val active = LedColors.shade(base, LedColors.Level.ACTIVE)
        val locked = LedColors.shade(base, LedColors.Level.LOCKED)
        assertEquals(base, active)
        assertTrue(hsv(off)[2] < hsv(active)[2])
        assertTrue(Color.alpha(off) < 255)
        assertTrue(hsv(locked)[1] > hsv(active)[1])
        assertTrue(hsv(locked)[2] > hsv(active)[2])
        // Same hue throughout, so each LED stays recognisable
        assertEquals(hsv(active)[0], hsv(locked)[0], 1f)
    }

    @Test
    fun everyLedHasItsOwnDefault() {
        assertEquals(LedColors.Led.entries.size, LedColors.Led.entries.map { it.defaultColor }.toSet().size)
    }
}
