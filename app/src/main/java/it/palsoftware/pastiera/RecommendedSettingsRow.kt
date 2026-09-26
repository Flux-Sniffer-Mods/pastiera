package it.palsoftware.pastiera

import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource

/**
 * Privacy & system > Backup: the recommended settings, which are the default configuration
 * (DefaultConfig, a Titan 2 Elite backup). Says how many of your settings differ, then applies it.
 */
@Composable
internal fun RecommendedSettingsRow() {
    val context = LocalContext.current
    var differing by remember { mutableStateOf<Int?>(null) }
    FluxActionRow(
        linkId = SettingLinkIds.RECOMMENDED_SETTINGS,
        title = stringResource(R.string.recommended_settings_title),
        description = stringResource(R.string.recommended_settings_description),
        onClick = { differing = DefaultConfig.differingSettings(context) }
    )
    differing?.let { count ->
        AlertDialog(
            onDismissRequest = { differing = null },
            title = { Text(stringResource(R.string.recommended_settings_title)) },
            text = {
                Text(
                    if (count == 0) stringResource(R.string.recommended_settings_nothing)
                    else pluralStringResource(R.plurals.recommended_settings_differ, count, count)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    differing = null
                    val ok = DefaultConfig.apply(context)
                    Toast.makeText(
                        context,
                        if (ok) R.string.recommended_settings_applied else R.string.recommended_settings_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                }) { Text(stringResource(R.string.recommended_settings_apply)) }
            },
            dismissButton = { TextButton(onClick = { differing = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}
