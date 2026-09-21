package pt.mataventuras.app.engine

import android.view.MotionEvent
import android.widget.TextView
import pt.mataventuras.domain.engine.ArcadeInput
import pt.mataventuras.domain.engine.ArcadeScene
import pt.mataventuras.domain.engine.ArcadeSpan
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
            RewardGame.INVADERS -> attachInvaders(activity)
            RewardGame.CHOMP -> attachChomp(activity)
            RewardGame.CLIMB -> attachClimb(activity)
            else -> {
                activity.loop = NativeRunnerHost.attach(activity)
            }
        }
    }

    private fun attachInvaders(activity: RunnerPluginActivity) {
        val loop = InvadersLoop()
        activity.invaders = loop
        val spans = ArrayList<ArcadeSpan>(48)
        mount(activity, spans) { width, height ->
            ArcadeScene.fillInvaders(spans, loop.state, width, height)
        }.also { board ->
            board.onNorm = { nx, _, action ->
                loop.moveX = ArcadeInput.steer(nx)
                if (action == MotionEvent.ACTION_UP) loop.fire = true
                if (action != MotionEvent.ACTION_DOWN) {
                    val state = loop.tick()
                    settle(activity, state.finished && state.alive, state.finished || !state.alive)
                    board.refresh()
                }
            }
        }
    }

    private fun attachChomp(activity: RunnerPluginActivity) {
        val loop = ChompLoop()
        activity.chomp = loop
        val spans = ArrayList<ArcadeSpan>(48)
        var originX = 0f
        var originY = 0f
        mount(activity, spans) { width, height ->
            ArcadeScene.fillChomp(spans, loop.state, width, height)
        }.also { board ->
            board.onNorm = { nx, ny, action ->
                if (action == MotionEvent.ACTION_DOWN) {
                    originX = nx
                    originY = ny
                } else {
                    val dir = ArcadeInput.chompDir(nx - originX, ny - originY)
                    loop.dirX = dir.first
                    loop.dirY = dir.second
                    val state = loop.tick()
                    settle(activity, state.finished && state.alive, state.finished || !state.alive)
                    board.refresh()
                }
            }
        }
    }

    private fun attachClimb(activity: RunnerPluginActivity) {
        val loop = ClimbLoop()
        activity.climb = loop
        val spans = ArrayList<ArcadeSpan>(48)
        var originY = 0f
        mount(activity, spans) { width, height ->
            ArcadeScene.fillClimb(spans, loop.state, width, height)
        }.also { board ->
            board.onNorm = { nx, ny, action ->
                if (action == MotionEvent.ACTION_DOWN) {
                    originY = ny
                } else {
                    loop.moveX = ArcadeInput.steer(nx)
                    if (action == MotionEvent.ACTION_UP) {
                        loop.jumping = ArcadeInput.climbJump(ny - originY)
                    }
                    val state = loop.tick()
                    settle(activity, state.finished && state.alive, state.finished || !state.alive)
                    board.refresh()
                }
            }
        }
    }

    private fun mount(
        activity: RunnerPluginActivity,
        spans: MutableList<ArcadeSpan>,
        fill: (Float, Float) -> Unit,
    ): ArcadeBoardView {
        val board = ArcadeBoardView(activity)
        board.refresh = {
            val width = board.width.coerceAtLeast(1).toFloat()
            val height = board.height.coerceAtLeast(1).toFloat()
            fill(width, height)
            board.show(spans)
        }
        activity.setContentView(board)
        board.refresh()
        return board
    }

    private fun settle(
        activity: RunnerPluginActivity,
        won: Boolean,
        done: Boolean,
    ) {
        if (done) activity.completeReward(won)
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
