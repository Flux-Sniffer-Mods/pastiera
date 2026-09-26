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
class DefaultConfigTest {
    private val context get() = RuntimeEnvironment.getApplication()

    @Before
    fun clear() {
        SettingsManager.getPreferences(context).edit().clear().commit()
    }

    @Test
    fun theBundledConfigurationIsAppliedThroughTheRestore() {
        assertTrue(DefaultConfig.apply(context))
        assertTrue(SettingsManager.getPreferences(context).getBoolean(DefaultConfig.PREF_APPLIED, false))
        // A few of its choices
        assertTrue(SettingsManager.getSmartAltOffAfterOpening(context))
        assertTrue(SettingsManager.getSmartCtrlOffAfterShortcut(context))
        assertFalse(SettingsManager.getEmojiSuggestionsEnabled(context))
        assertTrue(SettingsManager.getLedIndividualColorsEnabled(context))
        assertEquals(listOf("com.termux.x11", "bitpit.launcher"), SettingsManager.getHiddenKeyboardApps(context))
        // Letters on the emoji layer and symbols pages type their mappings, not a search
        assertFalse(SettingsManager.getEmojiLayerTypeToSearch(context))
        assertFalse(SettingsManager.getSymbolsTypeToSearch(context))
    }

    @Test
    fun unitTestsKeepPastierasOwnDefaults() {
        assertFalse(DefaultConfig.applyIfFreshInstall(context))
    }

    @Test
    fun devsChoiceIsTheDefaultVariationBar() {
        assertEquals(SettingsManager.STATIC_VARIATION_PRESET_DEV_CHOICE, SettingsManager.getStaticVariationBarPreset(context))
        assertEquals(
            SettingsManager.getDevChoiceStaticVariationBasePreset(),
            it.palsoftware.pastiera.data.variation.VariationRepository.loadStaticVariations(context.assets, context)
        )
        // The older on/off switch, set before presets existed, keeps its meaning
        SettingsManager.getPreferences(context).edit().putBoolean("static_variation_bar_mode", false).commit()
        assertEquals(SettingsManager.STATIC_VARIATION_PRESET_OFF, SettingsManager.getStaticVariationBarPreset(context))
    }

    @Test
    fun niagaraIsHiddenByDefault() {
        assertEquals(listOf("bitpit.launcher"), SettingsManager.getHiddenKeyboardApps(context))
    }

    @Test
    fun recommendedSettingsCountWhatDiffersThenMatchOnceApplied() {
        // A fresh start differs from the recommended configuration
        assertTrue(DefaultConfig.differingSettings(context) > 0)
        assertTrue(DefaultConfig.apply(context))
        assertEquals(0, DefaultConfig.differingSettings(context))
        SettingsManager.setEmojiLayerTypeToSearch(context, true)
        assertEquals(1, DefaultConfig.differingSettings(context))
    }
}
