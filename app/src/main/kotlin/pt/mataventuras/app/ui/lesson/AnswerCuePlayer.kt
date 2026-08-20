package pt.mataventuras.app.ui.lesson

import android.media.AudioManager
import android.media.ToneGenerator
import pt.mataventuras.app.ui.UiLogic

/**
 * Short on-device chime for a correct or wrong tap. TTS still speaks the line.
 */
class AnswerCuePlayer(
    private val playTone: (Int, Int) -> Unit,
    private val releaseTone: () -> Unit = {},
) {
    /**
     * Plays the hit or miss chime, then runs [haptic] with the Android haptic code.
     */
    fun play(
        correct: Boolean,
        haptic: (Int) -> Unit = {},
    ) {
        try {
            playTone(UiLogic.answerTone(correct), UiLogic.answerToneMs(correct))
        } catch (_: RuntimeException) {
            // Host tests and muted devices have no tone generator.
        }
        try {
            haptic(UiLogic.answerHaptic(correct))
        } catch (_: RuntimeException) {
            // Some views ignore unknown haptic codes.
        }
    }

    /**
     * Releases the native tone generator, if any.
     */
    fun release() {
        try {
            releaseTone()
        } catch (_: RuntimeException) {
            // Already released.
        }
    }

    companion object {
        /**
         * Tone generator for a real device. Null generator becomes a silent player.
         */
        fun device(): AnswerCuePlayer = wrapGenerator(createGenerator())

        internal fun wrapGenerator(generator: ToneGenerator?): AnswerCuePlayer =
            AnswerCuePlayer(
                playTone = { tone, ms -> generator?.startTone(tone, ms) },
                releaseTone = { generator?.release() },
            )

        internal fun createGenerator(): ToneGenerator? =
            try {
                ToneGenerator(AudioManager.STREAM_MUSIC, UiLogic.answerVolumePercent())
            } catch (_: RuntimeException) {
                null
            }
    }
}
