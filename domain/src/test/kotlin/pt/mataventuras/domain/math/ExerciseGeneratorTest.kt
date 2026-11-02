package pt.mataventuras.domain.math

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.model.LearningModule

class ExerciseGeneratorTest {
    private val generator = ExerciseGenerator(Random(42))

    @Test
    fun generateCoversEveryModule() {
        LearningModule.entries.forEach { module ->
            val exercise = generator.generate(module)
            assertEquals(module, exercise.module)
            assertTrue(exercise.options.size >= 2)
            assertTrue(exercise.isCorrect(exercise.correctIndex))
            if (exercise.play.targetIndices.isEmpty()) {
                val wrong = (exercise.correctIndex + 1) % exercise.options.size
                assertFalse(exercise.isCorrect(wrong))
            }
        }
    }

    @Test
    fun countingHasVisibleStars() {
        val exercise = generator.counting()
        assertTrue(exercise.visualCount in 1..10)
        assertEquals(exercise.visualCount.toString(), exercise.options[exercise.correctIndex])
        assertTrue(exercise.prompt.contains("estrelas"))
        assertFalse(exercise.isCorrect((exercise.correctIndex + 1) % exercise.options.size))
    }

    @Test
    fun additionUsesSumOrGap() {
        val usesGap = (0..8).map { ExerciseGenerator(Random(it)).addition().prompt.contains("+ ?") }.toSet()
        assertEquals(2, usesGap.size)
    }

    @Test
    fun additionAnswerMatchesTheSum() {
        val exercise = generator.additionSum()
        val parts = exercise.prompt.replace(" = ?", "").split(" + ")
        val sum = parts[0].toInt() + parts[1].toInt()
        assertEquals(sum.toString(), exercise.options[exercise.correctIndex])
        assertTrue(exercise.spoken.contains("mais"))
        assertTrue(parts[0].toInt() >= 12)
        assertTrue(parts[1].toInt() >= 12)
    }

    @Test
    fun missingAddendAsksHowMuchIsLeft() {
        val exercise = generator.missingAddend()
        assertTrue(exercise.prompt.contains("?"))
        assertTrue(exercise.spoken.contains("falta"))
        assertEquals(4, exercise.options.size)
    }

    @Test
    fun subtractionNeverGoesNegative() {
        repeat(20) {
            val exercise = ExerciseGenerator(Random(it)).subtraction()
            assertEquals(LearningModule.SUBTRACTION, exercise.module)
            assertTrue(exercise.options[exercise.correctIndex].toInt() >= 0)
            assertEquals(4, exercise.options.size)
        }
    }

    @Test
    fun divisionIsExactAndNonZero() {
        repeat(20) {
            val exercise = ExerciseGenerator(Random(it)).division()
            val value = exercise.options[exercise.correctIndex].toInt()
            assertEquals(LearningModule.DIVISION, exercise.module)
            assertTrue(value in 2..12)
            assertEquals(4, exercise.options.size)
        }
    }

    @Test
    fun logicCoversSequenceLargestAndSmallest() {
        val prompts = (0..12).map { ExerciseGenerator(Random(it)).logic().prompt }
        assertTrue(prompts.any { it.contains("Completa") })
        assertTrue(prompts.any { it.contains("maior") })
        assertTrue(prompts.any { it.contains("menor") })
    }

    @Test(expected = IllegalArgumentException::class)
    fun exerciseWithoutOptionsFails() {
        Exercise(LearningModule.NUMBERS, "?", "?", emptyList(), 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun outOfRangeIndexFails() {
        Exercise(LearningModule.NUMBERS, "?", "?", listOf("1"), 4)
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeIndexFails() {
        Exercise(LearningModule.NUMBERS, "?", "?", listOf("1", "2"), -1)
    }

    @Test
    fun shapeHasAPortugueseTargetName() {
        val exercise = generator.shape()
        assertTrue(exercise.targetShape != null)
        assertEquals(exercise.targetShape!!.displayName, exercise.options[exercise.correctIndex])
        assertTrue(exercise.prompt.contains("Toca"))
    }

    @Test
    fun numberIsZeroToNine() {
        val exercise = generator.number(AgeBand.THREE_TO_FIVE)
        val n = exercise.options[exercise.correctIndex].toInt()
        assertTrue(n in 0..9)
        assertTrue(exercise.prompt.contains("número"))
    }

    @Test
    fun youngAdditionStaysWithinTenAndShowsDots() {
        repeat(30) {
            val exercise = ExerciseGenerator(Random(it)).addition(AgeBand.THREE_TO_FIVE)
            assertTrue(exercise.visualCount in 1..10)
            val numbers = Regex("\\d+").findAll(exercise.prompt).map { it.value.toInt() }.toList()
            assertTrue(numbers.all { it in 0..10 })
        }
    }

    @Test
    fun youngLogicUsesStepsOfOneOrTwo() {
        val prompts = (0..40).map { ExerciseGenerator(Random(it)).logic(AgeBand.THREE_TO_FIVE).prompt }
        val sequences = prompts.filter { it.startsWith("Completa") }
        assertTrue(sequences.isNotEmpty())
        sequences.forEach { prompt ->
            val numbers = Regex("\\d+").findAll(prompt).map { it.value.toInt() }.toList()
            val step = numbers[1] - numbers[0]
            assertTrue(step == 1 || step == 2)
        }
    }

    @Test
    fun olderNumbersReachPastNine() {
        val values = (0..40).map { ExerciseGenerator(Random(it)).number(AgeBand.SIX_TO_EIGHT) }
        assertTrue(values.any { it.options[it.correctIndex].toInt() > 9 })
        assertTrue(values.all { it.options[it.correctIndex].toInt() in 0..100 })
    }

    @Test
    fun multiplicationCoversProductAndMissingFactor() {
        val prompts = (0..40).map { ExerciseGenerator(Random(it)).multiplication().prompt }
        assertTrue(prompts.any { it.contains("× ?") })
        assertTrue(prompts.any { it.contains(" × ") && it.endsWith("= ?") })
        assertTrue(prompts.any { it.contains("maçãs") })
        assertTrue(prompts.any { it.contains("sapo") })
    }

    @Test
    fun padOptionsFillsAShortList() {
        val numbers = padOptions(emptyList(), "4", numeric = true)
        assertEquals(listOf("5", "6", "7"), numbers)
        val words = padOptions(listOf("peça 0"), "peça", numeric = false)
        assertEquals(3, words.size)
        assertTrue("peça" !in words || words.count { it == "peça" } <= 1)
        assertEquals(listOf("a", "b", "c"), padOptions(listOf("a", "b", "c", "d"), "a", numeric = false))
    }

    @Test
    fun numericOptionsFillsADegenerateRange() {
        val options = ExerciseGenerator(Random(0)).numericOptions(5, 5, 5)
        assertEquals(4, options.size)
        assertTrue(5 in options)
    }
}
