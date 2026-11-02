package pt.mataventuras.domain.math

import kotlin.random.Random
import pt.mataventuras.domain.model.GeometricShape
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Ages 3–5. One round is a colour, the next is a shape.
 */
class SortBoard(
    private val random: Random,
) {
    /**
     * Four choices. Exactly one matches the spoken colour or shape.
     */
    fun make(): Exercise = if (random.nextBoolean()) colour() else shape()

    private fun colour(): Exercise {
        val target = COLOURS[random.nextInt(COLOURS.size)]
        val options = COLOURS.shuffled(random)
        return Exercise(
            module = LearningModule.SORT,
            prompt = "Toca no $target.",
            spoken = VoiceScripts.SORT_SPOKEN,
            options = options,
            correctIndex = options.indexOf(target),
        )
    }

    private fun shape(): Exercise {
        val target = GeometricShape.entries[random.nextInt(GeometricShape.entries.size)]
        val others =
            GeometricShape.entries
                .filter { it != target }
                .shuffled(random)
                .take(3)
        val options = (others + target).map { it.displayName }.shuffled(random)
        return Exercise(
            module = LearningModule.SORT,
            prompt = "Toca no ${target.displayName}.",
            spoken = VoiceScripts.SORT_SPOKEN,
            options = options,
            correctIndex = options.indexOf(target.displayName),
            targetShape = target,
        )
    }

    companion object {
        /** pt-PT colour names the lesson draws as circles. */
        val COLOURS: List<String> = listOf("vermelho", "azul", "amarelo", "verde")
    }
}
