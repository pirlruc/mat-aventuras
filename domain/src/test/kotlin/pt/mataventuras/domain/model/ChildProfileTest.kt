package pt.mataventuras.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChildProfileTest {
    @Test
    fun threeYearOldTokensAreLarger() {
        val three = tokensFor(AgeBand.THREE_TO_FIVE)
        val seven = tokensFor(AgeBand.SIX_TO_EIGHT)
        assertTrue(three.minButtonDp > seven.minButtonDp)
        assertFalse(three.usesTextNavigation)
        assertTrue(seven.usesTextNavigation)
        assertTrue(seven.confirmsBeforeExit)
    }

    @Test
    fun unknownMascotCodeFallsBackToTheHedgehog() {
        assertEquals(Mascot.SPEEDY_HEDGEHOG, Mascot.fromCode("unknown"))
        assertEquals(Mascot.PINK_PIGLET, Mascot.fromCode("pink_piglet"))
        assertEquals("Ouriço Veloz", Mascot.SPEEDY_HEDGEHOG.displayName)
    }

    @Test
    fun mascotPerModule() {
        assertEquals(Mascot.SPEEDY_HEDGEHOG, mascotFor(LearningModule.COUNTING))
        assertEquals(Mascot.PINK_PIGLET, mascotFor(LearningModule.SHAPES))
        assertEquals(Mascot.HERO_PUP, mascotFor(LearningModule.NUMBERS))
        assertEquals(Mascot.BRAVE_PLUMBER, mascotFor(LearningModule.ADDITION))
        assertEquals(Mascot.BRAVE_PLUMBER, mascotFor(LearningModule.SUBTRACTION))
        assertEquals(Mascot.MISCHIEVOUS_ALIEN, mascotFor(LearningModule.MULTIPLICATION))
        assertEquals(Mascot.MISCHIEVOUS_ALIEN, mascotFor(LearningModule.DIVISION))
        assertEquals(Mascot.HERO_PUP, mascotFor(LearningModule.LOGIC))
    }

    @Test
    fun modulesPerAgeBand() {
        assertEquals(5, modulesFor(AgeBand.THREE_TO_FIVE).size)
        assertEquals(6, modulesFor(AgeBand.SIX_TO_EIGHT).size)
        assertTrue(LearningModule.ADDITION.isSevenYears())
        assertTrue(LearningModule.DIVISION.isSevenYears())
        assertFalse(LearningModule.COUNTING.isSevenYears())
        assertTrue(LearningModule.COUNTING in modulesFor(AgeBand.THREE_TO_FIVE))
        assertTrue(LearningModule.MULTIPLICATION in modulesFor(AgeBand.SIX_TO_EIGHT))
        assertTrue(LearningModule.DIVISION in modulesFor(AgeBand.SIX_TO_EIGHT))
    }

    @Test
    fun engineKindPerAgeBand() {
        assertEquals(EngineKind.TWO_D, engineKindFor(AgeBand.THREE_TO_FIVE))
        assertEquals(EngineKind.THREE_D, engineKindFor(AgeBand.SIX_TO_EIGHT))
        assertEquals(EngineKind.TWO_D, pickRewardKind(AgeBand.THREE_TO_FIVE))
        assertEquals(EngineKind.TWO_D, pickRewardKind(AgeBand.THREE_TO_FIVE, kotlin.random.Random(0)))
        assertEquals(EngineKind.TWO_D, pickRewardKind(AgeBand.THREE_TO_FIVE, kotlin.random.Random(1)))
        val seven =
            (0..40).map { pickRewardKind(AgeBand.SIX_TO_EIGHT, kotlin.random.Random(it)) }.toSet()
        assertTrue(EngineKind.TWO_D in seven)
        assertTrue(EngineKind.THREE_D in seven)
    }

    @Test
    fun sessionAccuracy() {
        val empty = LearningSession(1, 1, LearningModule.ADDITION, 0, 0, 0, 0)
        assertEquals(0.0, empty.accuracy(), 0.0)
        val mixed = empty.copy(hits = 3, misses = 1)
        assertEquals(0.75, mixed.accuracy(), 0.0)
    }

    @Test
    fun shapeDisplayNamesAreEuropeanPortuguese() {
        assertEquals("rectângulo", GeometricShape.RECTANGLE.displayName)
        assertEquals("triângulo", GeometricShape.TRIANGLE.displayName)
        assertEquals("círculo", GeometricShape.CIRCLE.displayName)
    }
}
