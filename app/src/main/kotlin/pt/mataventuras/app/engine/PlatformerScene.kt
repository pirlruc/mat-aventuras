package pt.mataventuras.app.engine

import pt.mataventuras.domain.engine.Platformer2dState
import pt.mataventuras.domain.engine.PlatformerLevel
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
        level: PlatformerLevel = PlatformerWorld.DEFAULT,
    ): List<PlatformerSprite> {
        val tiles = ArrayList<PlatformerRect>(24)
        fillTiles(tiles, state, mascot, width, height, level)
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
        level: PlatformerLevel = PlatformerWorld.DEFAULT,
    ) {
        out.clear()
        val groundY = groundTop(height)
        val camera = state.x * SCALE_X - width * 0.3f
        addGround(out, camera, width, groundY, height, level)
        addLedges(out, camera, groundY, level)
        addCoins(out, state, camera, groundY, level)
        addHazards(out, state, camera, groundY, level)
        addPlayer(out, mascot, width, groundY, state.y, state.form)
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
        level: PlatformerLevel,
    ) {
        out.add(PlatformerRect(0f, groundY, width, 16f, level.grassArgb))
        level.pits.forEach { pit ->
            val left = worldX(pit.left, camera)
            val span = ((pit.right - pit.left) * SCALE_X).coerceAtLeast(8f)
            out.add(PlatformerRect(left, groundY, span, height - groundY, level.pitArgb))
        }
        addBrickRow(out, camera, width, groundY + 16f, height - groundY - 16f, level)
    }

    private fun addBrickRow(
        out: MutableList<PlatformerRect>,
        camera: Float,
        width: Float,
        top: Float,
        brickH: Float,
        level: PlatformerLevel,
    ) {
        var x = -((camera % 24f) + 24f)
        val h = brickH.coerceAtLeast(8f)
        while (x < width) {
            val world = (x + camera) / SCALE_X
            if (!level.inPit(world + 0.3f)) {
                out.add(PlatformerRect(x, top, 22f, h, level.brickArgb))
                out.add(PlatformerRect(x + 22f, top, 2f, h, MORTAR_ARGB))
            }
            x += 24f
        }
    }

    private fun addLedges(
        out: MutableList<PlatformerRect>,
        camera: Float,
        groundY: Float,
        level: PlatformerLevel,
    ) {
        level.ledges.forEach { ledge ->
            val x = worldX(ledge.x, camera)
            val y = groundY - ledge.y * SCALE_Y - 12f
            out.add(PlatformerRect(x, y, ledge.width * SCALE_X, 14f, level.brickArgb))
        }
    }

    private fun addCoins(
        out: MutableList<PlatformerRect>,
        state: Platformer2dState,
        camera: Float,
        groundY: Float,
        level: PlatformerLevel,
    ) {
        level.coins.forEachIndexed { i, coinX ->
            if ((state.collectedMask shr i) and 1 == 1) return@forEachIndexed
            val x = worldX(coinX, camera)
            out.add(PlatformerRect(x, groundY - 36f, 14f, 14f, COIN_ARGB))
            out.add(PlatformerRect(x + 4f, groundY - 32f, 6f, 6f, 0xFFFFF59D))
        }
    }

    private fun addHazards(
        out: MutableList<PlatformerRect>,
        state: Platformer2dState,
        camera: Float,
        groundY: Float,
        level: PlatformerLevel,
    ) {
        level.powerups.forEachIndexed { i, item ->
            if ((state.powerMask shr i) and 1 == 1) return@forEachIndexed
            val x = worldX(item.x, camera)
            val color = if (item.grow) 0xFFE53935 else 0xFFFFF176
            out.add(PlatformerRect(x, groundY - 40f, 16f, 16f, color))
        }
        level.enemies.forEachIndexed { i, enemy ->
            if ((state.stompedMask shr i) and 1 == 1) return@forEachIndexed
            val x = worldX(pt.mataventuras.domain.engine.PlatformerHazards.enemyX(enemy, state.elapsed), camera)
            out.add(PlatformerRect(x, groundY - 22f, 20f, 18f, 0xFF6D4C41))
        }
    }

    private fun addPlayer(
        out: MutableList<PlatformerRect>,
        mascot: Mascot,
        width: Float,
        groundY: Float,
        worldY: Float,
        form: Int = 0,
    ) {
        val fill = if (form >= 2) 0xFFFFF176 else mascot.primaryArgb
        val shade = hatArgb(fill)
        val px = width * 0.3f
        val py = groundY - worldY * SCALE_Y - 52f
        out.add(PlatformerRect(px + 10f, py + 20f, 16f, 22f, fill))
        out.add(PlatformerRect(px + 8f, py + 4f, 20f, 18f, fill))
        out.add(PlatformerRect(px + 6f, py, 24f, 10f, shade))
        out.add(PlatformerRect(px + 12f, py + 10f, 4f, 4f, 0xFF212121))
        out.add(PlatformerRect(px + 20f, py + 10f, 4f, 4f, 0xFF212121))
        out.add(PlatformerRect(px + 10f, py + 40f, 6f, 14f, shade))
        out.add(PlatformerRect(px + 20f, py + 40f, 6f, 14f, shade))
        out.add(PlatformerRect(px + 8f, py + 52f, 10f, 6f, 0xFF4E342E))
        out.add(PlatformerRect(px + 20f, py + 52f, 10f, 6f, 0xFF4E342E))
    }

    private fun worldX(
        world: Float,
        camera: Float,
    ): Float = world * SCALE_X - camera
}
