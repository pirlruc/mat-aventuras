package pt.mataventuras.domain.model

/**
 * Local child profile on this device.
 */
data class ChildProfile(
    val id: Long,
    val name: String,
    val ageGroup: AgeGroup,
    val favouriteMascot: Mascot,
    val avatarId: String,
    val points: Int,
    val createdAtEpochMs: Long,
)

/**
 * Completed lesson session, used by the parental dashboard.
 */
data class LearningSession(
    val id: Long,
    val profileId: Long,
    val module: LearningModule,
    val hits: Int,
    val misses: Int,
    val durationMs: Long,
    val startedAtEpochMs: Long,
) {
    /** Accuracy in 0..1. Zero when there were no attempts. */
    fun accuracy(): Double {
        val total = hits + misses
        if (total == 0) return 0.0
        return hits.toDouble() / total.toDouble()
    }
}

/**
 * Badge unlocked by a profile.
 */
data class UnlockedBadge(
    val code: String,
    val unlockedAtEpochMs: Long,
)

/**
 * Avatar unlocked by a profile.
 */
data class UnlockedAvatar(
    val avatarId: String,
    val unlockedAtEpochMs: Long,
)

/**
 * Local leaderboard row (siblings/friends on the same device).
 */
data class LeaderboardEntry(
    val rank: Int,
    val profileId: Long,
    val name: String,
    val points: Int,
    val averageAccuracy: Double,
    val mascot: Mascot,
)
