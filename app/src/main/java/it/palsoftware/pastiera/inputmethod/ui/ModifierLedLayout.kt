package it.palsoftware.pastiera.inputmethod.ui

/**
 * Logical modifier states that can be projected onto one or more physical key positions.
 */
internal enum class ModifierLedState {
    SHIFT,
    SYM,
    CTRL,
    ALT
}

/**
 * A single LED segment in normalized coordinates inside the LED canvas.
 *
 * Keeping both axes and both dimensions configurable allows device presets to mirror
 * one- or multi-row physical keyboard layouts without changing the renderer.
 */
internal data class ModifierLedSegment(
    val state: ModifierLedState,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
) {
    init {
        require(x in 0f..1f) { "x must be normalized" }
        require(y in 0f..1f) { "y must be normalized" }
        require(width > 0f && x + width <= 1.0001f) { "width must fit the LED canvas" }
        require(height > 0f && y + height <= 1.0001f) { "height must fit the LED canvas" }
    }
}

internal data class ModifierLedLayout(
    val id: String,
    val segments: List<ModifierLedSegment>
) {
    init {
        require(segments.isNotEmpty()) { "An LED layout needs at least one segment" }
    }
}

internal object ModifierLedLayouts {
    /** Reproduces the previous six-zone layout, including the two empty middle zones. */
    val DEFAULT = ModifierLedLayout(
        id = "default",
        segments = listOf(
            ModifierLedSegment(ModifierLedState.SHIFT, x = 0.000f, y = 0f, width = 0.164f, height = 1f),
            ModifierLedSegment(ModifierLedState.SYM, x = 0.168f, y = 0f, width = 0.164f, height = 1f),
            ModifierLedSegment(ModifierLedState.CTRL, x = 0.672f, y = 0f, width = 0.164f, height = 1f),
            ModifierLedSegment(ModifierLedState.ALT, x = 0.840f, y = 0f, width = 0.160f, height = 1f)
        )
    )

    /**
     * Photo-derived Titan 2 Elite modifier map.
     *
     * Alt and Sym follow their upper physical row. Shift is intentionally mirrored at both
     * outer bottom positions because Pastiera tracks the active Shift state, not its origin.
     * Ctrl follows the Fn key that users configure as Ctrl, just inside the right Shift key.
     */
    val TITAN_2_ELITE = ModifierLedLayout(
        id = "titan2-elite",
        segments = listOf(
            ModifierLedSegment(ModifierLedState.ALT, x = 0.000f, y = 0.000f, width = 0.164f, height = 0.38f),
            ModifierLedSegment(ModifierLedState.SYM, x = 0.840f, y = 0.000f, width = 0.160f, height = 0.38f),
            ModifierLedSegment(ModifierLedState.SHIFT, x = 0.000f, y = 0.620f, width = 0.164f, height = 0.38f),
            ModifierLedSegment(ModifierLedState.CTRL, x = 0.672f, y = 0.620f, width = 0.164f, height = 0.38f),
            ModifierLedSegment(ModifierLedState.SHIFT, x = 0.840f, y = 0.620f, width = 0.160f, height = 0.38f)
        )
    )

    /**
     * Titan 2 Elite: one LED per modifier, left to right in physical order (Alt, Shift, Ctrl,
     * Sym), on every screen. Nothing is shared, so Shift never lights the Sym LED, which matters
     * when Right Shift is the emoji picker key.
     */
    val TITAN_2_ELITE_SPLIT = ModifierLedLayout(
        id = "titan2-elite-split",
        segments = listOf(
            ModifierLedSegment(ModifierLedState.ALT, x = 0.00f, y = 0.5f, width = 0.22f, height = 0.5f),
            ModifierLedSegment(ModifierLedState.SHIFT, x = 0.26f, y = 0.5f, width = 0.22f, height = 0.5f),
            ModifierLedSegment(ModifierLedState.CTRL, x = 0.52f, y = 0.5f, width = 0.22f, height = 0.5f),
            ModifierLedSegment(ModifierLedState.SYM, x = 0.78f, y = 0.5f, width = 0.22f, height = 0.5f)
        )
    )

    fun resolve(
        physicalProfileOverride: String?,
        titan2EliteAutoDetected: Boolean
    ): ModifierLedLayout {
        val normalizedOverride = physicalProfileOverride?.trim()?.lowercase().orEmpty()
        val useAutoDetectedProfile = normalizedOverride.isEmpty() || normalizedOverride == "auto"
        return if (
            normalizedOverride == "titan2elite_qwerty" ||
            (useAutoDetectedProfile && titan2EliteAutoDetected)
        ) {
            TITAN_2_ELITE_SPLIT
        } else {
            DEFAULT
        }
    }
}
