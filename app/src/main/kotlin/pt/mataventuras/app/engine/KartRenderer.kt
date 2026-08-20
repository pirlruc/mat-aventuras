package pt.mataventuras.app.engine

import android.opengl.GLSurfaceView
import pt.mataventuras.domain.engine.Kart3dEngine
import pt.mataventuras.domain.engine.Kart3dState
import pt.mataventuras.domain.engine.KartHud
import pt.mataventuras.domain.engine.KartMesh
import pt.mataventuras.domain.engine.KartMeshId
import pt.mataventuras.domain.engine.KartScene
import pt.mataventuras.domain.engine.OvalTrack
import pt.mataventuras.domain.model.Mascot
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Steps the kart sim and issues GLES ES1 draws from [KartScene] instances.
 */
internal class KartRenderer(
    private val mascot: Mascot,
    private val engine: Kart3dEngine,
    private val nowNs: () -> Long = { System.nanoTime() },
    lapsTarget: Int = 3,
    private val onFrame: (Kart3dState, Boolean) -> Unit,
) : GLSurfaceView.Renderer {
    private val track = OvalTrack()
    private var state = engine.initial(lapsTarget = lapsTarget)
    private var lastNs = 0L
    private var lastHud: String = ""
    private lateinit var grass: FloatBuffer
    private lateinit var ribbon: FloatBuffer
    private lateinit var start: FloatBuffer
    private lateinit var box: FloatBuffer

    @Volatile
    var steer: Float = 0f

    @Volatile
    var boostRequested: Boolean = false

    /**
     * Latest simulation snapshot (for tests).
     */
    fun snapshot(): Kart3dState = state

    /**
     * Allocates meshes and configures the ES1 context (unit tests pass a fake).
     */
    internal fun prepare(gles: KartGles) {
        grass = buffer(KartMesh.grass())
        ribbon = buffer(KartMesh.trackRibbon(track))
        start = buffer(KartMesh.startLine(track))
        box = buffer(KartMesh.box(0.5f, 0.5f, 0.5f))
        gles.clearColor(0.45f, 0.72f, 0.95f, 1f)
        gles.enable(GL10.GL_DEPTH_TEST)
        gles.enable(GL10.GL_CULL_FACE)
        gles.shadeModel(GL10.GL_FLAT)
    }

    /**
     * Updates the projection for a new viewport size.
     */
    internal fun resize(
        gles: KartGles,
        width: Int,
        height: Int,
    ) {
        gles.viewport(0, 0, width, height)
        gles.matrixMode(GL10.GL_PROJECTION)
        gles.loadIdentity()
        val proj = engine.projectionMatrix(width.toFloat() / height.coerceAtLeast(1))
        gles.loadMatrixf(proj, 0)
        gles.matrixMode(GL10.GL_MODELVIEW)
    }

    /**
     * Advances simulation and HUD without issuing GLES calls (unit tests).
     */
    internal fun tick(): Kart3dState {
        val now = nowNs()
        if (lastNs == 0L) lastNs = now
        val dt = ((now - lastNs) / 1_000_000_000f).coerceAtMost(0.05f)
        lastNs = now
        val boost = boostRequested
        boostRequested = false
        state = engine.step(state, dt, steer, boost)
        publishHud()
        return state
    }

    /**
     * Ticks then draws the current [KartScene] through [gles].
     */
    internal fun drawScene(gles: KartGles) {
        tick()
        if (state.finished) return
        gles.clear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)
        gles.matrixMode(GL10.GL_MODELVIEW)
        gles.loadMatrixf(engine.viewMatrix(state), 0)
        KartScene.instances(track, state, mascot).forEach { item ->
            gles.pushMatrix()
            gles.translatef(item.x, item.y, item.z)
            gles.rotatef(item.yawDegrees, 0f, 1f, 0f)
            gles.scalef(item.scaleX, item.scaleY, item.scaleZ)
            draw(gles, meshOf(item.mesh), item.red, item.green, item.blue)
            gles.popMatrix()
        }
    }

    override fun onSurfaceCreated(
        gl: GL10,
        config: EGLConfig?,
    ) {
        prepare(KartGlesAndroid)
    }

    override fun onSurfaceChanged(
        gl: GL10,
        width: Int,
        height: Int,
    ) {
        resize(KartGlesAndroid, width, height)
    }

    override fun onDrawFrame(gl: GL10) {
        drawScene(KartGlesAndroid)
    }

    private fun publishHud() {
        val hud = KartScene.hudFingerprint(state)
        if (hud != lastHud || state.finished) {
            lastHud = hud
            onFrame(state, state.finished)
        }
    }

    private fun meshOf(id: KartMeshId): FloatBuffer =
        when (id) {
            KartMeshId.GRASS -> grass
            KartMeshId.TRACK -> ribbon
            KartMeshId.START -> start
            KartMeshId.BOX -> box
        }

    private fun draw(
        gles: KartGles,
        mesh: FloatBuffer,
        r: Float,
        g: Float,
        b: Float,
    ) {
        mesh.position(0)
        gles.color4f(r, g, b, 1f)
        gles.enableClientState(GL10.GL_VERTEX_ARRAY)
        gles.vertexPointer(3, GL10.GL_FLOAT, 0, mesh)
        gles.drawArrays(GL10.GL_TRIANGLES, 0, mesh.capacity() / 3)
        gles.disableClientState(GL10.GL_VERTEX_ARRAY)
    }

    private fun buffer(values: FloatArray): FloatBuffer =
        ByteBuffer.allocateDirect(values.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(values)
            .apply { position(0) }
}

/**
 * Builds the two HUD strings shown above the GL view.
 */
internal fun kartOverlay(
    state: Kart3dState,
): Pair<String, String> = KartHud.lapLabel(state) to KartScene.ringsLine(state)
