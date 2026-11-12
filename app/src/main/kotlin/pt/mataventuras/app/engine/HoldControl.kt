package pt.mataventuras.app.engine

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * One labelled control that holds [onPress] until the finger lifts.
 */
internal data class HoldControl(
    val label: String,
    val onPress: () -> Unit,
    val onRelease: () -> Unit,
)

/**
 * On-screen prize buttons. A tap is not enough: the press lasts until release.
 */
@Composable
internal fun HoldRow(
    controls: List<HoldControl>,
    modifier: Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        controls.forEach { control ->
            Button(
                onClick = {
                    control.onPress()
                    control.onRelease()
                },
                modifier =
                    Modifier.heightIn(min = 64.dp).pointerInput(control.label) {
                        detectTapGestures(
                            onPress = {
                                control.onPress()
                                tryAwaitRelease()
                                control.onRelease()
                            },
                        )
                    },
            ) {
                Text(control.label)
            }
        }
    }
}
