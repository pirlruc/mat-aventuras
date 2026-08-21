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
        assertEquals(-1f, Kart3dInput.steerFromTouch(0f), 0.001f)
        assertTrue(Kart3dInput.steerFromTouch(0.1f) < -0.5f)
        assertFalse(Kart3dInput.isBoostTouch(0.1f))
    }

    @Test
    fun centreBoosts() {
        assertEquals(0f, Kart3dInput.steerFromTouch(0.5f), 0.001f)
        assertTrue(Kart3dInput.isBoostTouch(0.5f))
    }

    @Test
    fun rightThirdSteersPositive() {
        assertEquals(1f, Kart3dInput.steerFromTouch(1f), 0.001f)
        assertTrue(Kart3dInput.steerFromTouch(0.9f) > 0.5f)
        assertFalse(Kart3dInput.isBoostTouch(0.9f))
    }

    @Test
    fun boundariesAreInclusiveForBoost() {
        assertTrue(Kart3dInput.isBoostTouch(0.5f))
        assertTrue(Kart3dInput.isBoostTouch(0.4f))
        assertFalse(Kart3dInput.isBoostTouch(0.1f))
        assertTrue(Kart3dInput.steerFromTouch(0.33f) < 0f)
        assertTrue(Kart3dInput.steerFromTouch(0.67f) > 0f)
        assertTrue(Kart3dInput.boostOnAction(0, 0.5f))
        assertFalse(Kart3dInput.boostOnAction(0, 0.1f))
        assertFalse(Kart3dInput.boostOnAction(1, 0.5f))
        assertTrue(Kart3dInput.releasesSteer(1))
        assertTrue(Kart3dInput.releasesSteer(3))
        assertFalse(Kart3dInput.releasesSteer(0))
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
