package it.palsoftware.pastiera

import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Settings grouped by task: moved rows keep their IDs and route to their new screens. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SettingsLayoutTest {
    private val context: Context get() = RuntimeEnvironment.getApplication()

    @Before
    fun resetSettings() {
        SettingsManager.getPreferences(context).edit().clear().commit()
    }

    private fun route(id: String) = requireNotNull(SettingLinkRegistry.byId(id)) { "Missing $id" }.route

    @Test
    fun movedRowsRouteToTheirNewScreens() {
        val expected = mapOf(
            SettingLinkIds.TEXT_INPUT_AUTO_SHOW_KEYBOARD to SettingsDestination.Apps,
            SettingLinkIds.TEXT_INPUT_CLEAR_ALT_ON_SPACE to SettingsDestination.Modifiers,
            SettingLinkIds.TEXT_INPUT_ALT_CTRL_SPEECH_SHORTCUT to SettingsDestination.Modifiers,
            SettingLinkIds.TEXT_INPUT_SHIFT_BACKSPACE_DELETE to SettingsDestination.EditingKeys,
            SettingLinkIds.TEXT_INPUT_ALT_BACKSPACE_DELETE to SettingsDestination.EditingKeys,
            SettingLinkIds.TEXT_INPUT_BACKSPACE_AT_START_DELETE to SettingsDestination.EditingKeys,
            SettingLinkIds.TEXT_INPUT_DELETE_NAV_MODE to SettingsDestination.EditingKeys,
            SettingLinkIds.TEXT_INPUT_TEXT_EXPANSION to SettingsDestination.TextExpansion,
            SettingLinkIds.TEXT_INPUT_AUTO_CAPITALIZE to SettingsDestination.TextInput,
            SettingLinkIds.ADVANCED_TRACKPAD_GESTURES to SettingsDestination.TrackpadGestures,
            SettingLinkIds.ADVANCED_SWIPE_INCREMENTAL_THRESHOLD to SettingsDestination.LookSound,
            SettingLinkIds.ADVANCED_BACKUP to SettingsDestination.Advanced,
            "trackpad.swipe_to_delete" to SettingsDestination.TrackpadGestures,
            "sym.auto_close" to SettingsDestination.FluxEmojiGifs,
            "sym.emoji_height" to SettingsDestination.FluxEmojiGifs,
            SettingLinkIds.MAIN_APP_SHORTCUTS to SettingsDestination.AppShortcuts
        )
        expected.forEach { (id, destination) -> assertEquals(id, destination, route(id).destination) }
    }

    @Test
    fun keyShortcutTogglesLiveOnTheKeyShortcutsPage() {
        listOf(
            "quick_launcher.sym_shortcuts",
            "quick_launcher.alt_shortcuts",
            "quick_launcher.sym_in_text_fields",
            "quick_launcher.alt_in_text_fields"
        ).forEach { id ->
            assertEquals(id, SettingsActivity.CUSTOMIZATION_DESTINATION_KEY_SHORTCUTS, route(id).customizationDestination)
        }
        assertEquals(
            SettingsActivity.CUSTOMIZATION_DESTINATION_LAUNCHER_SHORTCUTS,
            route("quick_launcher.enabled").customizationDestination
        )
    }

    @Test
    fun retiredRowsAreHiddenButOldLinksStillLand() {
        val retired = mapOf(
            SettingLinkIds.MODIFIERS_ALT_KEY_SHORTCUTS to SettingLinkIds.MODIFIERS_SYM_SHORTCUTS,
            SettingLinkIds.MAIN_CUSTOMIZATION to SettingLinkIds.MAIN_LOOK_SOUND
        )
        retired.forEach { (id, landsOn) ->
            val entry = requireNotNull(SettingLinkRegistry.byId(id))
            assertFalse(entry.isAvailable(context))
            assertEquals(landsOn, SettingLinkRegistry.visibleTarget(context, entry).id)
            assertFalse(SettingLinkRegistry.search(context, context.getString(entry.titleRes)).any { it.id == id })
        }
    }

    @Test
    fun everyScreenHasABreadcrumbTitle() {
        SettingsDestination.entries
            .filter { it != SettingsDestination.DeviceSymLayerEditor }
            .forEach { destination ->
                assertTrue("$destination lacks a title", SettingLinkRegistry.destinationTitles.containsKey(destination))
            }
    }

    @Test
    fun theNewGroupsAreSearchable() {
        mapOf(
            SettingLinkIds.MAIN_KEYBOARDS_LAYOUTS to R.string.settings_keyboards_layouts_title,
            SettingLinkIds.MAIN_TYPING to R.string.settings_typing_title,
            SettingLinkIds.MAIN_LOOK_SOUND to R.string.settings_look_sound_title,
            SettingLinkIds.MAIN_APPS to R.string.settings_apps_title,
            SettingLinkIds.MAIN_APP_SHORTCUTS to R.string.app_shortcuts_title
        ).forEach { (id, title) ->
            assertTrue(id, SettingLinkRegistry.search(context, context.getString(title)).any { it.id == id })
        }
    }

    @Test
    fun developerToolsOnlyShowWithDeveloperOptions() {
        val developerIds = listOf(
            SettingLinkIds.TRACKPAD_DEBUG,
            SettingLinkIds.ADVANCED_SHOW_RELEASE_NOTES_TUTORIAL,
            SettingLinkIds.MAIN_DEVELOPER
        )
        developerIds.forEach { id ->
            val entry = requireNotNull(SettingLinkRegistry.byId(id))
            assertFalse(id, entry.isAvailable(context))
            assertEquals(id, SettingLinkIds.DEVELOPER_OPTIONS_ENABLED, SettingLinkRegistry.visibleTarget(context, entry).id)
        }
        SettingsManager.setDeveloperOptionsEnabled(context, true)
        developerIds.forEach { id ->
            val entry = requireNotNull(SettingLinkRegistry.byId(id))
            assertTrue(id, entry.isAvailable(context))
            assertEquals(id, SettingsDestination.Developer, entry.route.destination)
        }
        // Corner calibration also needs the Titan 2 Elite screen
        assertEquals(SettingsDestination.Developer, route("advanced.corner_calibration").destination)
    }
}
