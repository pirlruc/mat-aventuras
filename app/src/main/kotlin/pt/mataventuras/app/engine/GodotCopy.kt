package pt.mataventuras.app.engine

import android.content.Context
import pt.mataventuras.app.R

/**
 * Godot HUD words from the bundled language pack.
 */
object GodotCopy {
    /**
     * Localized [key], or empty when the key is unknown.
     */
    fun text(
        context: Context,
        key: String,
    ): String {
        val id =
            when (key) {
                "exit" -> R.string.godot_exit
                "boost" -> R.string.godot_boost
                "play" -> R.string.godot_play
                else -> 0
            }
        if (id == 0) return ""
        return context.getString(id)
    }
}
