package pt.mataventuras.domain.math

import kotlin.random.Random
import pt.mataventuras.domain.model.LearningModule

/**
 * Story and number-line games for age-7 subtraction, multiplication, and division.
 * Prompts are pt-PT. Division is exact: the dividend is built from the factors.
 */
class ArithmeticGames(
    private val random: Random,
    internal val numericOptions: (Int, Int, Int) -> List<Int>,
) {
    /**
     * One subtraction game: difference, treasure chest, comparison, or frog jumps.
     */
    fun subtraction(): Exercise =
        when (random.nextInt(4)) {
            0 -> difference()
            1 -> chest()
            2 -> compare()
            else -> frogBack()
        }

    /**
     * Two-digit take-away.
     */
    internal fun difference(): Exercise {
        val a = random.nextInt(30, 90)
        val b = random.nextInt(11, a - 8)
        return ask(LearningModule.SUBTRACTION, "$a − $b = ?", "Quanto é $a menos $b?", a - b, 1, 80)
    }

    /**
     * Missing change from a coin chest.
     */
    internal fun chest(): Exercise {
        val start = random.nextInt(24, 61)
        val removed = random.nextInt(8, start - 5)
        val left = start - removed
        val prompt = "O baú tinha $start moedas e agora tem $left. Quantas saíram?"
        return ask(LearningModule.SUBTRACTION, prompt, "Quantas moedas saíram do baú?", removed, 1, 55)
    }

    /**
     * "How many more" comparison.
     */
    internal fun compare(): Exercise {
        val high = random.nextInt(20, 60)
        val low = random.nextInt(8, high - 2)
        val prompt = "A Ana tem $high cromos e o Rui tem $low. Quantos tem a Ana a mais?"
        val spoken = "Quantos cromos tem a Ana a mais do que o Rui?"
        return ask(LearningModule.SUBTRACTION, prompt, spoken, high - low, 1, 55)
    }

    /**
     * Number-line game: equal jumps backward.
     */
    internal fun frogBack(): Exercise {
        val step = random.nextInt(2, 8)
        val jumps = random.nextInt(2, 6)
        val landing = random.nextInt(2, 16)
        val start = landing + jumps * step
        val prompt = "O sapo está no $start e salta $jumps vezes, de $step em $step, para trás. Onde fica?"
        val spoken = "O sapo joga na reta numérica e salta para trás. Em que número fica?"
        return ask(LearningModule.SUBTRACTION, prompt, spoken, landing, 0, 40)
    }

    /**
     * One multiplication game: product, missing factor, equal bags, or frog jumps.
     */
    fun multiplication(): Exercise =
        when (random.nextInt(4)) {
            0 -> product()
            1 -> missingFactor()
            2 -> bags()
            else -> frogForward()
        }

    /**
     * Times-table product, factors 3 through 12.
     */
    internal fun product(): Exercise {
        val a = random.nextInt(3, 13)
        val b = random.nextInt(3, 13)
        return ask(LearningModule.MULTIPLICATION, "$a × $b = ?", "Quanto é $a vezes $b?", a * b, 6, 144)
    }

    /**
     * Missing factor, spoken as the matching division.
     */
    internal fun missingFactor(): Exercise {
        val a = random.nextInt(3, 13)
        val b = random.nextInt(3, 13)
        val prompt = "$a × ? = ${a * b}"
        val spoken = "Quanto é ${a * b} a dividir por $a?"
        return ask(LearningModule.MULTIPLICATION, prompt, spoken, b, 2, 13)
    }

    /**
     * Equal groups of apples in bags.
     */
    internal fun bags(): Exercise {
        val groups = random.nextInt(2, 10)
        val each = random.nextInt(2, 10)
        val prompt = "$groups sacos, com $each maçãs em cada saco. Quantas maçãs há?"
        val spoken = "São grupos iguais. Quantas maçãs há ao todo?"
        return ask(LearningModule.MULTIPLICATION, prompt, spoken, groups * each, 4, 100)
    }

    /**
     * Number-line game: equal jumps forward from zero.
     */
    internal fun frogForward(): Exercise {
        val jumps = random.nextInt(2, 10)
        val step = random.nextInt(2, 10)
        val prompt = "O sapo parte do zero e dá $jumps saltos de $step. Em que número fica?"
        val spoken = "Conta os saltos iguais do sapo. Onde é que ele fica?"
        return ask(LearningModule.MULTIPLICATION, prompt, spoken, jumps * step, 4, 100)
    }

    /**
     * One exact-division game: quotient, fair share, groups, or frog jumps.
     */
    fun division(): Exercise =
        when (random.nextInt(4)) {
            0 -> quotient()
            1 -> fairShare()
            2 -> teams()
            else -> frogFit()
        }

    /**
     * Exact quotient. Both factors stay inside 2..10.
     */
    internal fun quotient(): Exercise {
        val divisor = random.nextInt(2, 11)
        val result = random.nextInt(2, 11)
        val dividend = divisor * result
        val prompt = "$dividend ÷ $divisor = ?"
        val spoken = "Quanto é $dividend a dividir por $divisor?"
        return ask(LearningModule.DIVISION, prompt, spoken, result, 2, 12)
    }

    /**
     * Partitive division: share biscuits fairly.
     */
    internal fun fairShare(): Exercise {
        val friends = random.nextInt(2, 11)
        val each = random.nextInt(2, 11)
        val total = friends * each
        val prompt = "$total bolachas para $friends amigos. Quantas recebe cada um?"
        val spoken = "Reparte as bolachas em partes iguais. Quantas recebe cada amigo?"
        return ask(LearningModule.DIVISION, prompt, spoken, each, 2, 12)
    }

    /**
     * Quotative division: how many equal groups fit.
     */
    internal fun teams(): Exercise {
        val size = random.nextInt(2, 11)
        val count = random.nextInt(2, 11)
        val total = size * count
        val prompt = "Há $total berlindes em grupos de $size. Quantos grupos há?"
        return ask(LearningModule.DIVISION, prompt, "Quantos grupos de $size cabem em $total?", count, 2, 12)
    }

    /**
     * Number-line game: how many equal jumps reach the goal.
     */
    internal fun frogFit(): Exercise {
        val step = random.nextInt(2, 11)
        val jumps = random.nextInt(2, 11)
        val goal = step * jumps
        val prompt = "O sapo vai do zero até ao $goal, com saltos de $step. Quantos saltos dá?"
        return ask(LearningModule.DIVISION, prompt, "Quantos saltos iguais cabem até ao $goal?", jumps, 2, 12)
    }
}

private fun ArithmeticGames.ask(
    module: LearningModule,
    prompt: String,
    spoken: String,
    correct: Int,
    min: Int,
    max: Int,
): Exercise =
    numericChoice(
        module = module,
        prompt = prompt,
        spoken = spoken,
        options = numericOptions(correct, min, max),
        correct = correct,
    )
