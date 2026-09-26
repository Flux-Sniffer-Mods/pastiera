package it.palsoftware.pastiera

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.provider.Settings
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import it.palsoftware.pastiera.inputmethod.ClicksLauncherButtonAccessibilityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private data class HiddenAppRow(val packageName: String, val name: String, val icon: Drawable?)

/**
 * Picks the apps where Pastiera stays hidden, plus what it may still show there. Changes are
 * saved as they are made.
 */
@Composable
fun HiddenKeyboardAppsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var selected by remember { mutableStateOf(SettingsManager.getHiddenKeyboardApps(context).toSet()) }
    // Order is fixed when the dialog opens, so rows don't jump while you tick them
    val initiallySelected = remember { selected }
    var showLeds by remember { mutableStateOf(SettingsManager.getHiddenAppsShowLeds(context)) }
    var allowPanels by remember { mutableStateOf(SettingsManager.getHiddenAppsAllowPanels(context)) }
    var apps by remember { mutableStateOf<List<InstalledApp>?>(null) }
    var searching by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var accessibilityOn by remember { mutableStateOf(isPastieraAccessibilityServiceEnabled(context)) }
    val searchFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        apps = withContext(Dispatchers.IO) { AppListHelper.getInstalledApps(context) }
    }
    // Picks up the accessibility service being switched on in system settings
    LaunchedEffect(Unit) {
        while (true) {
            accessibilityOn = isPastieraAccessibilityServiceEnabled(context)
            delay(1000)
        }
    }

    val rows = remember(apps, query) {
        val installed = apps.orEmpty()
        val known = installed.associateBy { it.packageName }
        val notListed = initiallySelected.filter { it !in known }.map { HiddenAppRow(it, it, null) }
        val all = installed.map { HiddenAppRow(it.packageName, it.appName, it.icon) } + notListed
        val needle = query.trim()
        all.filter {
            needle.isEmpty() || it.name.contains(needle, ignoreCase = true) ||
                it.packageName.contains(needle, ignoreCase = true)
        }.sortedWith(
            compareByDescending<HiddenAppRow> { it.packageName in initiallySelected }
                .thenBy { it.name.lowercase() }
        )
    }

    fun toggle(packageName: String) {
        selected = if (packageName in selected) selected - packageName else selected + packageName
        SettingsManager.setHiddenKeyboardApps(context, selected.sorted())
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f).padding(8.dp)) {
            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.hidden_keyboard_apps_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        searching = !searching
                        if (!searching) query = ""
                    }) {
                        Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.hidden_keyboard_apps_search))
                    }
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.hidden_keyboard_apps_done))
                    }
                }
                if (searching) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        placeholder = { Text(stringResource(R.string.hidden_keyboard_apps_search)) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().focusRequester(searchFocus)
                    )
                    LaunchedEffect(Unit) { runCatching { searchFocus.requestFocus() } }
                }
                OptionRow(
                    title = stringResource(R.string.hidden_keyboard_apps_leds_only_title),
                    description = stringResource(R.string.hidden_keyboard_apps_leds_only_description),
                    checked = showLeds
                ) {
                    showLeds = it
                    SettingsManager.setHiddenAppsShowLeds(context, it)
                }
                OptionRow(
                    title = stringResource(R.string.hidden_keyboard_apps_panels_title),
                    description = stringResource(R.string.hidden_keyboard_apps_panels_description),
                    checked = allowPanels
                ) {
                    allowPanels = it
                    SettingsManager.setHiddenAppsAllowPanels(context, it)
                }
                if ((showLeds || allowPanels) && !accessibilityOn) {
                    Text(
                        text = stringResource(R.string.hidden_keyboard_apps_accessibility_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    TextButton(onClick = {
                        runCatching {
                            context.startActivity(
                                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    }) {
                        Text(stringResource(R.string.hidden_keyboard_apps_open_accessibility))
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                if (apps == null) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        items(rows, key = { it.packageName }) { row ->
                            AppRowItem(row, checked = row.packageName in selected) { toggle(row.packageName) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionRow(title: String, description: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onChange(!checked) }.padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun AppRowItem(row: HiddenAppRow, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
            row.icon?.let { icon ->
                AndroidView(
                    factory = { ctx ->
                        ImageView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            scaleType = ImageView.ScaleType.FIT_CENTER
                        }
                    },
                    update = { it.setImageDrawable(icon) },
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(row.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                row.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Checkbox(checked = checked, onCheckedChange = { onToggle() })
    }
}

/** Whether Pastiera's accessibility service (needed for apps like Termux:X11) is switched on. */
fun isPastieraAccessibilityServiceEnabled(context: Context): Boolean {
    val enabled = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    val component = ComponentName(context, ClicksLauncherButtonAccessibilityService::class.java)
    return enabled.split(':').any { ComponentName.unflattenFromString(it) == component }
}
