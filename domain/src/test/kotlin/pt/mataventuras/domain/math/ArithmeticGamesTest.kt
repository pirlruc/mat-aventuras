package pt.mataventuras.domain.math

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArithmeticGamesTest {
    @Test
    fun differenceIsTwoDigitTakeAway() {
        val exercise = games(4).difference()
        val parts = exercise.prompt.replace(" = ?", "").split(" − ")
        val value = answer(exercise)
        assertEquals(parts[0].toInt() - parts[1].toInt(), value)
        assertTrue(parts[0].toInt() >= 30)
        assertTrue(exercise.spoken.contains("menos"))
        assertTrue(value >= 0)
    }

    @Test
    fun chestAsksHowManyCoinsLeftTheGame() {
        val exercise = games(5).chest()
        val nums = numbers(exercise.prompt)
        assertEquals(nums[0] - nums[1], answer(exercise))
        assertTrue(exercise.spoken.contains("baú"))
        assertTrue(answer(exercise) >= 8)
    }

    @Test
    fun compareAsksHowManyMore() {
        val exercise = games(6).compare()
        val nums = numbers(exercise.prompt)
        assertEquals(nums[0] - nums[1], answer(exercise))
        assertTrue(exercise.prompt.contains("cromos"))
        assertTrue(answer(exercise) >= 3)
    }

    @Test
    fun frogJumpsBackwardOnANumberLine() {
        val exercise = games(7).frogBack()
        val nums = numbers(exercise.prompt)
        assertEquals(nums[0] - nums[1] * nums[2], answer(exercise))
        assertEquals(nums[2], nums[3])
        assertTrue(answer(exercise) >= 2)
        assertTrue(exercise.spoken.contains("reta numérica"))
    }

    @Test
    fun productMatchesTheTimesTable() {
        val exercise = games(8).product()
        val parts = exercise.prompt.replace(" = ?", "").split(" × ")
        assertEquals(parts[0].toInt() * parts[1].toInt(), answer(exercise))
        assertTrue(parts[0].toInt() in 3..12)
        assertTrue(parts[1].toInt() in 3..12)
        assertTrue(exercise.spoken.contains("vezes"))
    }

    @Test
    fun missingFactorMatchesTheProduct() {
        val exercise = games(9).missingFactor()
        val factor = exercise.prompt.substringBefore(" ×").toInt()
        val product = exercise.prompt.substringAfter("= ").toInt()
        assertTrue(exercise.prompt.contains("× ?"))
        assertEquals(product, factor * answer(exercise))
        assertTrue(exercise.spoken.contains("dividir"))
    }

    @Test
    fun bagsAreEqualGroups() {
        val exercise = games(10).bags()
        val nums = numbers(exercise.prompt)
        assertEquals(nums[0] * nums[1], answer(exercise))
        assertTrue(exercise.spoken.contains("grupos iguais"))
    }

    @Test
    fun frogJumpsForwardFromZero() {
        val exercise = games(11).frogForward()
        val nums = numbers(exercise.prompt)
        assertEquals(nums[0] * nums[1], answer(exercise))
        assertTrue(exercise.prompt.contains("zero"))
    }

    @Test
    fun quotientDividesExactly() {
        val exercise = games(12).quotient()
        val parts = exercise.prompt.replace(" = ?", "").split(" ÷ ")
        assertEquals(0, parts[0].toInt() % parts[1].toInt())
        assertEquals(parts[0].toInt() / parts[1].toInt(), answer(exercise))
        assertTrue(exercise.spoken.contains("dividir"))
    }

    @Test
    fun fairShareSplitsBiscuits() {
        val exercise = games(13).fairShare()
        val nums = numbers(exercise.prompt)
        assertEquals(0, nums[0] % nums[1])
        assertEquals(nums[0] / nums[1], answer(exercise))
        assertTrue(exercise.prompt.contains("bolachas"))
    }

    @Test
    fun teamsCountEqualGroups() {
        val exercise = games(14).teams()
        val nums = numbers(exercise.prompt)
        assertEquals(0, nums[0] % nums[1])
        assertEquals(nums[0] / nums[1], answer(exercise))
        assertTrue(exercise.prompt.contains("berlindes"))
    }

    @Test
    fun frogCountsJumpsThatFit() {
        val exercise = games(15).frogFit()
        val nums = numbers(exercise.prompt)
        assertEquals(0, nums[0] % nums[1])
        assertEquals(nums[0] / nums[1], answer(exercise))
        assertTrue(exercise.spoken.contains("saltos iguais"))
    }

    @Test
    fun eachOperationMixesItsGames() {
        val subtraction = (0..48).map { games(it).subtraction().prompt }
        assertTrue(subtraction.any { it.contains("−") })
        assertTrue(subtraction.any { it.contains("baú") })
        assertTrue(subtraction.any { it.contains("cromos") })
        assertTrue(subtraction.any { it.contains("para trás") })
        val multiplication = (0..48).map { games(it + 50).multiplication().prompt }
        assertTrue(multiplication.any { it.contains("× ?") })
        assertTrue(multiplication.any { it.contains("maçãs") })
        assertTrue(multiplication.any { it.contains("parte do zero") })
        val division = (0..48).map { games(it + 90).division() }
        assertTrue(division.all { answer(it) in 2..12 })
        assertTrue(division.any { it.prompt.contains("÷") })
        assertTrue(division.any { it.prompt.contains("bolachas") })
        assertTrue(division.any { it.prompt.contains("berlindes") })
        assertTrue(division.any { it.prompt.contains("até ao") })
    }

    private fun games(seed: Int): ArithmeticGames {
        val options = ExerciseGenerator(Random(seed + 99))::numericOptions
        return ArithmeticGames(Random(seed), options)
    }

    private fun answer(exercise: Exercise): Int = exercise.options[exercise.correctIndex].toInt()

    private fun numbers(prompt: String): List<Int> = Regex("\\d+").findAll(prompt).map { it.value.toInt() }.toList()
}
