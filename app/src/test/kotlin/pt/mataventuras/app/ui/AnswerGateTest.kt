package pt.mataventuras.app.ui

import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnswerGateTest {
    @Test
    fun aSecondTapCannotScoreUntilTheExerciseAdvances() {
        val gate = AnswerGate()
        val scored = AtomicInteger(0)
        assertTrue(shouldAcceptAnswer(false))
        repeat(2) {
            if (!gate.tryBegin()) return@repeat
            scored.incrementAndGet()
        }
        assertEquals(1, scored.get())
        assertFalse(gate.tryBegin())
        gate.release()
        assertTrue(gate.tryBegin())
        assertFalse(shouldAcceptAnswer(true))
    }
}
