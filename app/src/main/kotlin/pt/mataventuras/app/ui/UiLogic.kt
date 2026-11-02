package pt.mataventuras.app.ui

import android.media.ToneGenerator

/**
 * Constants the screens read. The rules themselves live in the concern files
 * next to this one (scoring, soup, sudoku, parent copy, speech).
 */
internal object UiLogic {
    /**
     * Flash length in milliseconds.
     */
    const val ANSWER_FLASH_MS: Int = 960

    /**
     * Largest font scale applied to fixed-size lesson text.
     */
    const val MAX_FONT_SCALE: Float = 1.3f

    /**
     * STREAM_MUSIC volume for [ToneGenerator] (0..100).
     */
    const val ANSWER_VOLUME_PERCENT: Int = 80

    /**
     * Age-button side in dp. Both bands share a side so the chips match.
     */
    const val AGE_BUTTON_SIDE_DP: Int = 168
}
