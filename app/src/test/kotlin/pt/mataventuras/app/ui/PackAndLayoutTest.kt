package pt.mataventuras.app.ui

import android.content.Context
import android.content.res.Configuration
import android.provider.Settings
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.core.app.ApplicationProvider
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pt.mataventuras.app.R
import pt.mataventuras.app.ui.home.columnsForWidth
import pt.mataventuras.app.ui.home.homeColumnCount
import pt.mataventuras.app.ui.home.widthDpFor
import pt.mataventuras.domain.voice.VoiceScripts

@RunWith(RobolectricTestRunner::class)
class PackAndLayoutTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun portuguesePackMatchesTheResource() {
        val context = context()
        assertEquals(context.getString(R.string.voice_lets_play), context.packLine(VoiceScripts.LETS_PLAY))
        assertEquals(
            context.getString(R.string.voice_snake_prompt) + " 4",
            context.packLine(VoiceScripts.SNAKE_PROMPT + " 4"),
        )
        assertEquals(context.getString(R.string.voice_money, 2, 5), context.packLine("Quanto é 2 € e 5 cêntimos?"))
        assertEquals(
            context.getString(R.string.voice_missing_letter, "c_sa"),
            context.packLine("Qual é a letra que falta em c_sa?"),
        )
        assertEquals(context.getString(R.string.voice_balloon, "3"), context.packLine("Rebenta o balão 3."))
        assertEquals(context.getString(R.string.voice_fish, "5"), context.packLine("Pesca o peixe 5."))
        assertEquals(context.getString(R.string.voice_continue_as, "Ana"), context.packLine("Continuar como Ana"))
        assertEquals(
            context.getString(R.string.voice_greeting_young, "Luna"),
            context.packLine("Olá! Eu sou o Luna. Vamos brincar com números?"),
        )
        assertEquals(
            context.getString(R.string.voice_greeting_older, "Luna"),
            context.packLine("Olá, eu sou o Luna. Pronto para um desafio de matemática?"),
        )
        assertEquals(context.getString(R.string.voice_mascot_me, "Luna"), context.packLine("Eu sou o Luna!"))
        assertEquals("plain", context.packLine("plain"))
    }

    @Test
    fun englishPackDoesNotReturnThePortugueseSentence() {
        val base = context()
        val config = Configuration(base.resources.configuration)
        config.setLocale(Locale.ENGLISH)
        val english = base.createConfigurationContext(config)
        val line = english.packLine(VoiceScripts.LETS_PLAY)
        assertEquals(english.getString(R.string.voice_lets_play), line)
        assertNotEquals(VoiceScripts.LETS_PLAY, line)
    }

    @Test
    fun prizeCopyReadsThePack() {
        val context = context()
        assertEquals(context.getString(R.string.prize_goal_kart), context.prizeGoal("kart"))
        assertEquals(context.getString(R.string.prize_controls_kart), context.prizeControls("KART"))
        assertEquals(context.getString(R.string.prize_goal_fallback), context.prizeGoal("missing"))
        val intro = context.prizeIntro("KART")
        assertEquals(
            context.getString(R.string.voice_lets_play) + " " +
                context.prizeGoal("KART") + " " + context.prizeControls("KART"),
            intro,
        )
    }

    @Test
    fun columnsFollowTheWindowWidth() {
        assertEquals(1, columnsForWidth(599))
        assertEquals(2, columnsForWidth(600))
        assertEquals(3, columnsForWidth(840))
        assertEquals(0, widthDpFor(WindowWidthSizeClass.Compact))
        assertEquals(600, widthDpFor(WindowWidthSizeClass.Medium))
        assertEquals(840, widthDpFor(WindowWidthSizeClass.Expanded))
    }

    @Test
    fun homeColumnCountUsesTheConfigurationWidth() {
        var columns = 0
        compose.setContent { columns = homeColumnCount() }
        assertTrue(columns >= 1)
    }

    @Test
    fun animatorScaleChangesTheDuration() {
        val resolver = context().contentResolver
        Settings.Global.putFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 0f)
        assertEquals(0f, animatorDurationScale(resolver))
        assertEquals(0, scaledDurationMillis(300, animatorDurationScale(resolver)))
        Settings.Global.putFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 0.5f)
        assertEquals(150, scaledDurationMillis(300, animatorDurationScale(resolver)))
        assertEquals(0, scaledDurationMillis(0, 1f))
    }

    private fun context(): Context = ApplicationProvider.getApplicationContext()
}
