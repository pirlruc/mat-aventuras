package pt.mataventuras.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OffroadRacerEngineTest {
    private val circuit = OffroadCircuit(11)
    private val engine = OffroadRacerEngine(circuit)

    @Test
    fun cruiseCompletesLapsAndCollectsGates() {
        var state = engine.initial(lapsTarget = 1)
        repeat(2_000) {
            state = engine.step(state, 0.05f, steer = 0f, boost = false)
        }
        assertTrue(state.finished)
        assertTrue(state.laps >= 1)
        val frozen = engine.step(state, 0.05f, steer = 1f, boost = true)
        assertEquals(state, frozen)
        assertTrue(circuit.palette in 0..3)
        assertTrue(circuit.length > 100f)
        assertTrue(circuit.gateDistance(0) > 0f)
        assertTrue(circuit.curveAt(0f) != circuit.curveAt(120f) || circuit.hillAt(10f) != 0f)
    }

    @Test
    fun boostRaisesSpeedAndHud() {
        val idle = engine.initial()
        val boosted = engine.step(idle, 0.05f, steer = 0f, boost = true)
        assertEquals(1, boosted.answerBoosts)
        assertTrue(boosted.boostTimer > 0f)
        assertEquals("Impulso!", KartHud.boostLabel(boosted))
        assertTrue(KartHud.lapLabel(boosted).startsWith("Volta"))
        assertTrue(KartHud.gatesLabel(boosted).contains("Portões"))
        repeat(20) {
            val next = engine.step(boosted, 0.05f, 0f, false)
            if (next.speed > idle.speed) {
                assertTrue(next.speed > idle.speed)
                return
            }
        }
    }

    @Test
    fun hardSteerLeavesTheDirt() {
        var state = engine.initial()
        var left = false
        repeat(80) {
            state = engine.step(state, 0.05f, steer = -1f, boost = true)
            if (state.offTrack) left = true
        }
        assertTrue(left)
        assertEquals("Volta à pista!", KartHud.offTrackLabel(state))
    }

    @Test
    fun touchBandsSteerAndBoost() {
        assertEquals(-1f, engine.steerFromTouch(0.1f), 0f)
        assertEquals(1f, engine.steerFromTouch(0.9f), 0f)
        assertEquals(0f, engine.steerFromTouch(0.5f), 0f)
        assertTrue(engine.isBoostTouch(0.5f))
        assertFalse(engine.isBoostTouch(0.1f))
        assertEquals(null, KartHud.boostLabel(engine.initial()))
        assertEquals(null, KartHud.offTrackLabel(engine.initial()))
    }

    @Test
    fun palettesVaryWithSeed() {
        val a = OffroadCircuit(0)
        val b = OffroadCircuit(3)
        assertTrue(a.palette != b.palette || a.curveAt(40f) != b.curveAt(40f))
        assertEquals(8, a.gateCount)
        assertTrue(a.widthAt(0f) > 0.5f)
    }
}
