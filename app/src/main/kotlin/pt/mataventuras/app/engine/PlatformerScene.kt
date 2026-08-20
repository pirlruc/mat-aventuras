package pt.mataventuras.app.engine

import pt.mataventuras.domain.engine.Platformer2dState
import pt.mataventuras.domain.engine.PlatformerWorld
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
 * Axis-aligned brick, coin, or body tile in pixel space.
 */
internal data class PlatformerRect(
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float,
    val argb: Long,
)

/**
 * Canvas layout for the age-3 Game Boy-style platformer.
 */
internal object PlatformerScene {
    const val SKY_ARGB: Long = 0xFF7EC0ED
    const val SKY_BAND_ARGB: Long = 0xFF5BA3D9
    const val BRICK_ARGB: Long = 0xFFC75A1A
    const val MORTAR_ARGB: Long = 0xFF5D2E0A
    const val GRASS_ARGB: Long = 0xFF3D9E2F
    const val PIT_ARGB: Long = 0xFF1A0A08
    const val COIN_ARGB: Long = 0xFFFFD54F
    private const val SCALE_X: Float = 40f
    private const val SCALE_Y: Float = 12f

    /**
     * Sky, bricks, runner, and coins in pixel space.
     */
    fun sprites(
        state: Platformer2dState,
        mascot: Mascot,
        width: Float,
        height: Float,
    ): List<PlatformerSprite> {
        val tiles = ArrayList<PlatformerRect>(24)
        fillTiles(tiles, state, mascot, width, height)
        return tiles.map { tile ->
            PlatformerSprite(
                tile.x + tile.w / 2f,
                tile.y + tile.h / 2f,
                minOf(tile.w, tile.h) / 2f,
                tile.argb,
            )
        }
    }

    /**
     * Clears [out] and writes bricks, pit, coins, and the mascot-coloured body.
     * Reusing [out] avoids a per-frame list allocation in the reward loop.
     */
    fun fillTiles(
        out: MutableList<PlatformerRect>,
        state: Platformer2dState,
        mascot: Mascot,
        width: Float,
        height: Float,
    ) {
        out.clear()
        val groundY = groundTop(height)
        val camera = state.x * SCALE_X - width * 0.3f
        addGround(out, camera, width, groundY, height)
        addLedges(out, camera, groundY)
        addCoins(out, state, camera, groundY)
        addPlayer(out, mascot, width, groundY, state.y)
    }

    /**
     * Ground band top-left Y in pixels.
     */
    fun groundTop(height: Float): Float = height * 0.75f

    /**
     * Darker hat/boots colour from the mascot fill.
     */
    fun hatArgb(primary: Long): Long {
        val r = (((primary shr 16) and 0xFF) * 65 / 100).coerceAtMost(255)
        val g = (((primary shr 8) and 0xFF) * 65 / 100).coerceAtMost(255)
        val b = ((primary and 0xFF) * 65 / 100).coerceAtMost(255)
        return (0xFFL shl 24) or (r shl 16) or (g shl 8) or b
    }

    private fun addGround(
        out: MutableList<PlatformerRect>,
        camera: Float,
        width: Float,
        groundY: Float,
        height: Float,
    ) {
        val pitLeft = worldX(PlatformerWorld.PIT_LEFT, camera)
        val pitRight = worldX(PlatformerWorld.PIT_RIGHT, camera)
        out.add(PlatformerRect(0f, groundY, pitLeft.coerceAtLeast(0f), 16f, GRASS_ARGB))
        val rightStart = pitRight.coerceAtLeast(0f)
        if (rightStart < width) {
            out.add(PlatformerRect(rightStart, groundY, width - rightStart, 16f, GRASS_ARGB))
        }
        out.add(PlatformerRect(pitLeft, groundY, pitRight - pitLeft, height - groundY, PIT_ARGB))
        addBrickRow(out, camera, width, groundY + 16f, height - groundY - 16f, pitLeft, pitRight)
    }

    private fun addBrickRow(
        out: MutableList<PlatformerRect>,
        camera: Float,
        width: Float,
        top: Float,
        brickH: Float,
        pitLeft: Float,
        pitRight: Float,
    ) {
        var x = -((camera % 24f) + 24f)
        while (x < width) {
            val skip = x + 24f > pitLeft && x < pitRight
            if (!skip) {
                out.add(PlatformerRect(x, top, 22f, brickH.coerceAtLeast(8f), BRICK_ARGB))
                out.add(PlatformerRect(x + 22f, top, 2f, brickH.coerceAtLeast(8f), MORTAR_ARGB))
            }
            x += 24f
        }
    }

    private fun addLedges(
        out: MutableList<PlatformerRect>,
        camera: Float,
        groundY: Float,
    ) {
        PlatformerWorld.LEDGES.forEach { ledge ->
            val x = worldX(ledge[0], camera)
            val y = groundY - ledge[1] * SCALE_Y - 12f
            out.add(PlatformerRect(x, y, ledge[2] * SCALE_X, 14f, BRICK_ARGB))
        }
    }

    private fun addCoins(
        out: MutableList<PlatformerRect>,
        state: Platformer2dState,
        camera: Float,
        groundY: Float,
    ) {
        PlatformerWorld.COIN_X.forEachIndexed { i, coinX ->
            if ((state.collectedMask shr i) and 1 == 1) return@forEachIndexed
            val x = worldX(coinX, camera)
            out.add(PlatformerRect(x, groundY - 28f, 16f, 16f, COIN_ARGB))
        }
    }

    private fun addPlayer(
        out: MutableList<PlatformerRect>,
        mascot: Mascot,
        width: Float,
        groundY: Float,
        worldY: Float,
    ) {
        val px = width * 0.3f
        val py = groundY - worldY * SCALE_Y - 40f
        out.add(PlatformerRect(px, py + 10f, 28f, 30f, mascot.primaryArgb))
        out.add(PlatformerRect(px + 4f, py, 20f, 12f, hatArgb(mascot.primaryArgb)))
    }

    private fun worldX(
        world: Float,
        camera: Float,
    ): Float = world * SCALE_X - camera
}
