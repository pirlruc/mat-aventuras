package pt.mataventuras.domain.progress

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.domain.model.AgeGroup
import pt.mataventuras.domain.model.ChildProfile
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.model.LearningSession
import pt.mataventuras.domain.model.Mascot

class RewardsEngineTest {
    private val engine = RewardsEngine()
    private val leaderboard = LeaderboardCalculator()

    @Test
    fun pointsNeverGoNegative() {
        assertEquals(10, engine.pointsForAttempt(true))
        assertEquals(-2, engine.pointsForAttempt(false))
        assertEquals(15, engine.pointsForRewardFinish(true))
        assertEquals(0, engine.pointsForRewardFinish(false))
        assertEquals(0, engine.applyPoints(1, -2))
        assertEquals(11, engine.applyPoints(1, 10))
    }

    @Test
    fun rewardOpensEveryThreeHits() {
        assertFalse(engine.shouldOpenReward(0))
        assertFalse(engine.shouldOpenReward(2))
        assertTrue(engine.shouldOpenReward(3))
        assertTrue(engine.shouldOpenReward(6))
    }

    @Test
    fun badgesAndAvatarsUnlockFromTotals() {
        val newBadges =
            engine.newBadges(
                alreadyUnlocked = emptySet(),
                totals =
                    ProgressTotals(
                        completedSessions = 1,
                        countingHits = 10,
                        shapeHits = 10,
                        arithmeticHits = 20,
                        perfectSessionWithMinimum = true,
                        totalTimeMs = RewardsEngine.THIRTY_MINUTES_MS,
                    ),
            )
        assertEquals(BadgeCode.entries.size, newBadges.size)
        assertEquals("Primeiros passos", BadgeCode.FIRST_STEPS.title)
        val already = setOf(BadgeCode.FIRST_STEPS.name)
        assertFalse(
            engine.newBadges(
                already,
                ProgressTotals(1, 0, 0, 0, false, 0),
            ).contains(BadgeCode.FIRST_STEPS),
        )
        val avatars = engine.newAvatars(emptySet(), 500)
        assertEquals(AvatarCode.entries.size, avatars.size)
        assertTrue(engine.newAvatars(setOf(AvatarCode.STARTER.name), 0).isEmpty())
        assertEquals("Lenda", AvatarCode.LEGEND.title)
    }

    @Test
    fun badgesWithoutAFirstSessionSkipFirstSteps() {
        val newly =
            engine.newBadges(
                emptySet(),
                ProgressTotals(0, 10, 10, 20, true, RewardsEngine.THIRTY_MINUTES_MS),
            )
        assertFalse(newly.contains(BadgeCode.FIRST_STEPS))
        assertTrue(newly.contains(BadgeCode.STAR_COUNTER))
    }

    @Test
    fun leaderboardOrdersByPointsThenAccuracy() {
        val a = profile(1, "Ana", 100)
        val b = profile(2, "Beto", 100)
        val c = profile(3, "Cata", 50)
        val sessions =
            listOf(
                session(1, 8, 2),
                session(2, 9, 1),
                session(3, 1, 1),
            )
        val table = leaderboard.rank(listOf(c, b, a), sessions)
        assertEquals(listOf(2L, 1L, 3L), table.map { it.profileId })
        assertEquals(1, table.first().rank)
    }

    @Test
    fun nameTieIsAlphabetical() {
        val ana = profile(1, "Ana", 10)
        val zoe = profile(2, "Zoe", 10)
        val table = leaderboard.rank(listOf(zoe, ana), emptyList())
        assertEquals(listOf("Ana", "Zoe"), table.map { it.name })
    }

    @Test
    fun leaderboardWithoutSessionsIsZeroAccuracy() {
        val table = leaderboard.rank(listOf(profile(1, "Ana", 0)), emptyList())
        assertEquals(0.0, table.single().averageAccuracy, 0.0)
    }

    @Test
    fun lessonProgressAggregatesModules() {
        val sessions =
            listOf(
                LearningSession(1, 1, LearningModule.COUNTING, 4, 0, 1_000, 0),
                LearningSession(2, 1, LearningModule.SHAPES, 3, 1, 1_000, 0),
                LearningSession(3, 1, LearningModule.ADDITION, 6, 0, 1_000, 0),
            )
        val totals = LessonProgress.totals(sessions, hitsThisRound = 6, missesThisRound = 0)
        assertEquals(3, totals.completedSessions)
        assertEquals(4, totals.countingHits)
        assertEquals(3, totals.shapeHits)
        assertEquals(6, totals.arithmeticHits)
        assertTrue(totals.perfectSessionWithMinimum)
        val imperfect = LessonProgress.totals(sessions, hitsThisRound = 2, missesThisRound = 1)
        assertFalse(imperfect.perfectSessionWithMinimum)
    }

    private fun profile(
        id: Long,
        name: String,
        points: Int,
    ) = ChildProfile(
        id = id,
        name = name,
        ageGroup = AgeGroup.SEVEN_YEARS,
        favouriteMascot = Mascot.SPEEDY_HEDGEHOG,
        avatarId = AvatarCode.STARTER.name,
        points = points,
        createdAtEpochMs = 0,
    )

    private fun session(
        profileId: Long,
        hits: Int,
        misses: Int,
    ) = LearningSession(
        id = profileId,
        profileId = profileId,
        module = LearningModule.ADDITION,
        hits = hits,
        misses = misses,
        durationMs = 1_000,
        startedAtEpochMs = 0,
    )
}
