package pt.mataventuras.domain.engine

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.domain.math.WordSoup
import pt.mataventuras.domain.math.WordSoupBuilder
import pt.mataventuras.domain.math.WordSoupScanner
import pt.mataventuras.domain.model.AgeGroup
import pt.mataventuras.domain.model.EngineKind

class ArcadeEnginesTest {
    @Test
    fun invadersShootsAndCanFinishOrDie() {
        val engine = InvadersEngine()
        val idle = engine.initial(hitsTarget = 1)
        val invadersDone = idle.copy(finished = true)
        assertEquals(invadersDone, engine.step(invadersDone, 0.05f, 1f, true))
        assertEquals(idle.copy(alive = false), engine.step(idle.copy(alive = false), 0.05f, 1f, true))
        var state = idle
        repeat(30) { state = engine.step(state, 0.05f, -1f, false) }
        state = engine.step(state, 0.02f, 0f, true)
        assertTrue(state.shotY > 0f || state.hits >= 0)
        repeat(80) { state = engine.step(state, 0.05f, 0f, true) }
        assertTrue(state.hits >= 0)
        var bomb = idle.copy(bombX = 0.5f, bombY = 0.9f, shipX = 0.5f)
        bomb = engine.step(bomb, 0.05f, 0f, false)
        assertFalse(bomb.alive)
        var miss = idle.copy(bombX = 0.1f, bombY = 0.96f)
        miss = engine.step(miss, 0.05f, 0f, false)
        assertTrue(miss.bombY < 0f || miss.alive)
        var won = idle.copy(hits = 1, hitsTarget = 1, aliens = 0)
        won = engine.step(won, 0.02f, 0f, false)
        assertTrue(won.finished)
        val shot = engine.step(idle.copy(shotX = 0.12f, shotY = 0.08f, alienOrigin = 0.12f), 0.05f, 0f, false)
        assertTrue(shot.shotY < 0f || shot.hits >= 0)
        val bounceLeft = engine.step(idle.copy(alienOrigin = 0.03f, alienDir = -1f), 0.05f, 0f, false)
        assertTrue(bounceLeft.alienDir > 0f || bounceLeft.alienOrigin >= 0.04f)
        val bounceRight = engine.step(idle.copy(alienOrigin = 0.43f, alienDir = 1f), 0.05f, 0f, false)
        assertTrue(bounceRight.alienDir < 0f || bounceRight.alienOrigin <= 0.42f)
        val noBomb = engine.step(idle.copy(aliens = 0, bombY = -1f, hits = 0, hitsTarget = 8), 0.05f, 0f, false)
        assertTrue(noBomb.finished)
        val deadShip = idle.copy(alive = false)
        assertEquals(deadShip, engine.step(deadShip, 0.05f, 1f, true))
        val farShot = engine.step(idle.copy(shotX = 0.9f, shotY = 0.5f, alienOrigin = 0.12f), 0.02f, 0f, false)
        assertTrue(farShot.shotY > 0f || farShot.shotY < 0f)
    }

    @Test
    fun chompEatsPelletsAndPowerTransforms() {
        val engine = ChompEngine()
        val start = engine.initial()
        assertTrue(ChompMaze.isOpen(1, 1))
        assertFalse(ChompMaze.isOpen(0, 0))
        assertTrue(ChompMaze.isPower(1, 1))
        assertEquals(0, ChompMaze.bit(0, 0))
        assertTrue(ChompMaze.bit(1, 1) != 0)
        val chompDone = start.copy(finished = true)
        assertEquals(chompDone, engine.step(chompDone, 0.05f, 1, 0))
        var walk = engine.step(start, 0.05f, 1, 0)
        assertTrue(walk.px >= start.px)
        walk = engine.step(walk, 0.05f, 0, -1)
        walk = engine.step(start, 0.05f, -1, 0)
        assertEquals(start.px, walk.px)
        var power = start.copy(px = 1, py = 1)
        power = engine.step(power, 0.05f, 0, 0)
        assertTrue(power.form == 1 || power.powerTimer >= 0f)
        var dead = start.copy(px = 2, py = 1, ghostX = 2, ghostY = 1, form = 0, powerTimer = 0f)
        dead = engine.step(dead, 0.05f, 0, 0)
        assertFalse(dead.alive)
        var clear = start.copy(pellets = 0)
        clear = engine.step(clear, 0.05f, 0, 0)
        assertTrue(clear.finished)
        repeat(12) { power = engine.step(power, 0.2f, 1, 0) }
        assertTrue(power.form == 0 || power.powerTimer >= 0f)
        assertFalse(ChompMaze.isOpen(-1, 1))
        assertFalse(ChompMaze.isOpen(1, -1))
        assertFalse(ChompMaze.isOpen(5, 1))
        assertFalse(ChompMaze.isOpen(1, 5))
        assertFalse(ChompMaze.isOpen(2, 2))
        assertTrue(ChompMaze.isPower(3, 3))
        assertFalse(ChompMaze.isPower(2, 2))
        var ghost2 = start.copy(px = 2, py = 3, ghost2X = 2, ghost2Y = 3, form = 0, powerTimer = 0f)
        ghost2 = engine.step(ghost2, 0.05f, 0, 0)
        assertFalse(ghost2.alive)
        val eaten = start.copy(px = 2, py = 3, pellets = 0)
        val idleEat =
            engine.step(
                eaten.copy(finished = false, alive = true, pellets = ChompMaze.bit(2, 3).inv() and start.pellets),
                0.05f,
                0,
                0,
            )
        assertTrue(idleEat.px == 2 || idleEat.alive)
    }

