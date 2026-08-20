package pt.mataventuras.app.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
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
import pt.mataventuras.domain.model.LearningSession
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
    fun parentCanSetPinAndSeeEmptyWorkList() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        val profile =
            kotlinx.coroutines.runBlocking {
                app.container.pinRepository.clear()
                val id = app.container.repository.createProfile("Ana", AgeGroup.SEVEN_YEARS, Mascot.HERO_PUP)
                app.container.repository.getProfile(id)!!
            }
        compose.setContent {
            ParentDashboard(app.container, profile, onSpeak = {}, onBack = {})
        }
        compose.onNodeWithText(VoiceScripts.PARENT_DASHBOARD).assertIsDisplayed()
        compose.onNodeWithText("PIN").performTextInput("4242")
        compose.onNodeWithText("Confirmar PIN").performTextInput("1111")
        compose.onNodeWithText("Guardar PIN").performClick()
        compose.waitUntil(8_000) {
            compose.onAllNodesWithText("Os PIN não coincidem ou não têm quatro números.")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        compose.onNodeWithText("Confirmar PIN").performTextReplacement("4242")
        compose.onNodeWithText("Guardar PIN").performClick()
        compose.waitUntil(8_000) {
            compose.onAllNodesWithText(VoiceScripts.STAYS_ON_DEVICE, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        compose.onNodeWithText("Nenhum módulo abaixo de 70% com amostra suficiente.").assertIsDisplayed()
        compose.onNodeWithText("Fechar").performClick()
    }

    @Test
    fun parentUnlocksAndReadsModuleMetrics() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        val profile =
            kotlinx.coroutines.runBlocking {
                app.container.pinRepository.clear()
                val id = app.container.repository.createProfile("Rui", AgeGroup.SEVEN_YEARS, Mascot.BRAVE_PLUMBER)
                app.container.repository.saveSession(
                    LearningSession(0, id, LearningModule.ADDITION, 1, 6, 90_000, 1),
                )
                app.container.repository.saveSession(
                    LearningSession(0, id, LearningModule.COUNTING, 8, 0, 30_000, 2),
                )
                val created = app.container.pinPolicy.create("4242")
                app.container.pinRepository.save(created)
                app.container.repository.getProfile(id)!!
            }
        compose.setContent {
            ParentDashboard(app.container, profile, onSpeak = {}, onBack = {})
        }
        compose.onNodeWithText("PIN").performTextReplacement("12")
        compose.onNodeWithText("Entrar").performClick()
        compose.waitUntil(8_000) {
            compose.onAllNodesWithText("O PIN tem quatro números.").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("PIN").performTextReplacement("0000")
        compose.onNodeWithText("Entrar").performClick()
        compose.waitUntil(8_000) {
            compose.onAllNodesWithText(VoiceScripts.WRONG_PIN, substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("PIN").performTextReplacement("4242")
        compose.onNodeWithText("Entrar").performClick()
        compose.waitUntil(8_000) {
            compose.onAllNodesWithText("Precisão").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Precisão").assertIsDisplayed()
        compose.onNodeWithText("Áreas a melhorar").assertIsDisplayed()
        compose.onNodeWithText("• addition", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Fechar").performClick()
    }

    @Test
    fun lessonScreenCountsCorrectWrongAndReward() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        val profile =
            kotlinx.coroutines.runBlocking {
                val id = app.container.repository.createProfile("Ana", AgeGroup.THREE_YEARS, Mascot.SPEEDY_HEDGEHOG)
                app.container.repository.getProfile(id)!!
            }
        var rewards = 0
        compose.setContent {
            MatAventurasTheme(AgeGroup.THREE_YEARS) {
                LessonScreen(
                    container = app.container,
                    profile = profile,
                    module = LearningModule.SHAPES,
                    onSpeak = {},
                    onReward = { rewards += 1 },
                    onExit = {},
                )
            }
        }
        compose.waitUntil(8_000) {
            compose.onAllNodesWithText("Sair").fetchSemanticsNodes().isNotEmpty()
        }
        clickWrongShape()
        repeat(3) { clickMatchingShape() }
        assertTrue(rewards >= 1)
        compose.onNodeWithText("Sair").performClick()
    }

    @Test
    fun countingLessonShowsStarGridAndPersistsOnExit() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        val profile =
            kotlinx.coroutines.runBlocking {
                val id = app.container.repository.createProfile("Ana", AgeGroup.THREE_YEARS, Mascot.SPEEDY_HEDGEHOG)
                app.container.repository.getProfile(id)!!
            }
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
        compose.waitUntil(8_000) {
            compose.onAllNodesWithText("Quantas estrelas vês?").fetchSemanticsNodes().isNotEmpty()
        }
        clickAnyNumericOption()
        clickAnyNumericOption()
        compose.onNodeWithText("Sair").performClick()
        kotlinx.coroutines.runBlocking {
            assertTrue(app.container.repository.allSessions().any { it.profileId == profile.id })
        }
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
        compose.onNodeWithText(VoiceScripts.LEADERBOARD).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(VoiceScripts.PARENT_DASHBOARD).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Somar com o Canalizador Valente").performScrollTo().performClick()
        assertEquals(LearningModule.ADDITION, opened)
    }

    @Test
    fun leaderboardWithActiveProfileShowsRewards() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        val profile =
            kotlinx.coroutines.runBlocking {
                val id = app.container.repository.createProfile("Rui", AgeGroup.SEVEN_YEARS, Mascot.BRAVE_PLUMBER)
                app.container.repository.unlockBadge(id, "FIRST_STEPS")
                app.container.repository.getProfile(id)!!
            }
        compose.setContent {
            MatAventurasTheme(AgeGroup.SEVEN_YEARS) {
                LeaderboardAndRewardsScreen(app.container, profile) {}
            }
        }
        compose.onNodeWithText(VoiceScripts.REWARDS).assertIsDisplayed()
        compose.onNodeWithText("★ Primeiros passos").assertIsDisplayed()
        compose.onNodeWithText("Voltar").performClick()
    }

    private fun clickMatchingShape() {
        val shapes = listOf("círculo", "quadrado", "triângulo", "rectângulo", "estrela")
        compose.waitUntil(8_000) {
            shapes.any { shape ->
                compose.onAllNodesWithText("Toca no $shape.").fetchSemanticsNodes().isNotEmpty()
            }
        }
        for (shape in shapes) {
            if (compose.onAllNodesWithText("Toca no $shape.").fetchSemanticsNodes().isNotEmpty()) {
                compose.onNode(hasText(shape) and hasClickAction()).performClick()
                return
            }
        }
    }

    private fun clickWrongShape() {
        val shapes = listOf("círculo", "quadrado", "triângulo", "rectângulo", "estrela")
        compose.waitUntil(8_000) {
            shapes.any { shape ->
                compose.onAllNodesWithText("Toca no $shape.").fetchSemanticsNodes().isNotEmpty()
            }
        }
        val prompt =
            shapes.first { shape ->
                compose.onAllNodesWithText("Toca no $shape.").fetchSemanticsNodes().isNotEmpty()
            }
        val other =
            shapes.first { shape ->
                shape != prompt &&
                    compose.onAllNodes(hasText(shape) and hasClickAction()).fetchSemanticsNodes().isNotEmpty()
            }
        compose.onNode(hasText(other) and hasClickAction()).performClick()
    }

    private fun clickAnyNumericOption() {
        compose.waitUntil(8_000) {
            (1..10).any { n ->
                compose.onAllNodes(hasText(n.toString()) and hasClickAction()).fetchSemanticsNodes().isNotEmpty()
            }
        }
        for (n in 1..10) {
            val nodes = compose.onAllNodes(hasText(n.toString()) and hasClickAction()).fetchSemanticsNodes()
            if (nodes.isNotEmpty()) {
                compose.onNode(hasText(n.toString()) and hasClickAction()).performClick()
                return
            }
        }
    }
}
