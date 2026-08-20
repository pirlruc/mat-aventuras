package pt.mataventuras.domain.parent

import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.model.LearningSession

/**
 * Summary shown on the parental dashboard.
 */
data class ParentSummary(
    val profileId: Long,
    val totalTimeMs: Long,
    val hits: Int,
    val misses: Int,
    val accuracy: Double,
    val byModule: List<ModulePerformance>,
    val needsWork: List<LearningModule>,
)

/**
 * Aggregated performance for one module.
 */
data class ModulePerformance(
    val module: LearningModule,
    val hits: Int,
    val misses: Int,
    val accuracy: Double,
    val timeMs: Long,
)

/**
 * Aggregates sessions into a parental summary.
 */
class ParentAnalytics {
    /**
     * Builds the summary for one profile.
     */
    fun summarise(
        profileId: Long,
        sessions: List<LearningSession>,
    ): ParentSummary {
        val owned = sessions.filter { it.profileId == profileId }
        val hits = owned.sumOf { it.hits }
        val misses = owned.sumOf { it.misses }
        val time = owned.sumOf { it.durationMs }
        val byModule =
            owned.groupBy { it.module }.map { (module, list) ->
                val h = list.sumOf { it.hits }
                val m = list.sumOf { it.misses }
                ModulePerformance(
                    module = module,
                    hits = h,
                    misses = m,
                    accuracy = accuracy(h, m),
                    timeMs = list.sumOf { it.durationMs },
                )
            }.sortedBy { it.module.name }
        val needsWork =
            byModule
                .filter { (it.hits + it.misses) >= MIN_SAMPLES && it.accuracy < IMPROVE_THRESHOLD }
                .sortedBy { it.accuracy }
                .map { it.module }
        return ParentSummary(
            profileId = profileId,
            totalTimeMs = time,
            hits = hits,
            misses = misses,
            accuracy = accuracy(hits, misses),
            byModule = byModule,
            needsWork = needsWork,
        )
    }

    private fun accuracy(
        hits: Int,
        misses: Int,
    ): Double {
        val total = hits + misses
        if (total == 0) return 0.0
        return hits.toDouble() / total.toDouble()
    }

    /** Parental dashboard thresholds. */
    companion object {
        const val IMPROVE_THRESHOLD: Double = 0.7
        const val MIN_SAMPLES: Int = 5
    }
}
