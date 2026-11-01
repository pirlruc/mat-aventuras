package pt.mataventuras.domain.math

import kotlin.random.Random
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Ages 6–8. Read an hour on a twelve-hour clock.
 */
class ClockBoard(
    private val random: Random,
) {
    /**
     * The hour hand is on one number. Options are times on the hour.
     */
    fun make(): Exercise {
        val hour = 1 + random.nextInt(12)
        val correct = "$hour:00"
        val others = (1..12).filter { it != hour }.shuffled(random).take(3).map { "$it:00" }
        val options = (others + correct).shuffled(random)
        return Exercise(
            module = LearningModule.CLOCK,
            prompt = VoiceScripts.clockPrompt(hour),
            spoken = VoiceScripts.CLOCK_SPOKEN,
            options = options,
            correctIndex = options.indexOf(correct),
        )
    }
}
