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
        val start = random.nextInt(1, if (module.isSevenYears()) 12 else 5)
        val step =
            when {
                module == LearningModule.COUNTING -> 1
                module.isSevenYears() -> 3 + random.nextInt(5)
                else -> 1 + random.nextInt(2)
            }
        return List(n * n) { (start + it * step).toString() }
    }
}
