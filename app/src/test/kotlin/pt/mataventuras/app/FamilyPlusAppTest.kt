package pt.mataventuras.app

import android.app.Activity
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.accessibility.enableAccessibilityChecks
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.core.app.ApplicationProvider
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import pt.mataventuras.app.engine.EngineExtras
import pt.mataventuras.app.engine.EngineLauncher
import pt.mataventuras.app.engine.GodotCopy
import pt.mataventuras.app.engine.PrizeExit
import pt.mataventuras.app.entitlement.AppLanguage
import pt.mataventuras.app.entitlement.BuildConfigEntitlements
import pt.mataventuras.app.entitlement.FamilySettings
import pt.mataventuras.app.entitlement.LanguagePacks
import pt.mataventuras.app.entitlement.applyLanguage
import pt.mataventuras.app.entitlement.recreateOnLanguage
import pt.mataventuras.app.prize.PrizeLaunch
import pt.mataventuras.app.speech.SpeechEngine
import pt.mataventuras.app.speech.canSpeakOffline
import pt.mataventuras.app.speech.offlineVoice
import pt.mataventuras.app.speech.voicesOrNull
import pt.mataventuras.app.ui.parent.FamilyPlusPanel
import pt.mataventuras.domain.engine.EnginePluginContract
import pt.mataventuras.domain.entitlement.Entitlements
import pt.mataventuras.domain.entitlement.FamilyExtras
import pt.mataventuras.domain.entitlement.PremiumFeature
import pt.mataventuras.domain.entitlement.Season
import pt.mataventuras.domain.entitlement.WorldTheme
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.model.Mascot

@RunWith(RobolectricTestRunner::class)
@Config(application = MatAventurasApp::class, sdk = [34])
class FamilyPlusAppTest {
    @get:Rule
    val compose = createComposeRule()

    @Before
    fun accessibilityChecks() {
        compose.enableAccessibilityChecks()
    }

    @Test
    fun premiumFlagUnlocksOrHidesEveryFeature() {
        val open = BuildConfigEntitlements(true)
        val shut = BuildConfigEntitlements(false)
        PremiumFeature.entries.forEach { feature ->
            assertTrue(open.isUnlocked(feature))
            assertFalse(shut.isUnlocked(feature))
        }
        assertEquals(BuildConfig.PREMIUM_UNLOCKED, BuildConfigEntitlements().isUnlocked(PremiumFeature.LANGUAGE_PACKS))
        assertTrue(BuildConfig.PREMIUM_UNLOCKED)
    }

