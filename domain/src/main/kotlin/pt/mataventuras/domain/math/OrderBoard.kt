package pt.mataventuras.domain.math

import kotlin.random.Random
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Ages 6–8. The next whole number, never past 100.
 */
class OrderBoard(
    private val random: Random,
) {
    /**
     * The prompt shows a number from 1 to 99. The answer is the next one.
     */
    fun make(): Exercise {
        val shown = random.nextInt(1, 100)
        val next = shown + 1
        val decoys = (1..100).filter { it != next }.shuffled(random).take(3)
        val options = (decoys + next).map { it.toString() }.shuffled(random)
        return Exercise(
            module = LearningModule.ORDER,
            prompt = "Qual vem a seguir: $shown?",
            spoken = VoiceScripts.ORDER_SPOKEN,
            options = options,
            correctIndex = options.indexOf(next.toString()),
        )
    }
}
