package pt.mataventuras.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArcadeSceneTest {
    @Test
    fun invadersPaintsFleetShipShotAndBomb() {
        val state =
            InvadersEngine().initial().copy(shotX = 0.5f, shotY = 0.4f, bombX = 0.2f, bombY = 0.7f)
        val spans = ArrayList<ArcadeSpan>(32)
        ArcadeScene.fillInvaders(spans, state, 200f, 100f)
        assertEquals(ArcadeScene.SKY_ARGB, spans.first().argb)
        assertEquals(InvadersEngine.FLEET, spans.count { it.argb == ArcadeScene.ALIEN_ARGB })
        assertTrue(spans.any { it.argb == ArcadeScene.SHIP_ARGB })
        assertTrue(spans.any { it.argb == ArcadeScene.SHOT_ARGB })
        assertTrue(spans.any { it.argb == ArcadeScene.BOMB_ARGB })
        val cleared = state.copy(aliens = 0, shotY = -1f, bombY = -1f, lives = 0)
        ArcadeScene.fillInvaders(spans, cleared, 200f, 100f)
        assertEquals(0, spans.count { it.argb == ArcadeScene.ALIEN_ARGB })
        assertEquals(0, spans.count { it.argb == ArcadeScene.SHOT_ARGB })
    }

    @Test
    fun chompPaintsWallsPelletsAndActors() {
        val state = ChompEngine().initial().copy(form = 1)
        val spans = ArrayList<ArcadeSpan>(40)
        ArcadeScene.fillChomp(spans, state, 100f, 100f)
        assertTrue(spans.any { it.argb == ArcadeScene.WALL_ARGB })
        assertTrue(spans.any { it.argb == ArcadeScene.PELLET_ARGB })
        assertTrue(spans.count { it.argb == ArcadeScene.GHOST_ARGB } >= 2)
        assertTrue(spans.any { it.argb == ArcadeScene.SHIP_ARGB })
        ArcadeScene.fillChomp(spans, state.copy(form = 0, pellets = 0), 100f, 100f)
        assertEquals(0, spans.count { it.argb == ArcadeScene.PELLET_ARGB })
        assertTrue(spans.any { it.argb == ArcadeScene.HERO_ARGB })
    }

    @Test
    fun climbPaintsFloorsLettersBarrelAndHero() {
        val state = ClimbEngine().initial()
        val spans = ArrayList<ArcadeSpan>(24)
        ArcadeScene.fillClimb(spans, state, 200f, 200f)
        assertEquals(ClimbEngine.FLOORS.size, spans.count { it.argb == ArcadeScene.FLOOR_ARGB })
        assertEquals(ClimbEngine.LETTERS.size, spans.count { it.argb == ArcadeScene.LETTER_ARGB })
        assertTrue(spans.any { it.argb == ArcadeScene.BARREL_ARGB })
        assertTrue(spans.any { it.argb == ArcadeScene.HERO_ARGB })
        val grown = state.copy(form = 1, collectedMask = (1 shl ClimbEngine.LETTERS.size) - 1)
        ArcadeScene.fillClimb(spans, grown, 200f, 200f)
        assertEquals(0, spans.count { it.argb == ArcadeScene.LETTER_ARGB })
        assertTrue(spans.any { it.argb == ArcadeScene.SHIP_ARGB })
    }

    @Test
    fun inputMapsSteerChompAndClimb() {
        assertEquals(-1f, ArcadeInput.steer(0f), 0.001f)
        assertEquals(0f, ArcadeInput.steer(0.5f), 0.001f)
        assertEquals(1f, ArcadeInput.steer(1f), 0.001f)
        assertEquals(0 to 0, ArcadeInput.chompDir(0.01f, -0.01f))
        assertEquals(1 to 0, ArcadeInput.chompDir(0.2f, 0.05f))
        assertEquals(-1 to 0, ArcadeInput.chompDir(-0.2f, 0.01f))
        assertEquals(0 to 1, ArcadeInput.chompDir(0.01f, 0.3f))
        assertEquals(0 to -1, ArcadeInput.chompDir(0.01f, -0.3f))
        assertTrue(ArcadeInput.climbJump(-0.2f))
        assertTrue(!ArcadeInput.climbJump(0.2f))
    }
}
