package it.palsoftware.pastiera

import android.content.Context
import android.os.Build
import it.palsoftware.pastiera.data.mappings.EmojiLayerProfiles
import it.palsoftware.pastiera.inputmethod.DeviceSpecific
import it.palsoftware.pastiera.shortcuts.AppShortcutSettings

/**
 * The recommended configuration for everything this build adds (Privacy & system > Backup and
 * settings > Recommended settings): the features that help every day on a hardware keyboard,
 * on, and the Titan 2 Elite's screen fitted when it's that phone. Fresh installs already start
 * close to this; the action switches on the ones left off for existing setups or to avoid
 * surprises, and lists each change before applying it.
 */
object RecommendedSettings {

    class Item(
        val titleRes: Int,
        val isApplied: (Context) -> Boolean,
        val apply: (Context) -> Unit
    )

    private fun switch(titleRes: Int, get: (Context) -> Boolean, set: (Context, Boolean) -> Unit, on: Boolean = true) =
        Item(titleRes, { get(it) == on }, { set(it, on) })

    fun items(): List<Item> = buildList {
        // Suggestions: the engine, the bar, conservative correction, emoji and autofill chips
        add(switch(R.string.experimental_suggestions_title, SettingsManager::isExperimentalSuggestionsEnabled, SettingsManager::setExperimentalSuggestionsEnabled))
        add(switch(R.string.auto_correct_suggestions_toggle_title, SettingsManager::getSuggestionsEnabled, SettingsManager::setSuggestionsEnabled))
        add(Item(
            R.string.auto_correct_auto_replace_title,
            { SettingsManager.getAutoReplaceOnSpaceEnter(it) && SettingsManager.getMaxAutoReplaceDistance(it) == 1 },
            {
                SettingsManager.setAutoReplaceOnSpaceEnter(it, true)
                // One character off at most: fixes typos without rewriting unusual words
                SettingsManager.setMaxAutoReplaceDistance(it, 1)
            }
        ))
        add(switch(R.string.emoji_suggestions_title, SettingsManager::getEmojiSuggestionsEnabled, SettingsManager::setEmojiSuggestionsEnabled))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            add(switch(R.string.inline_autofill_title, SettingsManager::getInlineAutofillEnabled, SettingsManager::setInlineAutofillEnabled))
        }
        add(switch(R.string.paste_suggestion_title, SettingsManager::getPasteSuggestionEnabled, SettingsManager::setPasteSuggestionEnabled))

        // Modifiers that switch themselves off when you're done with them
        add(switch(R.string.smart_alt_off_title, SettingsManager::getSmartAltOffAfterOpening, SettingsManager::setSmartAltOffAfterOpening))
        add(switch(R.string.smart_ctrl_off_title, SettingsManager::getSmartCtrlOffAfterShortcut, SettingsManager::setSmartCtrlOffAfterShortcut))

        // Emoji and SYM: close after picking, the emoji layer follows the app
        add(switch(R.string.emoji_key_auto_close_title, SettingsManager::getEmojiKeyAutoClose, SettingsManager::setEmojiKeyAutoClose))
        add(switch(R.string.sym_auto_close_title, SettingsManager::getSymAutoClose, SettingsManager::setSymAutoClose))
        add(switch(R.string.emoji_profiles_switch_by_app_title, EmojiLayerProfiles::switchByApp, EmojiLayerProfiles::setSwitchByApp))

        // Apps: the same shortcuts everywhere, Enter by app, terminals
        add(switch(R.string.app_shortcuts_enabled_title, { AppShortcutSettings.config(it).enabled }, AppShortcutSettings::setEnabled))
        add(switch(R.string.app_shortcuts_suggestions_title, { AppShortcutSettings.config(it).suggestionsEnabled }, AppShortcutSettings::setSuggestionsEnabled))
        add(switch(R.string.app_enter_behaviour_enable_title, SettingsManager::getAppEnterBehaviorEnabled, SettingsManager::setAppEnterBehaviorEnabled))
        add(switch(R.string.terminal_mode_enabled_title, SettingsManager::getTerminalModeEnabled, SettingsManager::setTerminalModeEnabled))

        // Privacy: learn nothing where apps ask for it
        add(switch(R.string.incognito_follow_apps_title, SettingsManager::getIncognitoFollowApps, SettingsManager::setIncognitoFollowApps))

        // Titan 2 Elite: the bar fitted to the rounded display
        if (DeviceSpecific.isTitan2EliteDevice()) {
            add(switch(R.string.titan2_elite_rounded_corners_title, SettingsManager::getTitan2EliteRoundedCornerInsetsEnabled, SettingsManager::setTitan2EliteRoundedCornerInsetsEnabled))
            add(switch(R.string.titan2_elite_fill_corners_title, SettingsManager::getTitan2EliteFillCorners, SettingsManager::setTitan2EliteFillCorners))
        }
    }

    /** What applying would change. */
    fun pending(context: Context): List<Item> = items().filterNot { it.isApplied(context) }

    fun apply(context: Context) = pending(context).forEach { it.apply(context) }
}