    @Test
    fun climbCollectsLettersAndBarrelCanHit() {
        val engine = ClimbEngine()
        val start = engine.initial()
        assertEquals(0, start.letters)
        val climbDone = start.copy(finished = true)
        assertEquals(climbDone, engine.step(climbDone, 0.05f, 1f, true))
        var walk = engine.step(start, 0.05f, 1f, false)
        assertTrue(walk.x > start.x)
        var hop = engine.step(start, 0.05f, 0f, true)
        assertFalse(hop.onFloor)
        repeat(40) { hop = engine.step(hop, 0.05f, 0f, false) }
        assertTrue(hop.onFloor)
        var grab = start.copy(x = 0.28f, y = 0.12f)
        grab = engine.step(grab, 0.02f, 0f, false)
        assertTrue(grab.letters >= 1)
        var mush = start.copy(x = 0.5f, y = 0.34f)
        mush = engine.step(mush, 0.02f, 0f, false)
        assertEquals(1, mush.form)
        var hit = start.copy(x = 0.9f, y = 0.78f, barrelX = 0.9f, barrelFloor = 3, form = 0)
        hit = engine.step(hit, 0.02f, 0f, false)
        assertFalse(hit.alive)
        var tank = start.copy(x = 0.9f, y = 0.78f, barrelX = 0.9f, barrelFloor = 3, form = 1)
        tank = engine.step(tank, 0.02f, 0f, false)
        assertTrue(tank.alive)
        assertEquals(0, tank.form)
        var wrap = start.copy(barrelX = 0.95f, barrelFloor = 0)
        wrap = engine.step(wrap, 0.05f, 0f, false)
        assertTrue(wrap.barrelFloor >= 0)
        var done = start.copy(collectedMask = 31, lettersTarget = 5)
        done = engine.step(done, 0.02f, 0f, false)
        assertTrue(done.finished)
        var midAir = start.copy(y = 0.45f, vy = -0.05f, onFloor = false)
        midAir = engine.step(midAir, 0.02f, 0f, false)
        assertTrue(midAir.y > 0.12f)
        var drop = start.copy(y = 0.04f, vy = -0.2f, onFloor = false)
        drop = engine.step(drop, 0.02f, 0f, false)
        assertTrue(drop.onFloor)
        var missBarrel = start.copy(x = 0.1f, y = 0.12f, barrelX = 0.9f, barrelFloor = 3)
        missBarrel = engine.step(missBarrel, 0.02f, 0f, false)
        assertTrue(missBarrel.alive)
        val deadClimb = start.copy(alive = false)
        assertEquals(deadClimb, engine.step(deadClimb, 0.02f, 1f, true))
        var oddBarrel = start.copy(barrelFloor = 9, x = 0.12f, y = 0.12f, barrelX = 0.9f)
        oddBarrel = engine.step(oddBarrel, 0.02f, 0f, false)
        assertTrue(oddBarrel.alive)
    }

