package pt.mataventuras.domain.math

import kotlin.random.Random
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Ages 3–5. The child taps the big one or the small one.
 */
class SizeBoard(
    private val random: Random,
) {
    /**
     * Three of one size and one of the other. The prompt names the singleton.
     */
    fun make(): Exercise {
        val askSmall = random.nextBoolean()
        val target = if (askSmall) "Pequeno" else "Grande"
        val other = if (askSmall) "Grande" else "Pequeno"
        val options = (List(3) { other } + target).shuffled(random)
        return Exercise(
            module = LearningModule.SIZE,
            prompt = if (askSmall) VoiceScripts.SIZE_SMALL_PROMPT else VoiceScripts.SIZE_BIG_PROMPT,
            spoken = VoiceScripts.SIZE_SPOKEN,
            options = options,
            correctIndex = options.indexOf(target),
        )
    }
}
