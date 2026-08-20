package pt.mataventuras.domain.math

import kotlin.random.Random
import pt.mataventuras.domain.model.LearningModule

/**
 * How an exercise is presented. CHOICE is four buttons; the others add a board.
 */
enum class PlayKind {
    CHOICE,
    SUDOKU,
    SOUP,
    PUZZLE,
    CIPHER,
}

/**
 * Board payload for interactive play. Empty [cells] means a classic button row.
 */
data class PlayBoard(
    val kind: PlayKind = PlayKind.CHOICE,
    val cells: List<String> = emptyList(),
    val columns: Int = 0,
    val targetIndices: List<Int> = emptyList(),
    val cipherCode: String = "",
)

/**
 * Play kinds that fit each curriculum module. Age-3 modules stay visual.
 */
object PlayKinds {
    /**
     * Kinds mixed into [module]. Age 3 avoids letter-only boards except pictures.
     */
    fun forModule(module: LearningModule): List<PlayKind> =
        when (module) {
            LearningModule.COUNTING ->
                listOf(PlayKind.CHOICE, PlayKind.SOUP, PlayKind.PUZZLE, PlayKind.CIPHER)
            LearningModule.SHAPES ->
                listOf(PlayKind.CHOICE, PlayKind.SUDOKU, PlayKind.SOUP, PlayKind.PUZZLE)
            LearningModule.NUMBERS ->
                listOf(PlayKind.CHOICE, PlayKind.SUDOKU, PlayKind.SOUP, PlayKind.CIPHER, PlayKind.PUZZLE)
            LearningModule.ADDITION, LearningModule.SUBTRACTION, LearningModule.MULTIPLICATION ->
                listOf(PlayKind.CHOICE, PlayKind.CIPHER, PlayKind.PUZZLE)
            LearningModule.LOGIC ->
                listOf(PlayKind.CHOICE, PlayKind.SUDOKU, PlayKind.SOUP, PlayKind.CIPHER)
        }

    /**
     * One kind from [forModule].
     */
    fun pick(
        module: LearningModule,
        random: Random,
    ): PlayKind {
        val kinds = forModule(module)
        return kinds[random.nextInt(kinds.size)]
    }
}
