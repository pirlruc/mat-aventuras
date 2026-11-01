package pt.mataventuras.app.ui

import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.model.ChildProfile
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Entry-screen rules kept out of Compose so branches stay unit-tested.
 */
internal object EntryLogic {
    /**
     * True when the continue-last-profile shortcut should appear.
     */
    fun showsContinue(profile: ChildProfile?): Boolean = profile != null

    /**
     * Resume button label (pt-PT).
     */
    fun continueLabel(profile: ChildProfile): String = VoiceScripts.continueAs(profile.name)

    /**
     * Curriculum blurb under the chosen age.
     */
    fun previewFor(age: AgeBand?): String = age?.let { VoiceScripts.agePreview(it) }.orEmpty()
}

/**
 * Lesson-exit rules. Both bands confirm before the lesson closes.
 */
internal object LessonFlow {
    /**
     * First tap asks. A second tap, while the confirm label is showing, leaves.
     */
    fun shouldAskExitConfirm(
        age: AgeBand,
        confirming: Boolean,
    ): Boolean =
        when (age) {
            AgeBand.THREE_TO_FIVE -> !confirming
            AgeBand.SIX_TO_EIGHT -> !confirming
        }

    /**
     * Exit button label (pt-PT).
     */
    fun exitLabel(confirming: Boolean): String = if (confirming) VoiceScripts.CONFIRM_LEAVE else VoiceScripts.LEAVE

    /**
     * True when the stay button is visible.
     */
    fun showsStay(confirming: Boolean): Boolean = confirming
}
