package pt.mataventuras.domain.engine

/**
 * Shared prize rules. GDScript reads the same `prize-tuning.json` asset.
 */
internal object PrizeTuning {
    val rules: Rules = Rules.from(parsePrizeJson(readResource()))

    private fun readResource(): String {
        val stream =
            PrizeTuning::class.java.getResourceAsStream("/prize-tuning.json")
                ?: error("prize-tuning.json missing")
        return stream.bufferedReader().use { it.readText() }
    }
}

/** One number block from [parsePrizeJson]. */
internal data class InvadersTune(
    val lives: Int,
    val fleet: Int,
    val grace: Float,
    val hitInvuln: Float,
    val hitX: Float,
    val hitY: Float,
    val shipHitX: Float,
)

/** Maze lives and the grace window after a hit. */
internal data class ChompTune(
    val lives: Int,
    val grace: Float,
)

/** Climb lives and the barrel hit tests. */
internal data class ClimbTune(
    val lives: Int,
    val hitInvuln: Float,
    val near: Float,
    val barrelSpeed: Float,
    val barrelHitX: Float,
    val floorSnap: Float,
    val freeze: Float,
)

/** Climb jump, gravity, and walk speed. */
internal data class ClimbMotion(
    val jump: Float,
    val gravity: Float,
    val move: Float,
)

/**
 * Domain runner uses metres. Godot runner uses the pixel fields.
 * Both come from this file so neither script keeps its own copy.
 */
internal data class RunnerTune(
    val speedX: Float,
    val gravity: Float,
    val jumpSpeed: Float,
    val rings: Int,
    val pixelSpeed: Float,
    val pixelGravity: Float,
    val pixelJump: Float,
    val coins: Int,
    val freeze: Float,
)

/** Dirt-race speeds. Lap count is [laps]. */
internal data class KartTune(
    val cruise: Float,
    val boost: Float,
    val offRoad: Float,
    val accel: Float,
    val steer: Float,
    val drift: Float,
    val boostSeconds: Float,
    val laps: Int,
)

/** Circuit length and checkpoint window. */
internal data class CourseTune(
    val gates: Int,
    val length: Float,
    val gateWindow: Float,
)

/** All prize blocks. */
internal data class Rules(
    val invaders: InvadersTune,
    val chomp: ChompTune,
    val climb: ClimbTune,
    val climbMotion: ClimbMotion,
    val runner: RunnerTune,
    val kart: KartTune,
    val course: CourseTune,
) {
    companion object {
        /**
         * Builds the rules from a flat `block.key` map.
         */
        fun from(values: Map<String, Double>): Rules =
            Rules(
                invaders =
                    InvadersTune(
                        lives = values.i("invaders.lives"),
                        fleet = values.i("invaders.fleet"),
                        grace = values.f("invaders.grace"),
                        hitInvuln = values.f("invaders.hitInvuln"),
                        hitX = values.f("invaders.hitX"),
                        hitY = values.f("invaders.hitY"),
                        shipHitX = values.f("invaders.shipHitX"),
                    ),
                chomp = ChompTune(lives = values.i("chomp.lives"), grace = values.f("chomp.grace")),
                climb =
                    ClimbTune(
                        lives = values.i("climb.lives"),
                        hitInvuln = values.f("climb.hitInvuln"),
                        near = values.f("climb.near"),
                        barrelSpeed = values.f("climb.barrelSpeed"),
                        barrelHitX = values.f("climb.barrelHitX"),
                        floorSnap = values.f("climb.floorSnap"),
                        freeze = values.f("climb.freeze"),
                    ),
                climbMotion =
                    ClimbMotion(
                        jump = values.f("climbMotion.jump"),
                        gravity = values.f("climbMotion.gravity"),
                        move = values.f("climbMotion.move"),
                    ),
                runner =
                    RunnerTune(
                        speedX = values.f("runner.speedX"),
                        gravity = values.f("runner.gravity"),
                        jumpSpeed = values.f("runner.jumpSpeed"),
                        rings = values.i("runner.rings"),
                        pixelSpeed = values.f("runner.pixelSpeed"),
                        pixelGravity = values.f("runner.pixelGravity"),
                        pixelJump = values.f("runner.pixelJump"),
                        coins = values.i("runner.coins"),
                        freeze = values.f("runner.freeze"),
                    ),
                kart =
                    KartTune(
                        cruise = values.f("kart.cruise"),
                        boost = values.f("kart.boost"),
                        offRoad = values.f("kart.offRoad"),
                        accel = values.f("kart.accel"),
                        steer = values.f("kart.steer"),
                        drift = values.f("kart.drift"),
                        boostSeconds = values.f("kart.boostSeconds"),
                        laps = values.i("kart.laps"),
                    ),
                course =
                    CourseTune(
                        gates = values.i("course.gates"),
                        length = values.f("course.length"),
                        gateWindow = values.f("course.gateWindow"),
                    ),
            )
    }
}

private fun Map<String, Double>.f(path: String): Float = req(path).toFloat()

private fun Map<String, Double>.i(path: String): Int = req(path).toInt()

private fun Map<String, Double>.req(path: String): Double = this[path] ?: error("prize-tuning missing $path")

/**
 * Nested objects of numbers, flattened to `block.key`.
 * The asset has no arrays and no string values.
 */
internal fun parsePrizeJson(text: String): Map<String, Double> = PrizeJson(text).parse()

private class PrizeJson(
    private val text: String,
) {
    private var index: Int = 0
    private val out: LinkedHashMap<String, Double> = LinkedHashMap()

    fun parse(): Map<String, Double> {
        readObject("")
        return out
    }

    private fun readObject(prefix: String) {
        skip()
        expect('{')
        skip()
        if (peek('}')) {
            index += 1
            return
        }
        var more = true
        while (more) {
            val key = readKey()
            skip()
            expect(':')
            skip()
            val path = if (prefix.isEmpty()) key else "$prefix.$key"
            if (peek('{')) {
                readObject(path)
            } else {
                out[path] = readNumber()
            }
            skip()
            more =
                if (peek(',')) {
                    index += 1
                    true
                } else {
                    false
                }
        }
        skip()
        expect('}')
    }

    private fun readKey(): String {
        skip()
        expect('"')
        val start = index
        while (index < text.length && text[index] != '"') index += 1
        val key = text.substring(start, index)
        expect('"')
        return key
    }

    private fun readNumber(): Double {
        val start = index
        if (peek('-')) index += 1
        while (index < text.length && (text[index].isDigit() || text[index] == '.')) index += 1
        return text.substring(start, index).toDouble()
    }

    private fun skip() {
        while (index < text.length && text[index].isWhitespace()) index += 1
    }

    private fun peek(char: Char): Boolean = index < text.length && text[index] == char

    private fun expect(char: Char) {
        if (!peek(char)) error("prize-tuning expected '$char'")
        index += 1
    }
}