    @Test
    fun settingsLanguageAndThemeStayOnDevice() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val settings = FamilySettings(context)
        assertEquals(WorldTheme.MEADOW, settings.theme())
        assertEquals(LanguagePacks.PORTUGUESE, settings.language())
        settings.saveTheme(WorldTheme.NIGHT)
        settings.saveLanguage("EN")
        assertEquals(WorldTheme.NIGHT, settings.theme())
        assertEquals("en", settings.language())
        settings.saveLanguage("nope")
        assertEquals(LanguagePacks.PORTUGUESE, settings.language())
        assertEquals("pt-BR", LanguagePacks.canonical("pt-br"))
        assertEquals("pt-PT", LanguagePacks.canonical(null))
        settings.saveLanguage("es")
        assertEquals("Salir", AppLanguage.wrap(context).getString(pt.mataventuras.app.R.string.godot_exit))
        settings.saveLanguage(LanguagePacks.PORTUGUESE)
        assertEquals("Sair", AppLanguage.wrap(context).getString(pt.mataventuras.app.R.string.godot_exit))
        applyLanguage(null, settings, "es")
        assertEquals("es", settings.language())
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()
        applyLanguage(activity, settings, "fr")
        assertEquals("fr", FamilySettings(activity).language())
        settings.saveLanguage(LanguagePacks.PORTUGUESE)
    }

    @Test
    fun rewardExtrasFollowTheFlag() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val empty = PrizeLaunch.withFamily(Intent(), FamilyExtras())
        assertFalse(empty.hasExtra(EnginePluginContract.EXTRA_WORLD_THEME))
        assertFalse(empty.hasExtra(EnginePluginContract.EXTRA_SEASON))
        val themed = PrizeLaunch.withFamily(Intent(), FamilyExtras(theme = 3, season = 1, prizeIndex = 2))
        assertEquals(3, EngineExtras.theme(themed))
        assertEquals(1, EngineExtras.season(themed))
        assertEquals(-1, EngineExtras.theme(Intent()))
        assertEquals(-1, EngineExtras.season(Intent()))
        val copy = Intent()
        EngineExtras.copyFamily(themed, copy)
        EngineExtras.copyFamily(Intent(), copy)
        assertEquals(3, copy.getIntExtra(EnginePluginContract.EXTRA_WORLD_THEME, -1))
        val restart =
            EngineLauncher.restartResultIntent(
                EnginePluginContract.PLUGIN_RUNNER_CLASS,
                "hero_pup",
                "Ana",
            )
        restart.putExtra(EnginePluginContract.EXTRA_WORLD_THEME, 2)
        restart.putExtra(EnginePluginContract.EXTRA_SEASON, 0)
        val relaunch = EngineLauncher.relaunchIntent(context, restart)!!
        assertEquals(2, relaunch.getIntExtra(EnginePluginContract.EXTRA_WORLD_THEME, -1))
        assertEquals(0, relaunch.getIntExtra(EnginePluginContract.EXTRA_SEASON, -1))
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        app.container.family.saveTheme(WorldTheme.SEA)
        app.container.entitlements = BuildConfigEntitlements(false)
        val locked = rewardIntent(context, app.container, AgeBand.THREE_TO_FIVE, Mascot.HERO_PUP, "Ana", 0)
        assertFalse(locked.hasExtra(EnginePluginContract.EXTRA_WORLD_THEME))
        assertFalse(locked.hasExtra(EnginePluginContract.EXTRA_SEASON))
        app.container.entitlements = BuildConfigEntitlements(true)
        val open = rewardIntent(context, app.container, AgeBand.SIX_TO_EIGHT, Mascot.HERO_PUP, "Ana", 60)
        assertEquals(WorldTheme.SEA.ordinal, open.getIntExtra(EnginePluginContract.EXTRA_WORLD_THEME, -1))
        assertTrue(open.hasExtra(EnginePluginContract.EXTRA_SEASON))
        app.container.entitlements = BuildConfigEntitlements()
        assertEquals("Sair", GodotCopy.text(context, "exit"))
        assertEquals("Impulso", GodotCopy.text(context, "boost"))
        assertEquals("Jogar", GodotCopy.text(context, "play"))
        assertEquals("", GodotCopy.text(context, "nope"))
    }

    @Test
    fun offlineVoiceSkipsReadAloud() {
        val pt = Locale.forLanguageTag("pt-PT")
        val local = voice("pt-local", pt, network = false, missing = false)
        val network = voice("pt-net", pt, network = true, missing = false)
        val missing = voice("pt-gone", pt, network = false, missing = true)
        val english = voice("en", Locale.ENGLISH, network = false, missing = false)
        assertNull(voicesOrNull { throw IllegalStateException("tts") })
        assertEquals(emptySet<Voice>(), voicesOrNull { emptySet() })
        assertFalse(offlineVoice(null, pt))
        assertFalse(offlineVoice(setOf(network, missing, english), pt))
        assertTrue(offlineVoice(setOf(local), pt))
        assertFalse(canSpeakOffline(languageOk = true, offline = false))
        assertFalse(canSpeakOffline(languageOk = false, offline = true))
        assertTrue(canSpeakOffline(languageOk = true, offline = true))
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val silent =
            SpeechEngine(
                context,
                voiceLookup = { setOf(network) },
                languageResult = { TextToSpeech.SUCCESS },
            )
        silent.speak("Olá")
        silent.onInit(TextToSpeech.SUCCESS)
        silent.speak("Olá")
        silent.release()
        val aloud =
            SpeechEngine(
                context,
                voiceLookup = { setOf(local) },
                languageResult = { TextToSpeech.SUCCESS },
            )
        aloud.speak("Olá")
        aloud.onInit(TextToSpeech.SUCCESS)
        aloud.speak(" ")
        aloud.release()
        val unsupported =
            SpeechEngine(
                context,
                voiceLookup = { setOf(local) },
                languageResult = { TextToSpeech.LANG_NOT_SUPPORTED },
            )
        unsupported.onInit(TextToSpeech.SUCCESS)
        unsupported.release()
        unsupported.onInit(TextToSpeech.SUCCESS)
        val idle =
            SpeechEngine(
                context,
                voiceLookup = { setOf(local) },
                languageResult = { TextToSpeech.SUCCESS },
            )
        idle.onInit(TextToSpeech.SUCCESS)
        idle.release()
    }

    @Test
    fun parentPanelHidesLockedFeaturesAndKeepsTheNoteBehindTheGate() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val settings = FamilySettings(context)
        settings.saveLanguage(LanguagePacks.PORTUGUESE)
        settings.saveTheme(WorldTheme.MEADOW)
        var open by mutableStateOf(emptySet<PremiumFeature>())
        var season by mutableStateOf(Season.WINTER)
        compose.setContent {
            FamilyPlusPanel(FlagEntitlements(open), settings, season)
        }
        compose.onNodeWithText("Esta opção não está neste aparelho.").assertIsDisplayed()
        assertTrue(compose.onAllNodesWithText("Prado").fetchSemanticsNodes().isEmpty())
        open = setOf(PremiumFeature.THEMED_WORLDS)
        season = Season.SPRING
        compose.onNodeWithText("Esta opção não está neste aparelho.").assertIsDisplayed()
        compose.onNodeWithText("● Prado").assertIsDisplayed()
        open = setOf(PremiumFeature.THEMED_WORLDS, PremiumFeature.SEASONAL_PRIZES)
        season = Season.SUMMER
        compose.onNodeWithText("Prémio da época: Verão").assertIsDisplayed()
        open = PremiumFeature.entries.toSet()
        Season.entries.forEach { next ->
            season = next
            compose.waitForIdle()
        }
        compose.onNodeWithText("Noite").performClick()
        assertEquals(WorldTheme.NIGHT, settings.theme())
        compose.onNodeWithText("● Noite").assertIsDisplayed()
        recreateOnLanguage = false
        compose.onNodeWithTag("lang-en").performSemanticsAction(SemanticsActions.OnClick)
        assertEquals("en", settings.language())
        recreateOnLanguage = true
        settings.saveLanguage(LanguagePacks.PORTUGUESE)
        settings.saveTheme(WorldTheme.MEADOW)
        AppLanguage.wrap(context)
    }

    @Test
    fun composeExitSitsAboveTheGodotSurface() {
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()
        PrizeExit.attach(activity, embed = false) { error("locked out") }
        assertNull(activity.window.decorView.findViewWithTag(PrizeExit.TAG))
        var exits = 0
        PrizeExit.attach(activity, embed = true) { exits += 1 }
        PrizeExit.attach(activity, embed = true) { error("twice") }
        assertTrue(activity.window.decorView.findViewWithTag<android.view.View>(PrizeExit.TAG) != null)
        compose.onAllNodesWithText("Sair").onFirst().performClick()
        assertEquals(1, exits)
        val plain = Robolectric.buildActivity(Activity::class.java).setup().get()
        try {
            PrizeExit.attach(plain, embed = true) { }
        } catch (_: IllegalStateException) {
            // A plain Activity has no ViewTree owner; the type checks still run.
        }
    }

    private fun voice(
        name: String,
        locale: Locale,
        network: Boolean,
        missing: Boolean,
    ): Voice {
        val features = if (missing) setOf(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED) else emptySet()
        return Voice(name, locale, Voice.QUALITY_NORMAL, Voice.LATENCY_NORMAL, network, features)
    }

    private class FlagEntitlements(
        private val unlocked: Set<PremiumFeature>,
    ) : Entitlements {
        override fun isUnlocked(feature: PremiumFeature): Boolean = feature in unlocked
    }
}
