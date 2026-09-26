package it.palsoftware.pastiera

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.view.KeyEvent
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import it.palsoftware.pastiera.backup.BackupManager
import it.palsoftware.pastiera.backup.RestoreInspectionResult
import it.palsoftware.pastiera.backup.RestoreManager
import it.palsoftware.pastiera.data.desktop.DesktopKeyboardLayout
import it.palsoftware.pastiera.inputmethod.ClicksLauncherButtonAccessibilityService
import it.palsoftware.pastiera.inputmethod.DeviceSpecific
import it.palsoftware.pastiera.inputmethod.StatusBarController
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku

/*
 * Pastiera Flux settings, one screen per area (Settings > Pastiera Flux):
 *   Emoji & GIFs          - emoji key, emoji layer keys, GIF search
 *   Titan 2 Elite screen  - rounded corners, outer buttons, status bar
 *   Hidden keyboard apps  - apps where Pastiera stays out of sight, and the service they need
 *   Linux desktop         - Ctrl and Sym, and the desktop's keyboard layout
 */

/** The top bar with a back arrow and a scrolling column, as on the other settings screens. */
@Composable
private fun FluxScreenScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars),
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back_content_description)
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            content = content
        )
    }
}

/** A short explanation at the top of a screen or under a section header. */
@Composable
private fun FluxNote(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

/** A row with a title, a description and a switch; tapping anywhere on it toggles. */
@Composable
private fun FluxSwitchRow(
    linkId: String,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(modifier = Modifier.fillMaxWidth().settingRow(linkId) { onCheckedChange(!checked) }) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

/** A row with a title and a description that does something when tapped. */
@Composable
private fun FluxActionRow(linkId: String, title: String, description: String, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().settingRow(linkId, onClick)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** The Titan 2 Elite screen settings apply here (on the phone, or with its rounded corners on). */
internal fun fluxTitanScreenAvailable(context: Context): Boolean =
    DeviceSpecific.isTitan2EliteDevice() || SettingsManager.getTitan2EliteRoundedCornerInsetsEnabled(context)

/** Pastiera's accessibility service is on: hidden apps' status LEDs, panels, Ctrl and Sym need it. */
internal fun isPastieraAccessibilityServiceOn(context: Context): Boolean {
    val expected = ComponentName(context, ClicksLauncherButtonAccessibilityService::class.java)
    val manager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return false
    return manager
        .getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        .any { info ->
            val serviceInfo = info.resolveInfo?.serviceInfo ?: return@any false
            ComponentName(serviceInfo.packageName, serviceInfo.name) == expected
        }
}

// ------------------------------------------------------------------ Emoji & GIFs

@Composable
fun FluxEmojiGifsScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val context = LocalContext.current
    var emojiPickerKey by remember {
        mutableStateOf(SettingsManager.getEmojiPickerKey(context))
    }
    var showEmojiPickerKeyDialog by remember { mutableStateOf(false) }
    var emojiKeyOpensLayer by remember { mutableStateOf(SettingsManager.getEmojiKeyOpensLayer(context)) }
    var emojiKeyAutoClose by remember { mutableStateOf(SettingsManager.getEmojiKeyAutoClose(context)) }
    var emojiLayerRecentsKey by remember { mutableStateOf(SettingsManager.getEmojiLayerRecentsKey(context)) }
    var showRecentsKeyDialog by remember { mutableStateOf(false) }
    var gifsEnabled by remember { mutableStateOf(SettingsManager.getGifsEnabled(context)) }
    var klipyApiKey by remember { mutableStateOf(SettingsManager.getUserKlipyApiKey(context)) }
    var emojiLayerGifKey by remember { mutableStateOf(SettingsManager.getEmojiLayerGifKey(context)) }
    var showGifKeyDialog by remember { mutableStateOf(false) }
    var enterPicksEmoji by remember { mutableStateOf(SettingsManager.getEmojiSearchEnterPicks(context)) }
    var recentsFirst by remember { mutableStateOf(SettingsManager.getRecentsFirstInSearch(context)) }
    var gifFavourites by remember { mutableStateOf(SettingsManager.getGifShowFavourites(context)) }
    var gifRecents by remember { mutableStateOf(SettingsManager.getGifShowRecents(context)) }
    var enterPicksSymbol by remember { mutableStateOf(SettingsManager.getSymbolSearchEnterPicks(context)) }
    var enterPicksGif by remember { mutableStateOf(SettingsManager.getGifSearchEnterPicks(context)) }
    var emojiPickerFocus by remember { mutableStateOf(SettingsManager.getEmojiPickerFocusSearch(context)) }
    var gifFocus by remember { mutableStateOf(SettingsManager.getGifFocusSearch(context)) }
    var layerTypeToSearch by remember { mutableStateOf(SettingsManager.getEmojiLayerTypeToSearch(context)) }
    var symbolsTypeToSearch by remember { mutableStateOf(SettingsManager.getSymbolsTypeToSearch(context)) }

    FluxScreenScaffold(stringResource(R.string.flux_emoji_gifs_title), onBack, modifier) {
        FluxNote(stringResource(R.string.flux_emoji_gifs_note))

        SettingsSectionDivider(stringResource(R.string.flux_section_emoji_key))

        Surface(
            modifier = Modifier.settingRow("flux_emoji.picker_key")
                .fillMaxWidth()
                .height(64.dp)
                .clickable { showEmojiPickerKeyDialog = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEmotions,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.emoji_picker_key_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                    Text(
                        text = emojiPickerKeyLabel(context, emojiPickerKey),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }

        HorizontalDivider()

        if (emojiPickerKey != KeyEvent.KEYCODE_UNKNOWN) {
            // What the emoji key opens
            Column(
                modifier = Modifier.settingRow("flux_emoji.key_target")
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.emoji_key_target_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                listOf(false to R.string.emoji_key_target_picker, true to R.string.emoji_key_target_layer)
                    .forEach { (layer, label) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                emojiKeyOpensLayer = layer
                                SettingsManager.setEmojiKeyOpensLayer(context, layer)
                            },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = emojiKeyOpensLayer == layer,
                                onClick = {
                                    emojiKeyOpensLayer = layer
                                    SettingsManager.setEmojiKeyOpensLayer(context, layer)
                                }
                            )
                            Text(stringResource(label), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
            }

            HorizontalDivider()

            // Auto-close for the emoji key's screens, separate from SYM auto-close
            Row(
                modifier = Modifier.settingRow("flux_emoji.key_auto_close")
                    .fillMaxWidth()
                    .clickable {
                        emojiKeyAutoClose = !emojiKeyAutoClose
                        SettingsManager.setEmojiKeyAutoClose(context, emojiKeyAutoClose)
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.emoji_key_auto_close_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = stringResource(R.string.emoji_key_auto_close_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = emojiKeyAutoClose,
                    onCheckedChange = { enabled ->
                        emojiKeyAutoClose = enabled
                        SettingsManager.setEmojiKeyAutoClose(context, enabled)
                    }
                )
            }

            HorizontalDivider()
        }

        SettingsSectionDivider(stringResource(R.string.flux_section_emoji_layer))

        // Recents key on the emoji layer
        Surface(
            modifier = Modifier.settingRow("flux_emoji.recents_key")
                .fillMaxWidth()
                .clickable { showRecentsKeyDialog = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.emoji_layer_recents_key_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (emojiLayerRecentsKey == KeyEvent.KEYCODE_UNKNOWN) {
                            stringResource(R.string.emoji_layer_recents_key_off)
                        } else {
                            stringResource(R.string.emoji_layer_recents_key_current, getLetterFromKeyCode(emojiLayerRecentsKey))
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        SettingsSectionDivider(stringResource(R.string.flux_section_gif_search))

        // GIF search (KLIPY): a GIF key on the emoji layer and a GIF tab in the picker
        Column(
            modifier = Modifier.settingRow("flux_emoji.gif_search")
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.gif_settings_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = stringResource(R.string.gif_settings_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = gifsEnabled,
                    onCheckedChange = { enabled ->
                        gifsEnabled = enabled
                        SettingsManager.setGifsEnabled(context, enabled)
                    }
                )
            }
            if (gifsEnabled) {
                OutlinedTextField(
                    value = klipyApiKey,
                    onValueChange = { value ->
                        klipyApiKey = value
                        SettingsManager.setKlipyApiKey(context, value)
                    },
                    label = { Text(stringResource(R.string.gif_api_key_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                Text(
                    text = stringResource(
                        if (SettingsManager.hasBuiltInKlipyApiKey()) R.string.gif_api_key_help_builtin
                        else R.string.gif_api_key_help
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                TextButton(onClick = {
                    runCatching {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, android.net.Uri.parse(it.palsoftware.pastiera.data.gif.KlipyGifs.SIGNUP_URL))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                }) {
                    Text(stringResource(R.string.gif_get_key))
                }
                // The emoji layer key that opens GIF search
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clickable { showGifKeyDialog = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.emoji_layer_gif_key_title),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (emojiLayerGifKey == KeyEvent.KEYCODE_UNKNOWN) {
                                stringResource(R.string.emoji_layer_recents_key_off)
                            } else {
                                stringResource(R.string.emoji_layer_recents_key_current, getLetterFromKeyCode(emojiLayerGifKey))
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        SettingsSectionDivider(stringResource(R.string.flux_section_search_on_open))
        FluxSwitchRow(
            linkId = "flux_emoji.focus_picker",
            title = stringResource(R.string.flux_focus_picker_title),
            description = stringResource(R.string.flux_focus_picker_description),
            checked = emojiPickerFocus,
            onCheckedChange = { enabled ->
                emojiPickerFocus = enabled
                SettingsManager.setEmojiPickerFocusSearch(context, enabled)
            }
        )
        FluxSwitchRow(
            linkId = "flux_emoji.focus_gif",
            title = stringResource(R.string.flux_focus_gif_title),
            description = stringResource(R.string.flux_focus_gif_description),
            checked = gifFocus,
            onCheckedChange = { enabled ->
                gifFocus = enabled
                SettingsManager.setGifFocusSearch(context, enabled)
            }
        )
        FluxSwitchRow(
            linkId = "flux_emoji.type_to_search_layer",
            title = stringResource(R.string.flux_type_to_search_layer_title),
            description = stringResource(R.string.flux_type_to_search_layer_description),
            checked = layerTypeToSearch,
            onCheckedChange = { enabled ->
                layerTypeToSearch = enabled
                SettingsManager.setEmojiLayerTypeToSearch(context, enabled)
            }
        )
        FluxSwitchRow(
            linkId = "flux_emoji.type_to_search_symbols",
            title = stringResource(R.string.flux_type_to_search_symbols_title),
            description = stringResource(R.string.flux_type_to_search_symbols_description),
            checked = symbolsTypeToSearch,
            onCheckedChange = { enabled ->
                symbolsTypeToSearch = enabled
                SettingsManager.setSymbolsTypeToSearch(context, enabled)
            }
        )

        SettingsSectionDivider(stringResource(R.string.flux_section_enter))
        FluxSwitchRow(
            linkId = "flux_emoji.enter_emoji",
            title = stringResource(R.string.flux_enter_emoji_title),
            description = stringResource(R.string.flux_enter_emoji_description),
            checked = enterPicksEmoji,
            onCheckedChange = { enabled ->
                enterPicksEmoji = enabled
                SettingsManager.setEmojiSearchEnterPicks(context, enabled)
            }
        )
        FluxSwitchRow(
            linkId = "flux_emoji.enter_symbol",
            title = stringResource(R.string.flux_enter_symbol_title),
            description = stringResource(R.string.flux_enter_symbol_description),
            checked = enterPicksSymbol,
            onCheckedChange = { enabled ->
                enterPicksSymbol = enabled
                SettingsManager.setSymbolSearchEnterPicks(context, enabled)
            }
        )
        FluxSwitchRow(
            linkId = "flux_emoji.enter_gif",
            title = stringResource(R.string.flux_enter_gif_title),
            description = stringResource(R.string.flux_enter_gif_description),
            checked = enterPicksGif,
            onCheckedChange = { enabled ->
                enterPicksGif = enabled
                SettingsManager.setGifSearchEnterPicks(context, enabled)
            }
        )

        SettingsSectionDivider(stringResource(R.string.flux_section_recents))
        FluxSwitchRow(
            linkId = "flux_emoji.recents_first",
            title = stringResource(R.string.flux_recents_first_title),
            description = stringResource(R.string.flux_recents_first_description),
            checked = recentsFirst,
            onCheckedChange = { enabled ->
                recentsFirst = enabled
                SettingsManager.setRecentsFirstInSearch(context, enabled)
            }
        )
        FluxSwitchRow(
            linkId = "flux_emoji.gif_favourites",
            title = stringResource(R.string.flux_gif_favourites_title),
            description = stringResource(R.string.flux_gif_favourites_description),
            checked = gifFavourites,
            onCheckedChange = { enabled ->
                gifFavourites = enabled
                SettingsManager.setGifShowFavourites(context, enabled)
            }
        )
        FluxSwitchRow(
            linkId = "flux_emoji.gif_recents",
            title = stringResource(R.string.flux_gif_recents_title),
            description = stringResource(R.string.flux_gif_recents_description),
            checked = gifRecents,
            onCheckedChange = { enabled ->
                gifRecents = enabled
                SettingsManager.setGifShowRecents(context, enabled)
            }
        )

        // Dedicated emoji picker key: press the key to use (works with whatever keys the device has)
        if (showEmojiPickerKeyDialog) {
            val keyCaptureFocus = remember { FocusRequester() }
            var rejectedKey by remember { mutableStateOf(false) }
            AlertDialog(
                onDismissRequest = { showEmojiPickerKeyDialog = false },
                title = { Text(stringResource(R.string.emoji_picker_key_title)) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(keyCaptureFocus)
                            .focusable()
                            .onPreviewKeyEvent { event ->
                                val native = event.nativeKeyEvent
                                // Let Back close the dialog as usual
                                if (native.keyCode == KeyEvent.KEYCODE_BACK) return@onPreviewKeyEvent false
                                if (native.action == KeyEvent.ACTION_DOWN && native.repeatCount == 0) {
                                    if (SettingsManager.isAllowedEmojiPickerKey(native.keyCode, native.isPrintingKey)) {
                                        SettingsManager.setEmojiPickerKey(context, native.keyCode)
                                        emojiPickerKey = native.keyCode
                                        showEmojiPickerKeyDialog = false
                                    } else {
                                        rejectedKey = true
                                    }
                                }
                                true
                            },
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.emoji_picker_key_description),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = stringResource(
                                R.string.emoji_picker_key_current,
                                emojiPickerKeyLabel(context, emojiPickerKey)
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = stringResource(
                                if (rejectedKey) R.string.emoji_picker_key_rejected
                                else R.string.emoji_picker_key_press_prompt
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (rejectedKey) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary
                        )
                    }
                    LaunchedEffect(Unit) {
                        runCatching { keyCaptureFocus.requestFocus() }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            SettingsManager.setEmojiPickerKey(context, KeyEvent.KEYCODE_UNKNOWN)
                            emojiPickerKey = KeyEvent.KEYCODE_UNKNOWN
                            showEmojiPickerKeyDialog = false
                        }
                    ) {
                        Text(stringResource(R.string.emoji_picker_key_turn_off))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEmojiPickerKeyDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        // Recents key on the emoji layer: press the letter key to use, like the emoji key
        if (showRecentsKeyDialog) {
            EmojiLayerKeyDialog(
                title = stringResource(R.string.emoji_layer_recents_key_title),
                description = stringResource(R.string.emoji_layer_recents_key_description),
                currentKey = emojiLayerRecentsKey,
                letterFor = ::getLetterFromKeyCode,
                onKeyPressed = { keyCode ->
                    SettingsManager.setEmojiLayerRecentsKey(context, keyCode).also { if (it) emojiLayerRecentsKey = keyCode }
                },
                onTurnOff = {
                    SettingsManager.setEmojiLayerRecentsKey(context, KeyEvent.KEYCODE_UNKNOWN)
                    emojiLayerRecentsKey = KeyEvent.KEYCODE_UNKNOWN
                },
                onDismiss = { showRecentsKeyDialog = false }
            )
        }

        // GIF key on the emoji layer: the same, for opening GIF search
        if (showGifKeyDialog) {
            EmojiLayerKeyDialog(
                title = stringResource(R.string.emoji_layer_gif_key_title),
                description = stringResource(R.string.emoji_layer_gif_key_description),
                currentKey = emojiLayerGifKey,
                letterFor = ::getLetterFromKeyCode,
                onKeyPressed = { keyCode ->
                    SettingsManager.setEmojiLayerGifKey(context, keyCode).also { if (it) emojiLayerGifKey = keyCode }
                },
                onTurnOff = {
                    SettingsManager.setEmojiLayerGifKey(context, KeyEvent.KEYCODE_UNKNOWN)
                    emojiLayerGifKey = KeyEvent.KEYCODE_UNKNOWN
                },
                onDismiss = { showGifKeyDialog = false }
            )
        }
    }
}

private fun getLetterFromKeyCode(keyCode: Int): String {
    return when (keyCode) {
        KeyEvent.KEYCODE_Q -> "Q"
        KeyEvent.KEYCODE_W -> "W"
        KeyEvent.KEYCODE_E -> "E"
        KeyEvent.KEYCODE_R -> "R"
        KeyEvent.KEYCODE_T -> "T"
        KeyEvent.KEYCODE_Y -> "Y"
        KeyEvent.KEYCODE_U -> "U"
        KeyEvent.KEYCODE_I -> "I"
        KeyEvent.KEYCODE_O -> "O"
        KeyEvent.KEYCODE_P -> "P"
        KeyEvent.KEYCODE_A -> "A"
        KeyEvent.KEYCODE_S -> "S"
        KeyEvent.KEYCODE_D -> "D"
        KeyEvent.KEYCODE_F -> "F"
        KeyEvent.KEYCODE_G -> "G"
        KeyEvent.KEYCODE_H -> "H"
        KeyEvent.KEYCODE_J -> "J"
        KeyEvent.KEYCODE_K -> "K"
        KeyEvent.KEYCODE_L -> "L"
        KeyEvent.KEYCODE_Z -> "Z"
        KeyEvent.KEYCODE_X -> "X"
        KeyEvent.KEYCODE_C -> "C"
        KeyEvent.KEYCODE_V -> "V"
        KeyEvent.KEYCODE_B -> "B"
        KeyEvent.KEYCODE_N -> "N"
        KeyEvent.KEYCODE_M -> "M"
        else -> "?"
    }
}

internal fun emojiPickerKeyLabel(context: Context, keyCode: Int): String = when (keyCode) {
    KeyEvent.KEYCODE_UNKNOWN -> context.getString(R.string.emoji_picker_key_off)
    KeyEvent.KEYCODE_SHIFT_RIGHT -> context.getString(R.string.emoji_picker_key_right_shift)
    KeyEvent.KEYCODE_SHIFT_LEFT -> context.getString(R.string.emoji_picker_key_left_shift)
    KeyEvent.KEYCODE_ALT_RIGHT -> context.getString(R.string.emoji_picker_key_right_alt)
    KeyEvent.KEYCODE_ALT_LEFT -> context.getString(R.string.emoji_picker_key_left_alt)
    KeyEvent.KEYCODE_CTRL_RIGHT -> context.getString(R.string.emoji_picker_key_right_ctrl)
    KeyEvent.KEYCODE_CTRL_LEFT -> context.getString(R.string.emoji_picker_key_left_ctrl)
    KeyEvent.KEYCODE_FUNCTION -> "Fn"
    else -> KeyEvent.keyCodeToString(keyCode)
        .removePrefix("KEYCODE_")
        .replace('_', ' ')
        .lowercase()
        .replaceFirstChar { it.uppercase() }
}

/**
 * Press-to-assign dialog for an emoji layer key (Recents, GIF): waits for a letter key press.
 * [onKeyPressed] stores it and returns false when it isn't allowed (not a layer key, or the
 * other special key).
 */
@Composable
private fun EmojiLayerKeyDialog(
    title: String,
    description: String,
    currentKey: Int,
    letterFor: (Int) -> String,
    onKeyPressed: (Int) -> Boolean,
    onTurnOff: () -> Unit,
    onDismiss: () -> Unit
) {
    val keyFocus = remember { FocusRequester() }
    var rejected by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(keyFocus)
                    .focusable()
                    .onPreviewKeyEvent { event ->
                        val native = event.nativeKeyEvent
                        // Let Back close the dialog as usual
                        if (native.keyCode == KeyEvent.KEYCODE_BACK) return@onPreviewKeyEvent false
                        if (native.action == KeyEvent.ACTION_DOWN && native.repeatCount == 0) {
                            if (onKeyPressed(native.keyCode)) onDismiss() else rejected = true
                        }
                        true
                    },
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = description, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = if (currentKey == KeyEvent.KEYCODE_UNKNOWN) {
                        stringResource(R.string.emoji_layer_recents_key_off)
                    } else {
                        stringResource(R.string.emoji_layer_recents_key_current, letterFor(currentKey))
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stringResource(
                        if (rejected) R.string.emoji_layer_key_rejected
                        else R.string.emoji_layer_recents_key_press_prompt
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (rejected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
            LaunchedEffect(Unit) {
                runCatching { keyFocus.requestFocus() }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onTurnOff()
                onDismiss()
            }) {
                Text(stringResource(R.string.emoji_layer_recents_key_turn_off))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

// ------------------------------------------------------------------ Titan 2 Elite screen

@Composable
fun FluxTitanScreenSettingsScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val context = LocalContext.current
    FluxScreenScaffold(stringResource(R.string.flux_titan_screen_title), onBack, modifier) {
        FluxNote(stringResource(R.string.flux_titan_screen_note))
        if (DeviceSpecific.isTitan2EliteDevice() ||
            SettingsManager.getTitan2EliteRoundedCornerInsetsEnabled(context)) {
            Surface(modifier = Modifier.fillMaxWidth()
                .settingRow("advanced.corner_calibration") {
                    context.startActivity(Intent(context, CornerCalibrationActivity::class.java))
                }) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.corner_calibration_title),
                        style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.corner_calibration_description),
                        style = MaterialTheme.typography.bodySmall)
                }
            }

            var fillCorners by remember {
                mutableStateOf(SettingsManager.getTitan2EliteFillCorners(context))
            }
            Surface(modifier = Modifier.fillMaxWidth().settingRow("titan_screen.fill_corners")) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(stringResource(R.string.titan2_elite_fill_corners_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium)
                        Text(stringResource(R.string.titan2_elite_fill_corners_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = fillCorners,
                        onCheckedChange = { enabled ->
                            fillCorners = enabled
                            SettingsManager.setTitan2EliteFillCorners(context, enabled)
                        }
                    )
                }
            }

            var straightOuterButtons by remember {
                mutableStateOf(SettingsManager.getTitan2EliteStraightOuterButtons(context))
            }
            Surface(modifier = Modifier.fillMaxWidth().settingRow("titan_screen.straight_outer_buttons")) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(stringResource(R.string.titan2_elite_straight_outer_buttons_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium)
                        Text(stringResource(R.string.titan2_elite_straight_outer_buttons_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = straightOuterButtons,
                        onCheckedChange = { enabled ->
                            straightOuterButtons = enabled
                            SettingsManager.setTitan2EliteStraightOuterButtons(context, enabled)
                        }
                    )
                }
            }

            var statusBarLiftDp by remember {
                mutableStateOf(SettingsManager.getTitan2EliteStatusBarLiftDp(context).toFloat())
            }
            Surface(modifier = Modifier.fillMaxWidth().settingRow("titan_screen.status_bar_lift")) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(stringResource(R.string.titan2_elite_status_bar_lift_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium)
                    Text(stringResource(R.string.titan2_elite_status_bar_lift_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Slider(
                            value = statusBarLiftDp,
                            onValueChange = { statusBarLiftDp = kotlin.math.round(it) },
                            onValueChangeFinished = {
                                SettingsManager.setTitan2EliteStatusBarLiftDp(
                                    context, statusBarLiftDp.toInt()
                                )
                            },
                            valueRange = 0f..SettingsManager.TITAN2_ELITE_STATUS_BAR_LIFT_MAX_DP.toFloat(),
                            steps = SettingsManager.TITAN2_ELITE_STATUS_BAR_LIFT_MAX_DP - 1,
                            modifier = Modifier.weight(1f)
                        )
                        Text(stringResource(
                            R.string.titan2_elite_status_bar_lift_value,
                            statusBarLiftDp.toInt()
                        ))
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------ Hidden keyboard apps

@Composable
fun FluxHiddenAppsScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var accessibilityOn by remember { mutableStateOf(isPastieraAccessibilityServiceOn(context)) }
    // Back from Android's accessibility settings: show the service's new state
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) accessibilityOn = isPastieraAccessibilityServiceOn(context)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    FluxScreenScaffold(stringResource(R.string.flux_hidden_apps_title), onBack, modifier) {
        FluxNote(stringResource(R.string.flux_hidden_apps_note))
        var hiddenKeyboardApps by remember {
            mutableStateOf(SettingsManager.getHiddenKeyboardApps(context))
        }
        var showHiddenAppsDialog by remember { mutableStateOf(false) }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .settingRow("hidden_apps.apps") { showHiddenAppsDialog = true }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(stringResource(R.string.hidden_keyboard_apps_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium)
                val hiddenAppNames = remember(hiddenKeyboardApps) {
                    val installed = AppListHelper.getCachedInstalledApps()
                        ?.associateBy { app -> app.packageName }
                    hiddenKeyboardApps.map { pkg -> installed?.get(pkg)?.appName ?: pkg }
                }
                Text(
                    text = if (hiddenKeyboardApps.isEmpty()) {
                        stringResource(R.string.hidden_keyboard_apps_description)
                    } else {
                        hiddenAppNames.joinToString(", ")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (showHiddenAppsDialog) {
            HiddenKeyboardAppsDialog(onDismiss = {
                showHiddenAppsDialog = false
                hiddenKeyboardApps = SettingsManager.getHiddenKeyboardApps(context)
            })
        }

        SettingsSectionDivider(stringResource(R.string.flux_section_accessibility))
        FluxActionRow(
            linkId = "hidden_apps.accessibility",
            title = stringResource(R.string.flux_accessibility_title),
            description = stringResource(
                if (accessibilityOn) R.string.flux_accessibility_on else R.string.flux_accessibility_off
            ),
            onClick = {
                context.startActivity(
                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        )
    }
}

// ------------------------------------------------------------------ Linux desktop

@Composable
fun FluxLinuxDesktopScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var standardCtrlSym by remember { mutableStateOf(SettingsManager.getHiddenAppStandardModifiers(context)) }
    var layoutWrittenAt by remember {
        mutableStateOf(DesktopKeyboardLayout.file(context)?.takeIf { it.isFile }?.lastModified())
    }

    FluxScreenScaffold(stringResource(R.string.flux_linux_desktop_title), onBack, modifier) {
        FluxNote(stringResource(R.string.flux_linux_desktop_note))

        SettingsSectionDivider(stringResource(R.string.flux_section_keys))
        FluxSwitchRow(
            linkId = "linux_desktop.standard_ctrl_sym",
            title = stringResource(R.string.flux_standard_ctrl_sym_title),
            description = stringResource(R.string.flux_standard_ctrl_sym_description),
            checked = standardCtrlSym,
            onCheckedChange = { enabled ->
                standardCtrlSym = enabled
                SettingsManager.setHiddenAppStandardModifiers(context, enabled)
            }
        )

        SettingsSectionDivider(stringResource(R.string.flux_section_layout))
        val writtenAt = layoutWrittenAt
        FluxActionRow(
            linkId = "linux_desktop.keyboard_layout",
            title = stringResource(R.string.flux_desktop_layout_title),
            description = stringResource(R.string.flux_desktop_layout_description) + "\n" +
                if (writtenAt == null) {
                    stringResource(R.string.flux_desktop_layout_not_written)
                } else {
                    stringResource(
                        R.string.flux_desktop_layout_written,
                        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(writtenAt))
                    )
                },
            onClick = {
                scope.launch {
                    val file = withContext(Dispatchers.IO) {
                        runCatching { DesktopKeyboardLayout.export(context) }.getOrNull()
                    }
                    if (file != null) layoutWrittenAt = file.lastModified()
                    Toast.makeText(
                        context,
                        if (file != null) R.string.flux_desktop_layout_written_toast else R.string.flux_desktop_layout_failed_toast,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }
}
