package pt.mataventuras.app.engine

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import pt.mataventuras.domain.engine.ArcadeSpan

/**
 * Native Canvas for one arcade prize. Touch steps the simulation; there is
 * no frame clock, so Robolectric does not stay busy.
 */
internal class ArcadeBoardView(
    context: Context,
) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val spans = ArrayList<ArcadeSpan>(48)

    /** Repaints from the current simulation. */
    var refresh: () -> Unit = {}

    /** Normalised finger position and [MotionEvent] action. */
    var onNorm: (Float, Float, Int) -> Unit = { _, _, _ -> }

    /**
     * Replaces the rectangles drawn on the next frame.
     */
    fun show(next: List<ArcadeSpan>) {
        val copy = ArrayList(next)
        spans.clear()
        spans.addAll(copy)
        invalidate()
    }

    /** How many rectangles the last [show] stored. */
    fun spanCount(): Int = spans.size

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int,
    ) {
        refresh()
    }

    override fun onDraw(canvas: Canvas) {
        for (span in spans) {
            paint.color = span.argb.toInt()
            canvas.drawRect(span.x, span.y, span.x + span.w, span.y + span.h, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val widthPx = width.coerceAtLeast(1).toFloat()
        val heightPx = height.coerceAtLeast(1).toFloat()
        onNorm(event.x / widthPx, event.y / heightPx, event.actionMasked)
        if (event.actionMasked == MotionEvent.ACTION_UP) performClick()
        return true
    }

    override fun performClick(): Boolean = super.performClick()
}
