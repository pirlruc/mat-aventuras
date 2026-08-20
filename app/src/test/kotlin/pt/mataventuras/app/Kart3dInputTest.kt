package pt.mataventuras.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.app.engine.Kart3dInput
import pt.mataventuras.app.ui.home.ModuleTitles
import pt.mataventuras.app.ui.parent.ParentLabels
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.model.Mascot

class Kart3dInputTest {
    @Test
    fun leftThirdSteersNegative() {
        assertEquals(-1f, Kart3dInput.steerFromTouch(0.1f), 0.001f)
        assertFalse(Kart3dInput.isBoostTouch(0.1f))
    }

    @Test
    fun centreBoosts() {
        assertEquals(0f, Kart3dInput.steerFromTouch(0.5f), 0.001f)
        assertTrue(Kart3dInput.isBoostTouch(0.5f))
    }

    @Test
    fun rightThirdSteersPositive() {
        assertEquals(1f, Kart3dInput.steerFromTouch(0.9f), 0.001f)
        assertFalse(Kart3dInput.isBoostTouch(0.9f))
    }

    @Test
    fun boundariesAreInclusiveForBoost() {
        assertTrue(Kart3dInput.isBoostTouch(0.34f))
        assertTrue(Kart3dInput.isBoostTouch(0.66f))
        assertEquals(-1f, Kart3dInput.steerFromTouch(0.33f), 0.001f)
        assertEquals(1f, Kart3dInput.steerFromTouch(0.67f), 0.001f)
    }

    @Test
    fun moduleTitlesCoverEveryPack() {
        val mascot = Mascot.BRAVE_PLUMBER
        LearningModule.entries.forEach { module ->
            assertTrue(ModuleTitles.of(module, mascot).contains(mascot.displayName))
        }
    }

    @Test
    fun parentDurationFormatsMinutesAndSeconds() {
        assertEquals("0m 0s", ParentLabels.formatDuration(0))
        assertEquals("1m 1s", ParentLabels.formatDuration(61_000))
        assertEquals("2m 5s", ParentLabels.formatDuration(125_000))
    }
}
