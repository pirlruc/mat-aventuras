package pt.mataventuras.domain.math

import kotlin.random.Random
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Ages 3–5. Dots 1..n are drawn. The child taps the next number, up to 10.
 */
class DotsBoard(
    private val random: Random,
) {
    /**
     * [visualCount] is how many dots are already joined. The answer is the next one.
     */
    fun make(): Exercise {
        val shown = random.nextInt(1, 10)
        val next = shown + 1
        val decoys = (1..10).filter { it != next }.shuffled(random).take(3)
        val options = (decoys + next).map { it.toString() }.shuffled(random)
        return Exercise(
            module = LearningModule.DOTS,
            prompt = "Qual é o próximo ponto?",
            spoken = VoiceScripts.DOTS_SPOKEN,
            options = options,
            correctIndex = options.indexOf(next.toString()),
            visualCount = shown,
        )
    }
}
