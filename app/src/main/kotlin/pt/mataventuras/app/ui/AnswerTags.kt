package pt.mataventuras.app.ui

/**
 * True in unit tests and debug builds. [MatAventurasApp] clears it on a release install.
 */
internal var debugAnswerTags: Boolean = true

/**
 * Answer test tags. Release builds do not mark the correct option.
 */
internal fun debugAnswerTag(
    correct: Boolean,
    debug: Boolean,
): String {
    if (!correct) return "distractor"
    return if (debug) "correct-answer" else "answer"
}
