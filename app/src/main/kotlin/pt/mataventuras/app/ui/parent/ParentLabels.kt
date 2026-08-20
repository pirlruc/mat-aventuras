package pt.mataventuras.app.ui.parent

/**
 * pt-PT duration labels for the parental dashboard.
 */
internal object ParentLabels {
    /**
     * Formats a millisecond duration as `{minutes}m {seconds}s`.
     */
    fun formatDuration(ms: Long): String = pt.mataventuras.app.ui.UiLogic.formatDuration(ms)
}
