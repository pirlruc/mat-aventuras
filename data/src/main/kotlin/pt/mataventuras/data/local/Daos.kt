package pt.mataventuras.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles ORDER BY points DESC, name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun get(id: Long): ProfileEntity?

    @Insert
    suspend fun insert(profile: ProfileEntity): Long

    @Update
    suspend fun update(profile: ProfileEntity)
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions WHERE profileId = :profileId ORDER BY startedAtEpochMs DESC")
    fun observeFor(profileId: Long): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions")
    suspend fun all(): List<SessionEntity>

    @Insert
    suspend fun insert(session: SessionEntity): Long
}

@Dao
interface BadgeDao {
    @Query("SELECT * FROM badges WHERE profileId = :profileId")
    fun observeFor(profileId: Long): Flow<List<BadgeEntity>>

    @Query("SELECT code FROM badges WHERE profileId = :profileId")
    suspend fun codesFor(profileId: Long): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(badge: BadgeEntity): Long
}

@Dao
interface AvatarDao {
    @Query("SELECT * FROM avatars WHERE profileId = :profileId")
    fun observeFor(profileId: Long): Flow<List<AvatarEntity>>

    @Query("SELECT avatarId FROM avatars WHERE profileId = :profileId")
    suspend fun idsFor(profileId: Long): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(avatar: AvatarEntity): Long
}
