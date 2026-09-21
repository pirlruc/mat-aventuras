package pt.mataventuras.domain.model

/**
 * Display name stored in Room and spoken by TTS. Control and format characters
 * are dropped and the result is capped so a profile row cannot grow or spoof
 * the parent screen with hidden text.
 */
object ChildName {
    /** Longest name kept after trimming. */
    const val MAX_LENGTH: Int = 24

    /** pt-PT placeholder when the field is blank. */
    const val PLACEHOLDER: String = "Amigo"

    /**
     * Trimmed name with control and format characters removed. Blank input becomes [PLACEHOLDER].
     */
    fun sanitize(raw: String): String {
        val cleaned =
            buildString(raw.length) {
                for (ch in raw) {
                    if (!ch.isISOControl() && ch.category != CharCategory.FORMAT) append(ch)
                }
            }.trim().replace(WHITESPACE, " ")
        return cleaned.take(MAX_LENGTH).trim().ifBlank { PLACEHOLDER }
    }

    private val WHITESPACE: Regex = Regex("\\s+")
}
