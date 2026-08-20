package pt.mataventuras.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import pt.mataventuras.app.ui.navigation.NavGraph
import pt.mataventuras.app.ui.parent.ParentDashboard
import pt.mataventuras.app.ui.rewards.LeaderboardAndRewardsScreen
import pt.mataventuras.app.ui.theme.MatAventurasTheme
import pt.mataventuras.app.ui.theme.bodySpSize
import pt.mataventuras.app.ui.theme.buttonRadius
import pt.mataventuras.app.ui.theme.titleSpSize
import pt.mataventuras.domain.model.AgeGroup
import pt.mataventuras.domain.model.tokensFor
import pt.mataventuras.domain.voice.VoiceScripts

@RunWith(RobolectricTestRunner::class)
@Config(application = MatAventurasApp::class, sdk = [34])
class NavAndParentTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun navGraphCreatesAProfileAndOpensALesson() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        kotlinx.coroutines.runBlocking { app.container.pinRepository.clear() }
        compose.setContent {
            NavGraph(container = app.container, onSpeak = {}, onReward = { _, _, _ -> })
        }
        compose.onNodeWithText(VoiceScripts.THREE_YEARS).performClick()
        compose.onNodeWithText("Como te chamas?").assertIsDisplayed()
        compose.onNodeWithText("Como te chamas?").performTextInput("Ana")
        compose.onNodeWithText("Vamos começar!").performClick()
        compose.waitUntil(8_000) {
            compose.onAllNodesWithText("Olá, Ana!").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("★").performScrollTo().performClick()
        compose.onNodeWithText("Voltar").performScrollTo().performClick()
        compose.onNodeWithText("Contar com o Ouriço Veloz").performScrollTo().performClick()
        compose.waitUntil(8_000) {
            compose.onAllNodesWithText("Sair").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Sair").performClick()
        compose.waitUntil(8_000) {
            compose.onAllNodesWithText("Olá, Ana!").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("· · ·").performScrollTo().performClick()
        compose.onNodeWithText(VoiceScripts.PARENT_DASHBOARD).assertIsDisplayed()
    }

    @Test
    fun leaderboardRendersPortugueseCopy() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        compose.setContent {
            MatAventurasTheme(AgeGroup.SEVEN_YEARS) {
                LeaderboardAndRewardsScreen(app.container, null) {}
            }
        }
        compose.onNodeWithText(VoiceScripts.LEADERBOARD).assertIsDisplayed()
    }

    @Test
    fun parentDashboardRendersPinGate() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        kotlinx.coroutines.runBlocking { app.container.pinRepository.clear() }
        compose.setContent {
            ParentDashboard(app.container, null, onSpeak = {}, onBack = {})
        }
        compose.onNodeWithText(VoiceScripts.PARENT_DASHBOARD).assertIsDisplayed()
        compose.onNodeWithText("Voltar").performClick()
    }

    @Test
    fun themeTokensAndMainActivity() {
        val three = tokensFor(AgeGroup.THREE_YEARS)
        three.buttonRadius()
        assert(three.titleSpSize.value > three.bodySpSize.value)
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        controller.get()
        controller.destroy()
    }
}
