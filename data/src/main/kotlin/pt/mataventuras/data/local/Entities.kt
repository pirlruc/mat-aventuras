package pt.mataventuras.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val ageGroup: String,
    val mascotCode: String,
    val avatarId: String,
    val points: Int,
    val createdAtEpochMs: Long,
)

@Entity(
    tableName = "sessions",
    indices = [Index("profileId")],
)
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val module: String,
    val hits: Int,
    val misses: Int,
    val durationMs: Long,
    val startedAtEpochMs: Long,
)

@Entity(
    tableName = "badges",
    indices = [Index(value = ["profileId", "code"], unique = true)],
)
data class BadgeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val code: String,
    val unlockedAtEpochMs: Long,
)

@Entity(
    tableName = "avatars",
    indices = [Index(value = ["profileId", "avatarId"], unique = true)],
)
data class AvatarEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val avatarId: String,
    val unlockedAtEpochMs: Long,
)
