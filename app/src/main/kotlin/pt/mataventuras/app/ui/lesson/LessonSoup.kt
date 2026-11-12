package pt.mataventuras.app.ui.lesson

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import pt.mataventuras.app.ui.answerTag
import pt.mataventuras.app.ui.playCellHeightDp
import pt.mataventuras.app.ui.soupIndexAt
import pt.mataventuras.app.ui.soupKeepsBoard
import pt.mataventuras.app.ui.soupMatchedPath
import pt.mataventuras.app.ui.soupPickIndex
import pt.mataventuras.app.ui.soupReleaseKind
import pt.mataventuras.app.ui.soupSelectedArgb
import pt.mataventuras.domain.math.Exercise
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.model.LearningModule

@Composable
internal fun SoupBoard(
    exercise: Exercise,
    onPick: (Int) -> Unit,
    onMissKeep: () -> Unit,
    age: AgeBand,
) {
    val columns = exercise.play.columns.coerceAtLeast(1)
    val cells = soupBoardCells(exercise.play.cells, exercise.options)
    val paths = exercise.play.soupPaths()
    val boardKey = LocalExerciseId.current
    var selected by remember(boardKey) { mutableStateOf(emptyList<Int>()) }
    var found by remember(boardKey) { mutableStateOf(emptySet<Int>()) }
    val foundCells = foundSoupCells(found, paths)
    val cellDp = playCellHeightDp(columns, age)
    val onGesture: (List<Int>) -> Unit = { path ->
        found = resolveSoupGesture(path, paths, found, cells.size, onPick, onMissKeep)
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag("soup-board")
                .pointerInput(boardKey, columns, cells.size, found) {
                    awaitEachGesture {
                        val gap = 8.dp.toPx()
                        val path =
                            collectSoupPath(columns, cells.size, gap, size.width.toFloat(), size.height.toFloat()) {
                                selected = it
                            }
                        onGesture(path)
                        selected = emptyList()
                    }
                },
    ) {
        cells.chunked(columns).forEachIndexed { row, rowCells ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowCells.forEachIndexed { col, cell ->
                    val index = row * columns + col
                    SoupCell(
                        module = exercise.module,
                        cell = cell,
                        correct = exercise.isCorrect(index),
                        selected = index in selected,
                        found = index in foundCells,
                        heightDp = cellDp,
                        onTap = { onGesture(listOf(index)) },
                    )
                }
            }
        }
    }
}

@Composable
internal fun RowScope.SoupCell(
    module: LearningModule,
    cell: String,
    correct: Boolean,
    selected: Boolean,
    found: Boolean,
    heightDp: Int,
    onTap: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .weight(1f)
                .height(heightDp.dp)
                .background(Color(soupSelectedArgb(selected, found)))
                .testTag(answerTag(correct))
                .semantics {
                    onClick {
                        onTap()
                        true
                    }
                },
    ) {
        GridCellFace(module = module, cell = cell)
    }
}

internal fun resolveSoupGesture(
    path: List<Int>,
    paths: List<List<Int>>,
    found: Set<Int>,
    cellCount: Int,
    onPick: (Int) -> Unit,
    onMissKeep: () -> Unit,
): Set<Int> {
    val match = soupMatchedPath(path, paths, found)
    if (match != null) {
        val next = found + match
        if (next.size >= paths.size) onPick(paths[match].first())
        return next
    }
    val kind = soupReleaseKind(path, paths, found)
    if (soupKeepsBoard(kind, paths.size)) {
        onMissKeep()
    } else {
        soupPickIndex(path, paths, found, cellCount)?.let(onPick)
    }
    return found
}

private suspend fun AwaitPointerEventScope.collectSoupPath(
    columns: Int,
    cellCount: Int,
    gapPx: Float,
    width: Float,
    height: Float,
    onPath: (List<Int>) -> Unit,
): List<Int> {
    val down = awaitFirstDown()
    var path =
        listOfNotNull(
            soupIndexAt(down.position.x, down.position.y, width, height, columns, cellCount, gapPx),
        )
    onPath(path)
    while (true) {
        val event = awaitPointerEvent()
        val change = event.changes.firstOrNull { it.id == down.id } ?: break
        if (!change.pressed) {
            change.consume()
            return path
        }
        path =
            extendSoup(
                path,
                soupIndexAt(
                    change.position.x,
                    change.position.y,
                    width,
                    height,
                    columns,
                    cellCount,
                    gapPx,
                ),
                columns,
            )
        onPath(path)
        change.consume()
    }
    return path
}
