package pt.mataventuras.domain.math

import kotlin.random.Random
import pt.mataventuras.domain.model.GeometricShape
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Ages 3–5. A black shape is the shadow. The child taps the matching form.
 */
class ShadowBoard(
    private val random: Random,
) {
    /**
     * The prompt does not name the shape. [targetShape] is the shadow.
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
            module = LearningModule.SHADOW,
            prompt = VoiceScripts.SHADOW_PROMPT,
            spoken = VoiceScripts.SHADOW_SPOKEN,
            options = options,
            correctIndex = options.indexOf(target.displayName),
            targetShape = target,
        )
    }
}
