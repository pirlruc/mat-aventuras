package pt.mataventuras.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * On-device Room database. Never opened from the `:engine3d` process.
 */
@Database(
    entities = [
        ProfileEntity::class,
        SessionEntity::class,
        BadgeEntity::class,
        AvatarEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class MatAventurasDatabase : RoomDatabase() {
    /**
     * Profiles ordered by points.
     */
    abstract fun profileDao(): ProfileDao

    /**
     * Lesson sessions.
     */
    abstract fun sessionDao(): SessionDao

    /**
     * Badge unlocks.
     */
    abstract fun badgeDao(): BadgeDao

    /**
     * Avatar unlocks.
     */
    abstract fun avatarDao(): AvatarDao
}
