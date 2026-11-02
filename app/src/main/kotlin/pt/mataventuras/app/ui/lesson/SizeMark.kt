package pt.mataventuras.app.ui.lesson

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import pt.mataventuras.app.R

/**
 * Big or small circle. The content description does not name the size.
 */
@Composable
internal fun SizeMark(
    word: String,
    option: Int,
) {
    val big = word == "Grande"
    val spoken = stringResource(R.string.option_n, option)
    Canvas(
        modifier =
            Modifier
                .size(if (big) 72.dp else 28.dp)
                .semantics { contentDescription = spoken },
    ) {
        drawCircle(Color(0xFF1565C0))
    }
}
