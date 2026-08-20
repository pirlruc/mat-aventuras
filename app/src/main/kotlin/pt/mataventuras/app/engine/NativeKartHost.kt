package pt.mataventuras.app.engine

import android.opengl.GLSurfaceView
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Attaches the native GLES kart surface to an isolated Activity.
 */
internal object NativeKartHost {
    /**
     * Builds the playable kart view tree and returns the session for tests.
     */
    fun attach(activity: IsolatedEngineActivity): KartSession {
        val mascot = activity.launchMascot()
        val hudLap = hudText(activity)
        val hudRings = hudText(activity)
        val hudHint = hudText(activity).apply { text = VoiceScripts.STEER_HINT }
        val session =
            KartSession(
                mascot = mascot,
                onHud = { lap, rings ->
                    activity.runOnUiThread {
                        hudLap.text = lap
                        hudRings.text = rings
                    }
                },
                onFinished = {
                    activity.completeRewardOnUi(true)
                },
            )
        val view =
            GLSurfaceView(activity).apply {
                setEGLContextClientVersion(1)
                setRenderer(session.renderer)
                renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                setOnTouchListener { v, event ->
                    val nx = event.x / v.width.coerceAtLeast(1)
                    session.handleTouch(nx, event.action)
                }
            }
        val overlay =
            LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32, 48, 32, 32)
                addView(hudLap)
                addView(hudRings)
                addView(hudHint)
            }
        activity.setContentView(
            FrameLayout(activity).apply {
                addView(view, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                addView(overlay)
            },
        )
        return session
    }

    private fun hudText(activity: IsolatedEngineActivity): TextView =
        TextView(activity).apply {
            textSize = 22f
            setTextColor(0xFFFFFFFF.toInt())
            setShadowLayer(4f, 1f, 1f, 0xFF000000.toInt())
        }
}
