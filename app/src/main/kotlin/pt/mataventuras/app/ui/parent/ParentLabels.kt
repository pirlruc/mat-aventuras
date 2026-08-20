package pt.mataventuras.app.ui.parent

import java.util.concurrent.TimeUnit

/**
 * pt-PT duration labels for the parental dashboard.
 */
internal object ParentLabels {
    /**
     * Formats a millisecond duration as `{minutes}m {seconds}s`.
     */
    fun formatDuration(ms: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
        return "${minutes}m ${seconds}s"
    }
}
