package pt.mataventuras.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Stored fixed ages become the 3–5 and 6–8 band names.
 */
@RunWith(RobolectricTestRunner::class)
class AgeBandMigrationTest {
    @get:Rule
    val helper =
        MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            MatAventurasDatabase::class.java,
        )

    @Test
    fun rewritesFixedAgeNames() {
        helper.createDatabase(NAME, 1).apply {
            execSQL(
                "INSERT INTO profiles (name, ageGroup, mascotCode, avatarId, points, createdAtEpochMs) " +
                    "VALUES ('Ana', 'THREE_YEARS', 'speedy_hedgehog', 'a', 0, 1)",
            )
            execSQL(
                "INSERT INTO profiles (name, ageGroup, mascotCode, avatarId, points, createdAtEpochMs) " +
                    "VALUES ('Bo', 'SEVEN_YEARS', 'hero_pup', 'b', 0, 1)",
            )
            close()
        }
        val migrated = helper.runMigrationsAndValidate(NAME, 2, true, MIGRATION_1_2)
        val ages = ArrayList<String>()
        migrated.query("SELECT ageGroup FROM profiles ORDER BY name").use { cursor ->
            while (cursor.moveToNext()) ages.add(cursor.getString(0))
        }
        assertEquals(listOf("THREE_TO_FIVE", "SIX_TO_EIGHT"), ages)
    }

    @Test
    fun addsPrizeLimitWithoutDeletingRows() {
        helper.createDatabase(PRIZE, 2).apply {
            execSQL(
                "INSERT INTO profiles (name, ageGroup, mascotCode, avatarId, points, createdAtEpochMs) " +
                    "VALUES ('Ana', 'THREE_TO_FIVE', 'speedy_hedgehog', 'a', 4, 1)",
            )
            close()
        }
        val migrated = helper.runMigrationsAndValidate(PRIZE, 3, true, MIGRATION_2_3)
        migrated.query("SELECT name, points, prizeLimitSeconds FROM profiles").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals("Ana", cursor.getString(0))
            assertEquals(4, cursor.getInt(1))
            assertEquals(120, cursor.getInt(2))
        }
    }

    @Test
    fun addsPrizeCadenceWithoutDeletingRows() {
        helper.createDatabase(CADENCE, 3).apply {
            execSQL(
                "INSERT INTO profiles (name, ageGroup, mascotCode, avatarId, points, " +
                    "createdAtEpochMs, prizeLimitSeconds) " +
                    "VALUES ('Ana', 'THREE_TO_FIVE', 'speedy_hedgehog', 'a', 4, 1, 60)",
            )
            close()
        }
        val migrated = helper.runMigrationsAndValidate(CADENCE, 4, true, MIGRATION_3_4)
        val columns = "name, points, prizeLimitSeconds, prizeEvery, activitiesCompleted"
        migrated.query("SELECT $columns FROM profiles").use { cursor ->
            assertEquals(true, cursor.moveToFirst())
            assertEquals("Ana", cursor.getString(0))
            assertEquals(4, cursor.getInt(1))
            assertEquals(60, cursor.getInt(2))
            assertEquals(10, cursor.getInt(3))
            assertEquals(0, cursor.getInt(4))
        }
    }

    private companion object {
        const val NAME: String = "age-band-migration"
        const val PRIZE: String = "prize-limit-migration"
        const val CADENCE: String = "prize-cadence-migration"
    }
}
