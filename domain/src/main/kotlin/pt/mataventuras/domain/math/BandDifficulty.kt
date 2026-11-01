package pt.mataventuras.domain.math

import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.model.LearningModule

/**
 * Operand ranges for one age band. Shared modules stay easy for ages 3–5.
 */
data class BandDifficulty(
    val numberLower: Int,
    val numberUpper: Int,
    val sumLimit: Int,
    val additionLow: Int,
    val additionHigh: Int,
    val additionDots: Boolean,
    val logicSteps: List<Int>,
    val extremaLower: Int,
    val extremaUpper: Int,
) {
    companion object {
        /**
         * Ranges for [age]. [numberUpper] and [sumLimit] are inclusive.
         * [additionHigh] and [extremaUpper] are exclusive, matching [kotlin.random.Random.nextInt].
         */
        fun forAge(age: AgeBand): BandDifficulty =
            when (age) {
                AgeBand.THREE_TO_FIVE -> young
                AgeBand.SIX_TO_EIGHT -> older
            }

        private val young =
            BandDifficulty(
                numberLower = 0,
                numberUpper = 9,
                sumLimit = 10,
                additionLow = 0,
                additionHigh = 6,
                additionDots = true,
                logicSteps = listOf(1, 2),
                extremaLower = 0,
                extremaUpper = 10,
            )

        private val older =
            BandDifficulty(
                numberLower = 0,
                numberUpper = 100,
                sumLimit = 96,
                additionLow = 12,
                additionHigh = 48,
                additionDots = false,
                logicSteps = listOf(3, 4, 5, 6, 10),
                extremaLower = 12,
                extremaUpper = 180,
            )
    }
}

/**
 * Harder sudoku, puzzles, and sequences. Numbers stay on the small board.
 */
fun usesOlderBoard(
    module: LearningModule,
    age: AgeBand,
): Boolean = age == AgeBand.SIX_TO_EIGHT && module in olderBoardModules

private val olderBoardModules =
    setOf(
        LearningModule.ADDITION,
        LearningModule.SUBTRACTION,
        LearningModule.MULTIPLICATION,
        LearningModule.DIVISION,
        LearningModule.LOGIC,
        LearningModule.WORDS,
    )
