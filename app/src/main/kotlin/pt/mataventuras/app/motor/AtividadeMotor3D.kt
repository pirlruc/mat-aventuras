package pt.mataventuras.app.motor

import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.activity.ComponentActivity
import pt.mataventuras.dominio.modelo.Mascote
import pt.mataventuras.dominio.motor.MotorKart3D
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Corrida 3D (prémio dos 7 anos) num processo isolado `:motor3d`.
 * GLES 2.0 — o heap 3D morre com o processo ao sair, libertando RAM do Compose.
 */
class AtividadeMotor3D : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mascote = Mascote.deCodigo(intent.getStringExtra(LancadorMotor.EXTRA_MASCOTE) ?: "")
        val vista = GLSurfaceView(this).apply {
            setEGLContextClientVersion(1)
            setRenderer(RenderizadorKart(mascote, MotorKart3D()) { concluido ->
                if (concluido) {
                    setResult(
                        RESULT_OK,
                        android.content.Intent().putExtra(LancadorMotor.RESULTADO_CONCLUIDO, true),
                    )
                    finish()
                }
            })
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
            setOnClickListener { /* impulso tratado no renderer via flag */ }
        }
        setContentView(vista)
        vista.setOnTouchListener { _, _ ->
            RenderizadorKart.impulsoPedido = true
            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Processo :motor3d termina com a Activity — sem libertar Filament/Unity no host.
    }
}

private class RenderizadorKart(
    private val mascote: Mascote,
    private val motor: MotorKart3D,
    private val aoConcluir: (Boolean) -> Unit,
) : GLSurfaceView.Renderer {
    private var estado = motor.inicial(voltasAlvo = 1)
    private var ultimoNs = 0L

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig?) {
        val r = ((mascote.corPrincipalArgb shr 16) and 0xFF) / 255f
        val g = ((mascote.corPrincipalArgb shr 8) and 0xFF) / 255f
        val b = (mascote.corPrincipalArgb and 0xFF) / 255f
        gl.glClearColor(r * 0.25f, g * 0.25f, b * 0.4f, 1f)
        gl.glEnable(GL10.GL_DEPTH_TEST)
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        gl.glViewport(0, 0, width, height)
        gl.glMatrixMode(GL10.GL_PROJECTION)
        gl.glLoadIdentity()
        val razao = width.toFloat() / height.coerceAtLeast(1)
        gl.glFrustumf(-razao, razao, -1f, 1f, 2f, 40f)
        gl.glMatrixMode(GL10.GL_MODELVIEW)
    }

    override fun onDrawFrame(gl: GL10) {
        val agora = System.nanoTime()
        if (ultimoNs == 0L) ultimoNs = agora
        val dt = ((agora - ultimoNs) / 1_000_000_000f).coerceAtMost(0.05f)
        ultimoNs = agora
        val impulso = impulsoPedido
        impulsoPedido = false
        estado = motor.passo(estado, dt, impulso)
        if (estado.concluido) {
            aoConcluir(true)
            return
        }
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)
        gl.glLoadIdentity()
        gl.glTranslatef(0f, -0.4f, -6f)
        gl.glRotatef(18f, 1f, 0f, 0f)
        gl.glRotatef(estado.posicaoPista * 4f, 0f, 1f, 0f)
        desenharPista(gl)
        gl.glTranslatef(0f, 0.35f, 0f)
        desenharKart(gl)
    }

    private fun desenharPista(gl: GL10) {
        gl.glColor4f(0.25f, 0.25f, 0.28f, 1f)
        gl.glPushMatrix()
        gl.glScalef(4f, 0.08f, 6f)
        cubo(gl)
        gl.glPopMatrix()
    }

    private fun desenharKart(gl: GL10) {
        val r = ((mascote.corPrincipalArgb shr 16) and 0xFF) / 255f
        val g = ((mascote.corPrincipalArgb shr 8) and 0xFF) / 255f
        val b = (mascote.corPrincipalArgb and 0xFF) / 255f
        gl.glColor4f(r, g, b, 1f)
        gl.glPushMatrix()
        gl.glScalef(0.4f, 0.25f, 0.6f)
        cubo(gl)
        gl.glPopMatrix()
    }

    private fun cubo(gl: GL10) {
        val v = floatArrayOf(
            -1f, -1f, 1f, 1f, -1f, 1f, 1f, 1f, 1f, -1f, 1f, 1f,
            -1f, -1f, -1f, -1f, 1f, -1f, 1f, 1f, -1f, 1f, -1f, -1f,
        )
        val buf = java.nio.ByteBuffer.allocateDirect(v.size * 4).order(java.nio.ByteOrder.nativeOrder()).asFloatBuffer()
        buf.put(v)
        buf.position(0)
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, buf)
        gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, 4)
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY)
    }

    companion object {
        @Volatile
        var impulsoPedido: Boolean = false
    }
}
