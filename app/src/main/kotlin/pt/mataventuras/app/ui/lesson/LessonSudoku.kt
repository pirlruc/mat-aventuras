package pt.mataventuras.app.ui.lesson

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.mataventuras.app.ui.holeLabel
import pt.mataventuras.app.ui.optionInt
import pt.mataventuras.app.ui.playCellHeightDp
import pt.mataventuras.app.ui.shapeKind
import pt.mataventuras.app.ui.sudokuBoxHeight
import pt.mataventuras.app.ui.sudokuBoxWidth
import pt.mataventuras.app.ui.sudokuCellArgb
import pt.mataventuras.app.ui.sudokuGapDp
import pt.mataventuras.domain.math.Exercise
import pt.mataventuras.domain.math.SudokuBoardState
import pt.mataventuras.domain.math.SudokuPlay
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.model.LearningModule

@Composable
internal fun SudokuBoard(
    exercise: Exercise,
    cells: List<String>,
    focus: Int,
    fillEveryBlank: Boolean,
    onFocus: (Int) -> Unit,
    age: AgeBand,
) {
    val columns = exercise.play.columns.coerceAtLeast(1)
    val shown = shownSudokuCells(cells, exercise.options)
    val cellDp = playCellHeightDp(columns, age) + sudokuExtraHeight(age)
    val boxW = sudokuBoxWidth(columns)
    val boxH = sudokuBoxHeight(columns)
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D47A1))
                .padding(6.dp),
    ) {
        shown.chunked(columns).forEachIndexed { row, rowCells ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = sudokuGapDp(row, boxH).dp),
            ) {
                rowCells.forEachIndexed { col, cell ->
                    val index = row * columns + col
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(cellDp.dp)
                                .padding(start = sudokuGapDp(col, boxW).dp)
                                .background(
                                    Color(
                                        sudokuCellArgb(
                                            cell,
                                            focused = index == focus,
                                            fillEveryBlank = fillEveryBlank,
                                        ),
                                    ),
                                ).semantics(mergeDescendants = true) {
                                    contentDescription = if (cell.isBlank()) "casa vazia" else cell
                                }.testTag("board-cell-$index")
                                .then(sudokuFocusModifier(fillEveryBlank, cell) { onFocus(index) }),
                    ) {
                        GridCellFace(
                            module = exercise.module,
                            cell = cell,
                            fillEveryBlank = fillEveryBlank,
                        )
                    }
                }
            }
        }
    }
}

internal fun scoreSudokuPick(
    fillSudoku: Boolean,
    exercise: Exercise,
    sudoku: SudokuBoardState,
    index: Int,
    onAttempt: (Boolean, Boolean) -> Unit,
): SudokuBoardState {
    val token = exercise.options.getOrNull(index)
    val step = if (fillSudoku && token != null) SudokuPlay.place(sudoku, token, exercise.play.solution) else null
    return when {
        !fillSudoku -> {
            onAttempt(exercise.isCorrect(index), true)
            sudoku
        }
        step == null -> sudoku
        !step.correct -> {
            onAttempt(false, false)
            sudoku
        }
        else -> {
            if (step.solved) onAttempt(true, true)
            step.state
        }
    }
}

internal fun sudokuFocusModifier(
    fillEveryBlank: Boolean,
    cell: String,
    onFocus: () -> Unit,
): Modifier =
    if (fillEveryBlank && SudokuPlay.isBlank(cell)) {
        Modifier.clickable(onClick = onFocus)
    } else {
        Modifier
    }

@Composable
internal fun GridCellFace(
    module: LearningModule,
    cell: String,
    fillEveryBlank: Boolean = false,
) {
    val label = holeLabel(cell, fillEveryBlank)
    val shape = shapeKind(label)
    val dots = optionInt(label) ?: 0
    when {
        shape != null -> ShapeGlyph(shape)
        dots > 0 && module == LearningModule.COUNTING -> DotStrip(dots)
        else -> Text(label, fontWeight = FontWeight.Bold, fontSize = 22.sp)
    }
}
