package pt.mataventuras.domain.parent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.domain.model.LearningModule

class MasteryTest {
    @Test
    fun everyModuleNamesAnEssentialTopic() {
        LearningModule.entries.forEach { module ->
            assertTrue(essentialTopic(module).isNotBlank())
        }
        assertEquals("Números naturais", essentialTopic(LearningModule.COUNTING))
        assertEquals("Adição", essentialTopic(LearningModule.ADDITION))
        assertEquals("Figuras geométricas", essentialTopic(LearningModule.SHAPES))
        assertEquals("Consciência fonológica", essentialTopic(LearningModule.LETTERS))
        assertEquals("Resolução de problemas", essentialTopic(LearningModule.MEMORY))
    }

    @Test
    fun bandsSplitAtFiftyAndEighty() {
        assertEquals(MasteryBand.BELOW_50, masteryBand(0.0))
        assertEquals(MasteryBand.BELOW_50, masteryBand(0.49))
        assertEquals(MasteryBand.FROM_50_TO_79, masteryBand(0.5))
        assertEquals(MasteryBand.FROM_50_TO_79, masteryBand(0.79))
        assertEquals(MasteryBand.FROM_80, masteryBand(0.8))
        assertEquals(MasteryBand.FROM_80, masteryBand(1.0))
        assertEquals("Abaixo de 50", MasteryBand.BELOW_50.label)
        assertEquals("50 a 79", MasteryBand.FROM_50_TO_79.label)
        assertEquals("80 ou mais", MasteryBand.FROM_80.label)
    }

    @Test
    fun emptyHistoryHasNoCells() {
        val summary = ParentSummary(1, 0, 0, 0, 0.0, emptyList(), emptyList())
        assertTrue(masteryGrid(summary).isEmpty())
    }

    @Test
    fun gridUsesTheModuleAccuracy() {
        val summary =
            ParentSummary(
                profileId = 1,
                totalTimeMs = 10,
                hits = 1,
                misses = 3,
                accuracy = 0.25,
                byModule =
                    listOf(
                        ModulePerformance(LearningModule.ADDITION, 1, 3, 0.25, 10),
                    ),
                needsWork = emptyList(),
            )
        val cell = masteryGrid(summary).single()
        assertEquals(LearningModule.ADDITION, cell.module)
        assertEquals("Adição", cell.topic)
        assertEquals(MasteryBand.BELOW_50, cell.band)
        assertEquals(1, cell.hits)
        assertEquals(3, cell.misses)
    }
}
