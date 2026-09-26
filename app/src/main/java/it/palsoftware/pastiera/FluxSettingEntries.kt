package it.palsoftware.pastiera

import android.content.Context

/** Pastiera Flux settings (Settings > Pastiera Flux); their screens expose the same stable IDs. */
internal fun fluxSettingEntries(): List<SettingEntry> {
    val emoji = SettingRoute(SettingsDestination.FluxEmojiGifs)
    val titan = SettingRoute(SettingsDestination.FluxTitanScreen)
    val hidden = SettingRoute(SettingsDestination.FluxHiddenApps)
    val desktop = SettingRoute(SettingsDestination.FluxLinuxDesktop)
    val onTitan: (Context) -> Boolean = ::fluxTitanScreenAvailable
    return listOf(
        // Main screen rows
        SettingEntry(SettingLinkIds.MAIN_FLUX_EMOJI_GIFS, R.string.flux_emoji_gifs_title,
            summaryRes = R.string.flux_emoji_gifs_description, route = emoji),
        SettingEntry(SettingLinkIds.MAIN_FLUX_TITAN_SCREEN, R.string.flux_titan_screen_title,
            summaryRes = R.string.flux_titan_screen_description, route = titan, availabilityCheck = onTitan),
        SettingEntry(SettingLinkIds.MAIN_FLUX_HIDDEN_APPS, R.string.flux_hidden_apps_title,
            summaryRes = R.string.flux_hidden_apps_description, route = hidden),
        SettingEntry(SettingLinkIds.MAIN_FLUX_LINUX_DESKTOP, R.string.flux_linux_desktop_title,
            summaryRes = R.string.flux_linux_desktop_description, route = desktop),
        // Emoji & GIFs
        SettingEntry("flux_emoji.picker_key", R.string.emoji_picker_key_title, route = emoji),
        SettingEntry("flux_emoji.key_target", R.string.emoji_key_target_title, route = emoji),
        SettingEntry("flux_emoji.key_auto_close", R.string.emoji_key_auto_close_title, route = emoji),
        SettingEntry("flux_emoji.recents_key", R.string.emoji_layer_recents_key_title, route = emoji),
        SettingEntry("flux_emoji.gif_search", R.string.gif_settings_title, route = emoji),
        // Titan 2 Elite screen (its corner calibration row is advanced.corner_calibration)
        SettingEntry("titan_screen.fill_corners", R.string.titan2_elite_fill_corners_title,
            summaryRes = R.string.titan2_elite_fill_corners_description, route = titan, availabilityCheck = onTitan),
        SettingEntry("titan_screen.straight_outer_buttons", R.string.titan2_elite_straight_outer_buttons_title,
            summaryRes = R.string.titan2_elite_straight_outer_buttons_description, route = titan, availabilityCheck = onTitan),
        SettingEntry("titan_screen.status_bar_lift", R.string.titan2_elite_status_bar_lift_title,
            summaryRes = R.string.titan2_elite_status_bar_lift_description, route = titan, availabilityCheck = onTitan),
        // Hidden keyboard apps
        SettingEntry("hidden_apps.apps", R.string.hidden_keyboard_apps_title,
            summaryRes = R.string.hidden_keyboard_apps_description, route = hidden),
        SettingEntry("hidden_apps.accessibility", R.string.flux_accessibility_title,
            summaryRes = R.string.flux_accessibility_off, route = hidden),
        // Linux desktop
        SettingEntry("linux_desktop.standard_ctrl_sym", R.string.flux_standard_ctrl_sym_title,
            summaryRes = R.string.flux_standard_ctrl_sym_description, route = desktop),
        SettingEntry("linux_desktop.keyboard_layout", R.string.flux_desktop_layout_title,
            summaryRes = R.string.flux_desktop_layout_description, route = desktop)
    )
}
