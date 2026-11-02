package pt.mataventuras.app.ui

import android.speech.tts.TextToSpeech

/**
 * Repeat the current prompt after a reward Activity covered the host.
 */
internal fun shouldRepeatSpokenPrompt(stoppedInBackground: Boolean): Boolean = stoppedInBackground

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
