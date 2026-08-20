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

fun ProfileEntity.toDomain(): ChildProfile = ChildProfile(
    id = id,
    name = name,
    ageGroup = AgeGroup.valueOf(ageGroup),
    favouriteMascot = Mascot.fromCode(mascotCode),
    avatarId = avatarId,
    points = points,
    createdAtEpochMs = createdAtEpochMs,
)

fun ChildProfile.toEntity(): ProfileEntity = ProfileEntity(
    id = id,
    name = name,
    ageGroup = ageGroup.name,
    mascotCode = favouriteMascot.code,
    avatarId = avatarId,
    points = points,
    createdAtEpochMs = createdAtEpochMs,
)

fun SessionEntity.toDomain(): LearningSession = LearningSession(
    id = id,
    profileId = profileId,
    module = LearningModule.valueOf(module),
    hits = hits,
    misses = misses,
    durationMs = durationMs,
    startedAtEpochMs = startedAtEpochMs,
)

fun LearningSession.toEntity(): SessionEntity = SessionEntity(
    id = id,
    profileId = profileId,
    module = module.name,
    hits = hits,
    misses = misses,
    durationMs = durationMs,
    startedAtEpochMs = startedAtEpochMs,
)

fun BadgeEntity.toDomain(): UnlockedBadge =
    UnlockedBadge(code = code, unlockedAtEpochMs = unlockedAtEpochMs)

fun AvatarEntity.toDomain(): UnlockedAvatar =
    UnlockedAvatar(avatarId = avatarId, unlockedAtEpochMs = unlockedAtEpochMs)
