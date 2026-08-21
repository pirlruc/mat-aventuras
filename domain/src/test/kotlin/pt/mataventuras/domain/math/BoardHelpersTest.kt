package pt.mataventuras.domain.math

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.domain.engine.KartHud
import pt.mataventuras.domain.engine.OffroadRacerEngine
import pt.mataventuras.domain.engine.Platformer2dEngine
import pt.mataventuras.domain.engine.PlatformerWorld
import pt.mataventuras.domain.model.LearningModule

class BoardHelpersTest {
    @Test
    fun soupPathsFallBackAndRejectBadIndices() {
        val empty = PlayBoard()
        assertTrue(empty.soupPaths().isEmpty())
        assertTrue(empty.soupHits().isEmpty())
        val single = PlayBoard(targetIndices = listOf(1), cells = listOf("a", "b"), columns = 2)
        assertEquals(listOf(listOf(1)), single.soupPaths())
        val words =
            PlayBoard(
                wordPaths = listOf(listOf(0, 1), listOf(2, 3)),
                cells = listOf("a", "b", "c", "d"),
                columns = 2,
            )
        assertEquals(2, words.soupPaths().size)
        var failed = false
        try {
            Exercise(
                module = LearningModule.LOGIC,
                prompt = "?",
                spoken = "?",
                options = listOf("a", "b"),
                correctIndex = 0,
                play = PlayBoard(wordPaths = listOf(listOf(9))),
            )
        } catch (_: IllegalArgumentException) {
            failed = true
        }
        assertTrue(failed)
        val ok =
            Exercise(
                module = LearningModule.LOGIC,
                prompt = "?",
                spoken = "?",
                options = listOf("a", "b", "c"),
                correctIndex = 0,
                play = PlayBoard(wordPaths = listOf(listOf(0, 1)), targetIndices = listOf(0, 1)),
            )
        assertTrue(ok.isCorrect(1))
        assertFalse(ok.isCorrect(2))
    }

    @Test
    fun cipherSpeechNamesUnknownSymbolsAndShortLegend() {
        assertEquals("Símbolo vale 9. Quanto é?", CipherSpeech.fromLegend(listOf("Z=9"), "Quanto é?"))
        assertTrue(CipherSpeech.fromLegend(listOf("Q"), "Fim").contains("Símbolo vale ?"))
        assertTrue(CipherSpeech.counting(2).contains("Círculo"))
        assertFalse(CipherSpeech.counting(2).contains("Quadrado"))
        assertTrue(SudokuGrids.isLatin(SudokuGrids.filled(2, Random(0)), 2))
        assertTrue(SudokuGrids.isConsistent(SudokuGrids.filled(3, Random(1)), 3))
        assertFalse(SudokuGrids.isLatin(emptyList(), 2))
        val tiny = WordSoupBuilder(Random(0)).build(2, 3)
        assertTrue(WordSoupScanner.isUnique(tiny))
        assertEquals(4, tiny.cells.size)
        val none = WordSoupBuilder(Random(1)).build(1, 2)
        assertTrue(none.cells.size == 1)
        val shapes = PuzzlePatterns.cells(LearningModule.SHAPES, 2, Random(4))
        assertEquals(4, shapes.size)
    }

    @Test
    fun platformerAndHudEdgeBranches() {
        assertEquals(0f, PlatformerWorld.DEFAULT.lastSafeX(0f))
        assertFalse(PlatformerWorld.DEFAULT.inPit(0f))
        val engine = Platformer2dEngine()
        var falling = engine.initial().copy(x = 30f, y = -0.2f, onGround = false, inPitFall = true)
        falling = engine.step(falling, 0.05f, jumping = false, moveX = 1f)
        assertTrue(falling.inPitFall || falling.y < 0f)
        val idle = OffroadRacerEngine().initial()
        assertEquals(null, KartHud.offTrackLabel(idle.copy(offTrack = true, finished = true)))
        assertEquals("Volta à pista!", KartHud.offTrackLabel(idle.copy(offTrack = true, finished = false)))
        val kart = pt.mataventuras.domain.engine.Kart3dEngine().initial()
        assertEquals(null, KartHud.offTrackLabel(kart.copy(offTrack = true, finished = true)))
        assertEquals("Volta à pista!", KartHud.offTrackLabel(kart.copy(offTrack = true, finished = false)))
        val walk = engine.step(engine.initial(), 0.05f, jumping = false, moveX = 1f)
        assertTrue(walk.onGround)
        assertTrue(walk.x > 0f)
        var air = engine.step(engine.initial(), 0.05f, jumping = true, moveX = 1f)
        air = engine.step(air, 0.02f, jumping = false, moveX = 1f)
        assertFalse(air.onGround)
        (0..6).forEach { WordSoupBuilder(Random(it)).build(3, 8) }
        assertTrue(SudokuGrids.isConsistent(SudokuGrids.filled(4, Random(8)), 4))
        assertTrue(SudokuGrids.isConsistent(SudokuGrids.filled(6, Random(9)), 6))
    }
}
