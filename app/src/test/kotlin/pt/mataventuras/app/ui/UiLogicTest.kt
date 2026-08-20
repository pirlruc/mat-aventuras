package pt.mataventuras.app.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.app.di.isRobolectricFingerprint
import pt.mataventuras.app.di.pinIterationsFor
import pt.mataventuras.domain.math.PlayKind
import pt.mataventuras.domain.model.AgeGroup
import pt.mataventuras.domain.model.ChildProfile
import pt.mataventuras.domain.model.GeometricShape
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
        assertFalse(UiLogic.showsStarGrid(LearningModule.SHAPES, 1))
        assertFalse(UiLogic.showsStarGrid(LearningModule.NUMBERS, 4))
        assertFalse(UiLogic.showsStarGrid(LearningModule.COUNTING, 0))
        assertTrue(UiLogic.showsNumberHero(LearningModule.NUMBERS))
        assertFalse(UiLogic.showsNumberHero(LearningModule.COUNTING))
        assertTrue(UiLogic.showsShapeGlyph(LearningModule.SHAPES))
        assertFalse(UiLogic.showsShapeGlyph(LearningModule.COUNTING))
        assertTrue(UiLogic.showsDotStrip(LearningModule.COUNTING))
        assertTrue(UiLogic.showsDotStrip(LearningModule.NUMBERS))
        assertFalse(UiLogic.showsDotStrip(LearningModule.ADDITION))
        assertEquals(GeometricShape.CIRCLE, UiLogic.shapeKind("círculo"))
        assertEquals(null, UiLogic.shapeKind("7"))
        assertEquals(4, UiLogic.optionInt("4"))
        assertEquals(null, UiLogic.optionInt("círculo"))
        assertEquals(104, UiLogic.optionMinHeightDp(LearningModule.SHAPES, 88))
        assertEquals(104, UiLogic.optionMinHeightDp(LearningModule.NUMBERS, 88))
        assertEquals(GeometricShape.SQUARE, UiLogic.shapeKind("quadrado"))
        assertEquals(GeometricShape.TRIANGLE, UiLogic.shapeKind("triângulo"))
        assertEquals(GeometricShape.RECTANGLE, UiLogic.shapeKind("rectângulo"))
        assertEquals(GeometricShape.STAR, UiLogic.shapeKind("estrela"))
        assertEquals(0, UiLogic.optionInt("0"))
        assertEquals(88, UiLogic.optionMinHeightDp(LearningModule.ADDITION, 88))
        assertEquals(
            GeometricShape.STAR,
            UiLogic.targetShapeToDraw(LearningModule.SHAPES, GeometricShape.STAR),
        )
        assertEquals(null, UiLogic.targetShapeToDraw(LearningModule.SHAPES, null))
        assertEquals(null, UiLogic.targetShapeToDraw(LearningModule.COUNTING, GeometricShape.CIRCLE))
        assertEquals(0f to -10f, UiLogic.triangleVertices(0f, 0f, 10f).first())
        assertEquals(3, UiLogic.triangleVertices(0f, 0f, 10f).size)
        assertEquals(10, UiLogic.starVertices(0f, 0f, 10f).size)
        assertEquals(0f, UiLogic.starVertices(0f, 0f, 10f).first().first, 0.001f)
        assertEquals(-10f, UiLogic.starVertices(0f, 0f, 10f).first().second, 0.001f)
        assertEquals("2 certos · 20 pts", UiLogic.lessonScoreLine(2, 20))
        assertEquals(3, UiLogic.starCenters(3, 120f).size)
        assertEquals(0, UiLogic.starCenters(0, 100f).size)
        assertFalse(UiLogic.showsStarGrid(LearningModule.COUNTING, 3, PlayKind.CIPHER))
        assertFalse(UiLogic.showsNumberHero(LearningModule.NUMBERS, PlayKind.SUDOKU))
        assertEquals(
            null,
            UiLogic.targetShapeToDraw(
                LearningModule.SHAPES,
                GeometricShape.CIRCLE,
                PlayKind.SOUP,
            ),
        )
        assertEquals(
            GeometricShape.CIRCLE,
            UiLogic.targetShapeToDraw(
                LearningModule.SHAPES,
                GeometricShape.CIRCLE,
                PlayKind.PUZZLE,
            ),
        )
        assertTrue(UiLogic.showsPlayGrid(PlayKind.SOUP))
        assertTrue(UiLogic.showsPlayGrid(PlayKind.SUDOKU))
        assertFalse(UiLogic.showsPlayGrid(PlayKind.CHOICE))
        assertTrue(UiLogic.showsSoupBoard(PlayKind.SOUP))
        assertFalse(UiLogic.showsSoupBoard(PlayKind.SUDOKU))
        assertTrue(UiLogic.showsSudokuGrid(PlayKind.SUDOKU))
        assertFalse(UiLogic.showsSudokuGrid(PlayKind.SOUP))
        assertTrue(UiLogic.showsCipherLegend(PlayKind.CIPHER))
        assertFalse(UiLogic.showsCipherLegend(PlayKind.CHOICE))
        assertTrue(UiLogic.showsPuzzleFrame(PlayKind.PUZZLE))
        assertFalse(UiLogic.showsPuzzleFrame(PlayKind.CHOICE))
        assertEquals(0, UiLogic.lessonLevel(0))
        assertEquals(0, UiLogic.lessonLevel(2))
        assertEquals(1, UiLogic.lessonLevel(3))
        assertEquals(2, UiLogic.lessonLevel(6))
        assertEquals(3, UiLogic.lessonLevel(99))
        assertEquals(72, UiLogic.playCellHeightDp(2))
        assertEquals(56, UiLogic.playCellHeightDp(4))
        assertEquals(52, UiLogic.playCellHeightDp(5))
        assertEquals(44, UiLogic.playCellHeightDp(6))
        assertTrue(UiLogic.shouldAcceptAnswer(false))
        assertFalse(UiLogic.shouldAcceptAnswer(true))
        assertTrue(UiLogic.shouldRepeatSpokenPrompt(true))
        assertFalse(UiLogic.shouldRepeatSpokenPrompt(false))
        assertTrue(UiLogic.showsOptionPalette(PlayKind.CHOICE))
        assertFalse(UiLogic.showsOptionPalette(PlayKind.SOUP))
        assertEquals("correct-answer", UiLogic.answerTag(true))
        assertEquals("distractor", UiLogic.answerTag(false))
        assertEquals(3, UiLogic.boardRowCount(9, 3))
        assertEquals(0, UiLogic.boardRowCount(4, 0))
        assertEquals("?", UiLogic.holeLabel(""))
        assertEquals("3", UiLogic.holeLabel("3"))
        assertTrue(UiLogic.isBoardHole(""))
        assertTrue(UiLogic.isBoardHole("?"))
        assertFalse(UiLogic.isBoardHole("2"))
        assertEquals("✓", UiLogic.answerFlashGlyph(true))
        assertEquals("✗", UiLogic.answerFlashGlyph(false))
        assertEquals(VoiceScripts.WELL_DONE, UiLogic.answerFlashCaption(true))
        assertEquals(VoiceScripts.TRY_AGAIN, UiLogic.answerFlashCaption(false))
        assertEquals(0xFF2E7D32, UiLogic.answerFlashArgb(true))
        assertEquals(0xFFC62828, UiLogic.answerFlashArgb(false))
        assertEquals(0x882E7D32, UiLogic.answerFlashScrimArgb(true))
        assertEquals(0x88C62828, UiLogic.answerFlashScrimArgb(false))
        assertEquals(480, UiLogic.answerFlashMs())
        assertEquals(1.2f, UiLogic.answerFlashScale(1f), 0.001f)
        assertEquals(0.85f, UiLogic.answerFlashScale(0f), 0.001f)
        assertTrue(UiLogic.showsAnswerFlash(0.4f))
        assertFalse(UiLogic.showsAnswerFlash(0f))
        assertEquals(android.media.ToneGenerator.TONE_PROP_ACK, UiLogic.answerTone(true))
        assertEquals(android.media.ToneGenerator.TONE_PROP_NACK, UiLogic.answerTone(false))
        assertEquals(180, UiLogic.answerToneMs(true))
        assertEquals(260, UiLogic.answerToneMs(false))
        assertEquals(80, UiLogic.answerVolumePercent())
        assertEquals(android.view.HapticFeedbackConstants.CONFIRM, UiLogic.answerHaptic(true, 34))
        assertEquals(android.view.HapticFeedbackConstants.REJECT, UiLogic.answerHaptic(false, 34))
        assertEquals(android.view.HapticFeedbackConstants.CONTEXT_CLICK, UiLogic.answerHaptic(true, 29))
        assertEquals(android.view.HapticFeedbackConstants.LONG_PRESS, UiLogic.answerHaptic(false, 29))
        UiLogic.answerHaptic(true)
        assertFalse(UiLogic.lessonFillsViewport(AgeGroup.THREE_YEARS))
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
        assertEquals(52, UiLogic.mascotChipDp(AgeGroup.SEVEN_YEARS))
        assertEquals(168, UiLogic.ageButtonSideDp())
        assertEquals(1f, UiLogic.ageButtonAlpha(true), 0f)
        assertEquals(0.7f, UiLogic.ageButtonAlpha(false), 0f)
        assertEquals(6, UiLogic.selectionBorderDp(true))
        assertEquals(2, UiLogic.selectionBorderDp(false))
        assertEquals(0xFFE65100, UiLogic.selectionHighlightArgb(true))
        assertEquals(0xFF90A4AE, UiLogic.selectionHighlightArgb(false))
        assertEquals("🦔", UiLogic.mascotGlyph(Mascot.SPEEDY_HEDGEHOG))
        assertEquals("🐶", UiLogic.mascotGlyph(Mascot.HERO_PUP))
        assertEquals("🐷", UiLogic.mascotGlyph(Mascot.PINK_PIGLET))
        assertEquals("🔧", UiLogic.mascotGlyph(Mascot.BRAVE_PLUMBER))
        assertEquals("👽", UiLogic.mascotGlyph(Mascot.MISCHIEVOUS_ALIEN))
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

    @Test
    fun soupSlideSelectsTheWholeWord() {
        val word = listOf(4, 5, 6, 7)
        assertEquals(SoupRelease.HIT, UiLogic.soupReleaseKind(word, word))
        assertEquals(SoupRelease.HIT, UiLogic.soupReleaseKind(word.asReversed(), word))
        assertEquals(SoupRelease.IGNORE, UiLogic.soupReleaseKind(listOf(4), word))
        assertEquals(SoupRelease.IGNORE, UiLogic.soupReleaseKind(listOf(4, 5), word))
        assertEquals(SoupRelease.MISS, UiLogic.soupReleaseKind(listOf(0, 1, 2, 3), word))
        assertEquals(SoupRelease.IGNORE, UiLogic.soupReleaseKind(emptyList(), word))
        assertEquals(SoupRelease.HIT, UiLogic.soupReleaseKind(listOf(2), listOf(2)))
        assertEquals(SoupRelease.MISS, UiLogic.soupReleaseKind(listOf(1), listOf(2)))
        assertEquals(4, UiLogic.soupPickIndex(word, word, 16))
        assertEquals(0, UiLogic.soupPickIndex(listOf(0, 1, 2, 3), word, 16))
        assertEquals(null, UiLogic.soupPickIndex(listOf(4), word, 16))
        assertEquals(2, UiLogic.soupPickIndex(listOf(2), listOf(2), 9))
        assertEquals(0, UiLogic.soupMissIndex(listOf(1, 2), 4))
        assertEquals(0, UiLogic.soupMissIndex(listOf(0, 1, 2), 3))
        assertEquals(listOf(0), UiLogic.soupExtendPath(emptyList(), 0, 4))
        assertEquals(listOf(0, 1), UiLogic.soupExtendPath(listOf(0), 1, 4))
        assertEquals(listOf(0), UiLogic.soupExtendPath(listOf(0), 0, 4))
        assertEquals(listOf(0), UiLogic.soupExtendPath(listOf(0), 2, 4))
        assertTrue(UiLogic.soupAreAdjacent(0, 1, 4))
        assertTrue(UiLogic.soupAreAdjacent(0, 5, 4))
        assertFalse(UiLogic.soupAreAdjacent(0, 2, 4))
        assertFalse(UiLogic.soupAreAdjacent(0, 0, 4))
        assertFalse(UiLogic.soupAreAdjacent(0, 1, 0))
        assertEquals(0, UiLogic.soupIndexAt(10f, 10f, 100f, 100f, 2, 4, 0f))
        assertEquals(3, UiLogic.soupIndexAt(80f, 80f, 100f, 100f, 2, 4, 0f))
        assertEquals(null, UiLogic.soupIndexAt(-1f, 10f, 100f, 100f, 2, 4, 0f))
        assertEquals(null, UiLogic.soupIndexAt(10f, 10f, 0f, 100f, 2, 4, 0f))
        assertEquals(null, UiLogic.soupIndexAt(10f, 10f, 100f, 100f, 0, 4, 0f))
        assertEquals(null, UiLogic.soupSlot(-1f, 40f, 8f, 2))
        assertEquals(0, UiLogic.soupSlot(10f, 40f, 8f, 2))
        assertEquals(1, UiLogic.soupSlot(50f, 40f, 8f, 2))
        assertEquals(null, UiLogic.soupIndexAt(10f, 10f, 100f, 100f, 2, 0, 0f))
        assertEquals(0xFFFFCC80, UiLogic.soupSelectedArgb(true))
        assertEquals(0xFF90CAF9, UiLogic.soupSelectedArgb(false))
        assertEquals(SoupRelease.IGNORE, UiLogic.soupReleaseKind(listOf(1), emptyList()))
        assertEquals(listOf(0, 1), UiLogic.soupExtendPath(listOf(0, 1), 0, 4))
        assertEquals(null, UiLogic.soupIndexAt(80f, 80f, 100f, 100f, 2, 3, 0f))
        assertEquals(null, UiLogic.soupIndexAt(10f, 10f, 100f, 0f, 2, 4, 0f))
        assertEquals(null, UiLogic.soupSlot(44f, 40f, 8f, 2))
        assertEquals(1, UiLogic.soupIndexAt(60f, 10f, 100f, 40f, 2, 2, 8f))
        assertEquals(null, UiLogic.soupIndexAt(10f, 10f, 10f, 100f, 2, 4, 100f))
    }
}
