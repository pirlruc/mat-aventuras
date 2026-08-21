package pt.mataventuras.app.engine

import pt.mataventuras.domain.engine.OffroadCircuit
import pt.mataventuras.domain.engine.OffroadState
import pt.mataventuras.domain.model.Mascot

/**
 * One filled strip or car brick in the 2.5D dirt-racer viewport.
 */
internal data class OffroadSpan(
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float,
    val argb: Long,
)

/**
 * Scanline-style rear view: hills, grass, rumble, dirt, gates, and a kart sprite.
 */
internal object OffroadScene {
    private val SKY: LongArray = longArrayOf(0xFF81D4FA, 0xFFFFCC80, 0xFFFF8A65, 0xFF1A237E)
    private val HAZE: LongArray = longArrayOf(0xFF4FC3F7, 0xFFFFB74D, 0xFFE64A19, 0xFF0D47A1)
    private val MOUNTAIN: LongArray = longArrayOf(0xFF2E7D32, 0xFF6D4C41, 0xFF4E342E, 0xFF263238)
    private val GRASS: LongArray = longArrayOf(0xFF43A047, 0xFFD4E157, 0xFF6D4C41, 0xFF263238)
    private val DIRT: LongArray = longArrayOf(0xFF8D6E63, 0xFFBCAAA4, 0xFF5D4037, 0xFF4E342E)
    private val LINE: LongArray = longArrayOf(0xFFFFF59D, 0xFFFFFDE7, 0xFFFFE082, 0xFFEEEEEE)
    const val STRIP_COUNT: Int = 28
    const val FLAME_ARGB: Long = 0xFFFF6F00
    const val HEADLAMP_ARGB: Long = 0xFFFFF176
    const val POST_ARGB: Long = 0xFF5D4037
    const val BANNER_ARGB: Long = 0xFFFFD54F

    /**
     * Sky colour for [circuit.palette].
     */
    fun skyArgb(circuit: OffroadCircuit): Long = SKY[circuit.palette]

    /**
     * Clears [out] and writes far-to-near road strips plus the kart.
     */
    fun fill(
        out: MutableList<OffroadSpan>,
        state: OffroadState,
        circuit: OffroadCircuit,
        mascot: Mascot,
        width: Float,
        height: Float,
    ) {
        out.clear()
        val horizon = height * 0.34f
        val ground = height * 0.94f
        addHorizon(out, state, circuit, width, horizon)
        for (i in STRIP_COUNT - 1 downTo 0) {
            addStrip(out, state, circuit, width, horizon, ground, i)
        }
        addGates(out, state, circuit, width, horizon, ground)
        addKart(out, mascot, width, ground, state)
    }

    private fun addHorizon(
        out: MutableList<OffroadSpan>,
        state: OffroadState,
        circuit: OffroadCircuit,
        width: Float,
        horizon: Float,
    ) {
        val pal = circuit.palette
        out.add(OffroadSpan(0f, horizon - 48f, width, 52f, HAZE[pal]))
        for (i in 0 until 4) {
            val dist = state.distance + i * 55f
            val x = width * (0.08f + i * 0.24f) + circuit.curveAt(dist) * 28f
            val peak = 26f + kotlin.math.abs(circuit.hillAt(dist)) * 34f
            out.add(OffroadSpan(x, horizon - peak, 88f, peak, MOUNTAIN[pal]))
        }
    }

    private fun addStrip(
        out: MutableList<OffroadSpan>,
        state: OffroadState,
        circuit: OffroadCircuit,
        width: Float,
        horizon: Float,
        ground: Float,
        index: Int,
    ) {
        val t = index / STRIP_COUNT.toFloat()
        val y = ground - (ground - horizon) * t
        val h = ((ground - horizon) / STRIP_COUNT).coerceAtLeast(2f)
        val scale = 1.55f / (0.35f + t * 2.4f)
        val dist = state.distance + 6f + t * 110f
        val curve = circuit.curveAt(dist)
        val hill = circuit.hillAt(dist) * (1f - t) * 36f
        val center = width * 0.5f - state.lateral * 95f * scale + curve * 62f * (1f - t)
        val roadW = 360f * scale * circuit.widthAt(dist)
        val pal = circuit.palette
        out.add(OffroadSpan(0f, y - hill, width, h + 1f, GRASS[pal]))
        out.add(OffroadSpan(center - roadW * 0.5f - 10f, y - hill, roadW + 20f, h + 1f, 0xFFE53935))
        out.add(OffroadSpan(center - roadW * 0.5f, y - hill, roadW, h + 1f, DIRT[pal]))
        if (index % 2 == 0) {
            out.add(OffroadSpan(center - 4f, y - hill, 8f, h, LINE[pal]))
        }
    }

    private fun addGates(
        out: MutableList<OffroadSpan>,
        state: OffroadState,
        circuit: OffroadCircuit,
        width: Float,
        horizon: Float,
        ground: Float,
    ) {
        for (i in 0 until circuit.gateCount) {
            if ((state.collectedMask shr i) and 1 == 1) continue
            val ahead = circuit.gateDistance(i) - state.distance
            val wrapped = if (ahead < 0f) ahead + circuit.length else ahead
            if (wrapped > 90f) continue
            val t = (wrapped / 90f).coerceIn(0f, 1f)
            val y = ground - (ground - horizon) * t - 20f
            val scale = 1.4f / (0.4f + t * 2f)
            val x = width * 0.5f - 18f * scale
            val barW = 36f * scale
            val barH = 10f * scale
            val postW = 6f * scale
            val postH = 26f * scale
            out.add(OffroadSpan(x, y - postH, postW, postH, POST_ARGB))
            out.add(OffroadSpan(x + barW - postW, y - postH, postW, postH, POST_ARGB))
            out.add(OffroadSpan(x, y, barW, barH, BANNER_ARGB))
        }
    }

    private fun addKart(
        out: MutableList<OffroadSpan>,
        mascot: Mascot,
        width: Float,
        ground: Float,
        state: OffroadState,
    ) {
        val cx = width * 0.5f + state.steer * 18f
        val y = ground - 78f
        val fill = mascot.primaryArgb
        out.add(OffroadSpan(cx - 36f, y + 40f, 20f, 18f, 0xFF212121))
        out.add(OffroadSpan(cx + 16f, y + 40f, 20f, 18f, 0xFF212121))
        out.add(OffroadSpan(cx - 30f, y + 16f, 60f, 30f, fill))
        out.add(OffroadSpan(cx - 8f, y + 20f, 16f, 22f, 0xFFFFF59D))
        out.add(OffroadSpan(cx - 18f, y - 2f, 36f, 24f, 0xFFECEFF1))
        out.add(OffroadSpan(cx - 24f, y - 12f, 48f, 12f, fill))
        out.add(OffroadSpan(cx - 20f, y - 20f, 8f, 16f, fill))
        out.add(OffroadSpan(cx + 12f, y - 20f, 8f, 16f, fill))
        out.add(OffroadSpan(cx - 26f, y + 12f, 10f, 8f, HEADLAMP_ARGB))
        out.add(OffroadSpan(cx + 16f, y + 12f, 10f, 8f, HEADLAMP_ARGB))
        if (state.boostTimer > 0f) {
            out.add(OffroadSpan(cx - 10f, y + 52f, 20f, 16f, FLAME_ARGB))
            out.add(OffroadSpan(cx - 28f, y + 58f, 12f, 10f, 0xFFFFCC80))
            out.add(OffroadSpan(cx + 16f, y + 58f, 12f, 10f, 0xFFFFCC80))
        }
    }
}
