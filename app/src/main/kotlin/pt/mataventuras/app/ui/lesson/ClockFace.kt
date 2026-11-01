package pt.mataventuras.app.ui.lesson

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Twelve-hour face. The prompt does not say the hour; the hand does.
 */
@Composable
internal fun ClockFace(hour: Int) {
    Canvas(
        modifier =
            Modifier
                .size(160.dp)
                .testTag("clock-face")
                .semantics { contentDescription = "Relógio" },
    ) {
        paintClockFace(hour)
    }
}

/**
 * Hour on a clock label such as `3:00`. Unknown text is 12.
 */
internal fun clockHour(label: String): Int = label.substringBefore(':').toIntOrNull()?.coerceIn(1, 12) ?: 12

internal fun DrawScope.paintClockFace(hour: Int) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val radius = size.minDimension / 2.2f
    drawCircle(Color.White, radius = radius, center = Offset(cx, cy))
    drawCircle(Color(0xFF1565C0), radius = radius, center = Offset(cx, cy), style = Stroke(width = 6f))
    val shown = ((hour - 1).mod(12)) + 1
    val angle = Math.toRadians((shown % 12) * 30.0 - 90.0)
    val length = radius * 0.55f
    drawLine(
        color = Color(0xFF0D47A1),
        start = Offset(cx, cy),
        end = Offset(cx + (cos(angle) * length).toFloat(), cy + (sin(angle) * length).toFloat()),
        strokeWidth = 8f,
    )
}
