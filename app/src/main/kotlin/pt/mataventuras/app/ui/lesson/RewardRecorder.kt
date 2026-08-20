package pt.mataventuras.app.ui.lesson

import pt.mataventuras.app.di.AppContainer

/**
 * Applies a finished reward mini-game to the last opened profile.
 */
internal object RewardRecorder {
    /**
     * Awards bonus points and newly unlocked avatars when [finished] is true.
     * Uses an atomic points update so an in-flight lesson tap cannot erase the bonus.
     */
    suspend fun apply(
        container: AppContainer,
        finished: Boolean,
    ) {
        val delta = container.rewards.pointsForRewardFinish(finished)
        if (delta == 0) return
        val id = container.lastProfile.read() ?: return
        val updated = container.repository.addPoints(id, delta) ?: return
        container.rewards.newAvatars(
            alreadyUnlocked = container.repository.avatarIds(id),
            points = updated.points,
        ).forEach { container.repository.unlockAvatar(id, it.name) }
        container.publishProfile(id)
    }
}
