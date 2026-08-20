package pt.mataventuras.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pt.mataventuras.data.local.AvatarEntity
import pt.mataventuras.data.local.BadgeEntity
import pt.mataventuras.data.local.MatAventurasDatabase
import pt.mataventuras.data.mapping.toDomain
import pt.mataventuras.data.mapping.toEntity
import pt.mataventuras.domain.model.AgeGroup
import pt.mataventuras.domain.model.ChildProfile
import pt.mataventuras.domain.model.LearningSession
import pt.mataventuras.domain.model.Mascot
import pt.mataventuras.domain.model.UnlockedAvatar
import pt.mataventuras.domain.model.UnlockedBadge
import pt.mataventuras.domain.progress.AvatarCode

/**
 * On-device profile, session, badge, and avatar store.
 */
class LocalRepository(
    private val database: MatAventurasDatabase,
    private val now: () -> Long = { System.currentTimeMillis() },
) {
    /** Live list of profiles, strongest first. */
    fun observeProfiles(): Flow<List<ChildProfile>> =
        database.profileDao().observeAll().map { list -> list.map { it.toDomain() } }

    /** Live sessions for one child. */
    fun observeSessions(profileId: Long): Flow<List<LearningSession>> =
        database.sessionDao().observeFor(profileId).map { list -> list.map { it.toDomain() } }

    /** Live badges for one child. */
    fun observeBadges(profileId: Long): Flow<List<UnlockedBadge>> =
        database.badgeDao().observeFor(profileId).map { list -> list.map { it.toDomain() } }

    /** Live avatars for one child. */
    fun observeAvatars(profileId: Long): Flow<List<UnlockedAvatar>> =
        database.avatarDao().observeFor(profileId).map { list -> list.map { it.toDomain() } }

    /** Creates a profile and grants the starter avatar. */
    suspend fun createProfile(name: String, ageGroup: AgeGroup, mascot: Mascot): Long {
        val nowMs = now()
        val id = database.profileDao().insert(
            ChildProfile(
                id = 0,
                name = name.trim(),
                ageGroup = ageGroup,
                favouriteMascot = mascot,
                avatarId = AvatarCode.STARTER.name,
                points = 0,
                createdAtEpochMs = nowMs,
            ).toEntity().copy(id = 0),
        )
        database.avatarDao().insert(
            AvatarEntity(
                profileId = id,
                avatarId = AvatarCode.STARTER.name,
                unlockedAtEpochMs = nowMs,
            ),
        )
        return id
    }

    /** Overwrites a stored profile. */
    suspend fun updateProfile(profile: ChildProfile) {
        database.profileDao().update(profile.toEntity())
    }

    /** Loads one profile, or null. */
    suspend fun getProfile(id: Long): ChildProfile? = database.profileDao().get(id)?.toDomain()

    /** Every stored session. */
    suspend fun allSessions(): List<LearningSession> =
        database.sessionDao().all().map { it.toDomain() }

    /** Inserts a session and returns its row id. */
    suspend fun saveSession(session: LearningSession): Long =
        database.sessionDao().insert(session.toEntity().copy(id = 0))

    /** Unlocks a badge; duplicates are ignored. */
    suspend fun unlockBadge(profileId: Long, code: String) {
        database.badgeDao().insert(
            BadgeEntity(
                profileId = profileId,
                code = code,
                unlockedAtEpochMs = now(),
            ),
        )
    }

    /** Unlocks an avatar; duplicates are ignored. */
    suspend fun unlockAvatar(profileId: Long, avatarId: String) {
        database.avatarDao().insert(
            AvatarEntity(
                profileId = profileId,
                avatarId = avatarId,
                unlockedAtEpochMs = now(),
            ),
        )
    }

    /** Badge codes already owned by [profileId]. */
    suspend fun badgeCodes(profileId: Long): Set<String> =
        database.badgeDao().codesFor(profileId).toSet()

    /** Avatar ids already owned by [profileId]. */
    suspend fun avatarIds(profileId: Long): Set<String> =
        database.avatarDao().idsFor(profileId).toSet()
}
