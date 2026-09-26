package it.palsoftware.pastiera.inputmethod

import android.content.Context
import android.os.Build
import android.util.Size
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InlineSuggestion
import android.view.inputmethod.InlineSuggestionsRequest
import android.widget.inline.InlinePresentationSpec
import androidx.annotation.RequiresApi
import androidx.autofill.inline.UiVersions
import androidx.autofill.inline.v1.InlineSuggestionUi

/**
 * Inline autofill (Android 11+): password managers and Android's autofill offer their chips
 * (saved logins, one-time codes, addresses) to the keyboard, which shows them in the suggestion
 * bar. The chips are drawn by the autofill service; tapping one fills the field.
 */
@RequiresApi(Build.VERSION_CODES.R)
object InlineAutofill {
    private const val MAX_CHIPS = 6

    fun request(context: Context): InlineSuggestionsRequest {
        val density = context.resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()
        val style = UiVersions.newStylesBuilder()
            .addStyle(InlineSuggestionUi.newStyleBuilder().build())
            .build()
        val spec = InlinePresentationSpec.Builder(Size(dp(64), dp(28)), Size(dp(260), dp(36)))
            .setStyle(style)
            .build()
        return InlineSuggestionsRequest.Builder(listOf(spec))
            .setMaxSuggestionCount(MAX_CHIPS)
            .build()
    }

    /** Inflates every chip, then hands them over in the service's order (pinned ones last). */
    fun inflate(context: Context, suggestions: List<InlineSuggestion>, onReady: (List<View>) -> Unit) {
        val ordered = suggestions.take(MAX_CHIPS).sortedBy { it.info.isPinned }
        val views = arrayOfNulls<View>(ordered.size)
        var pending = ordered.size
        val size = Size(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        ordered.forEachIndexed { index, suggestion ->
            try {
                suggestion.inflate(context, size, context.mainExecutor) { view ->
                    views[index] = view
                    if (--pending == 0) onReady(views.filterNotNull())
                }
            } catch (error: Exception) {
                if (--pending == 0) onReady(views.filterNotNull())
            }
        }
    }
}
