package pt.mataventuras.domain.math

import kotlin.random.Random
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.model.GeometricShape
import pt.mataventuras.domain.model.LearningModule

/**
 * Builds exercises. Prompt text is pt-PT UI copy. [random] is injected for tests.
 */
open class ExerciseGenerator(
    private val random: Random = Random.Default,
) {
    private val boards: PlayBoardFactory = PlayBoardFactory(random, ::numericOptions)
    private val games: ArithmeticGames = ArithmeticGames(random, ::numericOptions)

    /**
     * Next exercise for [module], mixing classic buttons with boards.
     * [level] 0..3 grows interactive boards as the child strings hits together.
     */
    open fun generate(
        module: LearningModule,
        level: Int = 0,
        age: AgeBand = AgeBand.SIX_TO_EIGHT,
    ): Exercise {
        val kind = PlayKinds.pick(module, random)
        if (kind != PlayKind.CHOICE) return boards.make(kind, module, level, age)
        return choice(module, age)
    }

    internal fun choice(
        module: LearningModule,
        age: AgeBand = AgeBand.SIX_TO_EIGHT,
    ): Exercise =
        when (module) {
            LearningModule.COUNTING -> counting()
            LearningModule.SHAPES -> shape()
            LearningModule.NUMBERS -> number(age)
            LearningModule.ADDITION -> addition(age)
            LearningModule.SUBTRACTION -> subtraction()
            LearningModule.MULTIPLICATION -> multiplication()
            LearningModule.DIVISION -> division()
            LearningModule.LOGIC -> logic(age)
            else -> boardChoice(module)
        }

    private fun boardChoice(module: LearningModule): Exercise =
        when (module) {
            LearningModule.ODD_ONE -> OddOneBoard(random).make()
            LearningModule.SIZE -> SizeBoard(random).make()
            LearningModule.PATTERNS -> PatternBoard(random).make()
            LearningModule.CLOCK -> ClockBoard(random).make()
            LearningModule.MONEY -> MoneyBoard(random).make()
            else ->
                if (isOlderBoard(module)) {
                    olderRound(module, random)
                } else {
                    youngRound(module, random)
                }
        }

    internal fun counting(): Exercise {
        val quantity = random.nextInt(1, 11)
        return numericChoice(
            module = LearningModule.COUNTING,
            prompt = "Quantas estrelas vês?",
            spoken = "Conta as estrelas. Quantas são?",
            options = numericOptions(quantity, 1, 10),
            correct = quantity,
            visualCount = quantity,
        )
    }

    internal fun shape(): Exercise {
        val target = GeometricShape.entries[random.nextInt(GeometricShape.entries.size)]
        val others =
            GeometricShape.entries
                .filter { it != target }
                .shuffled(random)
                .take(3)
        val options = (others + target).map { it.displayName }.shuffled(random)
        return Exercise(
            module = LearningModule.SHAPES,
            prompt = "Toca no ${target.displayName}.",
            spoken = "Procura o ${target.displayName}. Toca-lhe.",
            options = options,
            correctIndex = options.indexOf(target.displayName),
            targetShape = target,
        )
    }

    internal fun number(age: AgeBand = AgeBand.SIX_TO_EIGHT): Exercise {
        val band = BandDifficulty.forAge(age)
        val value = random.nextInt(band.numberLower, band.numberUpper + 1)
        return numericChoice(
            module = LearningModule.NUMBERS,
            prompt = "Qual é o número $value?",
            spoken = "Encontra o número $value.",
            options = numericOptions(value, band.numberLower, band.numberUpper),
            correct = value,
            visualCount = if (value <= 9) value else 0,
        )
    }

    internal fun addition(age: AgeBand = AgeBand.SIX_TO_EIGHT): Exercise =
        if (random.nextBoolean()) additionSum(age) else missingAddend(age)

    internal fun additionSum(age: AgeBand = AgeBand.SIX_TO_EIGHT): Exercise {
        val band = BandDifficulty.forAge(age)
        if (band.additionDots) return dottedSum(random, band, ::numericOptions)
        val a = random.nextInt(band.additionLow, band.additionHigh)
        val b = random.nextInt(band.additionLow, band.additionHigh)
        return numericChoice(
            module = LearningModule.ADDITION,
            prompt = "$a + $b = ?",
            spoken = "Quanto é $a mais $b?",
            options = numericOptions(a + b, band.additionLow * 2, (band.additionHigh - 1) * 2),
            correct = a + b,
        )
    }

    internal fun missingAddend(age: AgeBand = AgeBand.SIX_TO_EIGHT): Exercise {
        val band = BandDifficulty.forAge(age)
        if (band.additionDots) return dottedGap(random, band, ::numericOptions)
        val a = random.nextInt(11, 40)
        val b = random.nextInt(11, 40)
        val sum = a + b
        return numericChoice(
            module = LearningModule.ADDITION,
            prompt = "$a + ? = $sum",
            spoken = "Quanto falta a $a para $sum?",
            options = numericOptions(b, 8, 42),
            correct = b,
        )
    }

    internal fun subtraction(): Exercise = games.subtraction()

    internal fun multiplication(): Exercise = games.multiplication()

    /**
     * Exact division game: share, groups, quotient, or frog jumps.
     */
    internal fun division(): Exercise = games.division()

    internal fun logic(age: AgeBand = AgeBand.SIX_TO_EIGHT): Exercise =
        when (random.nextInt(3)) {
            0 -> skipSequence(age)
            1 -> extrema(largest = true, age = age)
            else -> extrema(largest = false, age = age)
        }

    private fun skipSequence(age: AgeBand): Exercise {
        val band = BandDifficulty.forAge(age)
        val step = band.logicSteps[random.nextInt(band.logicSteps.size)]
        val start = if (band.additionDots) random.nextInt(1, 6) else random.nextInt(3, 18)
        val n1 = start
        val n2 = start + step
        val n3 = start + step * 2
        val next = start + step * 3
        return numericChoice(
            module = LearningModule.LOGIC,
            prompt = "Completa: $n1, $n2, $n3, …",
            spoken = "Que número vem a seguir na sequência $n1, $n2, $n3?",
            options = numericOptions(next, (next - 8).coerceAtLeast(0), next + 12),
            correct = next,
        )
    }

    private fun extrema(
        largest: Boolean,
        age: AgeBand,
    ): Exercise {
        val band = BandDifficulty.forAge(age)
        val values = mutableSetOf<Int>()
        while (values.size < 4) {
            values += random.nextInt(band.extremaLower, band.extremaUpper)
        }
        val list = values.toList()
        val target = if (largest) list.max() else list.min()
        val prompt = if (largest) "Qual é o maior número?" else "Qual é o menor número?"
        val spoken = if (largest) "Toca no maior número." else "Toca no menor número."
        return Exercise(
            module = LearningModule.LOGIC,
            prompt = prompt,
            spoken = spoken,
            options = list.map { it.toString() },
            correctIndex = list.indexOf(target),
        )
    }

    internal fun numericOptions(
        correct: Int,
        min: Int,
        max: Int,
    ): List<Int> {
        val set = linkedSetOf(correct)
        var attempts = 0
        while (set.size < 4 && attempts < 40) {
            attempts += 1
            set += random.nextInt(min, max + 1)
        }
        var extra = min
        while (set.size < 4) {
            if (extra != correct) set += extra
            extra += 1
        }
        return set.toList().shuffled(random)
    }
}

