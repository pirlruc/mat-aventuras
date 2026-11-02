package pt.mataventuras.app.ui

import pt.mataventuras.domain.math.PlayBoard
import pt.mataventuras.domain.math.PlayKind
import pt.mataventuras.domain.math.SudokuHoles
import pt.mataventuras.domain.model.AgeBand

/**
 * Soup and sudoku draw a cell grid.
 */
internal fun showsPlayGrid(kind: PlayKind): Boolean = kind == PlayKind.SUDOKU || kind == PlayKind.SOUP

/**
 * Sudoku shows a titled board so it is not mistaken for a button row.
 */
internal fun sudokuBanner(
    kind: PlayKind,
    age: AgeBand = AgeBand.SIX_TO_EIGHT,
): String? {
    if (kind != PlayKind.SUDOKU) return null
    return if (age == AgeBand.THREE_TO_FIVE) "Quadrado mágico" else "Sudoku"
}

/**
 * Box width in cells for mini-sudoku grid lines.
 */
internal fun sudokuBoxWidth(columns: Int): Int =
    when (columns) {
        4 -> 2
        6 -> 3
        else -> maxOf(columns, 1)
    }

/**
 * Box height in cells for mini-sudoku grid lines.
 */
internal fun sudokuBoxHeight(columns: Int): Int =
    when (columns) {
        4, 6 -> 2
        else -> maxOf(columns, 1)
    }

/**
 * Gutter in dp. Box edges are thicker so the 2×2 / 2×3 regions read as sudoku.
 */
internal fun sudokuGapDp(
    indexInAxis: Int,
    box: Int,
): Int = if (indexInAxis > 0 && indexInAxis % box == 0) 4 else 1

/**
 * Rows needed to lay out [cellCount] in [columns].
 */
internal fun boardRowCount(
    cellCount: Int,
    columns: Int,
): Int = if (columns <= 0) 0 else (cellCount + columns - 1) / columns

/**
 * Empty sudoku cells show a question mark. Extra blanks stay empty.
 */
internal fun holeLabel(
    cell: String,
    fillEveryBlank: Boolean = false,
): String =
    when {
        fillEveryBlank && isBoardHole(cell) -> ""
        isExtraBlank(cell) -> ""
        cell.isEmpty() -> "?"
        else -> cell
    }

/**
 * Extra sudoku blanks that are not the question cell.
 */
internal fun isExtraBlank(cell: String): Boolean = cell == SudokuHoles.EXTRA_BLANK

/**
 * The cell whose value the option buttons ask for.
 */
internal fun isQuestionHole(cell: String): Boolean = cell.isEmpty() || cell == "?"

/**
 * Puzzle and sudoku holes, including extra blanks on harder boards.
 */
internal fun isBoardHole(cell: String): Boolean = isQuestionHole(cell) || isExtraBlank(cell)

/**
 * Question cells glow; extra blanks stay pale so the asked house is obvious.
 */
internal fun sudokuCellArgb(
    cell: String,
    focused: Boolean = false,
    fillEveryBlank: Boolean = false,
): Long =
    when {
        !isBoardHole(cell) -> 0xFFE3F2FD
        fillEveryBlank && focused -> 0xFFFFF59D
        fillEveryBlank -> 0xFFEEEEEE
        isQuestionHole(cell) -> 0xFFFFF59D
        else -> 0xFFEEEEEE
    }

/**
 * True when the board is solved house by house instead of one glowing cell.
 */
internal fun sudokuFillsEveryBlank(play: PlayBoard): Boolean = play.solution.isNotEmpty()
