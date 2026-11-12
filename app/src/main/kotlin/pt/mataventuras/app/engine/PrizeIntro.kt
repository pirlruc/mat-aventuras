package pt.mataventuras.app.engine

import android.app.Activity
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import pt.mataventuras.app.speech.SpeechEngine
import pt.mataventuras.domain.engine.PrizeBrief
import pt.mataventuras.domain.engine.PrizeHud
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Spoken goal and controls. [Jogar] dismisses the card. The game keeps loading underneath.
 */
internal object PrizeIntro {
    private const val ROOT: String = "prize-intro"
    private val sessions = HashMap<Activity, SpeechEngine>()

    /**
     * True while the Jogar card is covering this activity.
     */
    fun isHolding(activity: Activity): Boolean = sessions.containsKey(activity)

    /**
     * Stops the intro voice. Safe when [attach] never ran.
     */
    fun release(activity: Activity) {
        sessions.remove(activity)?.release()
    }

    /**
     * Shows the card on the window decor when this is a real device.
     * Robolectric skips it so unit tests can reach the canvas.
     */
    fun attach(
        activity: Activity,
        scene: String,
        embed: Boolean = GodotRuntime.shouldEmbed(),
    ) {
        if (!embed) return
        val decor = activity.window.decorView as ViewGroup
        if (decor.findViewWithTag<View>(ROOT) != null) return
        val speech = SpeechEngine(activity)
        sessions[activity] = speech
        val card = card(activity, scene)
        card.addOnAttachStateChangeListener(releaseWhenDetached(activity))
        decor.addView(card)
        speech.speak("${VoiceScripts.LETS_PLAY} ${PrizeBrief.goal(scene)} ${PrizeBrief.controls(scene)}")
    }

    private fun releaseWhenDetached(activity: Activity): View.OnAttachStateChangeListener =
        object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(view: View) {
                view.tag = ROOT
            }

            override fun onViewDetachedFromWindow(view: View) {
                release(activity)
            }
        }

    private fun card(
        activity: Activity,
        scene: String,
    ): LinearLayout {
        val root =
            LinearLayout(activity).apply {
                tag = ROOT
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setBackgroundColor(Color.WHITE)
                setPadding(48, 48, 48, 48)
                layoutParams =
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER,
                    )
            }
        root.addView(line(activity, PrizeBrief.goal(scene), 28f))
        root.addView(line(activity, PrizeBrief.controls(scene), 22f))
        val play =
            Button(activity).apply {
                text = PrizeHud.PLAY_LABEL
                setOnClickListener {
                    (root.parent as? ViewGroup)?.removeView(root)
                }
            }
        root.addView(play)
        return root
    }

    private fun line(
        activity: Activity,
        text: String,
        sizeSp: Float,
    ): TextView =
        TextView(activity).apply {
            this.text = text
            setTextColor(Color.BLACK)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp)
            gravity = Gravity.CENTER
        }
}
