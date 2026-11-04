package pt.mataventuras.app.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.app.di.pinIterationsFor
import pt.mataventuras.app.engine.GodotRuntime
import pt.mataventuras.app.ui.lesson.memoryCardDescription
import pt.mataventuras.app.ui.lesson.memoryCardTag
import pt.mataventuras.app.ui.lesson.pipOffsets
import pt.mataventuras.app.ui.lesson.quantityPips
import pt.mataventuras.app.ui.lesson.showsCardFront
import pt.mataventuras.domain.math.GroupTokens
import pt.mataventuras.domain.math.PlayBoard
import pt.mataventuras.domain.math.PlayKind
import pt.mataventuras.domain.math.SudokuHoles
import pt.mataventuras.domain.model.AgeBand
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
        assertTrue(showsStarGrid(LearningModule.COUNTING, 3))
        assertFalse(showsStarGrid(LearningModule.SHAPES, 1))
        assertFalse(showsStarGrid(LearningModule.NUMBERS, 4))
        assertFalse(showsStarGrid(LearningModule.COUNTING, 0))
        assertTrue(showsNumberHero(LearningModule.NUMBERS))
        assertFalse(showsNumberHero(LearningModule.COUNTING))
        assertTrue(showsShapeGlyph(LearningModule.SHAPES))
        assertFalse(showsShapeGlyph(LearningModule.COUNTING))
        assertTrue(showsDotStrip(LearningModule.COUNTING))
        assertTrue(showsDotStrip(LearningModule.NUMBERS))
        assertFalse(showsDotStrip(LearningModule.ADDITION))
        assertEquals(GeometricShape.CIRCLE, shapeKind("círculo"))
        assertEquals(null, shapeKind("7"))
        assertEquals(4, optionInt("4"))
        assertEquals(null, optionInt("círculo"))
        assertEquals(104, optionMinHeightDp(LearningModule.SHAPES, 88))
        assertEquals(104, optionMinHeightDp(LearningModule.NUMBERS, 88))
        assertEquals(GeometricShape.SQUARE, shapeKind("quadrado"))
        assertEquals(GeometricShape.TRIANGLE, shapeKind("triângulo"))
        assertEquals(GeometricShape.RECTANGLE, shapeKind("retângulo"))
        assertEquals(GeometricShape.STAR, shapeKind("estrela"))
        assertEquals(0, optionInt("0"))
        assertEquals(88, optionMinHeightDp(LearningModule.ADDITION, 88))
        assertEquals(
            GeometricShape.STAR,
            targetShapeToDraw(LearningModule.SHAPES, GeometricShape.STAR),
        )
        assertEquals(null, targetShapeToDraw(LearningModule.SHAPES, null))
        assertEquals(null, targetShapeToDraw(LearningModule.COUNTING, GeometricShape.CIRCLE))
        assertEquals(0f to -10f, triangleVertices(0f, 0f, 10f).first())
        assertEquals(3, triangleVertices(0f, 0f, 10f).size)
        assertEquals(10, starVertices(0f, 0f, 10f).size)
        assertEquals(0f, starVertices(0f, 0f, 10f).first().first, 0.001f)
        assertEquals(-10f, starVertices(0f, 0f, 10f).first().second, 0.001f)
        assertEquals("2 certos · 20 pontos", lessonScoreLine(2, 20))
        assertEquals(3, starCenters(3, 120f).size)
        assertEquals(0, starCenters(0, 100f).size)
        assertFalse(showsStarGrid(LearningModule.COUNTING, 3, PlayKind.CIPHER))
        assertFalse(showsNumberHero(LearningModule.NUMBERS, PlayKind.SUDOKU))
        assertEquals(
            null,
            targetShapeToDraw(
                LearningModule.SHAPES,
                GeometricShape.CIRCLE,
                PlayKind.SOUP,
            ),
        )
        assertEquals(
            null,
            targetShapeToDraw(
                LearningModule.SHAPES,
                GeometricShape.CIRCLE,
                PlayKind.SUDOKU,
            ),
        )
        assertEquals(
            GeometricShape.CIRCLE,
            targetShapeToDraw(
                LearningModule.SHAPES,
                GeometricShape.CIRCLE,
                PlayKind.PUZZLE,
            ),
        )
        assertTrue(showsPlayGrid(PlayKind.SOUP))
        assertTrue(showsPlayGrid(PlayKind.SUDOKU))
        assertFalse(showsPlayGrid(PlayKind.CHOICE))
        assertTrue(showsSoupBoard(PlayKind.SOUP))
        assertFalse(showsSoupBoard(PlayKind.SUDOKU))
        assertTrue(showsSudokuGrid(PlayKind.SUDOKU))
        assertFalse(showsSudokuGrid(PlayKind.SOUP))
        assertEquals("Sudoku", sudokuBanner(PlayKind.SUDOKU))
        assertEquals(
            "Quadrado mágico",
            sudokuBanner(PlayKind.SUDOKU, pt.mataventuras.domain.model.AgeBand.THREE_TO_FIVE),
        )
        assertEquals(null, sudokuBanner(PlayKind.SOUP))
        assertEquals(2, sudokuBoxWidth(4))
        assertEquals(3, sudokuBoxWidth(6))
        assertEquals(3, sudokuBoxWidth(3))
        assertEquals(2, sudokuBoxHeight(4))
        assertEquals(2, sudokuBoxHeight(6))
        assertEquals(3, sudokuBoxHeight(3))
        assertEquals(1, sudokuGapDp(0, 2))
        assertEquals(1, sudokuGapDp(1, 2))
        assertEquals(4, sudokuGapDp(2, 2))
        assertTrue(showsCipherLegend(PlayKind.CIPHER))
        assertFalse(showsCipherLegend(PlayKind.CHOICE))
        assertTrue(showsPuzzleFrame(PlayKind.PUZZLE))
        assertFalse(showsPuzzleFrame(PlayKind.CHOICE))
        assertEquals(0, lessonLevel(0))
        assertEquals(0, lessonLevel(2))
        assertEquals(1, lessonLevel(3))
        assertEquals(2, lessonLevel(6))
        assertEquals(3, lessonLevel(99))
        assertEquals(72, playCellHeightDp(2))
        assertEquals(56, playCellHeightDp(4))
        assertEquals(52, playCellHeightDp(5))
        assertEquals(48, playCellHeightDp(6))
        assertEquals(64, playCellHeightDp(6, pt.mataventuras.domain.model.AgeBand.THREE_TO_FIVE))
        assertTrue(shouldAcceptAnswer(false))
        assertFalse(shouldAcceptAnswer(true))
        assertTrue(shouldRepeatSpokenPrompt(true))
        assertFalse(shouldRepeatSpokenPrompt(false))
        assertTrue(showsOptionPalette(PlayKind.CHOICE))
        assertFalse(showsOptionPalette(PlayKind.SOUP))
        debugAnswerTags = true
        assertEquals("correct-answer", answerTag(true))
        assertEquals("distractor", answerTag(false))
        assertEquals("correct-answer", optionAnswerTag(correct = true, fillEveryBlank = false))
        assertEquals("distractor", optionAnswerTag(correct = false, fillEveryBlank = false))
        assertEquals("sudoku-digit", optionAnswerTag(correct = true, fillEveryBlank = true))
        assertEquals("sudoku-digit", optionAnswerTag(correct = false, fillEveryBlank = true))
        assertEquals(3, boardRowCount(9, 3))
        assertEquals(0, boardRowCount(4, 0))
        assertEquals("?", holeLabel(""))
        assertEquals("3", holeLabel("3"))
        assertEquals("", holeLabel(SudokuHoles.EXTRA_BLANK))
        assertEquals("", holeLabel("", fillEveryBlank = true))
        assertEquals("", holeLabel(SudokuHoles.EXTRA_BLANK, fillEveryBlank = true))
        assertEquals("4", holeLabel("4", fillEveryBlank = true))
        assertTrue(isBoardHole(""))
        assertTrue(isBoardHole("?"))
        assertTrue(isBoardHole(SudokuHoles.EXTRA_BLANK))
        assertTrue(isQuestionHole(""))
        assertFalse(isQuestionHole(SudokuHoles.EXTRA_BLANK))
        assertTrue(isExtraBlank(SudokuHoles.EXTRA_BLANK))
        assertFalse(isBoardHole("2"))
        assertEquals(0xFFFFF59D, sudokuCellArgb(""))
        assertEquals(0xFFEEEEEE, sudokuCellArgb(SudokuHoles.EXTRA_BLANK))
        assertEquals(0xFFE3F2FD, sudokuCellArgb("4"))
        assertEquals(0xFFFFF59D, sudokuCellArgb("", focused = true, fillEveryBlank = true))
        assertEquals(0xFFEEEEEE, sudokuCellArgb(SudokuHoles.EXTRA_BLANK, fillEveryBlank = true))
        assertEquals(0xFFE3F2FD, sudokuCellArgb("4", focused = true, fillEveryBlank = true))
        assertFalse(sudokuFillsEveryBlank(PlayBoard()))
        assertFalse(sudokuFillsEveryBlank(PlayBoard(kind = PlayKind.MEMORY, solution = listOf("1"))))
        assertTrue(sudokuFillsEveryBlank(PlayBoard(kind = PlayKind.SUDOKU, solution = listOf("1"))))
        assertTrue(contrastRatio(SHAPE_ON_BUTTON_ARGB, PRIMARY_BUTTON_ARGB) >= 4.5)
        assertTrue(contrastRatio(SHAPE_ON_BUTTON_ARGB, CONTINUE_BUTTON_ARGB) >= 4.5)
        assertTrue(CONTINUE_BUTTON_ARGB != PRIMARY_BUTTON_ARGB)
        assertEquals(PromptCue.Speak, promptCue(false, false, false))
        assertEquals(PromptCue.Wait, promptCue(true, false, false))
        assertEquals(PromptCue.Wait, promptCue(true, true, true))
        assertEquals(PromptCue.Release, promptCue(true, true, false))
        assertFalse(shouldRepeatSpokenPrompt(true, prizeCovering = true))
        val order = mutableListOf<String>()
        beginReward(stopSpeech = { order += "stop" }, launch = { order += "launch" })
        assertEquals(listOf("stop", "launch"), order)
        assertEquals("par-1", memoryCardTag(open = false, debug = true, pips = 1, pairKey = "1"))
        assertEquals("memory-pip", memoryCardTag(open = true, debug = true, pips = 1, pairKey = "1"))
        assertEquals("memory-face", memoryCardTag(open = false, debug = false, pips = null, pairKey = "A"))
        assertEquals("par-1", memoryCardDescription(false, true, 1, "1", "1"))
        assertEquals("carta fechada", memoryCardDescription(false, false, null, "A", "A"))
        assertEquals("1", memoryCardDescription(true, true, 1, "1", "qty:1"))
        assertEquals("A", memoryCardDescription(true, false, null, "A", "A"))
        assertEquals(1, quantityPips("qty:1"))
        assertEquals(6, quantityPips("qty:6"))
        assertEquals(null, quantityPips("1"))
        assertEquals(null, quantityPips("qty:"))
        assertEquals(null, quantityPips("qty:0"))
        assertEquals(null, quantityPips("qty:9"))
        assertTrue(showsCardFront(0f))
        assertFalse(showsCardFront(180f))
        assertEquals(listOf(1, 2, 3, 4, 5, 6), (1..6).map { pipOffsets(it).size })
        assertTrue(contrastRatio(0xFFFFFFFF, 0xFF000000) >= 4.5)
        assertEquals("✓", answerFlashGlyph(true))
        assertEquals("✗", answerFlashGlyph(false))
        assertEquals(VoiceScripts.WELL_DONE, answerFlashCaption(true))
        assertEquals(VoiceScripts.TRY_AGAIN, answerFlashCaption(false))
        assertEquals(0xFF2E7D32, answerFlashArgb(true))
        assertEquals(0xFFC62828, answerFlashArgb(false))
        assertEquals(0x882E7D32, answerFlashScrimArgb(true))
        assertEquals(0x88C62828, answerFlashScrimArgb(false))
        assertEquals(960, UiLogic.ANSWER_FLASH_MS)
        assertEquals(1.2f, answerFlashScale(1f), 0.001f)
        assertEquals(0.85f, answerFlashScale(0f), 0.001f)
        assertTrue(showsAnswerFlash(0.4f))
        assertFalse(showsAnswerFlash(0f))
        assertEquals(android.media.ToneGenerator.TONE_PROP_ACK, answerTone(true))
        assertEquals(android.media.ToneGenerator.TONE_PROP_NACK, answerTone(false))
        assertEquals(android.media.ToneGenerator.TONE_PROP_BEEP, answerTone(false, gentle = true))
        assertEquals(0xFFF9A825, answerFlashArgb(false, gentle = true))
        assertEquals("Ouriço", answerFlashGlyph(false, gentle = true, mascot = "Ouriço"))
        assertEquals("✗", answerFlashGlyph(false, gentle = true))
        assertEquals(0x88F9A825, answerFlashScrimArgb(false, gentle = true))
        assertEquals(140, answerToneMs(false, gentle = true))
        assertEquals(0, prizeStars(0))
        assertEquals(0, prizeStars(4, 0))
        assertEquals(2, prizeStars(2, 10))
        assertEquals(10, prizeStars(10, 10))
        assertEquals(3, prizeStars(3, 3))
        assertEquals("★★☆", prizeStarLine(2, 3))
        assertEquals("★★★★★★★★★★", prizeStarLine(10, 10))
        assertEquals(180, answerToneMs(true))
        assertEquals(260, answerToneMs(false))
        assertEquals(80, UiLogic.ANSWER_VOLUME_PERCENT)
        assertEquals(android.view.HapticFeedbackConstants.CONFIRM, answerHaptic(true, 34))
        assertEquals(android.view.HapticFeedbackConstants.REJECT, answerHaptic(false, 34))
        assertEquals(android.view.HapticFeedbackConstants.CONTEXT_CLICK, answerHaptic(true, 29))
        assertEquals(android.view.HapticFeedbackConstants.LONG_PRESS, answerHaptic(false, 29))
        answerHaptic(true)
        assertFalse(lessonFillsViewport(AgeBand.THREE_TO_FIVE))
        assertTrue(lessonFillsViewport(AgeBand.SIX_TO_EIGHT))
        assertTrue(lessonScrolls(AgeBand.THREE_TO_FIVE, PlayKind.CHOICE))
        assertFalse(lessonScrolls(AgeBand.SIX_TO_EIGHT, PlayKind.CHOICE))
        assertTrue(lessonScrolls(AgeBand.SIX_TO_EIGHT, PlayKind.SUDOKU))
        assertTrue(lessonScrolls(AgeBand.SIX_TO_EIGHT, PlayKind.SOUP))
        assertTrue(lessonScrolls(AgeBand.SIX_TO_EIGHT, PlayKind.CIPHER))
        assertTrue(lessonScrolls(AgeBand.SIX_TO_EIGHT, PlayKind.PUZZLE))
        assertTrue(lessonScrolls(AgeBand.SIX_TO_EIGHT, PlayKind.GROUPS))
        assertTrue(showsGroupsBoard(PlayKind.GROUPS))
        assertFalse(showsGroupsBoard(PlayKind.CHOICE))
        assertTrue(scrollsForBoard(PlayKind.GROUPS))
        assertFalse(scrollsForBoard(PlayKind.CHOICE))
        assertEquals("Jogo das cruzes", groupsBanner(LearningModule.SUBTRACTION))
        assertEquals("Jogo das filas", groupsBanner(LearningModule.DIVISION))
        assertEquals("Jogo da grelha", groupsBanner(LearningModule.MULTIPLICATION))
        assertEquals(0xFFFFCDD2, groupTokenArgb(GroupTokens.REMOVED))
        assertEquals(0xFFC8E6C9, groupTokenArgb(GroupTokens.KEPT))
    }

    @Test
    fun pinAndParentCopy() {
        assertEquals("Guardar PIN", pinSubmitLabel(true))
        assertEquals("Entrar", pinSubmitLabel(false))
        assertEquals(VoiceScripts.SET_PIN, pinPrompt(true))
        assertEquals(VoiceScripts.ENTER_PIN, pinPrompt(false))
        assertTrue(waitingForProfile(null, null))
        val profile =
            ChildProfile(1, "Ana", AgeBand.THREE_TO_FIVE, Mascot.HERO_PUP, AvatarCode.STARTER.name, 0, 0)
        assertTrue(waitingForProfile(profile, null))
        val summary =
            ParentSummary(1, 1_000, 1, 0, 1.0, emptyList(), emptyList())
        assertFalse(waitingForProfile(profile, summary))
        assertTrue(waitingForProfile(null, summary))
        val module =
            ModulePerformance(LearningModule.ADDITION, 1, 6, 0.14, 90_000)
        assertTrue(modulePerformanceLine(module).contains("Somar"))
        assertEquals(
            listOf("Nenhum módulo abaixo de 70% com amostra suficiente."),
            needsWorkLines(emptyList()),
        )
        assertEquals(listOf("• Contar"), needsWorkLines(listOf(LearningModule.COUNTING)))
        assertEquals("1m 30s", formatDuration(90_000))
        assertEquals("0m 0s", formatDuration(0))
    }

    @Test
    fun ageAndRewardCopy() {
        assertEquals("Amigo", fallbackChildName("  "))
        assertEquals("Rui", fallbackChildName(" Rui "))
        assertEquals(64, mascotChipDp(AgeBand.THREE_TO_FIVE))
        assertEquals(52, mascotChipDp(AgeBand.SIX_TO_EIGHT))
        assertEquals(168, UiLogic.AGE_BUTTON_SIDE_DP)
        assertEquals(1f, ageButtonAlpha(true), 0f)
        assertEquals(0.7f, ageButtonAlpha(false), 0f)
        assertEquals(6, selectionBorderDp(true))
        assertEquals(2, selectionBorderDp(false))
        assertEquals(0xFFE65100, selectionHighlightArgb(true))
        assertEquals(0xFF90A4AE, selectionHighlightArgb(false))
        assertEquals("🦔", mascotGlyph(Mascot.SPEEDY_HEDGEHOG))
        assertEquals("🐶", mascotGlyph(Mascot.HERO_PUP))
        assertEquals("🐷", mascotGlyph(Mascot.PINK_PIGLET))
        assertEquals("🔧", mascotGlyph(Mascot.BRAVE_PLUMBER))
        assertEquals("👽", mascotGlyph(Mascot.MISCHIEVOUS_ALIEN))
        assertEquals("🧸", ageButtonEmoji(true))
        assertEquals("🚀", ageButtonEmoji(false))
        assertTrue(usesIconNav(AgeBand.THREE_TO_FIVE))
        assertFalse(usesIconNav(AgeBand.SIX_TO_EIGHT))
        assertTrue(badgeLine(true, BadgeCode.FIRST_STEPS).startsWith("★"))
        assertTrue(badgeLine(false, BadgeCode.FIRST_STEPS).startsWith("☆"))
        assertTrue(avatarLine(true, AvatarCode.RUNNER).startsWith("★"))
        assertTrue(avatarLine(false, AvatarCode.STARTER).contains("0 pontos"))
        assertFalse(languageSupported(-1))
        assertFalse(languageSupported(-2))
        assertTrue(languageSupported(0))
        assertFalse(shouldSpeak(false, "Olá"))
        assertFalse(shouldSpeak(true, " "))
        assertTrue(shouldSpeak(true, "Olá"))
        assertEquals("", EntryLogic.previewFor(null))
        assertEquals(VoiceScripts.AGE_THREE_PREVIEW, EntryLogic.previewFor(AgeBand.THREE_TO_FIVE))
        val ana =
            ChildProfile(1, "Ana", AgeBand.THREE_TO_FIVE, Mascot.HERO_PUP, AvatarCode.STARTER.name, 0, 0)
        assertFalse(EntryLogic.showsContinue(null))
        assertTrue(EntryLogic.showsContinue(ana))
        assertEquals("Continuar como Ana", EntryLogic.continueLabel(ana))
        assertTrue(LessonFlow.shouldAskExitConfirm(AgeBand.THREE_TO_FIVE, false))
        assertFalse(LessonFlow.shouldAskExitConfirm(AgeBand.THREE_TO_FIVE, true))
        assertTrue(LessonFlow.shouldAskExitConfirm(AgeBand.SIX_TO_EIGHT, false))
        assertFalse(LessonFlow.shouldAskExitConfirm(AgeBand.SIX_TO_EIGHT, true))
        assertEquals(VoiceScripts.LEAVE, LessonFlow.exitLabel(false))
        assertEquals(VoiceScripts.CONFIRM_LEAVE, LessonFlow.exitLabel(true))
        assertFalse(LessonFlow.showsStay(false))
        assertTrue(LessonFlow.showsStay(true))
        var announced = ""
        var went = false
        HomeNav.announceAndGo({ announced = it }, VoiceScripts.LEADERBOARD, { went = true })
        assertEquals(VoiceScripts.LEADERBOARD, announced)
        assertTrue(went)
        assertTrue(GodotRuntime.isRobolectricFingerprint("robolectric"))
        assertFalse(GodotRuntime.isRobolectricFingerprint("google/sdk_gphone64"))
        assertEquals(1_000, pinIterationsFor("robolectric"))
        assertEquals(
            pt.mataventuras.domain.parent.PinPolicy.ITERATIONS,
            pinIterationsFor("user/release-keys"),
        )
        assertTrue(
            pt.mataventuras.app.di
                .roomAllowsMainThread("robolectric"),
        )
        assertFalse(
            pt.mataventuras.app.di
                .roomAllowsMainThread("user/release-keys"),
        )
        pt.mataventuras.app.di
            .processFingerprint()
    }

    @Test
    fun soupSlideSelectsTheWholeWord() {
        val word = listOf(4, 5, 6, 7)
        assertEquals(SoupRelease.HIT, soupReleaseKind(word, word))
        assertEquals(SoupRelease.HIT, soupReleaseKind(word.asReversed(), word))
        assertEquals(SoupRelease.IGNORE, soupReleaseKind(listOf(4), word))
        assertEquals(SoupRelease.IGNORE, soupReleaseKind(listOf(4, 5), word))
        assertEquals(SoupRelease.MISS, soupReleaseKind(listOf(0, 1, 2, 3), word))
        assertEquals(SoupRelease.IGNORE, soupReleaseKind(emptyList(), word))
        assertEquals(SoupRelease.HIT, soupReleaseKind(listOf(2), listOf(2)))
        assertEquals(SoupRelease.MISS, soupReleaseKind(listOf(1), listOf(2)))
        assertEquals(4, soupPickIndex(word, word, 16))
        assertEquals(0, soupPickIndex(listOf(0, 1, 2, 3), word, 16))
        assertEquals(null, soupPickIndex(listOf(4), word, 16))
        assertEquals(2, soupPickIndex(listOf(2), listOf(2), 9))
        assertEquals(0, soupMissIndex(listOf(1, 2), 4))
        assertEquals(0, soupMissIndex(listOf(0, 1, 2), 3))
        assertEquals(listOf(0), soupExtendPath(emptyList(), 0, 4))
        assertEquals(listOf(0, 1), soupExtendPath(listOf(0), 1, 4))
        assertEquals(listOf(0), soupExtendPath(listOf(0), 0, 4))
        assertEquals(listOf(0), soupExtendPath(listOf(0), 2, 4))
        assertTrue(soupAreAdjacent(0, 1, 4))
        assertTrue(soupAreAdjacent(0, 5, 4))
        assertFalse(soupAreAdjacent(0, 2, 4))
        assertFalse(soupAreAdjacent(0, 0, 4))
        assertFalse(soupAreAdjacent(0, 1, 0))
        assertEquals(0, soupIndexAt(10f, 10f, 100f, 100f, 2, 4, 0f))
        assertEquals(3, soupIndexAt(80f, 80f, 100f, 100f, 2, 4, 0f))
        assertEquals(null, soupIndexAt(-1f, 10f, 100f, 100f, 2, 4, 0f))
        assertEquals(null, soupIndexAt(10f, 10f, 0f, 100f, 2, 4, 0f))
        assertEquals(null, soupIndexAt(10f, 10f, 100f, 100f, 0, 4, 0f))
        assertEquals(null, soupSlot(-1f, 40f, 8f, 2))
        assertEquals(0, soupSlot(10f, 40f, 8f, 2))
        assertEquals(1, soupSlot(50f, 40f, 8f, 2))
        assertEquals(null, soupIndexAt(10f, 10f, 100f, 100f, 2, 0, 0f))
        assertEquals(0xFFFFCC80, soupSelectedArgb(true))
        assertEquals(0xFF90CAF9, soupSelectedArgb(false))
        assertEquals(0xFF81C784, soupSelectedArgb(false, found = true))
        assertEquals(SoupRelease.IGNORE, soupReleaseKind(listOf(1), emptyList(), emptySet()))
        assertEquals(null, soupPickIndex(listOf(1), emptyList(), emptySet(), 16))
        val two = listOf(listOf(0, 1, 2), listOf(4, 5, 6))
        assertEquals(0, soupMatchedPath(listOf(0, 1, 2), two, emptySet()))
        assertEquals(1, soupMatchedPath(listOf(6, 5, 4), two, setOf(0)))
        assertEquals(null, soupMatchedPath(listOf(0, 1, 2), two, setOf(0)))
        assertEquals(SoupRelease.HIT, soupReleaseKind(listOf(4, 5, 6), two, emptySet()))
        assertEquals(SoupRelease.IGNORE, soupReleaseKind(listOf(4), two, emptySet()))
        assertEquals(SoupRelease.MISS, soupReleaseKind(listOf(8, 9, 10), two, emptySet()))
        assertEquals(4, soupPickIndex(listOf(4, 5, 6), two, emptySet(), 16))
        assertEquals(0xFF81C784, soupSelectedArgb(true, found = true))
        assertEquals(SoupRelease.MISS, soupReleaseKind(listOf(0, 1, 2), two, setOf(0, 1)))
        assertEquals(3, soupPickIndex(listOf(8, 9, 10), two, emptySet(), 16))
        assertTrue(soupKeepsBoard(SoupRelease.MISS, 2))
        assertFalse(soupKeepsBoard(SoupRelease.MISS, 1))
        assertFalse(soupKeepsBoard(SoupRelease.HIT, 2))
        assertFalse(soupKeepsBoard(SoupRelease.IGNORE, 2))
        assertEquals(listOf(0, 1), soupExtendPath(listOf(0, 1), 0, 4))
        assertEquals(null, soupIndexAt(80f, 80f, 100f, 100f, 2, 3, 0f))
        assertEquals(null, soupIndexAt(10f, 10f, 100f, 0f, 2, 4, 0f))
        assertEquals(null, soupSlot(44f, 40f, 8f, 2))
        assertEquals(1, soupIndexAt(60f, 10f, 100f, 40f, 2, 2, 8f))
        assertEquals(null, soupIndexAt(10f, 10f, 10f, 100f, 2, 4, 100f))
    }
}
