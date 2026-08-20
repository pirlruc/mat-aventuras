package pt.mataventuras.domain.parent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.model.LearningSession

class ParentAnalyticsTest {
    private val analytics = ParentAnalytics()

    @Test
    fun ignoresOtherProfiles() {
        val sessions =
            listOf(
                session(1, LearningModule.ADDITION, 2, 0, 1_000),
                session(2, LearningModule.ADDITION, 50, 50, 9_000),
            )
        val summary = analytics.summarise(1, sessions)
        assertEquals(2, summary.hits)
        assertEquals(0, summary.misses)
        assertEquals(1_000L, summary.totalTimeMs)
        assertEquals(1.0, summary.accuracy, 0.0)
        assertTrue(summary.needsWork.isEmpty())
    }

    @Test
    fun flagsAModuleThatNeedsWork() {
        val sessions =
            List(5) {
                session(1, LearningModule.MULTIPLICATION, 1, 4, 500)
            } + listOf(session(1, LearningModule.ADDITION, 10, 0, 200))
        val summary = analytics.summarise(1, sessions)
        assertEquals(listOf(LearningModule.MULTIPLICATION), summary.needsWork)
        assertEquals(2, summary.byModule.size)
    }

    @Test
    fun emptyHistoryIsZeroed() {
        val summary = analytics.summarise(9, emptyList())
        assertEquals(0.0, summary.accuracy, 0.0)
        assertTrue(summary.byModule.isEmpty())
        assertEquals(0, summary.hits)
        assertEquals(0L, summary.totalTimeMs)
    }

    private fun session(
        profileId: Long,
        module: LearningModule,
        hits: Int,
        misses: Int,
        duration: Long,
    ) = LearningSession(
        id = profileId + module.ordinal.toLong(),
        profileId = profileId,
        module = module,
        hits = hits,
        misses = misses,
        durationMs = duration,
        startedAtEpochMs = 0,
    )
}
