package pt.mataventuras.app

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import java.util.ArrayList
import pt.mataventuras.app.engine.ChompLoop
import pt.mataventuras.app.engine.ClimbLoop
import pt.mataventuras.app.engine.EngineLauncher
import pt.mataventuras.app.engine.InvadersLoop
import pt.mataventuras.app.engine.NativeChompHost
import pt.mataventuras.app.engine.NativeClimbHost
import pt.mataventuras.app.engine.NativeInvadersHost
import pt.mataventuras.app.engine.NativeRewardHost
import pt.mataventuras.app.engine.OffroadScene
import pt.mataventuras.app.engine.OffroadSpan
import pt.mataventuras.app.engine.PlatformerRect
import pt.mataventuras.app.engine.PlatformerScene
import pt.mataventuras.domain.engine.ChompEngine
import pt.mataventuras.domain.engine.ClimbEngine
import pt.mataventuras.domain.engine.InvadersEngine
import pt.mataventuras.domain.engine.OffroadCircuit
import pt.mataventuras.domain.engine.OffroadRacerEngine
import pt.mataventuras.domain.engine.Platformer2dEngine
import pt.mataventuras.domain.engine.PlatformerWorld
import pt.mataventuras.domain.engine.RewardGame
import pt.mataventuras.domain.engine.RivalRacer
import pt.mataventuras.domain.model.AgeGroup
import pt.mataventuras.domain.model.Mascot
import pt.mataventuras.plugin.RunnerPluginActivity

@RunWith(RobolectricTestRunner::class)
class EngineCoverageTest {
    @Test
    fun arcadeLoopsNoOpWhenSettledAndHostsPaintBothUiModes() {
        val invadersDone =
            InvadersLoop(start = InvadersEngine().initial().copy(finished = true))
        assertEquals(invadersDone.state, invadersDone.tick())
        val invadersDead =
            InvadersLoop(start = InvadersEngine().initial().copy(alive = false))
        assertEquals(invadersDead.state, invadersDead.tick())
        val chompDone = ChompLoop(start = ChompEngine().initial().copy(finished = true))
        assertEquals(chompDone.state, chompDone.tick())
        val chompDead = ChompLoop(start = ChompEngine().initial().copy(alive = false))
        assertEquals(chompDead.state, chompDead.tick())
        val climbDone = ClimbLoop(start = ClimbEngine().initial().copy(finished = true))
        assertEquals(climbDone.state, climbDone.tick())
        val climbDead = ClimbLoop(start = ClimbEngine().initial().copy(alive = false))
        assertEquals(climbDead.state, climbDead.tick())

        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent =
            EngineLauncher.intentFor(
                ctx,
                AgeGroup.SEVEN_YEARS,
                Mascot.HERO_PUP,
                "Ana",
                game = RewardGame.INVADERS,
            )
        val controller = Robolectric.buildActivity(RunnerPluginActivity::class.java, intent).setup()
        val activity = controller.get()
        assertEquals(RewardGame.INVADERS.name, activity.sceneCode())
        NativeInvadersHost.attach(activity, showUi = true)
        NativeInvadersHost.attach(activity, showUi = false)
        NativeChompHost.attach(activity, showUi = true)
        NativeChompHost.attach(activity, showUi = false)
        NativeClimbHost.attach(activity, showUi = true)
        NativeClimbHost.attach(activity, showUi = false)
        NativeRewardHost.placeholder(activity, RewardGame.KART)
        NativeRewardHost.placeholder(activity, RewardGame.RUNNER)
        controller.pause().stop().destroy()
    }

    @Test
    fun offroadAndPlatformerScenesPaintSkipAndWrapBranches() {
        val circuit = OffroadCircuit(1)
        val start = OffroadRacerEngine(circuit).initial()
        val spans = ArrayList<OffroadSpan>(96)
        OffroadScene.fill(spans, start, circuit, Mascot.HERO_PUP, 800f, 480f)
        assertTrue(spans.any { it.argb == OffroadScene.POST_ARGB })
        assertTrue(OffroadScene.DRAW_AHEAD > circuit.gateDistance(0))

        OffroadScene.fill(
            spans,
            start.copy(distance = 120f, collectedMask = 1),
            circuit,
            Mascot.BRAVE_PLUMBER,
            400f,
            300f,
        )
        OffroadScene.fill(
            spans,
            start.copy(distance = 400f, boostTimer = 0.4f, steer = 0.5f),
            circuit,
            Mascot.SPEEDY_HEDGEHOG,
            320f,
            200f,
        )
        assertTrue(spans.any { it.argb == OffroadScene.BANNER_ARGB })
        assertTrue(spans.any { it.argb == OffroadScene.FLAME_ARGB })

        OffroadScene.fill(
            spans,
            start.copy(distance = 479.5f),
            circuit,
            Mascot.HERO_PUP,
            400f,
            240f,
        )
        OffroadScene.fill(
            spans,
            start.copy(distance = 500f),
            circuit,
            Mascot.PINK_PIGLET,
            400f,
            240f,
        )
        OffroadScene.fill(
            spans,
            start.copy(
                rivals =
                    listOf(
                        RivalRacer(start.distance + 2f, 0f, 24f, 0, 0xFFE53935),
                        RivalRacer(start.distance + 20f, 0.1f, 24f, 0, 0xFF1E88E5),
                        RivalRacer(start.distance + 300f, -0.1f, 24f, 0, 0xFF8E24AA),
                        RivalRacer(start.distance - 10f, 0f, 24f, 0, 0xFF43A047),
                    ),
            ),
            circuit,
            Mascot.MISCHIEVOUS_ALIEN,
            640f,
            360f,
        )

        val level = PlatformerWorld.random(1)
        val tiles = ArrayList<PlatformerRect>(64)
        val play = Platformer2dEngine().initial()
        PlatformerScene.fillTiles(tiles, play, Mascot.HERO_PUP, 640f, 360f, level)
        PlatformerScene.fillTiles(
            tiles,
            play.copy(form = 2, collectedMask = Int.MAX_VALUE, powerMask = Int.MAX_VALUE, stompedMask = Int.MAX_VALUE),
            Mascot.HERO_PUP,
            640f,
            360f,
            level,
        )
        assertTrue(tiles.any { it.argb == 0xFFFFF176 })
        assertTrue(PlatformerScene.hatArgb(0xFF00FF00) != 0L)
        assertTrue(PlatformerScene.sprites(play, Mascot.PINK_PIGLET, 320f, 200f).isNotEmpty())
    }
}
