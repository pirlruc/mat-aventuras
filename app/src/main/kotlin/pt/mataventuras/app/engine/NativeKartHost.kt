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
     * Continuous on device; dirty-only under Robolectric so Espresso can idle.
     */
    fun renderMode(embed: Boolean = GodotRuntime.shouldEmbed()): Int =
        if (embed) {
            GLSurfaceView.RENDERMODE_CONTINUOUSLY
        } else {
            GLSurfaceView.RENDERMODE_WHEN_DIRTY
        }

    /**
     * Builds the playable kart session. [showUi] is false under Robolectric so
     * GLSurfaceView does not keep Espresso busy.
     */
    fun attach(
        activity: IsolatedEngineActivity,
        showUi: Boolean = GodotRuntime.shouldEmbed(),
    ): KartSession {
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
        if (!showUi) {
            activity.setContentView(hudHint)
            return session
        }
        val view =
            GLSurfaceView(activity).apply {
                setEGLContextClientVersion(1)
                setRenderer(session.renderer)
                renderMode = renderMode(showUi)
                setOnTouchListener { v, event ->
                    val nx = event.x / v.width.coerceAtLeast(1)
                    session.handleTouch(nx, event.action)
                }
            }
        activity.pauseableSurface = view
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