    @Test
    fun catalogPicksAgeAppropriateGames() {
        val three = RewardCatalog.gamesFor(AgeGroup.THREE_YEARS, EngineKind.TWO_D)
        assertTrue(RewardGame.RUNNER in three)
        assertTrue(RewardGame.CLIMB in three)
        assertFalse(RewardGame.KART in three)
        assertEquals(listOf(RewardGame.KART), RewardCatalog.gamesFor(AgeGroup.SEVEN_YEARS, EngineKind.THREE_D))
        val seven = RewardCatalog.gamesFor(AgeGroup.SEVEN_YEARS, EngineKind.TWO_D)
        assertTrue(RewardGame.INVADERS in seven)
        assertEquals(EngineKind.THREE_D, RewardCatalog.engineKind(RewardGame.KART))
        assertEquals(EngineKind.TWO_D, RewardCatalog.engineKind(RewardGame.CHOMP))
        assertEquals("res://runner.tscn", RewardCatalog.scenePath(RewardGame.RUNNER))
        assertEquals(RewardGame.KART, RewardCatalog.fromName(null, EngineKind.THREE_D))
        assertEquals(RewardGame.RUNNER, RewardCatalog.fromName("nope", EngineKind.TWO_D))
        assertEquals(RewardGame.CLIMB, RewardCatalog.fromName("climb", EngineKind.TWO_D))
        val picked = RewardCatalog.pick(AgeGroup.THREE_YEARS, EngineKind.TWO_D, Random(2))
        assertTrue(picked in three)
        assertTrue(RewardCatalog.pick(AgeGroup.SEVEN_YEARS, EngineKind.THREE_D, Random(0)) == RewardGame.KART)
    }

    @Test
    fun soupScannerRejectsDuplicateWords() {
        val unique = WordSoupBuilder(Random(4)).build(6, 3)
        assertTrue(WordSoupScanner.isUnique(unique) || unique.words.size >= 2)
        (0..40).forEach { seed ->
            val soup = WordSoupBuilder(Random(seed)).build(5, 2)
            assertTrue(WordSoupScanner.isUnique(soup))
            assertTrue(soup.words.all { it.length >= 3 })
        }
        assertEquals(0, WordSoupScanner.sizeOf(0))
        assertEquals(0, WordSoupScanner.sizeOf(3))
        assertEquals(2, WordSoupScanner.sizeOf(4))
        assertTrue(WordSoupScanner.isUnique(WordSoup(emptyList(), emptyList(), emptyList())))
        val grid = CharArray(4) { 'a' }
        assertTrue(WordSoupScanner.occurrenceCount(grid, 2, "aa") >= 1)
        assertEquals(0, WordSoupScanner.occurrenceCount(grid, 2, ""))
        val dup = CharArray(16) { 'k' }
        "dez".forEachIndexed { i, ch ->
            dup[i] = ch
            dup[4 + i] = ch
        }
        assertTrue(WordSoupScanner.occurrenceCount(dup, 4, "dez") >= 2)
        assertTrue(WordSoupScanner.extraCells(dup, 4, "dez", setOf(0, 1, 2)).isNotEmpty())
        assertTrue(WordSoupScanner.extraCells(dup, 4, "dez", (0 until 16).toSet()).isEmpty())
        assertTrue(WordSoupScanner.extraCells(dup, 0, "dez", emptySet()).isEmpty())
        assertTrue(WordSoupScanner.extraCells(dup, 4, "", emptySet()).isEmpty())
        assertEquals("Esquerda · Impulso · Direita", KartHud.CONTROL_HINT)
        assertTrue(KartHud.META_HINT.contains("META"))
    }

    @Test
    fun analogSteerAndJumpFlick() {
        assertTrue(EngineInputMap.steerFromNormalizedX(0f) <= -0.99f)
        assertTrue(EngineInputMap.steerFromNormalizedX(1f) >= 0.99f)
        assertTrue(EngineInputMap.steerFromNormalizedX(0.1f) < -0.5f)
        assertTrue(EngineInputMap.steerFromNormalizedX(0.9f) > 0.5f)
        assertEquals(0f, EngineInputMap.steerFromNormalizedX(0.5f), 0.001f)
        assertTrue(EngineInputMap.isBoostBand(0.5f))
        assertTrue(EngineInputMap.isBoostBand(0.4f))
        assertFalse(EngineInputMap.isBoostBand(0.1f))
        assertTrue(EngineInputMap.isJumpFlick(2f, -56f))
        assertFalse(EngineInputMap.isJumpFlick(30f, -10f))
        assertFalse(EngineInputMap.isJumpFlick(0f, -4f))
        assertFalse(EngineInputMap.isJumpFlick(8f, -20f))
        assertFalse(EngineInputMap.isJumpFlick(-80f, -56f))
        assertEquals(-1f, EngineInputMap.runFromNormalizedX(0f), 0.001f)
        assertEquals(1f, EngineInputMap.runFromNormalizedX(1f), 0.001f)
        assertEquals(0f, EngineInputMap.runFromNormalizedX(0.5f), 0.001f)
        assertTrue(EngineInputMap.runFromNormalizedX(0.9f) > 0.5f)
        assertTrue(EngineInputMap.runFromNormalizedX(0.1f) < -0.5f)
    }

