package it.palsoftware.pastiera.spellcheck

import android.service.textservice.SpellCheckerService
import android.util.Log
import android.view.textservice.SuggestionsInfo
import android.view.textservice.TextInfo
import it.palsoftware.pastiera.core.suggestions.AndroidDictionaryRepository
import it.palsoftware.pastiera.core.suggestions.CasingHelper
import it.palsoftware.pastiera.core.suggestions.DictionaryRepository
import it.palsoftware.pastiera.core.suggestions.SuggestionController
import it.palsoftware.pastiera.core.suggestions.SuggestionEngine
import it.palsoftware.pastiera.core.suggestions.UserDictionaryStore
import java.lang.ref.WeakReference
import java.util.Locale
import kotlinx.coroutines.runBlocking

/**
 * Pastiera as Android's spell checker (Settings > System > Languages > Spell checker): apps
 * underline words that aren't in Pastiera's dictionary for the language, words you added
 * included, and offer its suggestions. It uses the keyboard's loaded dictionary when the
 * languages match, so the dictionary isn't loaded twice.
 */
class PastieraSpellCheckerService : SpellCheckerService() {

    override fun createSession(): Session = PastieraSpellSession()

    private inner class PastieraSpellSession : Session() {
        private lateinit var language: String

        override fun onCreate() {
            language = localeOf(locale).language.ifEmpty { Locale.getDefault().language }
        }

        override fun onGetSuggestions(textInfo: TextInfo?, suggestionsLimit: Int): SuggestionsInfo {
            val word = textInfo?.text.orEmpty()
            val cookie = textInfo?.cookie ?: 0
            val sequence = textInfo?.sequence ?: 0
            if (!SpellCheckRules.shouldCheck(word)) return inDictionary(cookie, sequence)
            val result = try {
                check(word, suggestionsLimit.coerceIn(1, 5))
            } catch (error: Exception) {
                Log.w(TAG, "Spell check failed", error)
                null
            } ?: return inDictionary(cookie, sequence)
            if (result.known) return inDictionary(cookie, sequence)
            val suggestions = result.suggestions
                .map { CasingHelper.applyCasing(it, word, false) }
                .filter { !it.equals(word, ignoreCase = false) }
                .distinct()
                .toTypedArray()
            return SuggestionsInfo(
                SuggestionsInfo.RESULT_ATTR_LOOKS_LIKE_TYPO or
                    (if (suggestions.isNotEmpty()) SuggestionsInfo.RESULT_ATTR_HAS_RECOMMENDED_SUGGESTIONS else 0),
                suggestions, cookie, sequence
            )
        }

        override fun onGetSuggestionsMultiple(
            textInfos: Array<out TextInfo>?,
            suggestionsLimit: Int,
            sequentialWords: Boolean
        ): Array<SuggestionsInfo> = textInfos.orEmpty().map { onGetSuggestions(it, suggestionsLimit) }.toTypedArray()

        private fun check(word: String, limit: Int): SuggestionController.SpellCheckResult? {
            keyboardController?.get()?.spellCheck(language, word, limit)?.let { return it }
            val (repository, engine) = ownDictionary(language) ?: return null
            if (repository.isKnownWord(word)) return SuggestionController.SpellCheckResult(true, emptyList())
            return SuggestionController.SpellCheckResult(false, engine.suggest(word, limit).map { it.candidate })
        }

        private fun inDictionary(cookie: Int, sequence: Int) =
            SuggestionsInfo(SuggestionsInfo.RESULT_ATTR_IN_THE_DICTIONARY, emptyArray(), cookie, sequence)
    }

    /** A dictionary of its own for a language the keyboard isn't typing in; null if Pastiera has none. */
    private fun ownDictionary(language: String): Pair<DictionaryRepository, SuggestionEngine>? = synchronized(dictionaries) {
        dictionaries[language]?.let { return it }
        if (language !in SpellCheckRules.LANGUAGES) return null
        val locale = Locale(language)
        val repository = AndroidDictionaryRepository(applicationContext, assets, UserDictionaryStore(), baseLocale = locale)
        runBlocking { repository.loadIfNeeded() }
        if (!repository.isReady) return null
        val entry = repository to SuggestionEngine(repository, locale = locale)
        dictionaries.clear() // one extra language at a time
        dictionaries[language] = entry
        return entry
    }

    private val dictionaries = HashMap<String, Pair<DictionaryRepository, SuggestionEngine>>()

    companion object {
        private const val TAG = "PastieraSpellChecker"

        /** The keyboard's suggestions, while it's running (same process). */
        @Volatile
        var keyboardController: WeakReference<SuggestionController>? = null

        private fun localeOf(tag: String?): Locale =
            if (tag.isNullOrEmpty()) Locale.getDefault() else Locale.forLanguageTag(tag.replace('_', '-'))
    }
}

/** What the spell checker looks at, apart from the dictionary. */
object SpellCheckRules {
    /** Android's spell checker picker, or its keyboard settings where that screen isn't reachable. */
    fun openSystemSettings(context: android.content.Context) {
        val spellCheckers = android.content.Intent().setClassName(
            "com.android.settings", "com.android.settings.Settings\$SpellCheckersSettingsActivity"
        )
        val fallback = android.content.Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS)
        for (intent in listOf(spellCheckers, fallback)) {
            val opened = runCatching {
                context.startActivity(intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
            }.isSuccess
            if (opened) return
        }
    }

    /** Languages with a bundled dictionary (assets/common/dictionaries_serialized). */
    val LANGUAGES = setOf("da", "de", "en", "es", "fr", "it", "nl", "no", "pl", "pt", "ru", "uk")

    /**
     * Words worth checking: not numbers, links, addresses, handles, hashtags, acronyms or
     * single letters.
     */
    fun shouldCheck(word: String): Boolean {
        val text = word.trim()
        if (text.length < 2) return false
        if (text.any { it.isDigit() }) return false
        if (text.any { it == '@' || it == '#' || it == '/' || it == ':' || it == '_' }) return false
        if (text.contains('.') && !text.endsWith('.')) return false
        val letters = text.filter { it.isLetter() }
        if (letters.isEmpty()) return false
        if (letters.length > 1 && letters.all { it.isUpperCase() }) return false
        return true
    }
}
