package pt.mataventuras.app.engine

import pt.mataventuras.domain.engine.Platformer2dState
import pt.mataventuras.domain.model.Mascot

/**
 * One filled circle in the 2D reward viewport.
 */
internal data class PlatformerSprite(
    val x: Float,
    val y: Float,
    val radius: Float,
    val argb: Long,
)

/**
 * Canvas layout for the age-3 runner. Geometry stays out of the Activity lambda.
 */
internal object PlatformerScene {
    /**
     * Sky, ground, runner, and five deco rings in pixel space.
     */
    fun sprites(
        state: Platformer2dState,
        mascot: Mascot,
        width: Float,
        height: Float,
    ): List<PlatformerSprite> {
        val groundY = height * 0.75f
        val px = (state.x * 40f) % (width + 80f)
        val py = groundY - state.y * 12f - 40f
        val deco =
            (0 until 5).map { i ->
                val ax = ((i * 180f) - (state.x * 20f) + width * 4) % width
                PlatformerSprite(ax, height * 0.55f, 18f, 0xFFFFD54F)
            }
        return listOf(PlatformerSprite(px, py, 36f, mascot.primaryArgb)) + deco
    }

    /**
     * Ground band top-left Y in pixels.
     */
    fun groundTop(height: Float): Float = height * 0.75f
}
