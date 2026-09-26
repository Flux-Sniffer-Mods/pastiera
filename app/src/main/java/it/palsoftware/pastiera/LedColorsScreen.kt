package it.palsoftware.pastiera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.palsoftware.pastiera.inputmethod.ui.LedColors

/**
 * Status LED colours: keep the theme's shared LED colours, or give Shift, Ctrl, Alt and SYM a
 * colour each (dim when off, the colour when active, brighter and more saturated when locked).
 */
@Composable
fun LedColorsScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(LedColors.enabled(context)) }
    var colors by remember {
        mutableStateOf(LedColors.Led.entries.associateWith { LedColors.baseColor(context, it) })
    }
    var editing by remember { mutableStateOf<LedColors.Led?>(null) }
    var lockedAnimation by remember { mutableStateOf(LedColors.lockedAnimationEnabled(context)) }

    FluxScreenScaffold(stringResource(R.string.led_colors_title), onBack, modifier) {
        FluxSwitchRow(
            linkId = SettingLinkIds.LED_INDIVIDUAL_COLORS,
            title = stringResource(R.string.led_colors_individual_title),
            description = stringResource(
                if (enabled) R.string.led_colors_individual_on else R.string.led_colors_individual_off
            ),
            checked = enabled,
            onCheckedChange = {
                enabled = it
                SettingsManager.setLedIndividualColorsEnabled(context, it)
            }
        )
        FluxSwitchRow(
            linkId = SettingLinkIds.LED_LOCKED_ANIMATION,
            title = stringResource(R.string.led_locked_animation_title),
            description = stringResource(R.string.led_locked_animation_description),
            checked = lockedAnimation,
            onCheckedChange = {
                lockedAnimation = it
                SettingsManager.setLedLockedAnimationEnabled(context, it)
            }
        )
        if (enabled) {
            SettingsSectionDivider(stringResource(R.string.led_colors_section))
            LedColors.Led.entries.forEach { led ->
                val base = colors.getValue(led)
                Surface(
                    Modifier
                        .fillMaxWidth()
                        .settingRow(ledLinkId(led)) { editing = led }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(ledName(led), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                            Text(
                                stringResource(R.string.led_colors_states),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // Off, active, locked
                        LedColors.Level.entries.forEach { level ->
                            Box(
                                Modifier
                                    .width(28.dp)
                                    .height(8.dp)
                                    .background(Color(LedColors.shade(base, level)), RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }
            FluxActionRow(
                linkId = null,
                title = stringResource(R.string.led_colors_reset_title),
                description = stringResource(R.string.led_colors_reset_description),
                onClick = {
                    LedColors.Led.entries.forEach { SettingsManager.setLedColor(context, it.key, it.defaultColor) }
                    colors = LedColors.Led.entries.associateWith { it.defaultColor }
                }
            )
        }
        Spacer(Modifier.height(16.dp))
    }

    editing?.let { led ->
        KeyboardThemeColorPickerDialog(
            initialColor = colors.getValue(led),
            onDismiss = { editing = null },
            onColorSelected = { color ->
                SettingsManager.setLedColor(context, led.key, color)
                colors = colors + (led to color)
                editing = null
            },
            onPreviewColorChanged = {}
        )
    }
}

private fun ledLinkId(led: LedColors.Led): String = when (led) {
    LedColors.Led.SHIFT -> "led_colors.shift"
    LedColors.Led.CTRL -> "led_colors.ctrl"
    LedColors.Led.ALT -> "led_colors.alt"
    LedColors.Led.SYM -> "led_colors.sym"
}

@Composable
private fun ledName(led: LedColors.Led): String = stringResource(
    when (led) {
        LedColors.Led.SHIFT -> R.string.led_colors_shift
        LedColors.Led.CTRL -> R.string.led_colors_ctrl
        LedColors.Led.ALT -> R.string.led_colors_alt
        LedColors.Led.SYM -> R.string.led_colors_sym
    }
)
