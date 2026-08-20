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
        assertTrue(finishedFrames >= 1)
        renderer.drawScene(gles)
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
        assertEquals(0, done)
    }

    @Test
    fun glesAdapterAndRendererOverridesRunThroughAProxy() {
        var frames = 0
        val renderer =
            KartRenderer(
                mascot = Mascot.SPEEDY_HEDGEHOG,
                engine = Kart3dEngine(),
                nowNs = {
                    frames += 1
                    frames * 50_000_000L
                },
                lapsTarget = 1,
                onFrame = { _, _ -> },
            )
        val loader = android.opengl.GLSurfaceView::class.java.classLoader
        val gl10Class = Class.forName("javax.microedition.khronos.opengles.GL10", true, loader)
        val gl =
            Proxy.newProxyInstance(gl10Class.classLoader, arrayOf(gl10Class)) { _, method, _ ->
                when (method.returnType) {
                    java.lang.Void.TYPE -> null
                    java.lang.Integer.TYPE -> 0
                    java.lang.Boolean.TYPE -> false
                    java.lang.Float.TYPE -> 0f
                    else -> null
                }
            }
        val adapter =
            KartGlesEs1::class.java.constructors.first().newInstance(gl) as KartGles
        adapter.clearColor(0.1f, 0.2f, 0.3f, 1f)
        adapter.enable(1)
        adapter.shadeModel(2)
        adapter.viewport(0, 0, 8, 8)
        adapter.matrixMode(3)
        adapter.loadIdentity()
        adapter.loadMatrixf(FloatArray(16), 0)
        adapter.clear(4)
        adapter.pushMatrix()
        adapter.translatef(1f, 2f, 3f)
        adapter.rotatef(10f, 0f, 1f, 0f)
        adapter.scalef(1f, 1f, 1f)
        adapter.color4f(1f, 1f, 1f, 1f)
        adapter.enableClientState(5)
        adapter.vertexPointer(3, 0, 0, java.nio.ByteBuffer.allocateDirect(36))
        adapter.drawArrays(4, 0, 3)
        adapter.disableClientState(5)
        adapter.popMatrix()
        val created =
            KartRenderer::class.java.methods.first { it.name == "onSurfaceCreated" }
        created.invoke(renderer, gl, null)
        val changed =
            KartRenderer::class.java.methods.first { it.name == "onSurfaceChanged" }
        changed.invoke(renderer, gl, 320, 240)
        val draw =
            KartRenderer::class.java.methods.first { it.name == "onDrawFrame" }
        draw.invoke(renderer, gl)
        assertTrue(renderer.snapshot().lapsTarget == 1)
    }

    private fun recordingGles(): KartGles {
        val loader = KartGles::class.java.classLoader
        return Proxy.newProxyInstance(
            loader,
            arrayOf(KartGles::class.java),
        ) { _, _, _ -> null } as KartGles
    }
}