private fun dottedSum(
    random: Random,
    band: BandDifficulty,
    numericOptions: (Int, Int, Int) -> List<Int>,
): Exercise {
    val a = random.nextInt(0, band.additionHigh)
    val room = band.sumLimit - a
    var b = if (room <= 0) 0 else random.nextInt(0, room + 1)
    if (a + b == 0) b = 1
    val sum = a + b
    return numericChoice(
        module = LearningModule.ADDITION,
        prompt = "$a + $b = ?",
        spoken = "Quanto é $a mais $b?",
        options = numericOptions(sum, 0, band.sumLimit),
        correct = sum,
        visualCount = sum,
    )
}

private fun dottedGap(
    random: Random,
    band: BandDifficulty,
    numericOptions: (Int, Int, Int) -> List<Int>,
): Exercise {
    val sum = random.nextInt(1, band.sumLimit + 1)
    val a = random.nextInt(0, sum + 1)
    val b = sum - a
    return numericChoice(
        module = LearningModule.ADDITION,
        prompt = "$a + ? = $sum",
        spoken = "Quanto falta a $a para $sum?",
        options = numericOptions(b, 0, band.sumLimit),
        correct = b,
        visualCount = sum,
    )
}

internal fun numericChoice(
    module: LearningModule,
    prompt: String,
    spoken: String,
    options: List<Int>,
    correct: Int,
    visualCount: Int = 0,
): Exercise =
    Exercise(
        module = module,
        prompt = prompt,
        spoken = spoken,
        options = options.map { it.toString() },
        correctIndex = options.indexOf(correct),
        visualCount = visualCount,
    )

private fun isOlderBoard(module: LearningModule): Boolean =
    module == LearningModule.FRACTION ||
        module == LearningModule.ORDER ||
        module == LearningModule.WORD_PROBLEM ||
        module == LearningModule.TANGRAM ||
        module == LearningModule.SPELL

internal fun olderRound(
    module: LearningModule,
    random: Random,
): Exercise =
    when (module) {
        LearningModule.FRACTION -> FractionBoard(random).make()
        LearningModule.ORDER -> OrderBoard(random).make()
        LearningModule.WORD_PROBLEM -> WordProblemBoard(random).make()
        LearningModule.TANGRAM -> TangramBoard(random).make()
        LearningModule.SPELL -> SpellBoard(random).make()
        else -> error("Board lessons are not choice questions.")
    }

private fun youngRound(
    module: LearningModule,
    random: Random,
): Exercise =
    when (module) {
        LearningModule.SORT -> SortBoard(random).make()
        LearningModule.DOTS -> DotsBoard(random).make()
        LearningModule.CLAPS -> ClapBoard(random).make()
        LearningModule.SHADOW -> ShadowBoard(random).make()
        else -> error("Board lessons are not choice questions.")
    }
