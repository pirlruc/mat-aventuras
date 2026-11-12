package pt.mataventuras.app.ui

import android.speech.tts.TextToSpeech

/**
 * Repeat the current prompt after a reward Activity covered the host.
 * A prize card still on screen keeps the prompt quiet.
 */
internal fun shouldRepeatSpokenPrompt(
    stoppedInBackground: Boolean,
    prizeCovering: Boolean = false,
): Boolean = stoppedInBackground && !prizeCovering

/**
 * What to do with the exercise prompt while a prize is opening or showing.
 */
internal enum class PromptCue {
    Speak,
    Wait,
    Release,
}

/**
 * A prize Activity that closed without a win should let the lesson speak again.
 * The win card stays quiet until Continuar.
 */
internal fun shouldDropPrizeHold(
    closed: Int,
    seenClosed: Int,
    celebrate: Boolean,
    holdPrompt: Boolean,
): Boolean = closed != seenClosed && !celebrate && holdPrompt

/**
 * [Release] speaks once when Continuar dismisses the win card.
 */
internal fun promptCue(
    heldForPrize: Boolean,
    prizeReturned: Boolean,
    celebrate: Boolean,
): PromptCue =
    if (celebrate || (heldForPrize && !prizeReturned)) {
        PromptCue.Wait
    } else if (heldForPrize) {
        PromptCue.Release
    } else {
        PromptCue.Speak
    }

/**
 * Stop any queued lesson line, then open the prize.
 */
internal fun beginReward(
    stopSpeech: () -> Unit,
    launch: () -> Unit,
) {
    stopSpeech()
    launch()
}

/**
 * TTS language is usable.
 */
internal fun languageSupported(result: Int): Boolean {
    if (result == TextToSpeech.LANG_MISSING_DATA) return false
    return result != TextToSpeech.LANG_NOT_SUPPORTED
}

/**
 * Speak only when ready and the line is non-blank.
 */
internal fun shouldSpeak(
    ready: Boolean,
    text: String,
): Boolean = ready && text.isNotBlank()

/**
 * `{minutes}m {seconds}s` duration.
 */
internal fun formatDuration(ms: Long): String {
    val minutes = ms / 60_000L
    val seconds = (ms / 1000L) % 60L
    return "${minutes}m ${seconds}s"
}
