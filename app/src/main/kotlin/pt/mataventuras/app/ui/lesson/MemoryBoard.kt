package pt.mataventuras.app.ui.lesson

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.mataventuras.domain.math.Exercise
import pt.mataventuras.domain.math.MemoryBoards
import kotlin.random.Random

/**
 * Two taps that share a pair key count as a correct answer.
 */
@Composable
internal fun MemoryBoard(
    exercise: Exercise,
    onPick: (Int) -> Unit,
) {
    val boards = remember { MemoryBoards(Random(1)) }
    var first by remember(exercise) { mutableStateOf<Int?>(null) }
    val columns = exercise.play.columns.coerceAtLeast(1)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        exercise.play.cells.chunked(columns).forEachIndexed { row, labels ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                labels.forEachIndexed { column, label ->
                    val index = row * columns + column
                    Button(
                        onClick = {
                            val opened = first
                            if (opened == null) {
                                first = index
                            } else {
                                val matched = boards.isPair(exercise.play, opened, index)
                                first = null
                                val wrong = (exercise.correctIndex + 1) % exercise.options.size
                                onPick(if (matched) exercise.correctIndex else wrong)
                            }
                        },
                        modifier = Modifier.weight(1f).height(64.dp),
                    ) {
                        Text(if (first == index) label else "?")
                    }
                }
            }
        }
    }
}
