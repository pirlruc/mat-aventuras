package pt.mataventuras.app.engine

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import java.lang.ref.WeakReference
import pt.mataventuras.domain.engine.PrizeHud

/**
 * Mascot overlay while a prize engine starts.
 * After [TIMEOUT_MS] the child can retry Godot or open the Canvas host.
 */
internal object PrizeBoot {
    const val TIMEOUT_MS: Long = 12_000L
    private const val TAG: String = "MatPrize"
    private const val ROOT: String = "prize-boot-root"
    private const val MASCOT: String = "prize-boot-mascot"
    private const val STATUS: String = "prize-boot-status"
    private const val RETRY: String = "prize-boot-retry"
    private const val BACK: String = "prize-boot-back"

    private val main = Handler(Looper.getMainLooper())
    private val timers = HashMap<Int, Runnable>()
    private val hosts = HashMap<Int, WeakReference<Activity>>()
    private val ready = HashSet<Int>()

    /**
     * Shows the mascot and the loading line. Buttons stay hidden until [timedOut].
     */
    fun show(
        activity: Activity,
        mascot: String,
        onRetry: () -> Unit,
        onBack: () -> Unit,
    ) {
        val id = System.identityHashCode(activity)
        ready.remove(id)
        val root = ensure(activity)
        root.visibility = View.VISIBLE
        root.findViewWithTag<TextView>(MASCOT).text = mascot
        root.findViewWithTag<TextView>(STATUS).text = PrizeHud.LOADING_LINE
        val retry = root.findViewWithTag<Button>(RETRY)
        val back = root.findViewWithTag<Button>(BACK)
        retry.visibility = View.GONE
        back.visibility = View.GONE
        retry.setOnClickListener { onRetry() }
        back.setOnClickListener { onBack() }
        Log.i(TAG, "boot-loading")
    }

    /**
     * Returns the overlay to the loading line without replacing the buttons' listeners.
     */
    fun markLoading(activity: Activity) {
        val id = System.identityHashCode(activity)
        ready.remove(id)
        val root = bootRoot(activity) ?: return
        root.visibility = View.VISIBLE
        root.findViewWithTag<TextView>(STATUS).text = PrizeHud.LOADING_LINE
        root.findViewWithTag<Button>(RETRY).visibility = View.GONE
        root.findViewWithTag<Button>(BACK).visibility = View.GONE
        Log.i(TAG, "boot-loading")
    }

    /**
     * Starts the boot timer. A ready prize cancels it.
     */
    fun arm(activity: Activity) {
        val id = System.identityHashCode(activity)
        hosts[id] = WeakReference(activity)
        timers.remove(id)?.let { main.removeCallbacks(it) }
        val tick =
            Runnable {
                timers.remove(id)
                hosts.remove(id)?.get()?.let { timedOut(it) }
            }
        timers[id] = tick
        main.postDelayed(tick, TIMEOUT_MS)
    }

    /**
     * Hides the overlay. The prize scene is on screen.
     */
    fun ready(activity: Activity) {
        val id = System.identityHashCode(activity)
        ready.add(id)
        timers.remove(id)?.let { main.removeCallbacks(it) }
        bootRoot(activity)?.visibility = View.GONE
        Log.i(TAG, "boot-ready")
    }

    /**
     * Offers retry and the Canvas host.
     */
    fun timedOut(activity: Activity) {
        val root = bootRoot(activity) ?: return
        root.visibility = View.VISIBLE
        root.findViewWithTag<TextView>(STATUS).text = PrizeHud.TIMEOUT_LINE
        root.findViewWithTag<Button>(RETRY).visibility = View.VISIBLE
        root.findViewWithTag<Button>(BACK).visibility = View.VISIBLE
        Log.i(TAG, "boot-timeout")
    }

    /**
     * Hides the overlay and drops the timer. Used when the Canvas host takes over.
     */
    fun hide(activity: Activity) {
        cancel(activity)
        bootRoot(activity)?.visibility = View.GONE
        Log.i(TAG, "boot-canvas")
    }

    private fun bootRoot(activity: Activity): View? = activity.window.decorView.findViewWithTag(ROOT)

    /**
     * Drops the timer. Safe when the overlay was never shown.
     */
    fun cancel(activity: Activity) {
        val id = System.identityHashCode(activity)
        hosts.remove(id)
        timers.remove(id)?.let { main.removeCallbacks(it) }
        ready.remove(id)
    }

    private fun ensure(activity: Activity): View {
        val content = activity.findViewById<ViewGroup>(android.R.id.content)
        val existing = content.findViewWithTag<View>(ROOT)
        if (existing != null) return existing
        val root = LinearLayout(activity)
        root.tag = ROOT
        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER
        root.setBackgroundColor(0xFF1E88E5.toInt())
        val pad = dp(activity, 24)
        root.setPadding(pad, pad, pad, pad)
        root.addView(label(activity, MASCOT, 72f))
        root.addView(label(activity, STATUS, 28f))
        root.addView(button(activity, RETRY, PrizeHud.RETRY_LABEL))
        root.addView(button(activity, BACK, PrizeHud.BACK_LABEL))
        content.addView(
            root,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )
        return root
    }

    /**
     * Godot's SurfaceView draws above ordinary views. Keep the mascot in front
     * until [ready] hides it.
     */
    fun keepAboveEngine(activity: Activity) {
        val root = bootRoot(activity) ?: return
        burySurfaces(activity.window.decorView)
        (root.parent as ViewGroup).bringChildToFront(root)
        root.bringToFront()
    }

    private fun burySurfaces(view: View) {
        if (view is SurfaceView) {
            view.setZOrderOnTop(false)
            view.setZOrderMediaOverlay(false)
        }
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) burySurfaces(view.getChildAt(index))
        }
    }

    private fun label(
        activity: Activity,
        tag: String,
        sizeSp: Float,
    ): TextView =
        TextView(activity).apply {
            this.tag = tag
            gravity = Gravity.CENTER
            textSize = sizeSp
            setTextColor(0xFFFFFFFF.toInt())
        }

    private fun button(
        activity: Activity,
        tag: String,
        title: String,
    ): Button =
        Button(activity).apply {
            this.tag = tag
            text = title
            visibility = View.GONE
            minimumHeight = dp(activity, 64)
            minimumWidth = dp(activity, 64)
        }

    private fun dp(
        activity: Activity,
        value: Int,
    ): Int = (value * activity.resources.displayMetrics.density).toInt().coerceAtLeast(1)
}
