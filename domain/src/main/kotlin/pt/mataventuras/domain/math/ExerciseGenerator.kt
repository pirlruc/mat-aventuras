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
     */
    fun generate(module: LearningModule): Exercise {
        val kind = PlayKinds.pick(module, random)
        if (kind != PlayKind.CHOICE) return boards.make(kind, module)
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
        val options = numericOptions(quantity, 1, 10)
        return Exercise(
            module = LearningModule.COUNTING,
            prompt = "Quantas estrelas vês?",
            spoken = "Conta as estrelas. Quantas são?",
            options = options.map { it.toString() },
            correctIndex = options.indexOf(quantity),
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
        val options = numericOptions(value, 0, 9)
        return Exercise(
            module = LearningModule.NUMBERS,
            prompt = "Qual é o número $value?",
            spoken = "Encontra o número $value.",
            options = options.map { it.toString() },
            correctIndex = options.indexOf(value),
            visualCount = value,
        )
    }

    internal fun addition(): Exercise {
        return if (random.nextBoolean()) additionSum() else missingAddend()
    }

    internal fun additionSum(): Exercise {
        val a = random.nextInt(1, 10)
        val b = random.nextInt(1, 10)
        val sum = a + b
        val options = numericOptions(sum, 2, 18)
        return Exercise(
            module = LearningModule.ADDITION,
            prompt = "$a + $b = ?",
            spoken = "Quanto é $a mais $b?",
            options = options.map { it.toString() },
            correctIndex = options.indexOf(sum),
        )
    }

    internal fun missingAddend(): Exercise {
        val a = random.nextInt(1, 10)
        val b = random.nextInt(1, 10)
        val sum = a + b
        val options = numericOptions(b, 1, 10)
        return Exercise(
            module = LearningModule.ADDITION,
            prompt = "$a + ? = $sum",
            spoken = "Quanto falta a $a para $sum?",
            options = options.map { it.toString() },
            correctIndex = options.indexOf(b),
        )
    }

    internal fun subtraction(): Exercise {
        val a = random.nextInt(2, 13)
        val b = random.nextInt(1, a)
        val difference = a - b
        val options = numericOptions(difference, 0, 12)
        return Exercise(
            module = LearningModule.SUBTRACTION,
            prompt = "$a − $b = ?",
            spoken = "Quanto é $a menos $b?",
            options = options.map { it.toString() },
            correctIndex = options.indexOf(difference),
        )
    }

    internal fun multiplication(): Exercise {
        val a = random.nextInt(1, 11)
        val b = random.nextInt(1, 11)
        val product = a * b
        val options = numericOptions(product, 1, 100)
        return Exercise(
            module = LearningModule.MULTIPLICATION,
            prompt = "$a × $b = ?",
            spoken = "Quanto é $a vezes $b?",
            options = options.map { it.toString() },
            correctIndex = options.indexOf(product),
        )
    }

    internal fun logic(): Exercise =
        when (random.nextInt(3)) {
            0 -> evenSequence()
            1 -> largestNumber()
            else -> smallestNumber()
        }

    private fun evenSequence(): Exercise {
        val start = random.nextInt(1, 6)
        val step = 2
        val n1 = start
        val n2 = start + step
        val n3 = start + step * 2
        val next = start + step * 3
        val options = numericOptions(next, next - 4, next + 4)
        return Exercise(
            module = LearningModule.LOGIC,
            prompt = "Completa: $n1, $n2, $n3, …",
            spoken = "Que número vem a seguir na sequência $n1, $n2, $n3?",
            options = options.map { it.toString() },
            correctIndex = options.indexOf(next),
        )
    }

    private fun largestNumber(): Exercise {
        val values = mutableSetOf<Int>()
        while (values.size < 4) {
            values += random.nextInt(1, 50)
        }
        val list = values.toList()
        val largest = list.max()
        return Exercise(
            module = LearningModule.LOGIC,
            prompt = "Qual é o maior número?",
            spoken = "Toca no maior número.",
            options = list.map { it.toString() },
            correctIndex = list.indexOf(largest),
        )
    }

    private fun smallestNumber(): Exercise {
        val values = mutableSetOf<Int>()
        while (values.size < 4) {
            values += random.nextInt(1, 50)
        }
        val list = values.toList()
        val smallest = list.min()
        return Exercise(
            module = LearningModule.LOGIC,
            prompt = "Qual é o menor número?",
            spoken = "Toca no menor número.",
            options = list.map { it.toString() },
            correctIndex = list.indexOf(smallest),
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
