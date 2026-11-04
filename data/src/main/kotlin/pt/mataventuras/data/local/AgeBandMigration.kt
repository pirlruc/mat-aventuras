package pt.mataventuras.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Rewrites fixed-age names stored before the 3–5 and 6–8 bands.
 * The column stays TEXT. No rows are deleted.
 */
val MIGRATION_1_2: Migration =
    object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "UPDATE profiles SET ageGroup = 'THREE_TO_FIVE' WHERE ageGroup = 'THREE_YEARS'",
            )
            db.execSQL(
                "UPDATE profiles SET ageGroup = 'SIX_TO_EIGHT' WHERE ageGroup = 'SEVEN_YEARS'",
            )
        }
    }

/**
 * Adds a parent-set prize length. Existing rows keep two minutes.
 * No rows are deleted.
 */
val MIGRATION_2_3: Migration =
    object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE profiles ADD COLUMN prizeLimitSeconds INTEGER NOT NULL DEFAULT 120",
            )
        }
    }

/**
 * Adds the parent prize cadence and the finished-activity count.
 * Existing rows open a prize every 10 activities and start from zero.
 * No rows are deleted.
 */
val MIGRATION_3_4: Migration =
    object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE profiles ADD COLUMN prizeEvery INTEGER NOT NULL DEFAULT 10",
            )
            db.execSQL(
                "ALTER TABLE profiles ADD COLUMN activitiesCompleted INTEGER NOT NULL DEFAULT 0",
            )
        }
    }
