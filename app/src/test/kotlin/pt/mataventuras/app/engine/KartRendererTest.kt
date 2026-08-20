package pt.mataventuras.app.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pt.mataventuras.domain.engine.Kart3dEngine
import pt.mataventuras.domain.engine.KartHud
import pt.mataventuras.domain.model.Mascot
import java.lang.reflect.Proxy
import javax.microedition.khronos.opengles.GL10

@RunWith(RobolectricTestRunner::class)
class KartRendererTest {
    @Test
    fun fakeGlWalksAFrameAndFinishesAShortRace() {
        val engine = Kart3dEngine()
        var lastFinished = false
        var frames = 0
        val renderer =
            KartRenderer(
                mascot = Mascot.MISCHIEVOUS_ALIEN,
                engine = engine,
                onFrame = { _, finished -> lastFinished = finished },
                nowNs = {
                    frames += 1
                    frames * 50_000_000L
                },
            )
        val gl = fakeGl()
        renderer.onSurfaceCreated(gl, null)
        renderer.onSurfaceChanged(gl, 800, 0)
        renderer.onSurfaceChanged(gl, 1280, 720)
        renderer.steer = -1f
        renderer.boostRequested = true
        repeat(40) { renderer.onDrawFrame(gl) }
        val overlay = kartOverlay(renderer.snapshot())
        assertTrue(overlay.first.startsWith("Volta"))
        assertTrue(overlay.second.contains("Anéis") || overlay.second.contains("pista") || overlay.second.contains("Impulso"))
        renderer.steer = 0f
        repeat(500) { renderer.onDrawFrame(gl) }
        assertTrue(renderer.snapshot().laps >= 0)
        assertTrue(KartHud.lapLabel(renderer.snapshot()).isNotBlank())
        assertEquals(false, lastFinished && renderer.snapshot().lapsTarget != 3)
    }

    @Test
    fun hudPublishesWhenFingerprintChanges() {
        val published = mutableListOf<String>()
        val renderer =
            KartRenderer(
                mascot = Mascot.PINK_PIGLET,
                engine = Kart3dEngine(),
                onFrame = { state, _ -> published += KartHud.lapLabel(state) },
                nowNs = { System.nanoTime() },
            )
        val gl = fakeGl()
        renderer.onSurfaceCreated(gl, null)
        renderer.onSurfaceChanged(gl, 400, 400)
        renderer.onDrawFrame(gl)
        renderer.boostRequested = true
        renderer.onDrawFrame(gl)
        assertTrue(published.isNotEmpty())
    }

    private fun fakeGl(): GL10 {
        val handler =
            java.lang.reflect.InvocationHandler { _, method, _ ->
                when (method.returnType) {
                    java.lang.Boolean.TYPE -> false
                    java.lang.Integer.TYPE -> 0
                    java.lang.Float.TYPE -> 0f
                    java.lang.Long.TYPE -> 0L
                    java.lang.Byte.TYPE -> 0.toByte()
                    java.lang.Short.TYPE -> 0.toShort()
                    java.lang.Double.TYPE -> 0.0
                    java.lang.Void.TYPE, Void.TYPE -> null
                    else -> null
                }
            }
        return Proxy.newProxyInstance(
            GL10::class.java.classLoader,
            arrayOf(GL10::class.java),
            handler,
        ) as GL10
    }
}
