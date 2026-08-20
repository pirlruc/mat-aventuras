package pt.mataventuras.app.engine

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import pt.mataventuras.domain.engine.Kart3dEngine
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Age-7 3D kart reward in an isolated `:engine3d` process.
 * GLES ES1 draws meshes produced by :domain. The 3D heap dies with the process.
 */
class Kart3dActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mascot =
            pt.mataventuras.domain.model.Mascot.fromCode(
                intent.getStringExtra(EngineLauncher.EXTRA_MASCOT) ?: "",
            )
        val engine = Kart3dEngine()
        val hudLap = hudText()
        val hudRings = hudText()
        val hudHint = hudText().apply { text = VoiceScripts.STEER_HINT }
        val renderer =
            KartRenderer(mascot, engine) { state, finished ->
                runOnUiThread {
                    val overlay = kartOverlay(state)
                    hudLap.text = overlay.first
                    hudRings.text = overlay.second
                    if (finished) {
                        setResult(
                            RESULT_OK,
                            android.content.Intent().putExtra(EngineLauncher.RESULT_FINISHED, true),
                        )
                        finish()
                    }
                }
            }
        val view =
            GLSurfaceView(this).apply {
                setEGLContextClientVersion(1)
                setRenderer(renderer)
                renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                setOnTouchListener { v, event ->
                    val nx = event.x / v.width.coerceAtLeast(1)
                    renderer.steer = Kart3dInput.steerFromTouch(nx)
                    if (event.action == MotionEvent.ACTION_DOWN && Kart3dInput.isBoostTouch(nx)) {
                        renderer.boostRequested = true
                    }
                    if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                        renderer.steer = 0f
                    }
                    true
                }
            }
        val overlay =
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32, 48, 32, 32)
                addView(hudLap)
                addView(hudRings)
                addView(hudHint)
            }
        setContentView(
            FrameLayout(this).apply {
                addView(view, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                addView(overlay)
            },
        )
    }

    private fun hudText(): TextView =
        TextView(this).apply {
            textSize = 22f
            setTextColor(0xFFFFFFFF.toInt())
            setShadowLayer(4f, 1f, 1f, 0xFF000000.toInt())
        }
}
