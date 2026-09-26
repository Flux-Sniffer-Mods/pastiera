package it.palsoftware.pastiera

import android.content.Context

/** Pastiera Flux settings (Settings > Pastiera Flux); their screens expose the same stable IDs. */
internal fun fluxSettingEntries(): List<SettingEntry> {
    val emoji = SettingRoute(SettingsDestination.FluxEmojiGifs)
    val titan = SettingRoute(SettingsDestination.FluxTitanScreen)
    val hidden = SettingRoute(SettingsDestination.FluxHiddenApps)
    val desktop = SettingRoute(SettingsDestination.FluxLinuxDesktop)
    val offline = SettingRoute(SettingsDestination.FluxOffline)
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
        SettingEntry(SettingLinkIds.MAIN_FLUX_OFFLINE, R.string.flux_offline_title,
            summaryRes = R.string.flux_offline_description, route = offline),
        SettingEntry("offline.enabled", R.string.flux_offline_switch_title,
            summaryRes = R.string.flux_offline_switch_description, route = offline),
        // Emoji & GIFs
        SettingEntry("flux_emoji.picker_key", R.string.emoji_picker_key_title, route = emoji),
        SettingEntry("flux_emoji.key_target", R.string.emoji_key_target_title, route = emoji),
        SettingEntry("flux_emoji.key_auto_close", R.string.emoji_key_auto_close_title, route = emoji),
        SettingEntry("flux_emoji.recents_key", R.string.emoji_layer_recents_key_title, route = emoji),
        SettingEntry("flux_emoji.gif_search", R.string.gif_settings_title, route = emoji),
        SettingEntry("flux_emoji.focus_picker", R.string.flux_focus_picker_title,
            summaryRes = R.string.flux_focus_picker_description, route = emoji),
        SettingEntry("flux_emoji.focus_gif", R.string.flux_focus_gif_title,
            summaryRes = R.string.flux_focus_gif_description, route = emoji),
        SettingEntry("flux_emoji.type_to_search_layer", R.string.flux_type_to_search_layer_title,
            summaryRes = R.string.flux_type_to_search_layer_description, route = emoji),
        SettingEntry("flux_emoji.type_to_search_symbols", R.string.flux_type_to_search_symbols_title,
            summaryRes = R.string.flux_type_to_search_symbols_description, route = emoji),
        SettingEntry("flux_emoji.recents_first", R.string.flux_recents_first_title,
            summaryRes = R.string.flux_recents_first_description, route = emoji),
        SettingEntry("flux_emoji.gif_favourites", R.string.flux_gif_favourites_title,
            summaryRes = R.string.flux_gif_favourites_description, route = emoji),
        SettingEntry("flux_emoji.gif_recents", R.string.flux_gif_recents_title,
            summaryRes = R.string.flux_gif_recents_description, route = emoji),
        SettingEntry("flux_emoji.enter_emoji", R.string.flux_enter_emoji_title,
            summaryRes = R.string.flux_enter_emoji_description, route = emoji),
        SettingEntry("flux_emoji.enter_symbol", R.string.flux_enter_symbol_title,
            summaryRes = R.string.flux_enter_symbol_description, route = emoji),
        SettingEntry("flux_emoji.enter_gif", R.string.flux_enter_gif_title,
            summaryRes = R.string.flux_enter_gif_description, route = emoji),
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
