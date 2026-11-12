package pt.mataventuras.app.ui

import java.util.concurrent.atomic.AtomicBoolean

/**
 * One lesson tap at a time. The screen calls [release] only after the exercise
 * advance (or the miss) has been composed, so a second tap cannot score twice.
 */
internal class AnswerGate {
    private val busy = AtomicBoolean(false)

    /**
     * True when this tap may score. A second call stays false until [release].
     */
    fun tryBegin(): Boolean {
        if (!shouldAcceptAnswer(busy.get())) return false
        return busy.compareAndSet(false, true)
    }

    /**
     * Opens the gate after the new exercise is on screen.
     */
    fun release() {
        busy.set(false)
    }
}
