package pt.mataventuras.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

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
    abstract fun profileDao(): ProfileDao
    abstract fun sessionDao(): SessionDao
    abstract fun badgeDao(): BadgeDao
    abstract fun avatarDao(): AvatarDao
}
