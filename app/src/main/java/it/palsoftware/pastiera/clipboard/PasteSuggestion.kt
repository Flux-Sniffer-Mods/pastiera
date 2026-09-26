package it.palsoftware.pastiera.clipboard

/** The paste suggestion chip's label: the copied text on one short line. */
object PasteSuggestion {
    private const val MAX_LABEL_LENGTH = 24

    fun label(text: String): String {
        val oneLine = text.trim().replace(Regex("\\s+"), " ")
        val shown = if (oneLine.length > MAX_LABEL_LENGTH) oneLine.take(MAX_LABEL_LENGTH - 1).trimEnd() + "…" else oneLine
        // ⎘ (copy/paste): a text symbol, not an emoji, like every indicator
        return "\u2398 $shown"
    }
}
