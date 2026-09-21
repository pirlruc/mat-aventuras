package pt.mataventuras.domain.math

import kotlin.random.Random
import pt.mataventuras.domain.model.GeometricShape
import pt.mataventuras.domain.model.LearningModule

/**
 * Builds exercises. Prompt text is pt-PT UI copy. [random] is injected for tests.
 */
class ExerciseGenerator(
    private val random: Random = Random.Default,
) {
    private val boards: PlayBoardFactory = PlayBoardFactory(random, ::numericOptions)

    /**
     * Next exercise for [module], mixing classic buttons with boards.
     * [level] 0..3 grows interactive boards as the child strings hits together.
     */
    fun generate(
        module: LearningModule,
        level: Int = 0,
    ): Exercise {
        val kind = PlayKinds.pick(module, random)
        if (kind != PlayKind.CHOICE) return boards.make(kind, module, level)
        return when (module) {
            LearningModule.COUNTING -> counting()
            LearningModule.SHAPES -> shape()
            LearningModule.NUMBERS -> number()
            LearningModule.ADDITION -> addition()
            LearningModule.SUBTRACTION -> subtraction()
            LearningModule.MULTIPLICATION -> multiplication()
            LearningModule.LOGIC -> logic()
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
        val others = GeometricShape.entries.filter { it != target }.shuffled(random).take(3)
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

    internal fun number(): Exercise {
        val value = random.nextInt(0, 10)
        return numericChoice(
            module = LearningModule.NUMBERS,
            prompt = "Qual é o número $value?",
            spoken = "Encontra o número $value.",
            options = numericOptions(value, 0, 9),
            correct = value,
            visualCount = value,
        )
    }

    internal fun addition(): Exercise {
        return if (random.nextBoolean()) additionSum() else missingAddend()
    }

    internal fun additionSum(): Exercise {
        val a = random.nextInt(12, 48)
        val b = random.nextInt(12, 48)
        return numericChoice(
            module = LearningModule.ADDITION,
            prompt = "$a + $b = ?",
            spoken = "Quanto é $a mais $b?",
            options = numericOptions(a + b, 20, 96),
            correct = a + b,
        )
    }

    internal fun missingAddend(): Exercise {
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

    internal fun subtraction(): Exercise {
        val a = random.nextInt(30, 90)
        val b = random.nextInt(11, a - 8)
        val difference = a - b
        return numericChoice(
            module = LearningModule.SUBTRACTION,
            prompt = "$a − $b = ?",
            spoken = "Quanto é $a menos $b?",
            options = numericOptions(difference, 1, 80),
            correct = difference,
        )
    }

    internal fun multiplication(): Exercise {
        return if (random.nextBoolean()) multiplicationProduct() else missingFactor()
    }

    internal fun multiplicationProduct(): Exercise {
        val a = random.nextInt(3, 13)
        val b = random.nextInt(3, 13)
        val product = a * b
        return numericChoice(
            module = LearningModule.MULTIPLICATION,
            prompt = "$a × $b = ?",
            spoken = "Quanto é $a vezes $b?",
            options = numericOptions(product, 6, 144),
            correct = product,
        )
    }

    internal fun missingFactor(): Exercise {
        val a = random.nextInt(3, 13)
        val b = random.nextInt(3, 13)
        val product = a * b
        return numericChoice(
            module = LearningModule.MULTIPLICATION,
            prompt = "$a × ? = $product",
            spoken = "Quanto é $product a dividir por $a?",
            options = numericOptions(b, 2, 13),
            correct = b,
        )
    }

    internal fun logic(): Exercise =
        when (random.nextInt(3)) {
            0 -> skipSequence()
            1 -> extrema(largest = true)
            else -> extrema(largest = false)
        }

    private fun skipSequence(): Exercise {
        val start = random.nextInt(3, 18)
        val step = listOf(3, 4, 5, 6, 10)[random.nextInt(5)]
        val n1 = start
        val n2 = start + step
        val n3 = start + step * 2
        val next = start + step * 3
        return numericChoice(
            module = LearningModule.LOGIC,
            prompt = "Completa: $n1, $n2, $n3, …",
            spoken = "Que número vem a seguir na sequência $n1, $n2, $n3?",
            options = numericOptions(next, next - 8, next + 12),
            correct = next,
        )
    }

    private fun extrema(largest: Boolean): Exercise {
        val values = mutableSetOf<Int>()
        while (values.size < 4) {
            values += random.nextInt(12, 180)
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

private fun numericChoice(
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
