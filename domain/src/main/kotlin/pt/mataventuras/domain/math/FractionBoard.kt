package pt.mataventuras.domain.math

import kotlin.random.Random
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Ages 6–8. Half of an even number up to 10.
 */
class FractionBoard(
    private val random: Random,
) {
    /**
     * Four numbers. Exactly one is half of the whole in the prompt.
     */
    fun make(): Exercise {
        val whole = EVENS[random.nextInt(EVENS.size)]
        val half = whole / 2
        val decoys = (1..10).filter { it != half }.shuffled(random).take(3)
        val options = (decoys + half).map { it.toString() }.shuffled(random)
        return Exercise(
            module = LearningModule.FRACTION,
            prompt = "Quanto é metade de $whole?",
            spoken = VoiceScripts.FRACTION_SPOKEN,
            options = options,
            correctIndex = options.indexOf(half.toString()),
        )
    }

    companion object {
        private val EVENS: List<Int> = listOf(2, 4, 6, 8, 10)
    }
}
