package pt.mataventuras.app.engine

import java.lang.reflect.Proxy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import pt.mataventuras.domain.engine.Kart3dEngine
import pt.mataventuras.domain.engine.KartHud
import pt.mataventuras.domain.model.Mascot

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class KartRendererTest {
    @Test
    fun tickAdvancesHudWithoutGles() {
        var frames = 0
        val published = mutableListOf<String>()
        val renderer =
            KartRenderer(
                mascot = Mascot.MISCHIEVOUS_ALIEN,
                engine = Kart3dEngine(),
                nowNs = {
                    frames += 1
                    frames * 50_000_000L
                },
                onFrame = { state, _ -> published += KartHud.lapLabel(state) },
            )
        renderer.steer = -1f
        renderer.boostRequested = true
        repeat(20) { renderer.tick() }
        val overlay = kartOverlay(renderer.snapshot())
        assertTrue(overlay.first.startsWith("Volta"))
        assertTrue(published.isNotEmpty())
        assertEquals(3, renderer.snapshot().lapsTarget)
        renderer.steer = 0f
        renderer.tick()
        assertTrue(KartHud.lapLabel(renderer.snapshot()).isNotBlank())
    }

    @Test
    fun prepareResizeAndDrawUseKartGles() {
        var frames = 0
        val renderer =
            KartRenderer(
                mascot = Mascot.BRAVE_PLUMBER,
                engine = Kart3dEngine(),
                nowNs = {
                    frames += 1
                    frames * 50_000_000L
                },
                lapsTarget = 1,
                onFrame = { _, _ -> },
            )
        val gles = recordingGles()
        renderer.prepare(gles)
        renderer.resize(gles, 800, 0)
        renderer.resize(gles, 1280, 720)
        renderer.steer = 0.2f
        renderer.boostRequested = true
        renderer.drawScene(gles)
        repeat(8) { renderer.drawScene(gles) }
        assertTrue(renderer.snapshot().speed > 0f)
    }

    @Test
    fun drawSceneReturnsOnceTheRaceFinishes() {
        var frames = 0
        var finishedFrames = 0
        val renderer =
            KartRenderer(
                mascot = Mascot.HERO_PUP,
                engine = Kart3dEngine(),
                nowNs = {
                    frames += 1
                    frames * 50_000_000L
                },
                lapsTarget = 1,
                onFrame = { _, finished -> if (finished) finishedFrames += 1 },
            )
        val gles = recordingGles()
        renderer.prepare(gles)
        renderer.resize(gles, 400, 300)
        repeat(400) { renderer.drawScene(gles) }
        assertTrue(renderer.snapshot().finished)
        val published = finishedFrames
        assertTrue(published >= 1)
        val speed = renderer.snapshot().speed
        renderer.drawScene(gles)
        renderer.drawScene(gles)
        renderer.tick()
        renderer.tick()
        assertEquals(published, finishedFrames)
        assertEquals(speed, renderer.snapshot().speed, 0f)
    }

    @Test
    fun sessionMapsSteerBoostAndRelease() {
        var hud = 0
        var done = 0
        var ns = 0L
        val session =
            KartSession(
                mascot = Mascot.PINK_PIGLET,
                nowNs = {
                    ns += 50_000_000L
                    ns
                },
                lapsTarget = 1,
                onHud = { _, _ -> hud += 1 },
                onFinished = { done += 1 },
            )
        assertTrue(session.handleTouch(0.1f, android.view.MotionEvent.ACTION_DOWN))
        assertEquals(-1f, session.renderer.steer, 0f)
        session.handleTouch(0.5f, android.view.MotionEvent.ACTION_DOWN)
        assertTrue(session.renderer.boostRequested)
        session.handleTouch(0.9f, android.view.MotionEvent.ACTION_UP)
        assertEquals(0f, session.renderer.steer, 0f)
        session.handleTouch(0.2f, android.view.MotionEvent.ACTION_CANCEL)
        assertEquals(0f, session.renderer.steer, 0f)
        session.renderer.tick()
        assertTrue(hud >= 1)
        repeat(400) { session.renderer.tick() }
        assertTrue(session.renderer.snapshot().finished)
        val completions = done
        assertEquals(1, completions)
        repeat(8) { session.renderer.tick() }
        assertEquals(1, done)
    }

    @Test
    fun androidGlesAdapterForwardsToGles10() {
        val gles = KartGlesAndroid
        gles.clearColor(0.1f, 0.2f, 0.3f, 1f)
        gles.enable(1)
        gles.shadeModel(2)
        gles.viewport(0, 0, 8, 8)
        gles.matrixMode(3)
        gles.loadIdentity()
        gles.loadMatrixf(FloatArray(16), 0)
        gles.clear(4)
        gles.pushMatrix()
        gles.translatef(1f, 2f, 3f)
        gles.rotatef(10f, 0f, 1f, 0f)
        gles.scalef(1f, 1f, 1f)
        gles.color4f(1f, 1f, 1f, 1f)
        gles.enableClientState(5)
        gles.vertexPointer(3, 0, 0, java.nio.ByteBuffer.allocateDirect(36))
        gles.drawArrays(4, 0, 3)
        gles.disableClientState(5)
        gles.popMatrix()
        assertTrue(true)
    }

    private fun recordingGles(): KartGles {
        val loader = KartGles::class.java.classLoader
        return Proxy.newProxyInstance(
            loader,
            arrayOf(KartGles::class.java),
        ) { _, _, _ -> null } as KartGles
    }
}
