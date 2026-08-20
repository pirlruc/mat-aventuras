package pt.mataventuras.app.engine

import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.activity.ComponentActivity
import pt.mataventuras.domain.engine.Kart3dEngine
import pt.mataventuras.domain.model.Mascot
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Age-7 3D kart reward in an isolated `:engine3d` process.
 * GLES ES1 — the 3D heap dies with the process on exit, freeing Compose RAM.
 */
class Kart3dActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mascot = Mascot.fromCode(intent.getStringExtra(EngineLauncher.EXTRA_MASCOT) ?: "")
        val view = GLSurfaceView(this).apply {
            setEGLContextClientVersion(1)
            setRenderer(
                KartRenderer(mascot, Kart3dEngine()) { finished ->
                    if (finished) {
                        setResult(
                            RESULT_OK,
                            android.content.Intent().putExtra(EngineLauncher.RESULT_FINISHED, true),
                        )
                        finish()
                    }
                },
            )
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }
        setContentView(view)
        view.setOnTouchListener { _, _ ->
            KartRenderer.boostRequested = true
            true
        }
    }
}

private class KartRenderer(
    private val mascot: Mascot,
    private val engine: Kart3dEngine,
    private val onFinished: (Boolean) -> Unit,
) : GLSurfaceView.Renderer {
    private var state = engine.initial(lapsTarget = 1)
    private var lastNs = 0L
    private val cubeVertices: FloatBuffer = cubeBuffer()

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig?) {
        val r = ((mascot.primaryArgb shr 16) and 0xFF) / 255f
        val g = ((mascot.primaryArgb shr 8) and 0xFF) / 255f
        val b = (mascot.primaryArgb and 0xFF) / 255f
        gl.glClearColor(r * 0.25f, g * 0.25f, b * 0.4f, 1f)
        gl.glEnable(GL10.GL_DEPTH_TEST)
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        gl.glViewport(0, 0, width, height)
        gl.glMatrixMode(GL10.GL_PROJECTION)
        gl.glLoadIdentity()
        val aspect = width.toFloat() / height.coerceAtLeast(1)
        gl.glFrustumf(-aspect, aspect, -1f, 1f, 2f, 40f)
        gl.glMatrixMode(GL10.GL_MODELVIEW)
    }

    override fun onDrawFrame(gl: GL10) {
        val now = System.nanoTime()
        if (lastNs == 0L) lastNs = now
        val dt = ((now - lastNs) / 1_000_000_000f).coerceAtMost(0.05f)
        lastNs = now
        val boost = boostRequested
        boostRequested = false
        state = engine.step(state, dt, boost)
        if (state.finished) {
            onFinished(true)
            return
        }
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)
        gl.glLoadIdentity()
        gl.glTranslatef(0f, -0.4f, -6f)
        gl.glRotatef(18f, 1f, 0f, 0f)
        gl.glRotatef(state.trackPosition * 4f, 0f, 1f, 0f)
        drawTrack(gl)
        gl.glTranslatef(0f, 0.35f, 0f)
        drawKart(gl)
    }

    private fun drawTrack(gl: GL10) {
        gl.glColor4f(0.25f, 0.25f, 0.28f, 1f)
        gl.glPushMatrix()
        gl.glScalef(4f, 0.08f, 6f)
        cube(gl)
        gl.glPopMatrix()
    }

    private fun drawKart(gl: GL10) {
        val r = ((mascot.primaryArgb shr 16) and 0xFF) / 255f
        val g = ((mascot.primaryArgb shr 8) and 0xFF) / 255f
        val b = (mascot.primaryArgb and 0xFF) / 255f
        gl.glColor4f(r, g, b, 1f)
        gl.glPushMatrix()
        gl.glScalef(0.4f, 0.25f, 0.6f)
        cube(gl)
        gl.glPopMatrix()
    }

    private fun cube(gl: GL10) {
        cubeVertices.position(0)
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, cubeVertices)
        gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, 4)
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY)
    }

    companion object {
        @Volatile
        var boostRequested: Boolean = false

        private fun cubeBuffer(): FloatBuffer {
            val vertices = floatArrayOf(
                -1f, -1f, 1f, 1f, -1f, 1f, 1f, 1f, 1f, -1f, 1f, 1f,
                -1f, -1f, -1f, -1f, 1f, -1f, 1f, 1f, -1f, 1f, -1f, -1f,
            )
            return ByteBuffer.allocateDirect(vertices.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertices)
                .apply { position(0) }
        }
    }
}
