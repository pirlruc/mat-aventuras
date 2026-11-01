package pt.mataventuras.app.ui.lesson

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Big or small circle. The word is only the content description.
 */
@Composable
internal fun SizeMark(word: String) {
    val big = word == "Grande"
    Canvas(
        modifier =
            Modifier
                .size(if (big) 72.dp else 28.dp)
                .semantics { contentDescription = word },
    ) {
        drawCircle(Color(0xFF1565C0))
    }
}
