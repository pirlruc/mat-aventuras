package pt.mataventuras.app.ui

import pt.mataventuras.domain.model.AgeGroup
import pt.mataventuras.domain.model.ChildProfile
import pt.mataventuras.domain.model.tokensFor
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
    fun previewFor(age: AgeGroup?): String = age?.let { VoiceScripts.agePreview(it) }.orEmpty()
}

/**
 * Lesson-exit rules. Age 7 confirms; age 3 leaves immediately.
 */
internal object LessonFlow {
    /**
     * First tap on age 7 asks; a second tap (or any tap on age 3) leaves.
     */
    fun shouldAskExitConfirm(
        age: AgeGroup,
        confirming: Boolean,
    ): Boolean = tokensFor(age).confirmsBeforeExit && !confirming

    /**
     * Exit button label (pt-PT).
     */
    fun exitLabel(confirming: Boolean): String =
        if (confirming) VoiceScripts.CONFIRM_LEAVE else VoiceScripts.LEAVE

    /**
     * True when the stay button is visible.
     */
    fun showsStay(confirming: Boolean): Boolean = confirming
}
