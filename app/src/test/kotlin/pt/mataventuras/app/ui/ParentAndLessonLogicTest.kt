package pt.mataventuras.app.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
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
import pt.mataventuras.app.ui.lesson.RewardRecorder
import pt.mataventuras.app.ui.parent.ParentDashboard
import pt.mataventuras.app.ui.rewards.LeaderboardAndRewardsScreen
import pt.mataventuras.app.ui.theme.MatAventurasTheme
import pt.mataventuras.domain.model.AgeBand
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
    fun recorderWritesSessionAndFirstStepsBadge() =
        runTest {
            val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
            val id = app.container.repository.createProfile("Rui", AgeBand.SIX_TO_EIGHT, Mascot.BRAVE_PLUMBER)
            val profile = app.container.repository.getProfile(id)!!
            LessonRecorder.persist(
                container = app.container,
                profile = profile,
                module = LearningModule.ADDITION,
                hits = 6,
                misses = 0,
                startedAt = 1_000,
                nowMs = 4_000,
            )
            val sessions = app.container.repository.sessionsFor(id)
            assertEquals(1, sessions.size)
            assertEquals(6, sessions[0].hits)
            assertEquals(3_000, sessions[0].durationMs)
            assertEquals(
                0,
                app.container.repository
                    .getProfile(id)!!
                    .points,
            )
            assertTrue(
                app.container.repository
                    .badgeCodes(id)
                    .contains("FIRST_STEPS"),
            )
            assertEquals(
                false,
                app.container.repository
                    .avatarIds(id)
                    .contains(AvatarCode.RUNNER.name),
            )
            app.container.repository.addPoints(id, 60)
            LessonRecorder.persist(
                container = app.container,
                profile = profile,
                module = LearningModule.ADDITION,
                hits = 6,
                misses = 0,
                startedAt = 5_000,
                nowMs = 6_000,
            )
            assertEquals(
                60,
                app.container.repository
                    .getProfile(id)!!
                    .points,
            )
            assertTrue(
                app.container.repository
                    .avatarIds(id)
                    .contains(AvatarCode.RUNNER.name),
            )
            LessonRecorder.persist(
                container = app.container,
                profile = profile.copy(id = 9_999),
                module = LearningModule.COUNTING,
                hits = 1,
                misses = 0,
                startedAt = 0,
                nowMs = 1,
            )
            assertEquals(
                2,
                app.container.repository
                    .sessionsFor(id)
                    .size,
            )
        }

    @Test
    fun parentCanSetPinAndSeeEmptyWorkList() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        val profile =
            kotlinx.coroutines.runBlocking {
                app.container.pinRepository.clear()
                val id = app.container.repository.createProfile("Ana", AgeBand.SIX_TO_EIGHT, Mascot.HERO_PUP)
                app.container.repository.getProfile(id)!!
            }
        compose.setContent {
            ParentDashboard(
                app.container,
                profile,
                onSpeak = {},
                onBack = {},
                challenge =
                    pt.mataventuras.domain.parent
                        .AdultChallenge(12, 3),
                adultHoldMs = 0,
            )
        }
        compose.onNodeWithText("Segurar para continuar").performTouchInput {
            down(center)
            up()
        }
        compose.onNodeWithText("Resposta").performTextInput("36")
        compose.onNodeWithText("Continuar").performClick()
        compose.onNodeWithText(VoiceScripts.PARENT_DASHBOARD).assertIsDisplayed()
        compose.onNodeWithText("PIN").performTextInput("4242")
        compose.onNodeWithText("Confirmar PIN").performTextInput("1111")
        compose.onNodeWithText("Guardar PIN").performClick()
        compose.waitUntil(8_000) {
            compose
                .onAllNodesWithText("Os PIN não coincidem ou não têm quatro números.")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        compose.onNodeWithText("Confirmar PIN").performTextReplacement("4242")
        compose.onNodeWithText("Guardar PIN").performClick()
        compose.waitUntil(8_000) {
            compose
                .onAllNodesWithText(VoiceScripts.STAYS_ON_DEVICE, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        compose.waitUntil(8_000) {
            compose
                .onAllNodesWithText("Nenhum módulo abaixo de 70% com amostra suficiente.")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        compose.onNodeWithText("1 min").performScrollTo().performClick()
        compose.onNodeWithText("5 atividades").performScrollTo().performClick()
        compose.waitForIdle()
        compose
            .onNodeWithText("Nenhum módulo abaixo de 70% com amostra suficiente.")
            .performScrollTo()
            .assertIsDisplayed()
        compose.onNodeWithText("Fechar").performClick()
    }

    @Test
    fun parentUnlocksAndReadsModuleMetrics() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        val profile =
            kotlinx.coroutines.runBlocking {
                app.container.pinRepository.clear()
                val id = app.container.repository.createProfile("Rui", AgeBand.SIX_TO_EIGHT, Mascot.BRAVE_PLUMBER)
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
        compose.onNodeWithText("Áreas a melhorar").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("• Somar", substring = true).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Fechar").performClick()
    }

    @Test
    fun parentPinSubmitSerializesSetAndUnlock() =
        runTest {
            val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
            val pins = app.container.pinRepository
            pins.clear()
            val gate =
                pt.mataventuras.domain.parent
                    .PinGate(app.container.pinPolicy)
            val mismatch =
                pt.mataventuras.app.ui.parent.ParentPin
                    .submit(true, "1234", "0000", gate, pins)
            assertTrue(mismatch is pt.mataventuras.domain.parent.PinGateResult.Stay)
            assertEquals(false, pins.isSet())
            val created =
                pt.mataventuras.app.ui.parent.ParentPin
                    .submit(true, "1234", "1234", gate, pins)
            assertEquals(pt.mataventuras.domain.parent.PinGateResult.Unlocked, created)
            assertEquals(true, pins.isSet())
            val asUnlock =
                pt.mataventuras.app.ui.parent.ParentPin
                    .submit(true, "1234", "", gate, pins)
            assertEquals(pt.mataventuras.domain.parent.PinGateResult.Unlocked, asUnlock)
            val wrong =
                pt.mataventuras.app.ui.parent.ParentPin
                    .submit(false, "0000", "", gate, pins)
            assertTrue(wrong is pt.mataventuras.domain.parent.PinGateResult.Stay)
            pins.clear()
            val missing =
                pt.mataventuras.app.ui.parent.ParentPin
                    .submit(false, "1234", "", gate, pins)
            assertTrue(missing is pt.mataventuras.domain.parent.PinGateResult.Stay)
        }

    @Test
    fun lessonScreenCountsCorrectWrongAndReward() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        val profile =
            kotlinx.coroutines.runBlocking {
                val id = app.container.repository.createProfile("Ana", AgeBand.THREE_TO_FIVE, Mascot.SPEEDY_HEDGEHOG)
                val created = app.container.repository.getProfile(id)!!
                app.container.repository.updateProfile(
                    created.copy(pace = created.pace.copy(completed = 8)),
                )
                app.container.repository.getProfile(id)!!
            }
        var rewards = 0
        compose.setContent {
            MatAventurasTheme(AgeBand.THREE_TO_FIVE) {
                LessonScreen(
                    container = app.container,
                    profile = profile,
                    module = LearningModule.COUNTING,
                    onSpeak = {},
                    onReward = { rewards += 1 },
                    onExit = {},
                )
            }
        }
        compose.waitUntil(8_000) {
            compose.onAllNodesWithText("Sair").fetchSemanticsNodes().isNotEmpty()
        }
        clickWrong()
        var attempts = 0
        while (rewards < 1 && attempts < 3) {
            clickCorrect()
            attempts += 1
        }
        assertTrue(rewards >= 1)
        compose.onNodeWithText("Sair").performClick()
    }

    @Test
    fun countingLessonShowsStarGridAndPersistsOnExit() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        val profile =
            kotlinx.coroutines.runBlocking {
                val id = app.container.repository.createProfile("Ana", AgeBand.THREE_TO_FIVE, Mascot.SPEEDY_HEDGEHOG)
                app.container.repository.getProfile(id)!!
            }
        compose.setContent {
            MatAventurasTheme(AgeBand.THREE_TO_FIVE) {
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
            compose.onAllNodesWithTag("correct-answer").fetchSemanticsNodes().isNotEmpty()
        }
        clickCorrect()
        clickCorrect()
        compose.onNodeWithText("Sair").performClick()
        assertTrue(true)
    }

    @Test
    fun sevenYearHomeShowsLabelledButtons() {
        val profile =
            ChildProfile(2, "Rui", AgeBand.SIX_TO_EIGHT, Mascot.BRAVE_PLUMBER, AvatarCode.STARTER.name, 12, 0)
        var opened: LearningModule? = null
        compose.setContent {
            MatAventurasTheme(AgeBand.SIX_TO_EIGHT) {
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
    fun youngHomeLockOpensTheParentArea() {
        val profile =
            ChildProfile(2, "Rui", AgeBand.THREE_TO_FIVE, Mascot.BRAVE_PLUMBER, AvatarCode.STARTER.name, 12, 0)
        var parents = false
        var board = false
        compose.setContent {
            MatAventurasTheme(AgeBand.THREE_TO_FIVE) {
                HomeScreen(
                    profile = profile,
                    onSpeak = {},
                    onModule = {},
                    onLeaderboard = { board = true },
                    onParents = { parents = true },
                )
            }
        }
        compose.onNodeWithContentDescription(VoiceScripts.LEADERBOARD).performScrollTo().performClick()
        assertTrue(board)
        compose.onNodeWithContentDescription(VoiceScripts.PARENT_DASHBOARD).performScrollTo().performClick()
        assertTrue(parents)
    }

    @Test
    fun sevenYearLessonAsksBeforeExit() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        val profile =
            kotlinx.coroutines.runBlocking {
                val id = app.container.repository.createProfile("Rui", AgeBand.SIX_TO_EIGHT, Mascot.BRAVE_PLUMBER)
                app.container.repository.getProfile(id)!!
            }
        var left = false
        compose.setContent {
            MatAventurasTheme(AgeBand.SIX_TO_EIGHT) {
                LessonScreen(
                    container = app.container,
                    profile = profile,
                    module = LearningModule.ADDITION,
                    onSpeak = {},
                    onReward = {},
                    onExit = { left = true },
                )
            }
        }
        compose.waitUntil(8_000) {
            compose.onAllNodesWithText(VoiceScripts.LEAVE).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText(VoiceScripts.LEAVE).performClick()
        compose.waitUntil(8_000) {
            compose.onAllNodesWithText(VoiceScripts.STAY).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText(VoiceScripts.STAY).assertIsDisplayed()
        compose.onNodeWithText(VoiceScripts.STAY).performClick()
        compose.onNodeWithText(VoiceScripts.LEAVE).performClick()
        compose.waitUntil(8_000) {
            compose.onAllNodesWithText(VoiceScripts.CONFIRM_LEAVE).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText(VoiceScripts.CONFIRM_LEAVE).performClick()
        compose.waitUntil(8_000) { left }
    }

    @Test
    fun rewardRecorderAwardsPointsOnlyWhenFinished() =
        runTest {
            val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
            val id = app.container.repository.createProfile("Rui", AgeBand.SIX_TO_EIGHT, Mascot.BRAVE_PLUMBER)
            val start = app.container.repository.getProfile(id)!!
            app.container.repository.updateProfile(start.copy(points = 40))
            app.container.lastProfile.save(id)
            RewardRecorder.apply(app.container, finished = false)
            assertEquals(
                40,
                app.container.repository
                    .getProfile(id)!!
                    .points,
            )
            RewardRecorder.apply(app.container, finished = true)
            assertEquals(
                55,
                app.container.repository
                    .getProfile(id)!!
                    .points,
            )
            assertTrue(
                app.container.repository
                    .avatarIds(id)
                    .contains(AvatarCode.RUNNER.name),
            )
            app.container.repository.addPoints(id, 10)
            RewardRecorder.apply(app.container, finished = true)
            assertEquals(
                80,
                app.container.repository
                    .getProfile(id)!!
                    .points,
            )
            LessonRecorder.persist(
                container = app.container,
                profile = start,
                module = LearningModule.ADDITION,
                hits = 1,
                misses = 0,
                startedAt = 1,
                nowMs = 2,
            )
            assertEquals(
                80,
                app.container.repository
                    .getProfile(id)!!
                    .points,
            )
            app.container.lastProfile.clear()
            RewardRecorder.apply(app.container, finished = true)
            assertEquals(
                80,
                app.container.repository
                    .getProfile(id)!!
                    .points,
            )
            app.container.lastProfile.save(9_999)
            RewardRecorder.apply(app.container, finished = true)
            assertEquals(
                80,
                app.container.repository
                    .getProfile(id)!!
                    .points,
            )
        }

    @Test
    fun profileResumePrefersStoredThenLatest() =
        runTest {
            val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
            app.container.lastProfile.clear()
            val first =
                ProfileResume.openNew(
                    app.container.lastProfile,
                    app.container.repository,
                    "Ana",
                    AgeBand.THREE_TO_FIVE,
                    Mascot.SPEEDY_HEDGEHOG,
                )!!
            val second =
                app.container.repository.createProfile("Rui", AgeBand.SIX_TO_EIGHT, Mascot.BRAVE_PLUMBER)
            app.container.lastProfile.save(first.id)
            val stored =
                ProfileResume.continueCandidate(app.container.lastProfile, app.container.repository)
            assertEquals(first.id, stored!!.id)
            app.container.lastProfile.save(9_999)
            val fallback =
                ProfileResume.continueCandidate(app.container.lastProfile, app.container.repository)
            assertEquals(second, fallback!!.id)
            ProfileResume.remember(app.container.lastProfile, first)
            assertEquals(first.id, app.container.lastProfile.read())
        }

    @Test
    fun leaderboardWithActiveProfileShowsRewards() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        val profile =
            kotlinx.coroutines.runBlocking {
                val id = app.container.repository.createProfile("Rui", AgeBand.SIX_TO_EIGHT, Mascot.BRAVE_PLUMBER)
                app.container.repository.unlockBadge(id, "FIRST_STEPS")
                app.container.repository.getProfile(id)!!
            }
        compose.setContent {
            MatAventurasTheme(AgeBand.SIX_TO_EIGHT) {
                LeaderboardAndRewardsScreen(app.container, profile) {}
            }
        }
        compose.waitUntil(8_000) {
            compose.onAllNodesWithText("★ Primeiros passos").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText(VoiceScripts.REWARDS).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("★ Primeiros passos").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Voltar").performClick()
    }

    @Test
    fun prizePromptWaitsUntilTheWinCardCloses() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        val profile =
            runBlocking {
                val id = app.container.repository.createProfile("Lia", AgeBand.THREE_TO_FIVE, Mascot.SPEEDY_HEDGEHOG)
                app.container.repository.updateProfile(
                    app.container.repository.getProfile(id)!!.let { child ->
                        child.copy(pace = child.pace.copy(every = 5))
                    },
                    cadenceOnly = true,
                )
                app.container.repository.getProfile(id)!!
            }
        val spoken = mutableListOf<String>()
        var rewards = 0
        var celebrate by mutableStateOf(false)
        compose.setContent {
            CompositionLocalProvider(LocalCelebrate provides celebrate) {
                LessonScreen(
                    container = app.container,
                    profile = profile,
                    module = LearningModule.COUNTING,
                    onSpeak = { spoken += it },
                    onReward = { rewards += 1 },
                    onExit = {},
                )
            }
        }
        repeat(5) { clickCorrect() }
        assertEquals(1, rewards)
        val held = spoken.size
        celebrate = true
        compose.waitForIdle()
        assertEquals(held, spoken.size)
        celebrate = false
        compose.waitForIdle()
        assertTrue(spoken.size > held)
        assertTrue(spoken.last() != VoiceScripts.WELL_DONE)
        repeat(5) { clickCorrect() }
        assertEquals(VoiceScripts.WELL_DONE, spoken.last())
    }

    @Test
    fun leavingAPrizeWithoutWinningSpeaksTheNextPrompt() {
        val app = ApplicationProvider.getApplicationContext<MatAventurasApp>()
        val profile =
            runBlocking {
                val id = app.container.repository.createProfile("Nuno", AgeBand.THREE_TO_FIVE, Mascot.SPEEDY_HEDGEHOG)
                app.container.repository.updateProfile(
                    app.container.repository.getProfile(id)!!.let { child ->
                        child.copy(pace = child.pace.copy(every = 5))
                    },
                    cadenceOnly = true,
                )
                app.container.repository.getProfile(id)!!
            }
        val spoken = mutableListOf<String>()
        val closed = mutableIntStateOf(0)
        compose.setContent {
            CompositionLocalProvider(LocalPrizeClosed provides closed.intValue) {
                MatAventurasTheme(AgeBand.THREE_TO_FIVE) {
                    LessonScreen(
                        container = app.container,
                        profile = profile,
                        module = LearningModule.COUNTING,
                        onSpeak = { spoken += it },
                        onReward = {},
                        onExit = {},
                    )
                }
            }
        }
        repeat(5) { clickCorrect() }
        assertEquals(VoiceScripts.WELL_DONE, spoken.last())
        val held = spoken.size
        closed.intValue = 1
        compose.waitForIdle()
        assertTrue(spoken.size > held)
    }

    private fun clickCorrect() {
        compose.waitUntil(8_000) {
            compose.onAllNodesWithTag("correct-answer").fetchSemanticsNodes().isNotEmpty()
        }
        val before = scoreLine()
        compose
            .onAllNodesWithTag("correct-answer")
            .onFirst()
            .performScrollTo()
            .performClick()
        compose.waitUntil(8_000) { scoreLine() != before }
    }

    private fun clickWrong() {
        compose.waitUntil(8_000) {
            compose.onAllNodesWithTag("distractor").fetchSemanticsNodes().isNotEmpty()
        }
        compose
            .onAllNodesWithTag("distractor")
            .onFirst()
            .performScrollTo()
            .performClick()
        compose.waitForIdle()
    }

    private fun scoreLine(): String {
        val nodes = compose.onAllNodesWithText("certos", substring = true).fetchSemanticsNodes()
        return nodes.joinToString { node ->
            node.config.getOrNull(SemanticsProperties.Text).toString()
        }
    }
}
