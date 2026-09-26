package it.palsoftware.pastiera

import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.automirrored.filled.ManageSearch
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RoundedCorner
import androidx.compose.material.icons.filled.ShortText
import androidx.compose.material.icons.filled.SmartButton
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/*
 * The main screen's groups, organised by what you are trying to do:
 *   Keyboards & layouts  - devices, input languages and layout switching, Titan 2 Elite screen
 *   Typing               - capitalisation and punctuation, editing keys, auto-correction, text expansion
 *   Look & sound         - theme and LED colours, the status bar, sound and haptics, variations, app language
 *   Apps                 - app shortcuts, Quick Launcher, hidden keyboard apps, Linux desktop, showing the keyboard
 * Modifiers & SYM, Emoji, symbols & GIFs, Trackpad & gestures and Privacy & system open their screens directly.
 */

@Composable
fun KeyboardsLayoutsHubScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onNavigate: (SettingsDestination) -> Unit
) {
    val context = LocalContext.current
    FluxScreenScaffold(stringResource(R.string.settings_keyboards_layouts_title), onBack, modifier) {
        SettingsCategoryRow(
            icon = Icons.Filled.Keyboard,
            title = stringResource(R.string.keyboards_devices_title),
            description = stringResource(R.string.settings_keyboards_devices_description),
            linkId = SettingLinkIds.MAIN_KEYBOARDS_DEVICES,
            onClick = { onNavigate(SettingsDestination.KeyboardsDevices) }
        )
        SettingsCategoryRow(
            icon = Icons.Filled.Language,
            title = stringResource(R.string.custom_input_styles_title),
            description = stringResource(R.string.settings_input_languages_description),
            linkId = SettingLinkIds.MAIN_CUSTOM_INPUT_STYLES,
            onClick = { onNavigate(SettingsDestination.CustomInputStyles) }
        )
        if (fluxTitanScreenAvailable(context)) {
            SettingsCategoryRow(
                icon = Icons.Filled.RoundedCorner,
                title = stringResource(R.string.flux_titan_screen_title),
                description = stringResource(R.string.flux_titan_screen_description),
                linkId = SettingLinkIds.MAIN_FLUX_TITAN_SCREEN,
                onClick = { onNavigate(SettingsDestination.FluxTitanScreen) }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun TypingHubScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onNavigate: (SettingsDestination) -> Unit
) {
    FluxScreenScaffold(stringResource(R.string.settings_typing_title), onBack, modifier) {
        SettingsCategoryRow(
            icon = Icons.Filled.TextFields,
            title = stringResource(R.string.settings_capitalisation_punctuation_title),
            description = stringResource(R.string.settings_capitalisation_punctuation_description),
            linkId = SettingLinkIds.MAIN_TEXT_INPUT,
            onClick = { onNavigate(SettingsDestination.TextInput) }
        )
        SettingsCategoryRow(
            icon = Icons.AutoMirrored.Filled.KeyboardReturn,
            title = stringResource(R.string.settings_editing_keys_title),
            description = stringResource(R.string.settings_editing_keys_description),
            linkId = SettingLinkIds.MAIN_EDITING_KEYS,
            onClick = { onNavigate(SettingsDestination.EditingKeys) }
        )
        SettingsCategoryRow(
            icon = Icons.Filled.Spellcheck,
            title = stringResource(R.string.settings_category_auto_correction),
            description = stringResource(R.string.settings_auto_correction_description),
            linkId = SettingLinkIds.MAIN_AUTO_CORRECTION,
            onClick = { onNavigate(SettingsDestination.AutoCorrection) }
        )
        SettingsCategoryRow(
            icon = Icons.Filled.ShortText,
            title = stringResource(R.string.text_expansion_title),
            description = stringResource(R.string.settings_text_expansion_hub_description),
            linkId = SettingLinkIds.TEXT_INPUT_TEXT_EXPANSION,
            onClick = { onNavigate(SettingsDestination.TextExpansion) }
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun LookSoundHubScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onNavigate: (SettingsDestination) -> Unit,
    onOpenCustomization: (String) -> Unit
) {
    val context = LocalContext.current
    FluxScreenScaffold(stringResource(R.string.settings_look_sound_title), onBack, modifier) {
        SettingsCategoryRow(
            icon = Icons.Filled.Palette,
            title = stringResource(R.string.settings_theme_led_colours_title),
            description = stringResource(R.string.settings_theme_led_colours_description),
            linkId = SettingLinkIds.MAIN_KEYBOARD_THEME,
            onClick = { onOpenCustomization(SettingsActivity.CUSTOMIZATION_DESTINATION_KEYBOARD_THEME) }
        )

        SettingsSectionDivider(stringResource(R.string.settings_section_status_bar))
        SettingsCategoryRow(
            icon = Icons.Filled.SmartButton,
            title = stringResource(R.string.status_bar_buttons_title),
            description = stringResource(R.string.status_bar_buttons_description),
            linkId = SettingLinkIds.MAIN_STATUS_BAR_BUTTONS,
            onClick = { onOpenCustomization(SettingsActivity.CUSTOMIZATION_DESTINATION_STATUS_BAR_BUTTONS) }
        )
        SwipePadThresholdRow()

        SettingsSectionDivider(stringResource(R.string.settings_section_sound_popups))
        SettingsCategoryRow(
            icon = Icons.AutoMirrored.Filled.VolumeUp,
            title = stringResource(R.string.settings_category_sounds),
            description = stringResource(R.string.settings_sounds_description),
            linkId = "customization.sounds",
            onClick = { onOpenCustomization("sounds") }
        )
        SettingsCategoryRow(
            icon = Icons.Filled.Tune,
            title = stringResource(R.string.variation_customize_title),
            description = stringResource(R.string.settings_variations_popup_description),
            linkId = "customization.variations",
            onClick = { onOpenCustomization(SettingsActivity.CUSTOMIZATION_DESTINATION_VARIATIONS) }
        )

        SettingsSectionDivider(stringResource(R.string.settings_section_language))
        SettingsCategoryRow(
            icon = ImageVector.vectorResource(R.drawable.translate_24),
            title = stringResource(R.string.app_language_title),
            description = currentAppLanguageLabel(context),
            linkId = SettingLinkIds.MAIN_APP_LANGUAGE,
            onClick = { onNavigate(SettingsDestination.AppLanguage) }
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun AppsHubScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onNavigate: (SettingsDestination) -> Unit,
    onOpenCustomization: (String) -> Unit
) {
    val context = LocalContext.current
    var autoShowKeyboard by remember { mutableStateOf(SettingsManager.getAutoShowKeyboard(context)) }
    FluxScreenScaffold(stringResource(R.string.settings_apps_title), onBack, modifier) {
        SettingsCategoryRow(
            icon = Icons.Filled.SwapHoriz,
            title = stringResource(R.string.app_shortcuts_title),
            description = stringResource(R.string.app_shortcuts_description),
            linkId = SettingLinkIds.MAIN_APP_SHORTCUTS,
            onClick = { onNavigate(SettingsDestination.AppShortcuts) }
        )
        SettingsCategoryRow(
            icon = Icons.AutoMirrored.Filled.ManageSearch,
            title = stringResource(R.string.starter_launcher_shortcuts_title),
            description = stringResource(R.string.settings_quick_launcher_description),
            linkId = SettingLinkIds.MAIN_LAUNCHER_SHORTCUTS,
            onClick = { onOpenCustomization(SettingsActivity.CUSTOMIZATION_DESTINATION_LAUNCHER_SHORTCUTS) }
        )
        SettingsCategoryRow(
            icon = Icons.Filled.VisibilityOff,
            title = stringResource(R.string.flux_hidden_apps_title),
            description = stringResource(R.string.flux_hidden_apps_description),
            linkId = SettingLinkIds.MAIN_FLUX_HIDDEN_APPS,
            onClick = { onNavigate(SettingsDestination.FluxHiddenApps) }
        )
        SettingsCategoryRow(
            icon = Icons.Filled.DesktopWindows,
            title = stringResource(R.string.flux_linux_desktop_title),
            description = stringResource(R.string.flux_linux_desktop_description),
            linkId = SettingLinkIds.MAIN_FLUX_LINUX_DESKTOP,
            onClick = { onNavigate(SettingsDestination.FluxLinuxDesktop) }
        )
        FluxSwitchRow(
            linkId = SettingLinkIds.TEXT_INPUT_AUTO_SHOW_KEYBOARD,
            title = stringResource(R.string.auto_show_keyboard_title),
            description = stringResource(R.string.auto_show_keyboard_description),
            checked = autoShowKeyboard,
            onCheckedChange = { enabled ->
                autoShowKeyboard = enabled
                SettingsManager.setAutoShowKeyboard(context, enabled)
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/** How far a swipe on the status bar's swipe pad moves the cursor by one step. */
@Composable
private fun SwipePadThresholdRow() {
    val context = LocalContext.current
    val prefs = remember { SettingsManager.getPreferences(context) }
    // Stored 3 to 25 (distance per step); the slider shows it inverted so right is more sensitive
    var threshold by remember { mutableStateOf(SettingsManager.getSwipeIncrementalThreshold(context)) }
    DisposableEffect(prefs) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "swipe_incremental_threshold") {
                threshold = SettingsManager.getSwipeIncrementalThreshold(context)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    val min = SettingsManager.getMinSwipeIncrementalThreshold()
    val max = SettingsManager.getMaxSwipeIncrementalThreshold()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .settingRow(SettingLinkIds.ADVANCED_SWIPE_INCREMENTAL_THRESHOLD)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.TouchApp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.swipe_incremental_threshold_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    text = "${String.format("%.1f", threshold)} ${stringResource(R.string.dip_unit)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Slider(
                value = max + min - threshold,
                onValueChange = { inverted ->
                    val actual = max + min - inverted
                    threshold = actual
                    SettingsManager.setSwipeIncrementalThreshold(context, actual)
                },
                valueRange = min..max,
                steps = 16,
                modifier = Modifier
                    .weight(1.0f)
                    .height(24.dp)
            )
        }
    }
}

/** Update checks (GitHub builds only): the nightly channel and the release channel. */
@Composable
internal fun SettingsUpdateRows(context: android.content.Context) {
    var checkingNightly by remember { mutableStateOf(false) }
    var checkingForUpdates by remember { mutableStateOf(false) }
    if (!it.palsoftware.pastiera.update.shouldUseGithubUpdateChecks(context)) return
    if (BuildConfig.RELEASE_CHANNEL == "nightly") {
        SettingsCategoryRow(
            icon = Icons.Filled.Code,
            title = stringResource(if (checkingNightly) R.string.nightly_update_checking else R.string.nightly_update_settings_title),
            description = stringResource(R.string.nightly_update_settings_description),
            enabled = !checkingNightly,
            onClick = {
                checkingNightly = true
                it.palsoftware.pastiera.update.checkForNightlyUpdate(context, ignoreDismissedReleases = false) { result ->
                    checkingNightly = false
                    if (result.hasAnnouncement) {
                        it.palsoftware.pastiera.update.showReleaseNotice(context, result)
                    } else {
                        Toast.makeText(
                            context,
                            if (result.successful) R.string.nightly_update_current else R.string.settings_update_check_failed,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        )
    }
    SettingsCategoryRow(
        icon = ImageVector.vectorResource(R.drawable.plektra_open_monochrome_24),
        title = stringResource(
            if (checkingForUpdates) R.string.settings_update_checking else R.string.settings_update_section_title
        ),
        description = stringResource(R.string.settings_update_section_description),
        enabled = !checkingForUpdates,
        onClick = {
            checkingForUpdates = true
            it.palsoftware.pastiera.update.checkForUpdate(
                context = context,
                releaseChannel = BuildConfig.RELEASE_CHANNEL,
                ignoreDismissedReleases = false
            ) { result ->
                checkingForUpdates = false
                when {
                    !result.successful -> Toast.makeText(
                        context,
                        context.getString(R.string.settings_update_check_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                    result.hasAnnouncement && result.releaseTag != null && result.displayName != null ->
                        it.palsoftware.pastiera.update.showUpdateDialog(
                            context,
                            result.releaseTag,
                            result.displayName,
                            result.releasePageUrl
                        )
                    else -> Toast.makeText(
                        context,
                        context.getString(R.string.settings_update_up_to_date),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    )
}

/**
 * Developer options (Privacy & system, behind its switch): calibration, debugging and preview
 * tools that most people never need.
 */
@Composable
fun DeveloperOptionsScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val context = LocalContext.current
    if (settingsChild(context, "developer") == "ime_test") {
        ImeTestScreen(modifier = modifier, onBack = onBack)
        return
    }
    FluxScreenScaffold(stringResource(R.string.developer_options_title), onBack, modifier) {
        FluxNote(stringResource(R.string.developer_options_note))
        if (fluxTitanScreenAvailable(context)) {
            FluxActionRow(
                linkId = "advanced.corner_calibration",
                title = stringResource(R.string.corner_calibration_title),
                description = stringResource(R.string.corner_calibration_description),
                onClick = { context.startActivity(android.content.Intent(context, CornerCalibrationActivity::class.java)) }
            )
        }
        FluxActionRow(
            linkId = SettingLinkIds.TRACKPAD_DEBUG,
            title = stringResource(R.string.trackpad_debug_title),
            description = stringResource(R.string.trackpad_debug_description),
            onClick = { context.startActivity(android.content.Intent(context, TrackpadDebugActivity::class.java)) }
        )
        FluxActionRow(
            linkId = SettingLinkIds.ADVANCED_SHOW_RELEASE_NOTES_TUTORIAL,
            title = stringResource(R.string.tutorial_show_release_notes),
            description = stringResource(R.string.tutorial_show_release_notes_description),
            onClick = {
                context.startActivity(android.content.Intent(context, TutorialActivity::class.java).apply {
                    putExtra(TutorialActivity.EXTRA_UPDATE_TUTORIAL, true)
                    putExtra(TutorialActivity.EXTRA_PREVIEW_UPDATE_TUTORIAL, true)
                    putExtra(TutorialActivity.EXTRA_PREVIOUS_VERSION, "0.84beta")
                })
            }
        )
        if (BuildConfig.DEBUG) {
            FluxActionRow(
                linkId = null,
                title = "IME Test Screen",
                description = "Test all input field types and IME actions",
                onClick = { openSettingsChild(context, "developer", "ime_test") }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
