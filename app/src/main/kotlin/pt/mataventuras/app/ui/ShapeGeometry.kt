package pt.mataventuras.app.ui

import kotlin.math.cos
import kotlin.math.sin

/**
 * Vertices of an upward triangle centred on ([cx], [cy]).
 */
internal fun triangleVertices(
    cx: Float,
    cy: Float,
    radius: Float,
): List<Pair<Float, Float>> =
    listOf(
        cx to cy - radius,
        cx - radius to cy + radius,
        cx + radius to cy + radius,
    )

/**
 * 10-point star polygon centred on ([cx], [cy]).
 */
internal fun starVertices(
    cx: Float,
    cy: Float,
    radius: Float,
): List<Pair<Float, Float>> {
    val inner = radius * 0.4f
    return List(10) { i ->
        val angle = (Math.PI / 2.0) + i * Math.PI / 5.0
        val rad = if (i % 2 == 0) radius else inner
        cx + (cos(angle) * rad).toFloat() to cy - (sin(angle) * rad).toFloat()
    }
}

/**
 * Star centres inside a square of [minDimension] pixels.
 */
internal fun starCenters(
    count: Int,
    minDimension: Float,
): List<Pair<Float, Float>> {
    val columns = 5
    val step = minDimension / 6f
    return List(count) { i ->
        (i % columns) * step + step to (i / columns) * step + step
    }
}
