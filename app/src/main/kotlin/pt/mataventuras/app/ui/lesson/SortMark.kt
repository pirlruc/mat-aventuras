package pt.mataventuras.app.ui.lesson

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import pt.mataventuras.domain.model.GeometricShape

/**
 * A colour circle or a shape. The name is the content description.
 */
@Composable
internal fun SortMark(label: String) {
    val shape = GeometricShape.entries.firstOrNull { it.displayName == label }
    Canvas(
        modifier =
            Modifier
                .size(56.dp)
                .semantics { contentDescription = label },
    ) {
        drawSortMark(shape, label)
    }
}

internal fun DrawScope.drawSortMark(
    shape: GeometricShape?,
    label: String,
) {
    if (shape != null) {
        paintShapeGlyph(shape)
    } else {
        drawCircle(colourOf(label))
    }
}

internal fun colourOf(label: String): Color =
    when (label) {
        "vermelho" -> Color(0xFFE53935)
        "azul" -> Color(0xFF1E88E5)
        "amarelo" -> Color(0xFFFDD835)
        "verde" -> Color(0xFF43A047)
        else -> Color(0xFF90A4AE)
    }
