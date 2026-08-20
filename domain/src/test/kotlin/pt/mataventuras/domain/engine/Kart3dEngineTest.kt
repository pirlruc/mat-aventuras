package pt.mataventuras.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Kart3dEngineTest {
    @Test
    fun boostCompletesTheTargetLaps() {
        val engine = Kart3dEngine(acceleration = 80f, drag = 0f, lapLength = 10f)
        var state = engine.initial(lapsTarget = 1)
        repeat(20) {
            state = engine.step(state, 0.2f, boost = true)
        }
        assertTrue(state.finished)
        assertEquals(state, engine.step(state, 0.1f, boost = false))
    }

    @Test
    fun withoutBoostTheKartStaysSlow() {
        val engine = Kart3dEngine()
        val next = engine.step(engine.initial(), 0.1f, boost = false)
        assertFalse(next.finished)
        assertEquals(0, next.answerBoosts)
        assertTrue(next.speed >= 2f)
    }
}
