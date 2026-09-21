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
                placeholder(activity, game)
                activity.invaders = InvadersLoop()
            }
            RewardGame.CHOMP -> {
                placeholder(activity, game)
                activity.chomp = ChompLoop()
            }
            RewardGame.CLIMB -> {
                placeholder(activity, game)
                activity.climb = ClimbLoop()
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
     * Robolectric / missing-plugin placeholder view.
     */
    fun placeholder(
        activity: IsolatedEngineActivity,
        game: RewardGame,
    ) {
        activity.setContentView(TextView(activity).apply { text = hint(game) })
    }
}
