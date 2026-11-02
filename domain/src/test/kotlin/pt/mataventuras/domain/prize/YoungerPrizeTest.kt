package pt.mataventuras.domain.prize

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.domain.model.AgeBand

class YoungerPrizeTest {
    @Test
    fun fishingAndColouringAcceptOnlyTheNamedChoice() {
        val fish = FishCatch.round(Random(1))
        val fishHit = fish.labels.indexOf(fish.target)
        assertTrue(FishCatch.caught(fish, fishHit))
        assertFalse(FishCatch.caught(fish, -1))
        val colour = Colouring.round(Random(2))
        val colourHit = colour.labels.indexOf(colour.target)
        assertTrue(Colouring.painted(colour, colourHit))
        assertFalse(Colouring.painted(colour, (colourHit + 1) % colour.labels.size))
    }

    @Test
    fun bubblesAdvanceInOrderAndAMissResets() {
        val round = BubbleRhythm.round(Random(3))
        val wrong = round.labels.indexOfFirst { it != "1" }
        assertEquals(0, BubbleRhythm.pop(round, wrong).step)
        assertEquals(0, BubbleRhythm.pop(round, -1).step)
        var step = round
        listOf("1", "2", "3").forEach { label ->
            step = BubbleRhythm.pop(step, step.labels.indexOf(label))
        }
        assertTrue(BubbleRhythm.done(step))
        assertTrue(ComposePrize.SNAKE in PrizeOffers.composePrizes(AgeBand.SIX_TO_EIGHT))
        assertTrue(ComposePrize.BREAKOUT in PrizeOffers.composePrizes(AgeBand.SIX_TO_EIGHT))
        assertTrue(ComposePrize.FISHING in PrizeOffers.composePrizes(AgeBand.THREE_TO_FIVE))
    }
}
