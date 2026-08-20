package pt.mataventuras.domain.progress

import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.model.LearningSession

/**
 * Builds [ProgressTotals] from stored sessions plus the round just finished.
 */
object LessonProgress {
    /**
     * Aggregates hits used by [RewardsEngine.newBadges].
     */
    fun totals(
        sessions: List<LearningSession>,
        hitsThisRound: Int,
        missesThisRound: Int,
    ): ProgressTotals {
        val counting = sessions.filter { it.module == LearningModule.COUNTING }.sumOf { it.hits }
        val shapes = sessions.filter { it.module == LearningModule.SHAPES }.sumOf { it.hits }
        val arithmetic = sessions.filter { it.module in ARITHMETIC }.sumOf { it.hits }
        val perfect = missesThisRound == 0 && hitsThisRound >= RewardsEngine.PERFECT_MINIMUM
        return ProgressTotals(
            completedSessions = sessions.size,
            countingHits = counting,
            shapeHits = shapes,
            arithmeticHits = arithmetic,
            perfectSessionWithMinimum = perfect,
            totalTimeMs = sessions.sumOf { it.durationMs },
        )
    }

    private val ARITHMETIC =
        setOf(
            LearningModule.ADDITION,
            LearningModule.SUBTRACTION,
            LearningModule.MULTIPLICATION,
        )
}
