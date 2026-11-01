package pt.mataventuras.domain.engine

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrizeTuningParityTest {
    @Test
    fun enginesUseTheSharedRules() {
        val rules = PrizeTuning.rules
        assertEquals(5, rules.invaders.lives)
        assertEquals(15, rules.invaders.fleet)
        assertEquals(5, InvadersEngine().initial().lives)
        assertEquals(InvadersEngine.FLEET, rules.invaders.fleet)
        assertEquals(3, ChompEngine().initial().lives)
        assertEquals(3, ClimbEngine().initial().lives)
        val kart = OffroadRacerEngine().initial()
        assertEquals(rules.kart.cruise, kart.speed)
        assertEquals(rules.kart.laps, kart.lapsTarget)
        assertEquals(rules.course.length, OffroadCircuit(1).length)
        assertEquals(rules.course.gates, OffroadCircuit(1).gateCount)
        assertEquals(rules.runner.rings, Platformer2dEngine().initial().ringsTarget)
        val walked = Platformer2dEngine().step(Platformer2dEngine().initial(), 0.05f, jumping = false, moveX = 1f)
        assertTrue(walked.x > 0f)
    }

    @Test
    fun missingKeyFailsClosed() {
        val failed = runCatching { Rules.from(emptyMap()) }
        assertTrue(failed.exceptionOrNull()?.message?.contains("prize-tuning missing") == true)
        assertEquals(0, parsePrizeJson("{}").size)
        assertTrue(runCatching { parsePrizeJson("{") }.isFailure)
        assertTrue(runCatching { parsePrizeJson("{\"a\":") }.isFailure)
    }

    @Test
    fun gdscriptReadsTheSameKeys() {
        val host = asset("host.gd")
        assertTrue(host.contains("res://prize-tuning.json"))
        assertTrue(host.contains("func tune("))
        assertTrue(host.contains("func add_boost("))
        assertTrue(host.contains("func lives_text("))
        listOf("invaders.gd", "chomp.gd", "climb.gd", "runner.gd", "kart.gd").forEach { name ->
            val script = asset(name)
            assertTrue(name, script.contains("Host.tune"))
            assertFalse(name, script.contains("const LIVES_MAX"))
            assertFalse(name, script.contains("const CRUISE"))
            assertFalse(name, script.contains("const SPEED"))
        }
        assertTrue(asset("kart.gd").contains("Host.add_boost"))
        assertTrue(asset("invaders.gd").contains("Host.lives_text"))
        val json = asset("prize-tuning.json")
        assertEquals(PrizeTuning.rules.invaders.lives, parsePrizeJson(json).i("invaders.lives"))
        assertEquals(PrizeTuning.rules.kart.cruise, parsePrizeJson(json).f("kart.cruise"))
        assertEquals(PrizeTuning.rules.runner.pixelSpeed, parsePrizeJson(json).f("runner.pixelSpeed"))
    }

    @Test
    fun youngHudIsHearts() {
        assertEquals("♥♥♥", PrizeHud.livesLine(young = true, current = 3, maximum = 3))
        assertEquals("·", PrizeHud.livesLine(young = true, current = 0, maximum = 3))
        assertEquals("Vidas 2/5", PrizeHud.livesLine(young = false, current = 2, maximum = 5))
        assertEquals("Ganhaste!", PrizeHud.WIN_TITLE)
        assertEquals("Continuar", PrizeHud.WIN_CONTINUE)
        assertEquals("Tentar outra vez", PrizeHud.RETRY_LABEL)
        assertEquals("Voltar", PrizeHud.BACK_LABEL)
        assertEquals("A preparar o prémio…", PrizeHud.LOADING_LINE)
        assertEquals("O prémio demorou.", PrizeHud.TIMEOUT_LINE)
    }

    private fun asset(name: String): String {
        val candidates =
            listOf(
                File("src/main/resources/$name"),
                File("app/src/main/assets/$name"),
                File("../app/src/main/assets/$name"),
            )
        val file = candidates.firstOrNull { it.isFile } ?: error("missing $name")
        return file.readText()
    }
}

private fun Map<String, Double>.f(path: String): Float = this[path]!!.toFloat()

private fun Map<String, Double>.i(path: String): Int = this[path]!!.toInt()
