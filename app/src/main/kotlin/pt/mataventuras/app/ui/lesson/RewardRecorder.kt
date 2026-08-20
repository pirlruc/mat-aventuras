package pt.mataventuras.app.ui.lesson

import pt.mataventuras.app.di.AppContainer

/**
 * Applies a finished reward mini-game to the last opened profile.
 */
internal object RewardRecorder {
    /**
     * Awards bonus points and newly unlocked avatars when [finished] is true.
     */
    suspend fun apply(
        container: AppContainer,
        finished: Boolean,
    ) {
        val delta = container.rewards.pointsForRewardFinish(finished)
        if (delta == 0) return
        val id = container.lastProfile.read() ?: return
        val current = container.repository.getProfile(id) ?: return
        val points = container.rewards.applyPoints(current.points, delta)
        container.repository.updateProfile(current.copy(points = points))
        container.rewards.newAvatars(
            alreadyUnlocked = container.repository.avatarIds(id),
            points = points,
        ).forEach { container.repository.unlockAvatar(id, it.name) }
    }
}
