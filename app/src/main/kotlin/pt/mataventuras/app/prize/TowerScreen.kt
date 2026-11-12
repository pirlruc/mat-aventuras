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
import pt.mataventuras.domain.prize.TowerDefence
import pt.mataventuras.domain.prize.TowerRound

/**
 * Ages 6–8. Tap the sum that stops the wave.
 */
@Composable
fun TowerScreen(
    random: Random,
    onFinished: () -> Unit,
) {
    val round = remember { TowerDefence.round(random) }
    Column {
        Text(round.prompt)
        round.labels.forEachIndexed { index, label ->
            Button(
                onClick = { finishTower(round, index, onFinished) },
                modifier = Modifier.prizeMinHeight().testTag("tower-$index"),
            ) {
                Canvas(Modifier.size(28.dp)) { drawCircle(Color(0xFFE53935)) }
                Text(label)
            }
        }
    }
}

internal fun finishTower(
    round: TowerRound,
    index: Int,
    onFinished: () -> Unit,
) {
    if (TowerDefence.stopped(round, index)) onFinished()
}
