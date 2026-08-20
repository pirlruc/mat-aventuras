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

class LocalRepository(
    private val database: MatAventurasDatabase,
    private val now: () -> Long = { System.currentTimeMillis() },
) {
    fun observeProfiles(): Flow<List<ChildProfile>> =
        database.profileDao().observeAll().map { list -> list.map { it.toDomain() } }

    fun observeSessions(profileId: Long): Flow<List<LearningSession>> =
        database.sessionDao().observeFor(profileId).map { list -> list.map { it.toDomain() } }

    fun observeBadges(profileId: Long): Flow<List<UnlockedBadge>> =
        database.badgeDao().observeFor(profileId).map { list -> list.map { it.toDomain() } }

    fun observeAvatars(profileId: Long): Flow<List<UnlockedAvatar>> =
        database.avatarDao().observeFor(profileId).map { list -> list.map { it.toDomain() } }

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

    suspend fun updateProfile(profile: ChildProfile) {
        database.profileDao().update(profile.toEntity())
    }

    suspend fun getProfile(id: Long): ChildProfile? = database.profileDao().get(id)?.toDomain()

    suspend fun allSessions(): List<LearningSession> =
        database.sessionDao().all().map { it.toDomain() }

    suspend fun saveSession(session: LearningSession): Long =
        database.sessionDao().insert(session.toEntity().copy(id = 0))

    suspend fun unlockBadge(profileId: Long, code: String) {
        database.badgeDao().insert(
            BadgeEntity(
                profileId = profileId,
                code = code,
                unlockedAtEpochMs = now(),
            ),
        )
    }

    suspend fun unlockAvatar(profileId: Long, avatarId: String) {
        database.avatarDao().insert(
            AvatarEntity(
                profileId = profileId,
                avatarId = avatarId,
                unlockedAtEpochMs = now(),
            ),
        )
    }

    suspend fun badgeCodes(profileId: Long): Set<String> =
        database.badgeDao().codesFor(profileId).toSet()

    suspend fun avatarIds(profileId: Long): Set<String> =
        database.avatarDao().idsFor(profileId).toSet()
}
