package pt.mataventuras.app.engine

import android.widget.TextView
import pt.mataventuras.domain.engine.RewardGame
import pt.mataventuras.domain.voice.VoiceScripts
import pt.mataventuras.plugin.RunnerPluginActivity

/**
 * Native Canvas fallback for every 2D prize game.
 */
internal object NativeRewardHost {
    /**
     * Attaches the simulation for [game]. Runner keeps [RunnerPluginActivity.loop]
     * so existing tests still tick the platformer.
     */
    fun attach(
        activity: RunnerPluginActivity,
        game: RewardGame,
    ) {
        when (game) {
            RewardGame.INVADERS -> {
                activity.invaders = NativeInvadersHost.attach(activity)
            }
            RewardGame.CHOMP -> {
                activity.chomp = NativeChompHost.attach(activity)
            }
            RewardGame.CLIMB -> {
                activity.climb = NativeClimbHost.attach(activity)
            }
            else -> {
                activity.loop = NativeRunnerHost.attach(activity)
            }
        }
    }

    /**
     * Hint copy for [game].
     */
    fun hint(game: RewardGame): String =
        when (game) {
            RewardGame.INVADERS -> VoiceScripts.INVADERS_HINT
            RewardGame.CHOMP -> VoiceScripts.CHOMP_HINT
            RewardGame.CLIMB -> VoiceScripts.CLIMB_HINT
            RewardGame.KART -> VoiceScripts.STEER_HINT
            RewardGame.RUNNER -> VoiceScripts.JUMP_HINT
        }

    /**
     * Robolectric placeholder view.
     */
    fun placeholder(
        activity: IsolatedEngineActivity,
        game: RewardGame,
    ) {
        activity.setContentView(TextView(activity).apply { text = hint(game) })
    }
}
