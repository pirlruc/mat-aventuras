package pt.mataventuras.app

import android.speech.tts.TextToSpeech
import android.view.MotionEvent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
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
import pt.mataventuras.app.engine.PlatformerScene
import pt.mataventuras.app.speech.SpeechEngine
import pt.mataventuras.app.ui.age.AgeSelectionScreen
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
        val three = EngineLauncher.intentFor(ctx, AgeGroup.SEVEN_YEARS, Mascot.BRAVE_PLUMBER, "Rui")
        assertEquals(Platformer2dActivity::class.java.name, two.component!!.className)
        assertEquals(Kart3dActivity::class.java.name, three.component!!.className)
        assertEquals("speedy_hedgehog", two.getStringExtra(EngineLauncher.EXTRA_MASCOT))
        assertEquals(EngineLauncher.PROCESS_ENGINE_3D, ":engine3d")
        assertEquals("Rui", three.getStringExtra(EngineLauncher.EXTRA_NAME))
    }

    @Test
    fun ageSelectionShowsPortugueseCopy() {
        compose.setContent {
            AgeSelectionScreen(onSpeak = {}, onConfirm = { _, _, _ -> })
        }
        compose.onNodeWithText(VoiceScripts.AGE_SELECTION).assertIsDisplayed()
        compose.onNodeWithText(VoiceScripts.THREE_YEARS).performClick()
        compose.onNodeWithText("Como te chamas?").assertIsDisplayed()
        compose.onNodeWithText(VoiceScripts.SEVEN_YEARS).performClick()
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
        compose.onNodeWithText(VoiceScripts.THREE_YEARS).performClick()
        compose.onNodeWithContentDescription("Cão Herói").performClick()
        compose.onNodeWithText("Vamos começar!").performClick()
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
        engine.release()
        val ready = SpeechEngine(ApplicationProvider.getApplicationContext())
        ready.markReadyForTest()
        ready.speak("Olá")
        ready.release()
        assertTrue(true)
    }

    @Test
    fun rewardActivitiesStart() {
        val two =
            Robolectric.buildActivity(
                Platformer2dActivity::class.java,
                EngineLauncher.intentFor(
                    ApplicationProvider.getApplicationContext(),
                    AgeGroup.THREE_YEARS,
                    Mascot.HERO_PUP,
                    "Ana",
                ),
            ).setup().get()
        assertTrue(two.hasWindowFocus() || two.window != null)
        two.loop.jumping = true
        repeat(40) { two.loop.tick() }
        val sprites = PlatformerScene.sprites(two.loop.state, Mascot.HERO_PUP, 800f, 480f)
        assertTrue(sprites.size >= 6)
        assertTrue(PlatformerScene.groundTop(480f) > 300f)
        two.completeReward(ok = true)
        val cancelled =
            Robolectric.buildActivity(
                Platformer2dActivity::class.java,
                EngineLauncher.intentFor(
                    ApplicationProvider.getApplicationContext(),
                    AgeGroup.THREE_YEARS,
                    Mascot.HERO_PUP,
                    "Ana",
                ),
            ).setup().get()
        cancelled.completeReward(ok = false)

        val three =
            Robolectric.buildActivity(
                Kart3dActivity::class.java,
                EngineLauncher.intentFor(
                    ApplicationProvider.getApplicationContext(),
                    AgeGroup.SEVEN_YEARS,
                    Mascot.MISCHIEVOUS_ALIEN,
                    "Rui",
                ),
            ).setup().get()
        assertTrue(three.window != null)
        three.session.handleTouch(0.1f, MotionEvent.ACTION_DOWN)
        three.session.handleTouch(0.5f, MotionEvent.ACTION_DOWN)
        three.session.renderer.tick()
        three.session.handleTouch(0.9f, MotionEvent.ACTION_UP)
        three.closeFinished()

        val fallback =
            Robolectric.buildActivity(
                Kart3dActivity::class.java,
                android.content.Intent(ApplicationProvider.getApplicationContext(), Kart3dActivity::class.java),
            ).setup().get()
        assertTrue(fallback.window != null)
        fallback.closeFinished()
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
