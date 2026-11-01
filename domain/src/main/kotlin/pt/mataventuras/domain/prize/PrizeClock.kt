package pt.mataventuras.domain.prize

/**
 * Prize length measured on a monotonic clock. The Android hosts pass
 * `SystemClock.elapsedRealtime()`.
 */
object PrizeClock {
    /**
     * Elapsed-realtime deadline for [limitSeconds]. A non-positive limit never expires.
     */
    fun deadlineMs(
        startedElapsedMs: Long,
        limitSeconds: Int,
    ): Long {
        if (limitSeconds <= 0) return Long.MAX_VALUE
        return startedElapsedMs + limitSeconds * 1_000L
    }

    /**
     * True when [nowElapsedMs] has reached [deadlineMs] and a limit was set.
     */
    fun expired(
        nowElapsedMs: Long,
        deadlineMs: Long,
        limitSeconds: Int,
    ): Boolean = limitSeconds > 0 && nowElapsedMs >= deadlineMs
}