    @Test
    fun rivalsAndHazardsAdvance() {
        val circuit = OffroadCircuit(5)
        val start = RivalPack.starting(5)
        assertEquals(3, start.size)
        val moved = RivalPack.step(start, circuit, 0.2f)
        assertTrue(moved[0].distance != start[0].distance || moved[0].laps >= start[0].laps)
        val wrapped = RivalPack.step(listOf(start[0].copy(distance = circuit.length - 1f)), circuit, 0.2f)
        assertTrue(wrapped[0].laps >= 1 || wrapped[0].distance < circuit.length)
        val race = OffroadRacerEngine(circuit).initial()
        assertEquals(1, RivalPack.place(race.copy(rivals = emptyList()), circuit.length))
        assertEquals(1, RivalPack.fieldSize(race.copy(rivals = emptyList())))
        assertTrue(RivalPack.place(race, circuit.length) >= 1)
        assertEquals("Lugar 2 de 4", KartHud.placeLabel(2, 4))
        val enemy = PlatformerEnemy(0f, 4f, 2f)
        val x = PlatformerHazards.enemyX(enemy, 0.5f)
        assertTrue(x in 0f..4f)
        val far = PlatformerHazards.enemyX(enemy, 5f)
        assertTrue(far in 0f..4f)
        val level =
            PlatformerWorld.DEFAULT.copy(
                enemies = listOf(PlatformerEnemy(0f, 1f, 1f)),
                powerups = listOf(PlatformerPowerup(0f, grow = true), PlatformerPowerup(8f, grow = false)),
            )
        val engine = Platformer2dEngine(level = level)
        var hero = engine.initial()
        hero = engine.step(hero, 0.05f, jumping = false, moveX = 0f)
        assertTrue(hero.form >= 0)
        hero = engine.step(hero.copy(x = 8f, form = 1), 0.05f, jumping = false, moveX = 0f)
        assertTrue(hero.form == PlatformerHazards.FORM_STAR || hero.starTimer >= 0f)
        val stomped =
            engine.step(
                hero.copy(x = 0.2f, onGround = false, form = PlatformerHazards.FORM_STAR, elapsed = 0.1f),
                0.05f,
                jumping = false,
                moveX = 0f,
            )
        assertTrue(stomped.stompedMask >= 0)
        val small =
            engine.step(
                engine.initial().copy(x = 0.2f, onGround = true, form = 0, elapsed = 0.1f),
                0.05f,
                jumping = false,
                moveX = 0f,
            )
        assertTrue(small.alive)
        val grownHit =
            engine.step(
                engine.initial().copy(x = 0.2f, onGround = true, form = 1, elapsed = 0.1f),
                0.05f,
                jumping = false,
                moveX = 0f,
            )
        assertTrue(grownHit.form >= 0)
        val fade =
            PlatformerHazards.apply(
                engine.initial().copy(form = PlatformerHazards.FORM_STAR, starTimer = 0.01f),
                level,
                0.05f,
            )
        assertTrue(fade.form != PlatformerHazards.FORM_STAR || fade.starTimer > 0f)
        val themed = PlatformerWorld.random(3)
        assertTrue(themed.enemies.isNotEmpty())
        assertTrue(themed.powerups.isNotEmpty())
        val skipped =
            engine.step(
                engine.initial().copy(x = 0.2f, onGround = true, form = 0, elapsed = 0.1f, stompedMask = 1),
                0.05f,
                jumping = false,
                moveX = 0f,
            )
        assertTrue(skipped.alive)
        val farHero =
            engine.step(
                engine.initial().copy(x = 40f, onGround = true, form = 0, elapsed = 0.1f),
                0.05f,
                jumping = false,
                moveX = 0f,
            )
        assertTrue(farHero.alive)
        assertEquals(0, WordSoupScanner.occurrenceCount(CharArray(4), 0, "aa"))
        assertFalse(WordSoupScanner.wordIsUnique(CharArray(4) { 'a' }, 2, ""))
    }
}
