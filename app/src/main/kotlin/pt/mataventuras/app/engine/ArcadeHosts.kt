package pt.mataventuras.app.engine

import android.widget.TextView
import pt.mataventuras.domain.engine.RewardGame
import pt.mataventuras.domain.voice.VoiceScripts

/**
 * Robolectric-safe native hosts for the arcade prizes.
 */
internal object NativeInvadersHost {
    fun attach(
        activity: IsolatedEngineActivity,
        showUi: Boolean = GodotRuntime.shouldEmbed(),
    ): InvadersLoop {
        val loop = InvadersLoop()
        if (!showUi) {
            activity.setContentView(TextView(activity).apply { text = VoiceScripts.INVADERS_HINT })
            return loop
        }
        NativeRewardHost.placeholder(activity, RewardGame.INVADERS)
        return loop
    }
}

/**
 * Maze-chomp native host.
 */
internal object NativeChompHost {
    fun attach(
        activity: IsolatedEngineActivity,
        showUi: Boolean = GodotRuntime.shouldEmbed(),
    ): ChompLoop {
        val loop = ChompLoop()
        if (!showUi) {
            activity.setContentView(TextView(activity).apply { text = VoiceScripts.CHOMP_HINT })
            return loop
        }
        NativeRewardHost.placeholder(activity, RewardGame.CHOMP)
        return loop
    }
}

/**
 * Letter-climb native host.
 */
internal object NativeClimbHost {
    fun attach(
        activity: IsolatedEngineActivity,
        showUi: Boolean = GodotRuntime.shouldEmbed(),
    ): ClimbLoop {
        val loop = ClimbLoop()
        if (!showUi) {
            activity.setContentView(TextView(activity).apply { text = VoiceScripts.CLIMB_HINT })
            return loop
        }
        NativeRewardHost.placeholder(activity, RewardGame.CLIMB)
        return loop
    }
}
