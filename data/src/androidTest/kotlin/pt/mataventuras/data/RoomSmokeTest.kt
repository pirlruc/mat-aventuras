package pt.mataventuras.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import pt.mataventuras.data.local.MatAventurasDatabase
import pt.mataventuras.data.repository.LocalRepository
import pt.mataventuras.domain.model.AgeGroup
import pt.mataventuras.domain.model.Mascot

/**
 * Opens Room on a device and writes one profile.
 */
@RunWith(AndroidJUnit4::class)
class RoomSmokeTest {
    @Test
    fun createsProfile() =
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val database =
                Room.inMemoryDatabaseBuilder(context, MatAventurasDatabase::class.java)
                    .allowMainThreadQueries()
                    .build()
            try {
                val id =
                    LocalRepository(database).createProfile(
                        "Ana",
                        AgeGroup.THREE_YEARS,
                        Mascot.SPEEDY_HEDGEHOG,
                    )
                assertTrue(id > 0L)
            } finally {
                database.close()
            }
        }
}
