package pt.mataventuras.domain.math

import pt.mataventuras.domain.model.GeometricShape
import pt.mataventuras.domain.model.LearningModule

/**
 * Exercise shown on the lesson screen. [prompt] and [spoken] are pt-PT UI copy.
 */
data class Exercise(
    val module: LearningModule,
    val prompt: String,
    val spoken: String,
    val options: List<String>,
    val correctIndex: Int,
    val visualCount: Int = 0,
    val targetShape: GeometricShape? = null,
) {
    init {
        require(options.isNotEmpty()) { "An exercise needs options." }
        require(correctIndex in options.indices) { "Correct index is out of range." }
    }

    /** True when the tapped option is the answer. */
    fun isCorrect(chosenIndex: Int): Boolean = chosenIndex == correctIndex
}

/**
 * Result of one attempt, for scoring and spoken feedback.
 */
data class AttemptResult(
    val correct: Boolean,
    val spoken: String,
)
