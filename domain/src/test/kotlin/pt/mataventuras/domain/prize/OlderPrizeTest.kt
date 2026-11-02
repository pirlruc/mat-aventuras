package pt.mataventuras.domain.prize

import kotlin.random.Random
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.domain.model.AgeBand

class OlderPrizeTest {
    @Test
    fun breakoutTowerAndGatesAcceptOnlyTheNamedChoice() {
        val letters = LetterBreakout.round(Random(1))
        val letterHit = letters.labels.indexOf(letters.target)
        assertTrue(LetterBreakout.broken(letters, letterHit))
        assertFalse(LetterBreakout.broken(letters, -1))
        val wave = TowerDefence.round(Random(2))
        val waveHit = wave.labels.indexOf(wave.target)
        assertTrue(TowerDefence.stopped(wave, waveHit))
        assertFalse(TowerDefence.stopped(wave, (waveHit + 1) % wave.labels.size))
        val gate = MathGate.round(Random(3))
        val gateHit = gate.labels.indexOf(gate.target)
        assertTrue(MathGate.passed(gate, gateHit))
        assertFalse(MathGate.passed(gate, (gateHit + 1) % gate.labels.size))
        val older = PrizeOffers.composePrizes(AgeBand.SIX_TO_EIGHT)
        assertTrue(ComposePrize.BREAKOUT in older)
        assertTrue(ComposePrize.TOWER in older)
        assertTrue(ComposePrize.RUNNER in older)
        assertFalse(ComposePrize.RUNNER in PrizeOffers.composePrizes(AgeBand.THREE_TO_FIVE))
    }
}
