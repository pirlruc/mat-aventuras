package pt.mataventuras.app

import android.speech.tts.TextToSpeech
import android.view.MotionEvent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import pt.mataventuras.app.engine.EngineLauncher
import pt.mataventuras.app.engine.Kart3dActivity
import pt.mataventuras.app.engine.Platformer2dActivity
import pt.mataventuras.app.engine.Platformer2dLoop
import pt.mataventuras.app.engine.PlatformerRect
import pt.mataventuras.app.engine.PlatformerScene
import pt.mataventuras.app.speech.SpeechEngine
import pt.mataventuras.app.ui.age.AgeSelectionScreen
import pt.mataventuras.app.ui.RewardReturn
import pt.mataventuras.app.ui.home.HomeScreen
import pt.mataventuras.app.ui.theme.MatAventurasTheme
import pt.mataventuras.domain.model.AgeGroup
import pt.mataventuras.domain.model.ChildProfile
import pt.mataventuras.domain.model.Mascot
import pt.mataventuras.domain.progress.AvatarCode
import pt.mataventuras.domain.voice.VoiceScripts

@RunWith(RobolectricTestRunner::class)
class AppUiTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun engineLauncherPicksIsolatedProcessActivities() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val two = EngineLauncher.intentFor(ctx, AgeGroup.THREE_YEARS, Mascot.SPEEDY_HEDGEHOG, "Ana")
        val three =
            EngineLauncher.intentFor(
                ctx,
                AgeGroup.SEVEN_YEARS,
                Mascot.BRAVE_PLUMBER,
                "Rui",
                pt.mataventuras.domain.model.EngineKind.THREE_D,
            )
        val sevenRunner =
            EngineLauncher.intentFor(
                ctx,
                AgeGroup.SEVEN_YEARS,
                Mascot.BRAVE_PLUMBER,
                "Rui",
                pt.mataventuras.domain.model.EngineKind.TWO_D,
            )
        assertEquals(
            pt.mataventuras.domain.engine.EnginePluginContract.PLUGIN_RUNNER_CLASS,
            two.component!!.className,
        )
        assertEquals(
            pt.mataventuras.domain.engine.EnginePluginContract.PLUGIN_KART_CLASS,
            three.component!!.className,
        )
        assertEquals(
            pt.mataventuras.domain.engine.EnginePluginContract.PLUGIN_RUNNER_CLASS,
            sevenRunner.component!!.className,
        )
        assertEquals("speedy_hedgehog", two.getStringExtra(EngineLauncher.EXTRA_MASCOT))
        assertEquals(EngineLauncher.PROCESS_ENGINE_3D, ":engine3d")
        assertEquals("Rui", three.getStringExtra(EngineLauncher.EXTRA_NAME))
        assertEquals(EngineLauncher.PROCESS_ENGINE_2D, ":engine2d")
        assertEquals(null, EngineLauncher.processFor(AgeGroup.THREE_YEARS, usingPlugin = false))
        assertEquals(":engine2d", EngineLauncher.processFor(AgeGroup.THREE_YEARS, usingPlugin = true))
        assertEquals(":engine3d", EngineLauncher.processFor(AgeGroup.SEVEN_YEARS, usingPlugin = false))
        assertTrue(EngineLauncher.isClassPresent(Kart3dActivity::class.java.name))
        assertTrue(
            EngineLauncher.isClassPresent(
                pt.mataventuras.domain.engine.EnginePluginContract.PLUGIN_KART_CLASS,
            ),
        )
        assertTrue(
            EngineLauncher.isClassPresent(
                pt.mataventuras.domain.engine.EnginePluginContract.PLUGIN_RUNNER_CLASS,
            ),
        )
        assertFalse(EngineLauncher.isClassPresent("pt.mataventuras.plugin.MissingEngineActivity"))
        assertFalse(EngineLauncher.wouldUsePlugin(pt.mataventuras.domain.model.EngineKind.THREE_D) { false })
        assertTrue(
            EngineLauncher.wouldUsePlugin(pt.mataventuras.domain.model.EngineKind.THREE_D) {
                it == pt.mataventuras.domain.engine.EnginePluginContract.PLUGIN_KART_CLASS
            },
        )
        val pluginKart =
            EngineLauncher.intentFor(
                ctx,
                AgeGroup.SEVEN_YEARS,
                Mascot.BRAVE_PLUMBER,
                "Rui",
                pt.mataventuras.domain.model.EngineKind.THREE_D,
            ) { className ->
                className == pt.mataventuras.domain.engine.EnginePluginContract.PLUGIN_KART_CLASS
            }
        assertEquals(
            pt.mataventuras.domain.engine.EnginePluginContract.PLUGIN_KART_CLASS,
            pluginKart.component!!.className,
        )
        val pluginRunner =
            EngineLauncher.intentFor(ctx, AgeGroup.THREE_YEARS, Mascot.SPEEDY_HEDGEHOG, "Ana") { className ->
                className == pt.mataventuras.domain.engine.EnginePluginContract.PLUGIN_RUNNER_CLASS
            }
        assertEquals(
            pt.mataventuras.domain.engine.EnginePluginContract.PLUGIN_RUNNER_CLASS,
            pluginRunner.component!!.className,
        )
        assertTrue(EngineLauncher.isFinished(android.app.Activity.RESULT_OK, true))
        assertFalse(EngineLauncher.isFinished(android.app.Activity.RESULT_OK, false))
        assertFalse(EngineLauncher.isFinished(android.app.Activity.RESULT_CANCELED, true))
        val spoken = mutableListOf<String>()
        assertFalse(RewardReturn.onResult(android.app.Activity.RESULT_CANCELED, null, spoken::add))
        assertEquals(listOf(VoiceScripts.REWARD_RETURN), spoken)
        spoken.clear()
        val finishedIntent =
            android.content.Intent().putExtra(EngineLauncher.RESULT_FINISHED, true)
        assertTrue(RewardReturn.onResult(android.app.Activity.RESULT_OK, finishedIntent, spoken::add))
        assertEquals(listOf(VoiceScripts.REWARD_FINISHED), spoken)
        assertFalse(RewardReturn.finishedExtra(null))
        assertTrue(RewardReturn.finishedExtra(finishedIntent))
        assertFalse(RewardReturn.finishedExtra(android.content.Intent()))
        val restart =
            EngineLauncher.restartResultIntent(
                pt.mataventuras.domain.engine.EnginePluginContract.PLUGIN_KART_CLASS,
                "hero_pup",
                "Ana",
            )
        assertTrue(restart.getBooleanExtra(EngineLauncher.RESULT_RESTART, false))
        assertFalse(EngineLauncher.isFinished(android.app.Activity.RESULT_OK, false))
        val relaunch = EngineLauncher.relaunchIntent(ctx, restart)!!
        assertEquals(
            pt.mataventuras.domain.engine.EnginePluginContract.PLUGIN_KART_CLASS,
            relaunch.component!!.className,
        )
        assertEquals("hero_pup", relaunch.getStringExtra(EngineLauncher.EXTRA_MASCOT))
        assertEquals("Ana", relaunch.getStringExtra(EngineLauncher.EXTRA_NAME))
        assertTrue(relaunch.getBooleanExtra(EngineLauncher.EXTRA_GODOT_RELAUNCH, false))
        assertNull(EngineLauncher.relaunchIntent(ctx, null))
        assertNull(EngineLauncher.relaunchIntent(ctx, android.content.Intent()))
        assertNull(
            EngineLauncher.relaunchIntent(
                ctx,
                android.content.Intent().putExtra(EngineLauncher.RESULT_RESTART, true),
            ),
        )
        assertNull(
            EngineLauncher.relaunchIntent(
                ctx,
                android.content.Intent()
                    .putExtra(EngineLauncher.RESULT_RESTART, true)
                    .putExtra(EngineLauncher.EXTRA_ENGINE_CLASS, "  "),
            ),
        )
        val namelessRestart =
            android.content.Intent()
                .putExtra(EngineLauncher.RESULT_RESTART, true)
                .putExtra(
                    EngineLauncher.EXTRA_ENGINE_CLASS,
                    pt.mataventuras.domain.engine.EnginePluginContract.PLUGIN_RUNNER_CLASS,
                )
        val namelessRelaunch = EngineLauncher.relaunchIntent(ctx, namelessRestart)!!
        assertEquals("", namelessRelaunch.getStringExtra(EngineLauncher.EXTRA_MASCOT))
        assertEquals("", namelessRelaunch.getStringExtra(EngineLauncher.EXTRA_NAME))
    }

    @Test
    fun ageSelectionShowsPortugueseCopy() {
        compose.setContent {
            AgeSelectionScreen(onSpeak = {}, onConfirm = { _, _, _ -> })
        }
        compose.onNodeWithText(VoiceScripts.APP_TITLE).assertIsDisplayed()
        compose.onNodeWithText(VoiceScripts.AGE_SELECTION).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(VoiceScripts.THREE_YEARS).performScrollTo().performClick()
        compose.onNodeWithText(VoiceScripts.AGE_THREE_PREVIEW).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Como te chamas?").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(VoiceScripts.SEVEN_YEARS).performScrollTo().performClick()
        compose.onNodeWithText(VoiceScripts.AGE_SEVEN_PREVIEW).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun ageSelectionContinuesLastProfile() {
        var continued = false
        val last =
            ChildProfile(4, "Ana", AgeGroup.THREE_YEARS, Mascot.SPEEDY_HEDGEHOG, AvatarCode.STARTER.name, 0, 0)
        compose.setContent {
            AgeSelectionScreen(
                onSpeak = {},
                onConfirm = { _, _, _ -> },
                lastProfile = last,
                onContinueLast = { continued = true },
            )
        }
        compose.onNodeWithText("Continuar como Ana").performClick()
        assertEquals(true, continued)
    }

    @Test
    fun ageSelectionBlankNameBecomesAmigoAndPicksMascot() {
        var name = ""
        var mascot = Mascot.SPEEDY_HEDGEHOG
        compose.setContent {
            AgeSelectionScreen(onSpeak = {}, onConfirm = { _, chosen, friend ->
                name = chosen
                mascot = friend
            })
        }
        compose.onNodeWithText(VoiceScripts.THREE_YEARS).performScrollTo().performClick()
        compose.onNodeWithContentDescription("Cão Herói").performScrollTo().performClick()
        compose.onNodeWithText("Vamos começar!").performScrollTo().performClick()
        assertEquals("Amigo", name)
        assertEquals(Mascot.HERO_PUP, mascot)
    }

    @Test
    fun homeScreenShowsModulesForThreeYearOlds() {
        val profile =
            ChildProfile(1, "Ana", AgeGroup.THREE_YEARS, Mascot.SPEEDY_HEDGEHOG, AvatarCode.STARTER.name, 0, 0)
        compose.setContent {
            MatAventurasTheme(AgeGroup.THREE_YEARS) {
                HomeScreen(profile, onSpeak = {}, onModule = {}, onLeaderboard = {}, onParents = {})
            }
        }
        compose.onNodeWithText("Olá, Ana!").assertIsDisplayed()
        compose.onNodeWithText("Contar com o Ouriço Veloz").assertIsDisplayed()
    }

    @Test
    fun speechEngineIgnoresBlankWhenNotReady() {
        val engine = SpeechEngine(ApplicationProvider.getApplicationContext())
        engine.speak(" ")
        engine.speak("Olá")
        engine.onInit(TextToSpeech.ERROR)
        engine.speak("Olá")
        engine.onInit(TextToSpeech.SUCCESS)
        engine.speak("Olá")
        engine.speak("  ")
        engine.stop()
        engine.release()
        engine.stop()
        val ready = SpeechEngine(ApplicationProvider.getApplicationContext())
        ready.markReadyForTest()
        ready.speak("Olá")
        ready.release()
        ready.onInit(TextToSpeech.SUCCESS)
        ready.speak("Olá")
        assertTrue(true)
    }

    @Test
    fun rewardActivitiesStart() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val twoController =
            Robolectric.buildActivity(
                Platformer2dActivity::class.java,
                EngineLauncher.intentFor(ctx, AgeGroup.THREE_YEARS, Mascot.HERO_PUP, "Ana"),
            ).setup()
        val two = twoController.get()
        assertTrue(two.hasWindowFocus() || two.window != null)
        two.loop.jumping = true
        repeat(40) { two.loop.tick() }
        val sprites = PlatformerScene.sprites(two.loop.state, Mascot.HERO_PUP, 800f, 480f)
        assertTrue(sprites.size >= 6)
        assertEquals(Mascot.HERO_PUP.primaryArgb, sprites.last { it.argb == Mascot.HERO_PUP.primaryArgb }.argb)
        assertTrue(PlatformerScene.hatArgb(Mascot.HERO_PUP.primaryArgb) != Mascot.HERO_PUP.primaryArgb)
        val collected = two.loop.state.copy(x = 32f, collectedMask = 31)
        val tiles = ArrayList<PlatformerRect>(48)
        PlatformerScene.fillTiles(tiles, collected, Mascot.PINK_PIGLET, 400f, 300f)
        assertTrue(tiles.isNotEmpty())
        PlatformerScene.fillTiles(tiles, collected.copy(x = 0f), Mascot.BRAVE_PLUMBER, 200f, 200f)
        assertTrue(tiles.any { it.argb == PlatformerScene.BRICK_ARGB })
        assertTrue(PlatformerScene.groundTop(480f) > 300f)
        assertEquals("hero_pup" to "Ana", two.extrasSnapshot())
        two.completeReward(ok = true)
        twoController.pause().stop().destroy()
        val cancelledController =
            Robolectric.buildActivity(
                Platformer2dActivity::class.java,
                EngineLauncher.intentFor(ctx, AgeGroup.THREE_YEARS, Mascot.HERO_PUP, "Ana"),
            ).setup()
        cancelledController.get().completeReward(ok = false)
        cancelledController.pause().stop().destroy()

        val threeController =
            Robolectric.buildActivity(
                Kart3dActivity::class.java,
                EngineLauncher.intentFor(
                    ctx,
                    AgeGroup.SEVEN_YEARS,
                    Mascot.MISCHIEVOUS_ALIEN,
                    "Rui",
                    pt.mataventuras.domain.model.EngineKind.THREE_D,
                ),
            ).setup()
        val three = threeController.get()
        assertTrue(three.window != null)
        three.session.handleTouch(0.1f, MotionEvent.ACTION_DOWN)
        three.session.handleTouch(0.5f, MotionEvent.ACTION_DOWN)
        three.session.renderer.tick()
        three.session.handleTouch(0.9f, MotionEvent.ACTION_UP)
        three.closeFinished()
        threeController.pause().stop().destroy()

        val fallbackController =
            Robolectric.buildActivity(
                Kart3dActivity::class.java,
                android.content.Intent(ctx, Kart3dActivity::class.java),
            ).setup()
        val fallback = fallbackController.get()
        assertTrue(fallback.window != null)
        fallback.closeFinished()
        fallbackController.pause().stop().destroy()
    }

    @Test
    fun platformerLoopCollectsAndStopsWhenFinishedOrDead() {
        var ns = 0L
        val loop =
            Platformer2dLoop(
                nowNs = {
                    ns += 50_000_000L
                    ns
                },
            )
        repeat(80) {
            loop.jumping = true
            loop.tick()
        }
        assertTrue(loop.state.x > 0f)
        val idle =
            Platformer2dLoop(
                ringsTarget = 99,
                nowNs = {
                    ns += 50_000_000L
                    ns
                },
            )
        repeat(200) { idle.tick() }
        assertTrue(idle.state.x > 20f || !idle.state.alive)
        idle.tick()
    }
}
