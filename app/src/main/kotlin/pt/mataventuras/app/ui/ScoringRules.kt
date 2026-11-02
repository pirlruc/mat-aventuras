package pt.mataventuras.app.ui

import android.media.ToneGenerator
import android.os.Build
import android.view.HapticFeedbackConstants
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Lesson board size / cipher alphabet from consecutive hits (0, 3, 6, 9+).
 */
internal fun lessonLevel(hits: Int): Int = (hits / 3).coerceIn(0, 3)

/**
 * Test tag for a tappable answer.
 */
internal fun answerTag(correct: Boolean): String = debugAnswerTag(correct, debugAnswerTags)

/**
 * Age-7 sudoku fills every blank, so no single button is the whole answer.
 */
internal fun optionAnswerTag(
    correct: Boolean,
    fillEveryBlank: Boolean,
): String = if (fillEveryBlank) "sudoku-digit" else answerTag(correct)

/**
 * Glyph on the correct/wrong flash (kids can read a tick or cross).
 */
internal fun answerFlashGlyph(
    correct: Boolean,
    gentle: Boolean = false,
    mascot: String = "",
): String {
    if (correct) return "✓"
    if (gentle && mascot.isNotEmpty()) return mascot
    return "✗"
}

/**
 * Caption under the flash glyph. Same pt-PT lines TTS already speaks.
 */
@Suppress("MaxLineLength")
internal fun answerFlashCaption(correct: Boolean): String = if (correct) VoiceScripts.WELL_DONE else VoiceScripts.TRY_AGAIN

/**
 * Solid colour for the flash glyph.
 */
internal fun answerFlashArgb(
    correct: Boolean,
    gentle: Boolean = false,
): Long {
    if (correct) return 0xFF2E7D32
    if (gentle) return 0xFFF9A825
    return 0xFFC62828
}

/**
 * Scrim behind the flash so the board still peeks through.
 */
internal fun answerFlashScrimArgb(
    correct: Boolean,
    gentle: Boolean = false,
): Long {
    if (correct) return 0x882E7D32
    if (gentle) return 0x88F9A825
    return 0x88C62828
}

/**
 * Scale of the flash glyph for [alpha] 0..1.
 */
internal fun answerFlashScale(alpha: Float): Float = 0.85f + 0.35f * alpha

/**
 * Draw the flash only while it is still fading.
 */
internal fun showsAnswerFlash(alpha: Float): Boolean = alpha > 0f

/**
 * ToneGenerator programme: ack on a hit, nack on a miss.
 */
internal fun answerTone(
    correct: Boolean,
    gentle: Boolean = false,
): Int {
    if (correct) return ToneGenerator.TONE_PROP_ACK
    if (gentle) return ToneGenerator.TONE_PROP_BEEP
    return ToneGenerator.TONE_PROP_NACK
}

/**
 * How long the chime plays.
 */
internal fun answerToneMs(
    correct: Boolean,
    gentle: Boolean = false,
): Int {
    if (correct) return 180
    if (gentle) return 140
    return 260
}

/**
 * Filled stars toward the next prize. A finished set shows all three.
 */
internal fun prizeStars(streak: Int): Int {
    if (streak <= 0) return 0
    val remainder = streak % 3
    return if (remainder == 0) 3 else remainder
}

/**
 * Three stars, filled up to [prizeStars].
 */
internal fun prizeStarLine(streak: Int): String {
    val filled = prizeStars(streak)
    return "★".repeat(filled) + "☆".repeat(3 - filled)
}

/**
 * View haptic code. CONFIRM/REJECT need API 30; older devices get a click or long-press.
 */
internal fun answerHaptic(
    correct: Boolean,
    sdkInt: Int = Build.VERSION.SDK_INT,
): Int =
    if (sdkInt >= 30) {
        if (correct) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.REJECT
    } else {
        if (correct) HapticFeedbackConstants.CONTEXT_CLICK else HapticFeedbackConstants.LONG_PRESS
    }

/**
 * Score line under the options.
 */
internal fun lessonScoreLine(
    hits: Int,
    points: Int,
    pattern: String = "%d certos · %d pontos",
): String = pattern.format(hits, points)
