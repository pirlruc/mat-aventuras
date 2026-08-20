package pt.mataventuras.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Room accessors for child profiles.
 */
@Dao
interface ProfileDao {
    /** Profiles ordered by points, then name. */
    @Query("SELECT * FROM profiles ORDER BY points DESC, name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<ProfileEntity>>

    /** One profile by id. */
    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun get(id: Long): ProfileEntity?

    /** Inserts a profile and returns the row id. */
    @Insert
    suspend fun insert(profile: ProfileEntity): Long

    /** Overwrites a profile row. */
    @Update
    suspend fun update(profile: ProfileEntity)
}

/**
 * Room accessors for lesson sessions.
 */
@Dao
interface SessionDao {
    /** Sessions for one child, newest first. */
    @Query("SELECT * FROM sessions WHERE profileId = :profileId ORDER BY startedAtEpochMs DESC")
    fun observeFor(profileId: Long): Flow<List<SessionEntity>>

    /** Every session in the database. */
    @Query("SELECT * FROM sessions")
    suspend fun all(): List<SessionEntity>

    /** Inserts a session and returns the row id. */
    @Insert
    suspend fun insert(session: SessionEntity): Long
}

/**
 * Room accessors for unlocked badges.
 */
@Dao
interface BadgeDao {
    /** Badges owned by one child. */
    @Query("SELECT * FROM badges WHERE profileId = :profileId")
    fun observeFor(profileId: Long): Flow<List<BadgeEntity>>

    /** Badge codes owned by one child. */
    @Query("SELECT code FROM badges WHERE profileId = :profileId")
    suspend fun codesFor(profileId: Long): List<String>

    /** Inserts a badge; duplicates are ignored. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(badge: BadgeEntity): Long
}

/**
 * Room accessors for unlocked avatars.
 */
@Dao
interface AvatarDao {
    /** Avatars owned by one child. */
    @Query("SELECT * FROM avatars WHERE profileId = :profileId")
    fun observeFor(profileId: Long): Flow<List<AvatarEntity>>

    /** Avatar ids owned by one child. */
    @Query("SELECT avatarId FROM avatars WHERE profileId = :profileId")
    suspend fun idsFor(profileId: Long): List<String>

    /** Inserts an avatar; duplicates are ignored. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(avatar: AvatarEntity): Long
}
