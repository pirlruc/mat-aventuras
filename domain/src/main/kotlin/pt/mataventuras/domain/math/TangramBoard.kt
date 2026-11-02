package pt.mataventuras.domain.math

import kotlin.random.Random
import pt.mataventuras.domain.model.GeometricShape
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Ages 6–8. The missing tangram piece is drawn. The child taps that shape.
 */
class TangramBoard(
    private val random: Random,
) {
    /**
     * The prompt does not name the piece. [targetShape] is the missing one.
     */
    fun make(): Exercise {
        val target = GeometricShape.entries[random.nextInt(GeometricShape.entries.size)]
        val others =
            GeometricShape.entries
                .filter { it != target }
                .shuffled(random)
                .take(3)
        val options = (others + target).map { it.displayName }.shuffled(random)
        return Exercise(
            module = LearningModule.TANGRAM,
            prompt = VoiceScripts.TANGRAM_PROMPT,
            spoken = VoiceScripts.TANGRAM_SPOKEN,
            options = options,
            correctIndex = options.indexOf(target.displayName),
            targetShape = target,
        )
    }
}
