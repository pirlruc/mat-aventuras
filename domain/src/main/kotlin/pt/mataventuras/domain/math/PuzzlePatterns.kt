package pt.mataventuras.domain.math

import kotlin.random.Random
import pt.mataventuras.domain.model.GeometricShape
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.model.isSevenYears

/**
 * Visible puzzle frame whose missing cell is the real answer.
 */
object PuzzlePatterns {
    /**
     * Repeating shapes or a number sequence of [n]×[n] cells.
     */
    fun cells(
        module: LearningModule,
        n: Int,
        random: Random,
    ): List<String> {
        if (module == LearningModule.SHAPES) {
            val glyphs = GeometricShape.entries.shuffled(random).take(2).map { it.displayName }
            return List(n * n) { glyphs[it % glyphs.size] }
        }
        val spec = sequence(module, random)
        return List(n * n) { (spec.start + it * spec.step).toString() }
    }

    private fun sequence(
        module: LearningModule,
        random: Random,
    ): SequenceSpec {
        if (module == LearningModule.COUNTING || !module.isSevenYears()) {
            val start = random.nextInt(1, 5)
            val step = if (module == LearningModule.COUNTING) 1 else 1 + random.nextInt(2)
            return SequenceSpec(start, step)
        }
        return ageSevenSequence(module, random)
    }

    private fun ageSevenSequence(
        module: LearningModule,
        random: Random,
    ): SequenceSpec =
        when (module) {
            LearningModule.SUBTRACTION -> {
                val step = 2 + random.nextInt(2)
                SequenceSpec(28 + random.nextInt(12), -step)
            }
            LearningModule.MULTIPLICATION -> {
                val step = listOf(2, 3, 4, 5, 6, 10)[random.nextInt(6)]
                SequenceSpec(step * (1 + random.nextInt(3)), step)
            }
            LearningModule.DIVISION -> {
                val step = listOf(2, 3, 4, 5, 6)[random.nextInt(5)]
                SequenceSpec(step * (9 + random.nextInt(3)), -step)
            }
            else -> SequenceSpec(random.nextInt(1, 12), 3 + random.nextInt(5))
        }
}

private data class SequenceSpec(
    val start: Int,
    val step: Int,
)
