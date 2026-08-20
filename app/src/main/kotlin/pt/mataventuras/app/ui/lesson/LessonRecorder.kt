package pt.mataventuras.app.ui.lesson

import pt.mataventuras.app.di.AppContainer
import pt.mataventuras.domain.model.ChildProfile
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.model.LearningSession
import pt.mataventuras.domain.progress.LessonProgress

/**
 * Writes lesson results, badges, and avatars after the child leaves a round.
 */
internal object LessonRecorder {
    /**
     * Persists the round and unlocks any newly earned rewards.
     */
    suspend fun persist(
        container: AppContainer,
        profile: ChildProfile,
        points: Int,
        module: LearningModule,
        hits: Int,
        misses: Int,
        startedAt: Long,
        nowMs: Long = System.currentTimeMillis(),
    ) {
        val current = container.repository.getProfile(profile.id) ?: return
        container.repository.updateProfile(current.copy(points = points))
        container.repository.saveSession(
            LearningSession(
                id = 0,
                profileId = profile.id,
                module = module,
                hits = hits,
                misses = misses,
                durationMs = nowMs - startedAt,
                startedAtEpochMs = startedAt,
            ),
        )
        val sessions = container.repository.allSessions().filter { it.profileId == profile.id }
        val alreadyBadges = container.repository.badgeCodes(profile.id)
        val alreadyAvatars = container.repository.avatarIds(profile.id)
        container.rewards.newBadges(
            alreadyUnlocked = alreadyBadges,
            totals = LessonProgress.totals(sessions, hits, misses),
        ).forEach { container.repository.unlockBadge(profile.id, it.name) }
        container.rewards.newAvatars(alreadyAvatars, points).forEach {
            container.repository.unlockAvatar(profile.id, it.name)
        }
    }
}
