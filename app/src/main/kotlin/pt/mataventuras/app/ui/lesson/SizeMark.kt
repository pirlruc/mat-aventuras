package pt.mataventuras.app.ui.lesson

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
    val glyph = if (big) 72.dp else 28.dp
    val hit = if (big) 72.dp else 48.dp
    Box(
        modifier =
            Modifier
                .size(hit)
                .semantics { contentDescription = spoken },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(glyph)) {
            drawCircle(Color(0xFF1565C0))
        }
    }
}
