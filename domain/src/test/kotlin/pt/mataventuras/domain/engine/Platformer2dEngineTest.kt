package pt.mataventuras.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Platformer2dEngineTest {
    @Test
    fun jumpAndCollectFinishTheRound() {
        val engine = Platformer2dEngine()
        var state = engine.initial(ringsTarget = 1)
        state = engine.step(state, 0.1f, jumping = true)
        assertFalse(state.onGround)
        state = engine.collect(state, ringX = state.x)
        assertTrue(state.finished)
        val idle = engine.step(state, 0.1f, jumping = false)
        assertEquals(state, idle)
        val far = engine.collect(engine.initial(1), ringX = 50f)
        assertEquals(0, far.rings)
        val dead = engine.initial().copy(alive = false)
        assertEquals(dead, engine.step(dead, 0.1f, jumping = true))
        assertEquals(dead, engine.collect(dead, 0f))
    }

    @Test
    fun landingReturnsToTheGround() {
        val engine = Platformer2dEngine()
        var state = engine.initial()
        state = engine.step(state, 0.05f, jumping = true)
        repeat(40) {
            state = engine.step(state, 0.05f, jumping = false)
        }
        assertTrue(state.onGround)
        assertTrue(state.alive)
    }

    @Test
    fun pitJumpInAirAndFinishedCollect() {
        val engine = Platformer2dEngine()
        var airborne = engine.step(engine.initial(), 0.05f, jumping = true)
        airborne = engine.step(airborne, 0.05f, jumping = true)
        assertFalse(airborne.onGround)
        val rooted = engine.step(engine.initial(), 0.05f, jumping = true, moveX = 0f)
        assertTrue(rooted.onGround)
        val ready = engine.initial(ringsTarget = 1).copy(rings = 1, finished = false)
        val closed = engine.step(ready, 0.01f, jumping = false)
        assertTrue(closed.finished)
        assertEquals(closed, engine.collect(closed, 0f))
        var falling = engine.initial().copy(x = Platformer2dEngine.PIT_X + 1f, onGround = true)
        repeat(4) { falling = engine.step(falling, 0.05f, jumping = false, moveX = 1f) }
        assertTrue(falling.inPitFall)
        assertTrue(falling.x < PlatformerWorld.PIT_RIGHT + 1f)
        repeat(40) { falling = engine.step(falling, 0.05f, jumping = false, moveX = 0f) }
        assertTrue(falling.alive)
        assertFalse(falling.inPitFall)
        assertTrue(falling.onGround)
        assertTrue(falling.x < PlatformerWorld.PIT_LEFT)
        val short = engine.collect(engine.initial(), ringX = 0.5f, radius = 0.1f)
        assertEquals(0, short.rings)
        var tagged = engine.collect(engine.initial(), ringX = 0f, coinIndex = 0)
        assertEquals(1, tagged.rings)
        tagged = engine.collect(tagged, ringX = 0f, coinIndex = 0)
        assertEquals(1, tagged.rings)
        tagged = engine.collect(tagged, ringX = 0f, coinIndex = 1)
        assertEquals(2, tagged.rings)
        var ledge = engine.initial().copy(x = 16f)
        ledge = engine.step(ledge, 0.05f, jumping = true)
        repeat(24) { ledge = engine.step(ledge, 0.05f, jumping = false) }
        assertTrue(ledge.onGround)
        assertTrue(ledge.y >= 3f || ledge.alive)
        assertEquals(28f, PlatformerWorld.PIT_LEFT)
        assertEquals(2, PlatformerWorld.LEDGES.size)
        assertEquals(5, PlatformerWorld.COIN_X.size)
        val random = PlatformerWorld.random(4)
        assertTrue(random.pits.size >= 3)
        assertTrue(random.coins.size >= 8)
        assertTrue(random.inPit(random.pits[0].left + 0.5f))
        assertFalse(random.inPit(0f))
        assertTrue(random.lastSafeX(random.pits[0].right) < random.pits[0].left + 1f)
        val themed = PlatformerWorld.random(1)
        assertTrue(themed.skyArgb != PlatformerWorld.random(2).skyArgb || themed.grassArgb != 0L)
    }
}
