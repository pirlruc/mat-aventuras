package pt.mataventuras.domain.progress

import pt.mataventuras.domain.model.ChildProfile
import pt.mataventuras.domain.model.LeaderboardEntry
import pt.mataventuras.domain.model.LearningSession

/**
 * Local leaderboard: points descending, then average accuracy.
 */
class LeaderboardCalculator {
    /**
     * Builds the table for profiles on this device.
     */
    fun rank(
        profiles: List<ChildProfile>,
        sessions: List<LearningSession>,
    ): List<LeaderboardEntry> {
        val byProfile = sessions.groupBy { it.profileId }
        val ordered =
            profiles.sortedWith(
                compareByDescending<ChildProfile> { it.points }
                    .thenByDescending { averageAccuracy(byProfile[it.id].orEmpty()) }
                    .thenBy { it.name.lowercase() },
            )
        return ordered.mapIndexed { index, profile ->
            LeaderboardEntry(
                rank = index + 1,
                profileId = profile.id,
                name = profile.name,
                points = profile.points,
                averageAccuracy = averageAccuracy(byProfile[profile.id].orEmpty()),
                mascot = profile.favouriteMascot,
            )
        }
    }

    private fun averageAccuracy(sessions: List<LearningSession>): Double {
        if (sessions.isEmpty()) return 0.0
        return sessions.map { it.accuracy() }.average()
    }
}
