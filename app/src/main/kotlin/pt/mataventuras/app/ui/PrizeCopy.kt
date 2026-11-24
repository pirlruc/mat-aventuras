package pt.mataventuras.app.ui

import android.content.Context
import pt.mataventuras.app.R

/**
 * Prize goal and controls from the language pack.
 * Callers pass [pt.mataventuras.domain.engine.PrizeBrief.composeKey] for a Compose prize.
 */
fun Context.prizeGoal(scene: String): String = getString(prizeIds(scene).first)

/** Control sentence for [scene]. */
fun Context.prizeControls(scene: String): String = getString(prizeIds(scene).second)

/** Spoken introduction: let's play, the goal, then the controls. */
fun Context.prizeIntro(scene: String): String =
    getString(R.string.voice_lets_play) + " " + prizeGoal(scene) + " " + prizeControls(scene)

private fun prizeIds(scene: String): Pair<Int, Int> =
    PRIZES[scene.uppercase()] ?: (R.string.prize_goal_fallback to R.string.prize_controls_fallback)

private val PRIZES: Map<String, Pair<Int, Int>> =
    mapOf(
        "RUNNER" to (R.string.prize_goal_runner to R.string.prize_controls_runner),
        "RUNNER_GATE" to (R.string.prize_goal_runner_gate to R.string.prize_controls_runner_gate),
        "CLIMB" to (R.string.prize_goal_climb to R.string.prize_controls_climb),
        "CHOMP" to (R.string.prize_goal_chomp to R.string.prize_controls_chomp),
        "INVADERS" to (R.string.prize_goal_invaders to R.string.prize_controls_invaders),
        "KART" to (R.string.prize_goal_kart to R.string.prize_controls_kart),
        "BALLOONS" to (R.string.prize_goal_balloons to R.string.prize_controls_balloons),
        "FISHING" to (R.string.prize_goal_fishing to R.string.prize_controls_fishing),
        "COLOURING" to (R.string.prize_goal_colouring to R.string.prize_controls_colouring),
        "BUBBLES" to (R.string.prize_goal_bubbles to R.string.prize_controls_bubbles),
        "SNAKE" to (R.string.prize_goal_snake to R.string.prize_controls_snake),
        "BREAKOUT" to (R.string.prize_goal_breakout to R.string.prize_controls_breakout),
        "TOWER" to (R.string.prize_goal_tower to R.string.prize_controls_tower),
    )
