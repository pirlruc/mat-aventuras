package pt.mataventuras.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrizeFreezeTest {
    @Test
    fun runnerIgnoresButtonsUntilTheFreezeEndsAndTheHoldLifts() {
        val engine = Platformer2dEngine()
        val frozen = engine.initial().copy(freeze = 1f, x = 10f, vx = 4f)
        val held = engine.step(frozen, 0.2f, jumping = true, moveX = 1f)
        assertEquals(0f, held.vx, 0f)
        assertTrue(held.freeze < 1f)
        assertTrue(held.awaitRelease)
        val stillHeld = engine.step(held.copy(freeze = 0f), 0.05f, jumping = true, moveX = 1f)
        assertEquals(0f, stillHeld.vx, 0f)
        assertTrue(stillHeld.awaitRelease)
        val released = engine.step(stillHeld, 0.05f, jumping = false, moveX = 0f)
        assertFalse(released.awaitRelease)
        val dead = engine.step(engine.initial().copy(alive = false, freeze = 1f), 0.1f, true, 1f)
        assertFalse(dead.awaitRelease)
        val done = engine.step(engine.initial().copy(finished = true), 0.1f, false, 0f)
        assertTrue(done.finished)
    }

    @Test
    fun climbIgnoresButtonsDuringTheFreeze() {
        val engine = ClimbEngine()
        val frozen = engine.initial().copy(freeze = 1f)
        val next = engine.step(frozen, 0.1f, moveX = 1f, jumping = true)
        assertEquals(0f, next.vy, 0f)
        assertTrue(next.freeze < 1f)
        assertTrue(next.awaitRelease)
        val dead = engine.step(engine.initial().copy(alive = false), 0.1f, 1f, true)
        assertFalse(dead.awaitRelease)
        val lettingGo = engine.step(next.copy(freeze = 0f), 0.05f, moveX = 0f, jumping = false)
        assertFalse(lettingGo.awaitRelease)
    }

    @Test
    fun everyPrizeNamesItsGoal() {
        val scenes =
            listOf(
                "RUNNER",
                "CLIMB",
                "CHOMP",
                "INVADERS",
                "KART",
                "BALLOONS",
                "FISHING",
                "COLOURING",
                "BUBBLES",
                "SNAKE",
                "BREAKOUT",
                "TOWER",
                "RUNNER_GATE",
            )
        scenes.forEach { scene ->
            assertTrue(PrizeBrief.goal(scene).isNotEmpty())
            assertTrue(PrizeBrief.controls(scene).isNotEmpty())
        }
        assertFalse(PrizeBrief.goal("RUNNER") == PrizeBrief.goal("RUNNER_GATE"))
        assertEquals("RUNNER_GATE", PrizeBrief.composeKey("RUNNER"))
        assertEquals("SNAKE", PrizeBrief.composeKey("SNAKE"))
        assertEquals("Joga até ao fim.", PrizeBrief.goal("unknown"))
        assertEquals("Toca em Jogar.", PrizeBrief.controls("unknown"))
    }
}
