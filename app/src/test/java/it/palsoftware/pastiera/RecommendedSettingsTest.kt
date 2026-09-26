package it.palsoftware.pastiera

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class RecommendedSettingsTest {
    private val context get() = RuntimeEnvironment.getApplication()

    @Test
    fun applyingTurnsEverythingOnAndLeavesNothingPending() {
        SettingsManager.setSmartAltOffAfterOpening(context, false)
        SettingsManager.setMaxAutoReplaceDistance(context, 3)
        assertTrue(RecommendedSettings.pending(context).isNotEmpty())
        RecommendedSettings.apply(context)
        assertTrue(RecommendedSettings.pending(context).isEmpty())
        assertTrue(SettingsManager.getSmartAltOffAfterOpening(context))
        assertEquals(1, SettingsManager.getMaxAutoReplaceDistance(context))
    }

    @Test
    fun everyItemHasItsOwnTitle() {
        val titles = RecommendedSettings.items().map { it.titleRes }
        assertEquals(titles.size, titles.toSet().size)
    }
}
