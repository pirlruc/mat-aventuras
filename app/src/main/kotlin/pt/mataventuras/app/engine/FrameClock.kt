package pt.mataventuras.app.engine

/**
 * Converts monotonic nanos into a clamped frame delta so reward loops share one recipe.
 */
internal class FrameClock(
    private val nowNs: () -> Long,
    private val maxDt: Float = 0.05f,
) {
    private var lastNs: Long = 0L

    /**
     * Seconds since the previous [delta] call, capped at [maxDt].
     */
    fun delta(): Float {
        val now = nowNs()
        if (lastNs == 0L) lastNs = now
        val dt = ((now - lastNs) / 1_000_000_000f).coerceAtMost(maxDt)
        lastNs = now
        return dt
    }
}
