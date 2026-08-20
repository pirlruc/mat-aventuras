package pt.mataventuras.app.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.app.di.isRobolectricFingerprint
import pt.mataventuras.app.di.pinIterationsFor
import pt.mataventuras.domain.model.AgeGroup
import pt.mataventuras.domain.model.ChildProfile
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.model.Mascot
import pt.mataventuras.domain.parent.ModulePerformance
import pt.mataventuras.domain.parent.ParentSummary
import pt.mataventuras.domain.progress.AvatarCode
import pt.mataventuras.domain.progress.BadgeCode
import pt.mataventuras.domain.voice.VoiceScripts

class UiLogicTest {
    @Test
    fun starGridRules() {
        assertTrue(UiLogic.showsStarGrid(LearningModule.COUNTING, 3))
        assertTrue(UiLogic.showsStarGrid(LearningModule.SHAPES, 1))
        assertFalse(UiLogic.showsStarGrid(LearningModule.NUMBERS, 4))
        assertFalse(UiLogic.showsStarGrid(LearningModule.COUNTING, 0))
        assertEquals("2 certos · 20 pts", UiLogic.lessonScoreLine(2, 20))
        assertEquals(3, UiLogic.starCenters(3, 120f).size)
        assertEquals(0, UiLogic.starCenters(0, 100f).size)
    }

    @Test
    fun pinAndParentCopy() {
        assertEquals("Guardar PIN", UiLogic.pinSubmitLabel(true))
        assertEquals("Entrar", UiLogic.pinSubmitLabel(false))
        assertEquals(VoiceScripts.SET_PIN, UiLogic.pinPrompt(true))
        assertEquals(VoiceScripts.ENTER_PIN, UiLogic.pinPrompt(false))
        assertTrue(UiLogic.waitingForProfile(null, null))
        val profile =
            ChildProfile(1, "Ana", AgeGroup.THREE_YEARS, Mascot.HERO_PUP, AvatarCode.STARTER.name, 0, 0)
        assertTrue(UiLogic.waitingForProfile(profile, null))
        val summary =
            ParentSummary(1, 1_000, 1, 0, 1.0, emptyList(), emptyList())
        assertFalse(UiLogic.waitingForProfile(profile, summary))
        assertTrue(UiLogic.waitingForProfile(null, summary))
        val module =
            ModulePerformance(LearningModule.ADDITION, 1, 6, 0.14, 90_000)
        assertTrue(UiLogic.modulePerformanceLine(module).contains("addition"))
        assertEquals(
            listOf("Nenhum módulo abaixo de 70% com amostra suficiente."),
            UiLogic.needsWorkLines(emptyList()),
        )
        assertEquals(listOf("• counting"), UiLogic.needsWorkLines(listOf(LearningModule.COUNTING)))
        assertEquals("1m 30s", UiLogic.formatDuration(90_000))
        assertEquals("0m 0s", UiLogic.formatDuration(0))
    }

    @Test
    fun ageAndRewardCopy() {
        assertEquals("Amigo", UiLogic.fallbackChildName("  "))
        assertEquals("Rui", UiLogic.fallbackChildName(" Rui "))
        assertEquals(64, UiLogic.mascotChipDp(AgeGroup.THREE_YEARS))
        assertEquals(48, UiLogic.mascotChipDp(AgeGroup.SEVEN_YEARS))
        assertEquals(180, UiLogic.ageButtonSideDp(true))
        assertEquals(150, UiLogic.ageButtonSideDp(false))
        assertEquals(1f, UiLogic.ageButtonAlpha(true), 0f)
        assertEquals(0.55f, UiLogic.ageButtonAlpha(false), 0f)
        assertEquals("🧸", UiLogic.ageButtonEmoji(true))
        assertEquals("🚀", UiLogic.ageButtonEmoji(false))
        assertTrue(UiLogic.usesIconNav(AgeGroup.THREE_YEARS))
        assertFalse(UiLogic.usesIconNav(AgeGroup.SEVEN_YEARS))
        assertTrue(UiLogic.badgeLine(true, BadgeCode.FIRST_STEPS).startsWith("★"))
        assertTrue(UiLogic.badgeLine(false, BadgeCode.FIRST_STEPS).startsWith("☆"))
        assertTrue(UiLogic.avatarLine(true, AvatarCode.RUNNER).startsWith("★"))
        assertTrue(UiLogic.avatarLine(false, AvatarCode.STARTER).contains("0 pts"))
        assertFalse(UiLogic.languageSupported(-1))
        assertFalse(UiLogic.languageSupported(-2))
        assertTrue(UiLogic.languageSupported(0))
        assertFalse(UiLogic.shouldSpeak(false, "Olá"))
        assertFalse(UiLogic.shouldSpeak(true, " "))
        assertTrue(UiLogic.shouldSpeak(true, "Olá"))
        assertEquals("", EntryLogic.previewFor(null))
        assertEquals(VoiceScripts.AGE_THREE_PREVIEW, EntryLogic.previewFor(AgeGroup.THREE_YEARS))
        val ana =
            ChildProfile(1, "Ana", AgeGroup.THREE_YEARS, Mascot.HERO_PUP, AvatarCode.STARTER.name, 0, 0)
        assertFalse(EntryLogic.showsContinue(null))
        assertTrue(EntryLogic.showsContinue(ana))
        assertEquals("Continuar como Ana", EntryLogic.continueLabel(ana))
        assertFalse(LessonFlow.shouldAskExitConfirm(AgeGroup.THREE_YEARS, false))
        assertTrue(LessonFlow.shouldAskExitConfirm(AgeGroup.SEVEN_YEARS, false))
        assertFalse(LessonFlow.shouldAskExitConfirm(AgeGroup.SEVEN_YEARS, true))
        assertEquals(VoiceScripts.LEAVE, LessonFlow.exitLabel(false))
        assertEquals(VoiceScripts.CONFIRM_LEAVE, LessonFlow.exitLabel(true))
        assertFalse(LessonFlow.showsStay(false))
        assertTrue(LessonFlow.showsStay(true))
        var announced = ""
        var went = false
        HomeNav.announceAndGo({ announced = it }, VoiceScripts.LEADERBOARD, { went = true })
        assertEquals(VoiceScripts.LEADERBOARD, announced)
        assertTrue(went)
        assertTrue(isRobolectricFingerprint("robolectric"))
        assertFalse(isRobolectricFingerprint("google/sdk_gphone64"))
        assertEquals(1_000, pinIterationsFor("robolectric"))
        assertEquals(
            pt.mataventuras.domain.parent.PinPolicy.ITERATIONS,
            pinIterationsFor("user/release-keys"),
        )
        assertTrue(pt.mataventuras.app.di.roomAllowsMainThread("robolectric"))
        assertFalse(pt.mataventuras.app.di.roomAllowsMainThread("user/release-keys"))
        pt.mataventuras.app.di.processFingerprint()
    }
}
