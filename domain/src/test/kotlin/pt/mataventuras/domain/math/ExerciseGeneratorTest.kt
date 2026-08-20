package pt.mataventuras.domain.math

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.domain.model.LearningModule

class ExerciseGeneratorTest {
    private val generator = ExerciseGenerator(Random(42))

    @Test
    fun generateCoversEveryModule() {
        LearningModule.entries.forEach { module ->
            val exercise = generator.generate(module)
            assertEquals(module, exercise.module)
            assertEquals(4, exercise.options.size)
            assertTrue(exercise.isCorrect(exercise.correctIndex))
            assertFalse(exercise.isCorrect((exercise.correctIndex + 1) % 4))
        }
    }

    @Test
    fun countingHasVisibleStars() {
        val exercise = generator.counting()
        assertTrue(exercise.visualCount in 1..10)
        assertEquals(exercise.visualCount.toString(), exercise.options[exercise.correctIndex])
        assertTrue(exercise.prompt.contains("estrelas"))
    }

    @Test
    fun additionAnswerMatchesTheSum() {
        val exercise = generator.addition()
        val parts = exercise.prompt.replace(" = ?", "").split(" + ")
        val sum = parts[0].toInt() + parts[1].toInt()
        assertEquals(sum.toString(), exercise.options[exercise.correctIndex])
        assertTrue(exercise.spoken.contains("mais"))
    }

    @Test
    fun subtractionNeverGoesNegative() {
        repeat(20) {
            val exercise = ExerciseGenerator(Random(it)).subtraction()
            val value = exercise.options[exercise.correctIndex].toInt()
            assertTrue(value >= 0)
            assertTrue(exercise.spoken.contains("menos"))
        }
    }

    @Test
    fun logicIsASequenceOrALargestValue() {
        val a = ExerciseGenerator(Random(1)).logic()
        val b = ExerciseGenerator(Random(2)).logic()
        assertEquals(LearningModule.LOGIC, a.module)
        assertEquals(LearningModule.LOGIC, b.module)
        assertTrue(a.prompt.contains("Completa") || a.prompt.contains("maior"))
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
        val exercise = generator.number()
        val n = exercise.options[exercise.correctIndex].toInt()
        assertTrue(n in 0..9)
        assertTrue(exercise.prompt.contains("número"))
    }

    @Test
    fun multiplicationAnswerMatchesTheProduct() {
        val exercise = generator.multiplication()
        val parts = exercise.prompt.replace(" = ?", "").split(" × ")
        assertEquals(
            (parts[0].toInt() * parts[1].toInt()).toString(),
            exercise.options[exercise.correctIndex],
        )
        assertTrue(exercise.spoken.contains("vezes"))
    }

    @Test
    fun numericOptionsFillsADegenerateRange() {
        val options = ExerciseGenerator(Random(0)).numericOptions(5, 5, 5)
        assertEquals(4, options.size)
        assertTrue(5 in options)
    }
}
