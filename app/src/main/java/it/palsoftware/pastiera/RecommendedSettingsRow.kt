package it.palsoftware.pastiera

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

/** Privacy & system > Backup: shows what the recommended settings would change, then applies them. */
@Composable
internal fun RecommendedSettingsRow() {
    val context = LocalContext.current
    var pending by remember { mutableStateOf<List<RecommendedSettings.Item>?>(null) }
    FluxActionRow(
        linkId = SettingLinkIds.RECOMMENDED_SETTINGS,
        title = stringResource(R.string.recommended_settings_title),
        description = stringResource(R.string.recommended_settings_description),
        onClick = { pending = RecommendedSettings.pending(context) }
    )
    pending?.let { items ->
        AlertDialog(
            onDismissRequest = { pending = null },
            title = { Text(stringResource(R.string.recommended_settings_title)) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    if (items.isEmpty()) {
                        Text(stringResource(R.string.recommended_settings_nothing))
                    } else {
                        Text(stringResource(R.string.recommended_settings_will_turn_on))
                        items.forEach { item -> Text("• " + stringResource(item.titleRes)) }
                    }
                }
            },
            confirmButton = {
                if (items.isNotEmpty()) {
                    TextButton(onClick = {
                        RecommendedSettings.apply(context)
                        pending = null
                        Toast.makeText(context, R.string.recommended_settings_applied, Toast.LENGTH_SHORT).show()
                    }) { Text(stringResource(R.string.recommended_settings_apply)) }
                }
            },
            dismissButton = { TextButton(onClick = { pending = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

/** Privacy & system > Backup: applies the bundled default configuration (DefaultConfig). */
@Composable
internal fun DefaultConfigRow() {
    val context = LocalContext.current
    var confirming by remember { mutableStateOf(false) }
    FluxActionRow(
        linkId = SettingLinkIds.DEFAULT_CONFIG,
        title = stringResource(R.string.default_config_title),
        description = stringResource(R.string.default_config_description),
        onClick = { confirming = true }
    )
    if (confirming) {
        AlertDialog(
            onDismissRequest = { confirming = false },
            title = { Text(stringResource(R.string.default_config_title)) },
            text = { Text(stringResource(R.string.default_config_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    confirming = false
                    val ok = DefaultConfig.apply(context)
                    Toast.makeText(
                        context,
                        if (ok) R.string.default_config_applied else R.string.default_config_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                }) { Text(stringResource(R.string.default_config_apply)) }
            },
            dismissButton = { TextButton(onClick = { confirming = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}
