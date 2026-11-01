package pt.mataventuras.app.ui.lesson

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.mataventuras.app.ui.UiLogic
import pt.mataventuras.domain.math.Exercise

/**
 * Missing-letter word with its own letter buttons.
 */
@Composable
internal fun WordCompleteBoard(
    exercise: Exercise,
    onPick: (Int) -> Unit,
) {
    val shown = exercise.play.cells.firstOrNull().orEmpty()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = shown,
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.testTag("word-complete"),
        )
        exercise.options.forEachIndexed { index, letter ->
            Button(
                onClick = { onPick(index) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .testTag(UiLogic.optionAnswerTag(exercise.isCorrect(index), false)),
            ) {
                Text(letter, fontWeight = FontWeight.Bold, fontSize = 28.sp)
            }
        }
    }
}
