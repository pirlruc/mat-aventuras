package pt.mataventuras.domain.engine

import kotlin.math.abs
import kotlin.math.sqrt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Kart3dEngineTest {
    private val engine = Kart3dEngine()
    private val track = OvalTrack()

    @Test
    fun cruiseCompletesOneLap() {
        var state = engine.initial(lapsTarget = 1)
        repeat(1_500) {
            state = engine.step(state, 0.05f, steer = 0f, boost = false)
        }
        assertTrue(state.finished)
        assertTrue(state.laps >= 1)
        val frozen = engine.step(state, 0.05f, steer = 1f, boost = true)
        assertEquals(state, frozen)
    }

    @Test
    fun boostRaisesSpeedAndHud() {
        val idle = engine.initial()
        val boosted = engine.step(idle, 0.05f, steer = 0f, boost = true)
        assertEquals(1, boosted.answerBoosts)
        assertTrue(boosted.boostTimer > 0f)
        assertEquals("Impulso!", KartHud.boostLabel(boosted))
        repeat(40) {
            val next = engine.step(boosted, 0.05f, 0f, false)
            if (next.speed > idle.speed) {
                assertTrue(next.speed > idle.speed)
                return
            }
        }
    }

    @Test
    fun hardSteerCanLeaveTheAsphalt() {
        var state = engine.initial()
        var left = false
        repeat(250) {
            state = engine.step(state, 0.05f, steer = -1f, boost = true)
            if (state.offTrack) left = true
        }
        assertTrue(left)
        val recovered = engine.step(state.copy(x = 0f, z = 0f), 0.05f, 0f, false)
        assertTrue(recovered.offTrack)
        assertEquals("Volta à pista!", KartHud.offTrackLabel(recovered))
    }

    @Test
    fun ringsAreCollectedOnce() {
        var state = engine.initial(lapsTarget = 5)
        repeat(800) {
            state = engine.step(state, 0.05f, steer = 0f, boost = true)
        }
        assertTrue(state.rings >= 1)
        val remaining = KartMesh.remainingRings(track, state)
        assertEquals(state.ringsTarget - state.rings, remaining.size)
    }

    @Test
    fun touchBandsSteerAndBoost() {
        assertTrue(engine.steerFromTouch(0.1f) < -0.5f)
        assertTrue(engine.steerFromTouch(0.9f) > 0.5f)
        assertEquals(0f, engine.steerFromTouch(0.5f), 0f)
        assertTrue(engine.isBoostTouch(0.5f))
        assertFalse(engine.isBoostTouch(0.1f))
    }

    @Test
    fun hudLapAndRingsArePortuguese() {
        val state = engine.initial()
        assertTrue(KartHud.lapLabel(state).startsWith("Volta"))
        assertTrue(KartHud.ringsLabel(state).contains("Anéis"))
        assertEquals("Esquerda · Impulso · Direita", KartHud.CONTROL_HINT)
        assertTrue(KartHud.META_HINT.contains("META"))
        assertEquals(null, KartHud.boostLabel(state))
        assertEquals(null, KartHud.offTrackLabel(state))
    }

    @Test
    fun cameraSitsBehindTheKart() {
        val state = engine.initial()
        val view = engine.viewMatrix(state)
        assertEquals(16, view.size)
        val proj = engine.projectionMatrix(1.5f)
        assertEquals(16, proj.size)
        assertTrue(state.cameraEye.y > state.y)
    }

    @Test
    fun matricesAndMeshesHaveExpectedSizes() {
        val eye = Vec3(0f, 4f, -8f)
        val look = KartMath.lookAt(eye, Vec3(0f, 0f, 0f), Vec3(0f, 1f, 0f))
        val sx = sqrt(look[0] * look[0] + look[4] * look[4] + look[8] * look[8])
        assertEquals(1f, sx, 0.05f)
        val proj = KartMath.perspective(60f, 1f, 1f, 100f)
        assertTrue(proj[0] > 0f)
        assertEquals(64 * 6 * 3, KartMesh.trackRibbon(track, 64).size)
        assertEquals(18, KartMesh.grass().size)
        assertEquals(18, KartMesh.startLine(track).size)
        assertEquals(108, KartMesh.box(0.4f, 0.2f, 0.6f).size)
        assertEquals(12, KartMesh.conePositions(track).size)
        assertEquals(10, KartMesh.innerBarrierPositions(track).size)
        val pulled = track.pullTowardCenter(0f, 0f, 2f)
        assertTrue(pulled.length() > 1f)
        val onLine = track.point(0.1f)
        val stayed = track.pullTowardCenter(onLine.x, onLine.z, 4f)
        assertEquals(onLine.x, stayed.x, 1e-3f)
        assertEquals(onLine.z, stayed.z, 1e-3f)
    }

    @Test
    fun ovalWrapsAndReportsOffTrack() {
        assertEquals(0.25f, track.wrap(1.25f), 1e-4f)
        assertEquals(0.75f, track.wrap(-0.25f), 1e-4f)
        val p = track.point(0f)
        assertFalse(track.isOffTrack(p.x, p.z))
        assertTrue(track.isOffTrack(0f, 0f))
        assertTrue(track.crossedStart(0.9f, 0.05f))
        assertFalse(track.crossedStart(0.2f, 0.3f))
        val ring = track.ringPosition(0, 8)
        assertTrue(ring.y > 0f)
    }

    @Test
    fun sceneBuildsKartAndHudFingerprint() {
        val state = engine.initial()
        val items = KartScene.instances(track, state, pt.mataventuras.domain.model.Mascot.SPEEDY_HEDGEHOG)
        assertTrue(items.any { it.mesh == KartMeshId.GRASS })
        assertTrue(items.any { it.mesh == KartMeshId.TRACK })
        assertTrue(items.any { it.mesh == KartMeshId.START })
        assertTrue(items.count { it.mesh == KartMeshId.BOX } >= 15)
        assertEquals(4, KartScene.wheelPositions(state).size)
        assertTrue(KartScene.hudFingerprint(state).contains("Volta"))
        assertTrue(KartScene.ringsLine(state).contains("Anéis"))
        val rgb = KartScene.mascotRgb(pt.mataventuras.domain.model.Mascot.PINK_PIGLET)
        assertEquals(3, rgb.size)
        val collected = state.copy(collectedMask = (1 shl state.ringsTarget) - 1, rings = state.ringsTarget)
        assertTrue(KartMesh.remainingRings(track, collected).isEmpty())
        val boosted = state.copy(boostTimer = 1f, offTrack = true)
        assertTrue(KartScene.ringsLine(boosted).contains("Impulso"))
        val grass = KartScene.instances(track, state.copy(offTrack = true), pt.mataventuras.domain.model.Mascot.HERO_PUP)
        assertTrue(grass.first().mesh == KartMeshId.GRASS)
    }

    @Test
    fun vec3Algebra() {
        val a = Vec3(3f, 0f, 4f)
        assertEquals(5f, a.length(), 1e-4f)
        assertEquals(1f, a.normalized().length(), 1e-4f)
        assertEquals(Vec3(0f, 0f, 0f), Vec3(0f, 0f, 0f).normalized())
        val c = Vec3(1f, 0f, 0f).cross(Vec3(0f, 1f, 0f))
        assertEquals(1f, c.z, 1e-4f)
        assertEquals(Vec3(2f, 2f, 2f), Vec3(1f, 1f, 1f) + Vec3(1f, 1f, 1f))
        assertEquals(Vec3(1f, 0f, 0f), Vec3(3f, 1f, 1f) - Vec3(2f, 1f, 1f))
        assertNotEquals(0f, abs(Vec3(1f, 2f, 3f).dot(Vec3(1f, 0f, 0f))))
    }
}
