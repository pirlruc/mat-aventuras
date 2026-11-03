package pt.mataventuras.app.ui

import kotlin.math.pow

/** Orange Continuar on the blue win card. White text sits on this fill. */
internal const val CONTINUE_BUTTON_ARGB: Long = 0xFFBF360C

/** Shape glyphs drawn on the primary blue option button. */
internal const val SHAPE_ON_BUTTON_ARGB: Long = 0xFFFFFFFF

/** Theme primary, also the win-card fill. */
internal const val PRIMARY_BUTTON_ARGB: Long = 0xFF1565C0

/**
 * WCAG contrast ratio between two opaque ARGB colours.
 */
internal fun contrastRatio(
    foreground: Long,
    background: Long,
): Double {
    val lighter = maxOf(relativeLuminance(foreground), relativeLuminance(background))
    val darker = minOf(relativeLuminance(foreground), relativeLuminance(background))
    return (lighter + 0.05) / (darker + 0.05)
}

private fun relativeLuminance(argb: Long): Double {
    fun channel(value: Int): Double {
        val scaled = value / 255.0
        return if (scaled <= 0.03928) scaled / 12.92 else ((scaled + 0.055) / 1.055).pow(2.4)
    }
    val red = channel(((argb shr 16) and 0xFF).toInt())
    val green = channel(((argb shr 8) and 0xFF).toInt())
    val blue = channel((argb and 0xFF).toInt())
    return 0.2126 * red + 0.7152 * green + 0.0722 * blue
}
