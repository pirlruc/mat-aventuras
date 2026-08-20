package pt.mataventuras.data.mapping

import pt.mataventuras.data.local.AvatarEntity
import pt.mataventuras.data.local.BadgeEntity
import pt.mataventuras.data.local.ProfileEntity
import pt.mataventuras.data.local.SessionEntity
import pt.mataventuras.domain.model.AgeGroup
import pt.mataventuras.domain.model.ChildProfile
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.model.LearningSession
import pt.mataventuras.domain.model.Mascot
import pt.mataventuras.domain.model.UnlockedAvatar
import pt.mataventuras.domain.model.UnlockedBadge

/** Maps a stored profile row to the domain model. */
fun ProfileEntity.toDomain(): ChildProfile = ChildProfile(
    id = id,
    name = name,
    ageGroup = AgeGroup.valueOf(ageGroup),
    favouriteMascot = Mascot.fromCode(mascotCode),
    avatarId = avatarId,
    points = points,
    createdAtEpochMs = createdAtEpochMs,
)

/** Maps a domain profile to a Room row. */
fun ChildProfile.toEntity(): ProfileEntity = ProfileEntity(
    id = id,
    name = name,
    ageGroup = ageGroup.name,
    mascotCode = favouriteMascot.code,
    avatarId = avatarId,
    points = points,
    createdAtEpochMs = createdAtEpochMs,
)

/** Maps a stored session row to the domain model. */
fun SessionEntity.toDomain(): LearningSession = LearningSession(
    id = id,
    profileId = profileId,
    module = LearningModule.valueOf(module),
    hits = hits,
    misses = misses,
    durationMs = durationMs,
    startedAtEpochMs = startedAtEpochMs,
)

/** Maps a domain session to a Room row. */
fun LearningSession.toEntity(): SessionEntity = SessionEntity(
    id = id,
    profileId = profileId,
    module = module.name,
    hits = hits,
    misses = misses,
    durationMs = durationMs,
    startedAtEpochMs = startedAtEpochMs,
)

/** Maps a badge row to the domain model. */
fun BadgeEntity.toDomain(): UnlockedBadge =
    UnlockedBadge(code = code, unlockedAtEpochMs = unlockedAtEpochMs)

/** Maps an avatar row to the domain model. */
fun AvatarEntity.toDomain(): UnlockedAvatar =
    UnlockedAvatar(avatarId = avatarId, unlockedAtEpochMs = unlockedAtEpochMs)
