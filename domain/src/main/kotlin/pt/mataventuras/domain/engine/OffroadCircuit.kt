package pt.mataventuras.domain.engine

import kotlin.random.Random

/**
 * One randomised Super Off Road-style circuit. Distance is metres along the track.
 */
class OffroadCircuit(
    val seed: Int,
) {
    private val rng: Random = Random(seed)
    private val curves: FloatArray = FloatArray(SEGMENT_COUNT) { rng.nextFloat() * 1.7f - 0.85f }
    private val hills: FloatArray = FloatArray(SEGMENT_COUNT) { rng.nextFloat() * 1.1f - 0.35f }
    private val widths: FloatArray = FloatArray(SEGMENT_COUNT) { 0.82f + rng.nextFloat() * 0.28f }

    /** Loop length in metres. */
    val length: Float = LENGTH

    /** 0..3 palette: meadow, desert, dusk, night. */
    val palette: Int = kotlin.math.abs(seed) % 4

    /** Checkpoints that count as gates. */
    val gateCount: Int = GATE_COUNT

    /**
     * Road curvature at [distance] (left negative).
     */
    fun curveAt(distance: Float): Float = sample(curves, distance)

    /**
     * Hill height at [distance].
     */
    fun hillAt(distance: Float): Float = sample(hills, distance)

    /**
     * Road width multiplier at [distance].
     */
    fun widthAt(distance: Float): Float = sample(widths, distance)

    /**
     * Distance of gate [index] on one lap.
     */
    fun gateDistance(index: Int): Float = length * (index + 1f) / (gateCount + 1f)

    private fun sample(
        values: FloatArray,
        distance: Float,
    ): Float {
        val wrapped = ((distance % length) + length) % length
        val t = wrapped / length * values.size
        val i = t.toInt() % values.size
        val j = (i + 1) % values.size
        val f = t - t.toInt()
        return values[i] + (values[j] - values[i]) * f
    }

    private companion object {
        const val LENGTH: Float = 480f
        const val SEGMENT_COUNT: Int = 24
        const val GATE_COUNT: Int = 8
    }
}
