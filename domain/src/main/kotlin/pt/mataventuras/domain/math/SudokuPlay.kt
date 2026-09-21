package pt.mataventuras.domain.math

/**
 * Cell-by-cell sudoku. Each blank is filled in turn against [PlayBoard.solution].
 */
data class SudokuBoardState(
    val cells: List<String>,
    val focus: Int,
)

/**
 * Result of placing one digit. [solved] is true only when no blanks remain.
 */
data class SudokuStep(
    val state: SudokuBoardState,
    val correct: Boolean,
    val solved: Boolean,
)

/**
 * Focus and placement rules for a board with several empty houses.
 */
object SudokuPlay {
    /**
     * First blank focused, or -1 when the grid is already full.
     */
    fun start(cells: List<String>): SudokuBoardState = SudokuBoardState(cells, firstBlank(cells))

    /**
     * True when [cell] still needs a digit or shape.
     */
    fun isBlank(cell: String): Boolean = cell.isEmpty() || cell == SudokuHoles.EXTRA_BLANK

    /**
     * Indexes that still need a value.
     */
    fun blanks(cells: List<String>): List<Int> = cells.indices.filter { isBlank(cells[it]) }

    /**
     * Moves the glow to [index] when that house is still empty.
     */
    fun focus(
        state: SudokuBoardState,
        index: Int,
    ): SudokuBoardState =
        if (index in state.cells.indices && isBlank(state.cells[index])) {
            state.copy(focus = index)
        } else {
            state
        }

    /**
     * Writes [token] into the focused blank when it matches [solution].
     * A mismatch leaves the board unchanged.
     */
    fun place(
        state: SudokuBoardState,
        token: String,
        solution: List<String>,
    ): SudokuStep {
        val index = state.focus
        if (index !in state.cells.indices || !isBlank(state.cells[index])) {
            return SudokuStep(state, correct = false, solved = false)
        }
        if (solution.getOrNull(index) != token) {
            return SudokuStep(state, correct = false, solved = false)
        }
        val nextCells = state.cells.toMutableList().also { it[index] = token }
        val nextFocus = firstBlank(nextCells)
        return SudokuStep(
            state = SudokuBoardState(nextCells, nextFocus),
            correct = true,
            solved = nextFocus < 0,
        )
    }

    private fun firstBlank(cells: List<String>): Int = cells.indexOfFirst { isBlank(it) }
}
