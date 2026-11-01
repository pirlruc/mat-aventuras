package pt.mataventuras.app.ui.lesson

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pt.mataventuras.domain.math.Exercise
import pt.mataventuras.domain.math.MemoryFace

/**
 * Every pair stays open. The lesson advances only when the board is finished.
 */
@Composable
internal fun MemoryBoard(
    exercise: Exercise,
    onPick: (Int) -> Unit,
) {
    var face by remember(exercise) { mutableStateOf(MemoryFace()) }
    val solved = face.solved(exercise.play.cells.size)
    LaunchedEffect(solved) {
        if (solved) onPick(exercise.correctIndex)
    }
    val columns = exercise.play.columns.coerceAtLeast(1)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        exercise.play.cells.chunked(columns).forEachIndexed { row, labels ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                labels.forEachIndexed { column, label ->
                    val index = row * columns + column
                    val open = index in face.matched || index in face.up
                    Button(
                        onClick = { face = face.reveal(exercise.play, index) },
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .semantics(mergeDescendants = true) {
                                contentDescription = if (open) label else "carta fechada"
                            },
                    ) {
                        Text(if (open) label else "?", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
