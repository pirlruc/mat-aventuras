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

    private companion object {
        const val NAME: String = "age-band-migration"
    }
}
