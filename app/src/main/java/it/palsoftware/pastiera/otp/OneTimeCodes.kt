package it.palsoftware.pastiera.otp

import android.os.Handler
import android.os.Looper

/**
 * One-time codes from notifications (SMS, email, authenticator apps): the latest code is kept
 * in memory only, for a few minutes, until it's typed. Nothing is stored or sent anywhere.
 */
object OneTimeCodes {
    const val LIFETIME_MS = 3 * 60 * 1000L

    private data class Code(val value: String, val at: Long)

    @Volatile private var latest: Code? = null

    /** Told when a new code arrives, on the main thread (the keyboard offers it straight away). */
    @Volatile var onNewCode: ((String) -> Unit)? = null

    private val main by lazy { Handler(Looper.getMainLooper()) }

    // Words that mark a message as carrying a code, in the keyboard's languages
    private val keyword = Regex(
        "(code|otp|passcode|pin|verif|2fa|two.factor|one.time|login|sign.in|security|authenticat|" +
            "codice|código|codigo|kod|код|mã|pinnwort|bestätigung|vérification|verificación|weryfik)",
        RegexOption.IGNORE_CASE
    )
    // 4–8 digits, optionally split in two halves ("123-456", "123 456") or after a prefix ("G-123456")
    // (a full stop or colon next to it is fine; a decimal point or time separator between digits isn't)
    private val candidate = Regex("(?<!\\d|\\d[.,/:])(?:[A-Z]{1,3}-)?(\\d{3,4}[- ]\\d{3,4}|\\d{4,8})(?![\\d%]|[.,/:]\\d)")
    private val currencyBefore = Regex("[$€£¥₹]\\s*$")

    /** The one-time code in a notification's text, if it looks like it carries one. */
    fun extract(text: String): String? {
        if (!keyword.containsMatchIn(text)) return null
        val codes = candidate.findAll(text)
            .filterNot { currencyBefore.containsMatchIn(text.substring(0, it.range.first)) }
            .map { it.groupValues[1].replace("-", "").replace(" ", "") }
            .filter { it.length in 4..8 }
            // A bare year is rarely the code when there's anything else
            .toList()
        val nonYears = codes.filterNot { it.length == 4 && (it.startsWith("19") || it.startsWith("20")) }
        val pool = nonYears.ifEmpty { codes }
        return pool.firstOrNull { it.length == 6 } ?: pool.firstOrNull()
    }

    fun offer(code: String, now: Long = System.currentTimeMillis()) {
        latest = Code(code, now)
        main.post { onNewCode?.invoke(code) }
    }

    /** The current code, if one arrived in the last few minutes. */
    fun current(now: Long = System.currentTimeMillis()): String? =
        latest?.takeIf { now - it.at <= LIFETIME_MS }?.value

    fun consume() {
        latest = null
    }
}
