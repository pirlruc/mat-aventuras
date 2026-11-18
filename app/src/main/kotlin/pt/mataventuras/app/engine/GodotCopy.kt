package pt.mataventuras.app.engine

import android.content.Context
import pt.mataventuras.app.R

/**
 * Godot HUD words from the bundled language pack.
 */
object GodotCopy {
    private val ids: Map<String, Int> =
        mapOf(
            "exit" to R.string.godot_exit,
            "boost" to R.string.godot_boost,
            "play" to R.string.godot_play,
            "won" to R.string.godot_won,
            "game_over" to R.string.godot_game_over,
            "off_track" to R.string.godot_off_track,
            "grew" to R.string.godot_grew,
            "powered" to R.string.godot_powered,
            "star" to R.string.godot_star,
            "kart_hud" to R.string.godot_kart_hud,
            "invaders_play" to R.string.godot_invaders_play,
            "invaders_won" to R.string.godot_invaders_won,
            "climb_play" to R.string.godot_climb_play,
            "climb_won" to R.string.godot_climb_won,
            "chomp_play" to R.string.godot_chomp_play,
            "chomp_won" to R.string.godot_chomp_won,
            "runner_play" to R.string.godot_runner_play,
        )

    /**
     * Localized [key], or empty when the key is unknown.
     */
    fun text(
        context: Context,
        key: String,
    ): String {
        val id = ids[key] ?: 0
        if (id == 0) return ""
        return context.getString(id)
    }
}
