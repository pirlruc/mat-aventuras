package pt.mataventuras.app.prize

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import java.lang.ref.WeakReference
import pt.mataventuras.app.engine.IsolatedEngineActivity
import pt.mataventuras.domain.engine.EnginePluginContract
import pt.mataventuras.domain.prize.PrizeClock

/**
 * Stops a prize when the parent limit elapses. The clock is
 * [SystemClock.elapsedRealtime], not the wall clock.
 */
internal object PrizeLimit {
    private val main = Handler(Looper.getMainLooper())
    private val timers = HashMap<Int, Runnable>()
    private val hosts = HashMap<Int, WeakReference<Activity>>()

    /**
     * Arms [activity] from its `prize_limit` extra. A missing or zero limit does nothing.
     */
    fun arm(activity: Activity) {
        val seconds = activity.intent.getStringExtra(EnginePluginContract.EXTRA_PRIZE_LIMIT)?.toIntOrNull() ?: 0
        if (seconds <= 0) return
        val id = System.identityHashCode(activity)
        timers.remove(id)?.let { main.removeCallbacks(it) }
        hosts[id] = WeakReference(activity)
        val deadline = PrizeClock.deadlineMs(SystemClock.elapsedRealtime(), seconds)
        val tick =
            Runnable {
                timers.remove(id)
                stopIfDue(hosts.remove(id)?.get(), deadline, seconds)
            }
        timers[id] = tick
        main.postDelayed(tick, seconds * 1_000L)
    }

    /**
     * Drops a pending stop. Safe when [arm] never ran.
     */
    fun cancel(activity: Activity) {
        val id = System.identityHashCode(activity)
        hosts.remove(id)
        timers.remove(id)?.let { main.removeCallbacks(it) }
    }

    /**
     * Stops [host] when the prize time is up. A missing, finishing, or
     * not-yet-due host is left alone.
     */
    internal fun stopIfDue(
        host: Activity?,
        deadline: Long,
        seconds: Int,
    ) {
        if (host == null || host.isDestroyed || host.isFinishing) return
        if (!PrizeClock.expired(SystemClock.elapsedRealtime(), deadline, seconds)) return
        if (host is IsolatedEngineActivity) {
            host.completeReward(false)
        } else {
            host.setResult(Activity.RESULT_CANCELED)
            host.finish()
        }
    }
}
