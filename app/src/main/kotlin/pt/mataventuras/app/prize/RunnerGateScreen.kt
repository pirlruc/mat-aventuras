package pt.mataventuras.app.prize

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlin.random.Random
import pt.mataventuras.domain.prize.GateRound
import pt.mataventuras.domain.prize.MathGate

/**
 * Ages 6–8. Run through the gate that shows the sum.
 */
@Composable
fun RunnerGateScreen(
    random: Random,
    onFinished: () -> Unit,
) {
    val round = remember { MathGate.round(random) }
    Column {
        Text(round.prompt)
        round.labels.forEachIndexed { index, label ->
            Button(
                onClick = { finishGate(round, index, onFinished) },
                modifier = Modifier.prizeMinHeight().testTag("gate-$index"),
            ) {
                Canvas(Modifier.size(28.dp)) { drawCircle(Color(0xFFFDD835)) }
                Text(label)
            }
        }
    }
}

internal fun finishGate(
    round: GateRound,
    index: Int,
    onFinished: () -> Unit,
) {
    if (MathGate.passed(round, index)) onFinished()
}
