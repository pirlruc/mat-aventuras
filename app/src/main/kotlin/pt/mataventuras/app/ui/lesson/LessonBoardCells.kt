package pt.mataventuras.app.ui.lesson

import pt.mataventuras.app.ui.optionInt
import pt.mataventuras.app.ui.showsDotStrip
import pt.mataventuras.app.ui.soupExtendPath
import pt.mataventuras.app.ui.sudokuBanner
import pt.mataventuras.domain.math.PlayKind
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.model.LearningModule

internal fun sudokuTitle(
    kind: PlayKind,
    age: AgeBand,
): String = sudokuBanner(kind, age) ?: "Sudoku"

internal fun sudokuExtraHeight(age: AgeBand): Int = if (age == AgeBand.THREE_TO_FIVE) 0 else 8

internal fun shownSudokuCells(
    cells: List<String>,
    options: List<String>,
): List<String> = cells.ifEmpty { options }

internal fun soupBoardCells(
    cells: List<String>,
    options: List<String>,
): List<String> = cells.ifEmpty { options }

internal fun foundSoupCells(
    found: Set<Int>,
    paths: List<List<Int>>,
): Set<Int> = found.flatMap { index -> paths.getOrElse(index) { emptyList() } }.toSet()

internal fun extendSoup(
    path: List<Int>,
    next: Int?,
    columns: Int,
): List<Int> = if (next != null) soupExtendPath(path, next, columns) else path

internal fun puzzleCells(cells: List<String>): List<String> = cells.ifEmpty { listOf("1", "2", "3", "?") }

internal fun visualDotCount(
    module: LearningModule,
    text: String,
): Int = if (showsDotStrip(module)) optionInt(text) ?: 0 else 0
