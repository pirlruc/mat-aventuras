package pt.mataventuras.app.prize

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import pt.mataventuras.app.DEBUG_WIN_TAG

/**
 * Debug-only control Maestro taps after Jogar. The tag is absent from release builds.
 */
@Composable
internal fun DebugWinButton(onWin: () -> Unit) {
    Button(
        onClick = onWin,
        modifier =
            Modifier
                .testTag(DEBUG_WIN_TAG)
                .semantics { testTagsAsResourceId = true },
    ) {
        Text(DEBUG_WIN_TAG)
    }
}
