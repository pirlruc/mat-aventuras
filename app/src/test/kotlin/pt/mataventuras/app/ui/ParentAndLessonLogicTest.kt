package pt.mataventuras.app.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import pt.mataventuras.app.MatAventurasApp
import pt.mataventuras.app.ui.home.HomeScreen
import pt.mataventuras.app.ui.lesson.LessonRecorder
import pt.mataventuras.app.ui.lesson.LessonScreen
import pt.mataventuras.app.ui.parent.ParentDashboard
import pt.mataventuras.app.ui.rewards.LeaderboardAndRewardsScreen
import pt.mataventuras.app.ui.theme.MatAventurasTheme
import pt.mataventuras.domain.model.AgeGroup
import pt.mataventuras.domain.model.ChildProfile
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.model.Mascot
import pt.mataventuras.domain.progress.AvatarCode
import pt.mataventuras.domain.voice.VoiceScripts

@RunWith(RobolectricTestRunner::class)
@Config(application = MatAventurasApp::class, sdk = [34])
class ParentAndLessonLogicTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun recorderWritesSessionAndFirstStepsBadge() = runTest {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        val id = app.container.repository.createProfile("Rui", AgeGroup.SEVEN_YEARS, Mascot.BRAVE_PLUMBER)
        val profile = app.container.repository.getProfile(id)!!
        LessonRecorder.persist(
            container = app.container,
            profile = profile,
            points = 60,
            module = LearningModule.ADDITION,
            hits = 6,
            misses = 0,
            startedAt = 1_000,
            nowMs = 4_000,
        )
        val sessions = app.container.repository.allSessions().filter { it.profileId == id }
        assertEquals(1, sessions.size)
        assertEquals(6, sessions[0].hits)
        assertEquals(3_000, sessions[0].durationMs)
        assertTrue(app.container.repository.badgeCodes(id).contains("FIRST_STEPS"))
        assertTrue(app.container.repository.avatarIds(id).contains(AvatarCode.RUNNER.name))
        LessonRecorder.persist(
            container = app.container,
            profile = profile.copy(id = 9_999),
            points = 0,
            module = LearningModule.COUNTING,
            hits = 1,
            misses = 0,
            startedAt = 0,
            nowMs = 1,
        )
        assertEquals(1, app.container.repository.allSessions().filter { it.profileId == id }.size)
    }

    @Test
    fun parentCanSetPinAndOpenDashboard() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        runTest { app.container.pinRepository.clear() }
        val profile =
            ChildProfile(1, "Ana", AgeGroup.SEVEN_YEARS, Mascot.HERO_PUP, AvatarCode.STARTER.name, 0, 0)
        compose.setContent {
            ParentDashboard(app.container, profile, onSpeak = {}, onBack = {})
        }
        compose.onNodeWithText(VoiceScripts.PARENT_DASHBOARD).assertIsDisplayed()
        compose.onNodeWithText("PIN").performTextInput("4242")
        compose.onNodeWithText("Confirmar PIN").performTextInput("4242")
        compose.onNodeWithText("Guardar PIN").performClick()
        compose.waitUntil(8_000) {
            compose.onAllNodesWithText(VoiceScripts.STAYS_ON_DEVICE, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        compose.onNodeWithText("Fechar").performClick()
    }

    @Test
    fun lessonScreenShowsOptionsAndExit() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        val profile =
            ChildProfile(1, "Ana", AgeGroup.THREE_YEARS, Mascot.SPEEDY_HEDGEHOG, AvatarCode.STARTER.name, 0, 0)
        compose.setContent {
            MatAventurasTheme(AgeGroup.THREE_YEARS) {
                LessonScreen(
                    container = app.container,
                    profile = profile,
                    module = LearningModule.COUNTING,
                    onSpeak = {},
                    onReward = {},
                    onExit = {},
                )
            }
        }
        compose.onNodeWithText("Sair").assertIsDisplayed()
        compose.onNodeWithText("Sair").performClick()
    }

    @Test
    fun sevenYearHomeShowsLabelledButtons() {
        val profile =
            ChildProfile(2, "Rui", AgeGroup.SEVEN_YEARS, Mascot.BRAVE_PLUMBER, AvatarCode.STARTER.name, 12, 0)
        var opened: LearningModule? = null
        compose.setContent {
            MatAventurasTheme(AgeGroup.SEVEN_YEARS) {
                HomeScreen(
                    profile = profile,
                    onSpeak = {},
                    onModule = { opened = it },
                    onLeaderboard = {},
                    onParents = {},
                )
            }
        }
        compose.onNodeWithText("Olá, Rui!").assertIsDisplayed()
        compose.onNodeWithText(VoiceScripts.LEADERBOARD).assertIsDisplayed()
        compose.onNodeWithText(VoiceScripts.PARENT_DASHBOARD).assertIsDisplayed()
        compose.onNodeWithText("Somar com o Canalizador Valente").performClick()
        assertEquals(LearningModule.ADDITION, opened)
    }

    @Test
    fun leaderboardWithActiveProfileShowsRewards() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        compose.setContent {
            MatAventurasTheme(AgeGroup.SEVEN_YEARS) {
                LeaderboardAndRewardsScreen(app.container, null) {}
            }
        }
        compose.onNodeWithText(VoiceScripts.REWARDS).assertIsDisplayed()
    }
}
