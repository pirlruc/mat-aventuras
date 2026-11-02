package pt.mataventuras.app.ui

/**
 * True in unit tests and debug builds. A release install leaves this false.
 */
internal var debugAnswerTags: Boolean = false

/**
 * Answer test tags. A release build uses one tag for every option.
 */
internal fun debugAnswerTag(
    correct: Boolean,
    debug: Boolean,
): String {
    if (!debug) return "option"
    return if (correct) "correct-answer" else "distractor"
}
