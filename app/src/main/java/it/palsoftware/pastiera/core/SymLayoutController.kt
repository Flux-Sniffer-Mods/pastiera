package it.palsoftware.pastiera.core

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.inputmethod.InputConnection
import it.palsoftware.pastiera.SettingsManager
import it.palsoftware.pastiera.SymPagesConfig
import it.palsoftware.pastiera.data.emoji.RecentEmojiManager
import it.palsoftware.pastiera.inputmethod.AlternateCharacterManager

class SymLayoutController(
    private val context: Context,
    private val prefs: SharedPreferences,
    private val alternateCharacterManager: AlternateCharacterManager
) {

    companion object {
        private const val PREF_CURRENT_SYM_PAGE = "current_sym_page"
        /**
         * Labels of the emoji layer's Recents key, as text symbols rather than emoji: shows the
         * recent emoji / back to the layer. U+FE0E keeps the arrow from turning into an emoji.
         */
        const val RECENTS_KEY_LABEL = "\u21BA"         // ↺
        const val RECENTS_BACK_LABEL = "\u21A9\uFE0E"  // ↩ (text presentation)
        /**
         * Label of the search key on the emoji layer and the symbols pages: a text symbol, so it
         * isn't mistaken for an emoji the key would type.
         */
        const val SEARCH_KEY_LABEL = "\u2315"         // ⌕
        /** Label of the emoji layer's GIF key. */
        const val GIF_KEY_LABEL = "GIF"
    }

    private enum class SymPage {
        DEVICE,
        EMOJI,
        SYMBOLS,
        CLIPBOARD,
        EMOJI_PICKER
    }

    enum class SymKeyResult {
        NOT_HANDLED,
        CONSUME,
        CALL_SUPER
    }

    private var symPage: Int = prefs.getInt(PREF_CURRENT_SYM_PAGE, 0)

    /**
     * The emoji key (not the Sym cycle) opened the current page: the emoji layer is allowed even
     * when it isn't in the cycle, and its auto-close follows the emoji key's own setting.
     */
    var openedByEmojiKey: Boolean = false
        private set

    /** Where the search key was pressed. */
    enum class SearchTarget { EMOJI_LAYER, SYMBOLS, PICKER }

    /** The search key was pressed: the input method opens that screen's search. */
    var onSearchKey: ((SearchTarget) -> Unit)? = null

    /** The emoji layer's GIF key was pressed: the input method opens GIF search. */
    var onEmojiLayerGifKey: (() -> Unit)? = null

    /** Type to search: a letter on the emoji layer (true) or a symbols page (false), and its text. */
    var onTypeToSearch: ((emoji: Boolean, text: String) -> Unit)? = null

    /** The emoji layer shows recent emoji on its keys (its Recents key was pressed). */
    var emojiLayerShowsRecents: Boolean = false
        private set

    private fun leavePage() {
        openedByEmojiKey = false
        emojiLayerShowsRecents = false
    }

    init {
        alignSymPageToConfig(SettingsManager.getSymPagesConfig(context))
    }

    fun currentSymPage(): Int {
        alignSymPageToConfig()
        return symPage
    }

    fun isSymActive(): Boolean = currentSymPage() > 0

    fun toggleSymPage(): Int {
        val config = SettingsManager.getSymPagesConfig(context)
        alignSymPageToConfig(config)
        val pages = buildActivePages(config)
        symPage = nextSymPageValue(pages)
        leavePage()
        persistSymPage()
        return symPage
    }

    fun peekNextSymPage(): Int {
        val config = SettingsManager.getSymPagesConfig(context)
        alignSymPageToConfig(config)
        return nextSymPageValue(buildActivePages(config))
    }

    fun closeSymPage(): Boolean {
        if (symPage == 0) {
            return false
        }
        symPage = 0
        leavePage()
        persistSymPage()
        return true
    }
    
    fun openClipboardPage(): Boolean {
        val clipboardPageValue = SymPage.CLIPBOARD.toPrefValue()
        
        // Toggle behavior: open if closed, close if already open
        // Always allow direct access to clipboard page, even if disabled in cycling settings
        if (symPage == clipboardPageValue) {
            closeSymPage()
            return false
        }
        symPage = clipboardPageValue
        leavePage()
        persistSymPage()
        return true
    }

    fun openEmojiPickerPage(): Boolean {
        val emojiPickerPageValue = SymPage.EMOJI_PICKER.toPrefValue()
        
        // Toggle behavior: open if closed, close if already open
        // Always allow direct access to emoji picker page
        if (symPage == emojiPickerPageValue) {
            closeSymPage()
            return false
        }
        symPage = emojiPickerPageValue
        leavePage()
        persistSymPage()
        return true
    }

    /** The emoji key: toggles the emoji picker, or the emoji layer when [layer]. */
    fun toggleEmojiKeyPage(layer: Boolean): Boolean {
        val target = (if (layer) SymPage.EMOJI else SymPage.EMOJI_PICKER).toPrefValue()
        if (symPage == target) {
            closeSymPage()
            return false
        }
        symPage = target
        leavePage()
        openedByEmojiKey = true
        persistSymPage()
        return true
    }

    /** The emoji layer's Recents key: recent emoji on the keys, or back to the layer. */
    fun toggleEmojiLayerRecents(): Boolean {
        if (currentPageType() != SymPage.EMOJI) return false
        emojiLayerShowsRecents = !emojiLayerShowsRecents
        return true
    }

    /**
     * The emoji layer's keys: its own emoji, or the recent ones (most recent on Q, then along the
     * rows) while Recents is shown. The Recents key keeps its toggle label either way.
     */
    /** A symbols page's keys, with the search key showing its label. */
    private fun withSearchKey(mappings: Map<Int, String>?): Map<Int, String>? {
        val searchKey = SettingsManager.getSearchKey(context)
        if (mappings == null || searchKey == KeyEvent.KEYCODE_UNKNOWN) return mappings
        return mappings.toMutableMap().apply { put(searchKey, SEARCH_KEY_LABEL) }
    }

    private fun emojiLayerMappings(): Map<Int, String> {
        val base = alternateCharacterManager.getSymMappings()
        val recentsKey = SettingsManager.getEmojiLayerRecentsKey(context)
        val showingRecents = emojiLayerShowsRecents && recentsKey != KeyEvent.KEYCODE_UNKNOWN
        // While the layer shows recent emoji, the GIF key holds one of them too
        val gifKey = if (showingRecents) KeyEvent.KEYCODE_UNKNOWN else SettingsManager.activeEmojiLayerGifKey(context)
        // The search key too, except while the layer shows recent emoji (every key holds one then)
        val searchKey = if (showingRecents) KeyEvent.KEYCODE_UNKNOWN else SettingsManager.getSearchKey(context)
        if (recentsKey == KeyEvent.KEYCODE_UNKNOWN && gifKey == KeyEvent.KEYCODE_UNKNOWN &&
            searchKey == KeyEvent.KEYCODE_UNKNOWN
        ) return base
        val shown = if (showingRecents) {
            val keys = SettingsManager.EMOJI_LAYER_KEYS.filter { it != recentsKey }
            keys.zip(RecentEmojiManager.getRecentEmojis(context, keys.size)).toMap().toMutableMap()
        } else {
            base.toMutableMap()
        }
        if (recentsKey != KeyEvent.KEYCODE_UNKNOWN) {
            shown[recentsKey] = if (emojiLayerShowsRecents) RECENTS_BACK_LABEL else RECENTS_KEY_LABEL
        }
        if (gifKey != KeyEvent.KEYCODE_UNKNOWN) shown[gifKey] = GIF_KEY_LABEL
        if (searchKey != KeyEvent.KEYCODE_UNKNOWN) shown[searchKey] = SEARCH_KEY_LABEL
        return shown
    }

    fun openEmojiPage(): Boolean {
        val emojiPageValue = SymPage.EMOJI.toPrefValue()

        if (symPage == emojiPageValue) {
            closeSymPage()
            return false
        }
        symPage = emojiPageValue
        leavePage()
        persistSymPage()
        return true
    }

    fun openSymbolsPage(): Boolean {
        val symbolsPageValue = SymPage.SYMBOLS.toPrefValue()
        
        // Toggle behavior: open if closed, close if already open
        // Always allow direct access to symbols page, even if disabled in cycling settings
        if (symPage == symbolsPageValue) {
            closeSymPage()
            return false
        }
        symPage = symbolsPageValue
        leavePage()
        persistSymPage()
        return true
    }

    fun reset() {
        symPage = 0
        leavePage()
        persistSymPage()
    }

    fun restoreSymPageIfNeeded(onStatusBarUpdate: () -> Unit) {
        val restoreSymPage = SettingsManager.getRestoreSymPage(context)
        if (restoreSymPage > 0) {
            val config = SettingsManager.getSymPagesConfig(context)
            val pages = buildActivePages(config)
            val allowedValues = pages.map { it.toPrefValue() }
            symPage = when {
                restoreSymPage in allowedValues -> restoreSymPage
                allowedValues.isNotEmpty() -> allowedValues.first()
                else -> 0
            }
            persistSymPage()
            SettingsManager.clearRestoreSymPage(context)
            Handler(Looper.getMainLooper()).post {
                onStatusBarUpdate()
            }
        }
    }

    fun emojiMapText(): String {
        return if (currentPageType() == SymPage.EMOJI) alternateCharacterManager.buildEmojiMapText() else ""
    }

    fun currentSymMappings(): Map<Int, String>? {
        return when (currentPageType()) {
            SymPage.DEVICE -> withSearchKey(alternateCharacterManager.getDeviceSymMappings())
            SymPage.EMOJI -> emojiLayerMappings()
            SymPage.SYMBOLS -> withSearchKey(alternateCharacterManager.getSymMappings2())
            SymPage.CLIPBOARD -> null // Clipboard doesn't use mappings
            SymPage.EMOJI_PICKER -> null // Emoji picker doesn't use mappings
            else -> null
        }
    }

    fun previewChordMappings(shiftPressed: Boolean): Map<Int, String> {
        val pageToUse = when (currentPageType()) {
            SymPage.DEVICE, SymPage.EMOJI, SymPage.SYMBOLS -> currentPageType()
            else -> preferredChordPage()
        } ?: return emptyMap()

        return mappingsForPage(pageToUse, shiftPressed)
    }

    fun previewNextSoftwareSymPageMappings(shiftPressed: Boolean): Map<Int, String> {
        val nextTextPage = nextSoftwareTextPageType() ?: return emptyMap()
        return mappingsForPage(nextTextPage, shiftPressed)
    }

    fun nextSoftwareTextSymPage(): Int {
        return nextSoftwareTextPageType()?.toPrefValue() ?: 0
    }

    private fun nextSoftwareTextPageType(): SymPage? {
        val config = SettingsManager.getSymPagesConfig(context)
        alignSymPageToConfig(config)
        val pages = buildActivePages(config)
        if (pages.isEmpty()) {
            return null
        }
        val cycle = listOf(null) + pages
        val currentPage = currentPageType()
        val currentIndex = cycle.indexOf(currentPage).takeIf { it >= 0 } ?: 0
        return (1..cycle.size).asSequence()
            .map { offset -> cycle[(currentIndex + offset) % cycle.size] }
            .firstOrNull { it == SymPage.DEVICE || it == SymPage.EMOJI || it == SymPage.SYMBOLS }
    }

    private fun nextSymPageValue(pages: List<SymPage>): Int {
        val cycle = mutableListOf(0)
        cycle.addAll(pages.map { it.toPrefValue() })
        if (cycle.size <= 1) {
            return 0
        }
        val currentIndex = cycle.indexOf(symPage).takeIf { it >= 0 } ?: 0
        val nextIndex = (currentIndex + 1) % cycle.size
        return cycle[nextIndex]
    }

    private fun mappingsForPage(pageToUse: SymPage, shiftPressed: Boolean): Map<Int, String> {
        return when (pageToUse) {
            SymPage.DEVICE -> alternateCharacterManager.getDeviceSymMappings()
            SymPage.EMOJI -> if (shiftPressed) {
                alternateCharacterManager.getSymMappings() + alternateCharacterManager.getSymMappingsUppercase()
            } else {
                alternateCharacterManager.getSymMappings()
            }
            SymPage.SYMBOLS -> if (shiftPressed) {
                alternateCharacterManager.getSymMappings2() + alternateCharacterManager.getSymMappings2Uppercase()
            } else {
                alternateCharacterManager.getSymMappings2()
            }
            else -> emptyMap()
        }
    }

    /**
     * Resolves the character for a physical SYM+key chord without opening
     * the visual SYM layout. If a text SYM page is already active, use it.
     * Otherwise use the first enabled text page in configured order.
     */
    fun resolveChordSymbol(keyCode: Int, shiftPressed: Boolean): String? {
        val pageToUse = when (currentPageType()) {
            SymPage.DEVICE, SymPage.EMOJI, SymPage.SYMBOLS -> currentPageType()
            else -> preferredChordPage()
        } ?: return null

        return when (pageToUse) {
            SymPage.DEVICE -> alternateCharacterManager.getDeviceSymMappings()[keyCode]
            SymPage.EMOJI -> {
                if (shiftPressed) {
                    alternateCharacterManager.getSymMappingsUppercase()[keyCode] ?: alternateCharacterManager.getSymMappings()[keyCode]
                } else {
                    alternateCharacterManager.getSymMappings()[keyCode]
                }
            }
            SymPage.SYMBOLS -> {
                if (shiftPressed) {
                    alternateCharacterManager.getSymMappings2Uppercase()[keyCode] ?: alternateCharacterManager.getSymMappings2()[keyCode]
                } else {
                    alternateCharacterManager.getSymMappings2()[keyCode]
                }
            }
            else -> null
        }
    }

    fun handleKeyWhenActive(
        keyCode: Int,
        event: KeyEvent?,
        inputConnection: InputConnection?,
        ctrlLatchActive: Boolean,
        altLatchActive: Boolean,
        updateStatusBar: () -> Unit,
        handleBoundaryText: (String, InputConnection?) -> Boolean = { _, _ -> false }
    ): SymKeyResult {
        val page = currentPageType()
        // The emoji layer opened with the emoji key follows the emoji key's auto-close
        val autoCloseEnabled = if (page == SymPage.EMOJI && openedByEmojiKey) {
            SettingsManager.emojiScreenClosesAfterInput(
                context, isPicker = false, openedByEmojiKey = true, byTouch = false
            )
        } else {
            SettingsManager.getSymAutoClose(context)
        }

        // While the layer shows recent emoji, the GIF key is one of them
        val gifKey = if (emojiLayerShowsRecents) KeyEvent.KEYCODE_UNKNOWN else SettingsManager.activeEmojiLayerGifKey(context)
        if (page == SymPage.EMOJI && gifKey != KeyEvent.KEYCODE_UNKNOWN && keyCode == gifKey) {
            if ((event?.repeatCount ?: 0) == 0) onEmojiLayerGifKey?.invoke()
            return SymKeyResult.CONSUME
        }
        val recentsKey = SettingsManager.getEmojiLayerRecentsKey(context)
        if (page == SymPage.EMOJI && recentsKey != KeyEvent.KEYCODE_UNKNOWN && keyCode == recentsKey) {
            if ((event?.repeatCount ?: 0) == 0 && toggleEmojiLayerRecents()) {
                updateStatusBar()
            }
            return SymKeyResult.CONSUME
        }

        // The search key: that screen's search (the picker's only reaches here when its search
        // isn't taking typing)
        val searchKey = SettingsManager.getSearchKey(context)
        if (searchKey != KeyEvent.KEYCODE_UNKNOWN && keyCode == searchKey &&
            event?.isAltPressed != true && event?.isCtrlPressed != true && !altLatchActive && !ctrlLatchActive
        ) {
            val target = when (page) {
                SymPage.EMOJI -> if (emojiLayerShowsRecents) null else SearchTarget.EMOJI_LAYER
                SymPage.SYMBOLS, SymPage.DEVICE -> SearchTarget.SYMBOLS
                SymPage.EMOJI_PICKER -> SearchTarget.PICKER
                else -> null
            }
            if (target != null) {
                if ((event?.repeatCount ?: 0) == 0) onSearchKey?.invoke(target)
                return SymKeyResult.CONSUME
            }
        }

        // Type to search (its settings): a plain letter starts emoji or symbol search with it
        val typeToSearch = when (page) {
            SymPage.EMOJI -> SettingsManager.getEmojiLayerTypeToSearch(context)
            SymPage.SYMBOLS, SymPage.DEVICE -> SettingsManager.getSymbolsTypeToSearch(context)
            else -> false
        }
        if (typeToSearch && event != null && keyCode in KeyEvent.KEYCODE_A..KeyEvent.KEYCODE_Z &&
            !event.isAltPressed && !event.isCtrlPressed && !altLatchActive && !ctrlLatchActive
        ) {
            if (event.repeatCount == 0) {
                val typed = event.unicodeChar.takeIf { it > 0 }?.toChar() ?: ('a' + (keyCode - KeyEvent.KEYCODE_A))
                onTypeToSearch?.invoke(page == SymPage.EMOJI, typed.toString())
            }
            return SymKeyResult.CONSUME
        }

        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                closeSymAndUpdate(updateStatusBar)
                return SymKeyResult.CALL_SUPER
            }
            KeyEvent.KEYCODE_ENTER -> {
                if (autoCloseEnabled) {
                    closeSymAndUpdate(updateStatusBar)
                    return SymKeyResult.CALL_SUPER
                }
            }
            KeyEvent.KEYCODE_ALT_LEFT, KeyEvent.KEYCODE_ALT_RIGHT -> {
                closeSymAndUpdate(updateStatusBar)
                return SymKeyResult.NOT_HANDLED
            }
        }

        val symChar = when (page) {
            SymPage.DEVICE -> alternateCharacterManager.getDeviceSymMappings()[keyCode]
            SymPage.EMOJI -> emojiLayerMappings()[keyCode]
            SymPage.SYMBOLS -> alternateCharacterManager.getSymMappings2()[keyCode]
            SymPage.CLIPBOARD -> null // Clipboard doesn't use key mappings
            SymPage.EMOJI_PICKER -> null // Emoji picker doesn't use key mappings
            else -> null
        }

        if (symChar != null && inputConnection != null) {
            if (
                !handleBoundaryText(symChar, inputConnection) &&
                (
                    symChar.length != 1 ||
                        !SettingsManager.shouldApplyFrenchPunctuationSpacing(context) ||
                        !it.palsoftware.pastiera.core.Punctuation.commitFrenchSpacedPunctuation(inputConnection, symChar[0])
                )
            ) {
                inputConnection.commitText(symChar, 1)
            }
            if (autoCloseEnabled) {
                closeSymAndUpdate(updateStatusBar)
            }
            return SymKeyResult.CONSUME
        }

        return SymKeyResult.NOT_HANDLED
    }

    fun handleKeyUp(keyCode: Int, shiftPressed: Boolean): Boolean {
        return alternateCharacterManager.handleKeyUp(keyCode, isSymActive(), shiftPressed)
    }

    fun emojiMapTextForLayout(): String = alternateCharacterManager.buildEmojiMapText()

    private fun closeSymAndUpdate(updateStatusBar: () -> Unit) {
        if (closeSymPage()) {
            updateStatusBar()
        }
    }

    private fun buildActivePages(config: SymPagesConfig = SettingsManager.getSymPagesConfig(context)): List<SymPage> {
        return config.enabledOrderedPages().mapNotNull { pageId ->
            when (pageId) {
                SymPagesConfig.PAGE_DEVICE -> SymPage.DEVICE
                SymPagesConfig.PAGE_EMOJI -> SymPage.EMOJI
                SymPagesConfig.PAGE_SYMBOLS -> SymPage.SYMBOLS
                SymPagesConfig.PAGE_CLIPBOARD -> SymPage.CLIPBOARD
                SymPagesConfig.PAGE_EMOJI_PICKER -> SymPage.EMOJI_PICKER
                else -> null
            }
        }
    }

    private fun preferredChordPage(config: SymPagesConfig = SettingsManager.getSymPagesConfig(context)): SymPage? {
        return buildActivePages(config).firstOrNull {
            it == SymPage.DEVICE || it == SymPage.EMOJI || it == SymPage.SYMBOLS
        }
    }

    private fun currentPageType(): SymPage? {
        alignSymPageToConfig()
        return when (symPage) {
            5 -> SymPage.DEVICE
            1 -> SymPage.EMOJI
            2 -> SymPage.SYMBOLS
            3 -> SymPage.CLIPBOARD
            4 -> SymPage.EMOJI_PICKER
            else -> null
        }
    }

    private fun SymPage.toPrefValue(): Int = when (this) {
        SymPage.DEVICE -> 5
        SymPage.EMOJI -> 1
        SymPage.SYMBOLS -> 2
        SymPage.CLIPBOARD -> 3
        SymPage.EMOJI_PICKER -> 4
    }

    private fun alignSymPageToConfig(config: SymPagesConfig = SettingsManager.getSymPagesConfig(context)) {
        val allowedValues = buildActivePages(config).map { it.toPrefValue() }
        // The emoji layer opened with the emoji key stays, even when it isn't in the Sym cycle
        if (openedByEmojiKey && symPage == SymPage.EMOJI.toPrefValue()) return
        if (allowedValues.isEmpty()) {
            if (symPage != 0 && symPage !in 2..5) {
                // Allow symbols page (2), clipboard page (3) and emoji picker page (4) even if all cycling pages are disabled
                symPage = 0
                persistSymPage()
            }
            return
        }

        if (symPage == 0) {
            return
        }

        // Allow symbols page (2), clipboard page (3) and emoji picker page (4) to remain active even if disabled in cycling settings
        if (symPage in 2..5) {
            return
        }

        if (symPage !in allowedValues) {
            symPage = allowedValues.first()
            persistSymPage()
        }
    }

    private fun persistSymPage() {
        prefs.edit().putInt(PREF_CURRENT_SYM_PAGE, symPage).apply()
    }

}
