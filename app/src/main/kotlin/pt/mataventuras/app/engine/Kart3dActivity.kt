package pt.mataventuras.app.engine

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Age-7 3D kart reward in an isolated `:engine3d` process.
 * GLES ES1 draws meshes produced by :domain. The 3D heap dies with the process.
 *
 * Drop-in plugins must keep this process name and the [EngineLauncher] extras;
 * they must not open Room or request INTERNET.
 */
class Kart3dActivity : IsolatedEngineActivity() {
    internal lateinit var session: KartSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mascot = launchMascot()
        val hudLap = hudText()
        val hudRings = hudText()
        val hudHint = hudText().apply { text = VoiceScripts.STEER_HINT }
        session =
            KartSession(
                mascot = mascot,
                onHud = { lap, rings ->
                    runOnUiThread {
                        hudLap.text = lap
                        hudRings.text = rings
                    }
                },
                onFinished = {
                    runOnUiThread { closeFinished() }
                },
            )
        val view =
            GLSurfaceView(this).apply {
                setEGLContextClientVersion(1)
                setRenderer(session.renderer)
                renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                setOnTouchListener { v, event ->
                    val nx = event.x / v.width.coerceAtLeast(1)
                    session.handleTouch(nx, event.action)
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

    /**
     * Returns [EngineLauncher.RESULT_FINISHED] and finishes (isolated process then dies).
     */
    internal fun closeFinished() {
        completeReward(true)
    }

    private fun hudText(): TextView =
        TextView(this).apply {
            textSize = 22f
            setTextColor(0xFFFFFFFF.toInt())
            setShadowLayer(4f, 1f, 1f, 0xFF000000.toInt())
        }
}
