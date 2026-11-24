package pt.mataventuras.app.ui

import android.content.ContentResolver
import android.provider.Settings

/**
 * System animator duration scale. Zero means reduced motion: animations finish immediately.
 */
internal fun animatorDurationScale(resolver: ContentResolver): Float =
    Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)

/**
 * [baseMillis] multiplied by the animator scale. A scale of zero yields zero.
 */
internal fun scaledDurationMillis(
    baseMillis: Int,
    scale: Float,
): Int {
    if (scale <= 0f || baseMillis <= 0) return 0
    return (baseMillis * scale).toInt()
}
