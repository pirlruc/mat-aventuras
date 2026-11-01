package pt.mataventuras.app.engine

import android.app.Activity
import android.view.View

/**
 * Delayed Godot attach callbacks. [IsolatedEngineActivity.onDestroy] clears them
 * so a destroyed host never commits a fragment.
 */
internal object EngineAttachRetries {
    private class Wait(
        val view: View,
        val listener: View.OnLayoutChangeListener,
        val retry: Runnable,
        val force: Runnable,
    )

    private val pending = HashMap<Int, Wait>()

    /**
     * Replaces any previous wait on [activity].
     */
    fun track(
        activity: Activity,
        view: View,
        listener: View.OnLayoutChangeListener,
        retry: Runnable,
        force: Runnable,
    ) {
        cancel(activity)
        pending[System.identityHashCode(activity)] = Wait(view, listener, retry, force)
    }

    /**
     * Drops the layout listener and both [android.view.View.postDelayed] runnables.
     */
    fun cancel(activity: Activity) {
        val wait = pending.remove(System.identityHashCode(activity)) ?: return
        wait.view.removeOnLayoutChangeListener(wait.listener)
        wait.view.removeCallbacks(wait.retry)
        wait.view.removeCallbacks(wait.force)
    }
}
