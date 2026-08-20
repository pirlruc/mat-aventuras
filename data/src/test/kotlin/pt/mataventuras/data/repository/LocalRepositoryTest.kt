package pt.mataventuras.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pt.mataventuras.data.local.MatAventurasDatabase
import pt.mataventuras.data.pin.PinRepository
import pt.mataventuras.domain.model.AgeGroup
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.model.LearningSession
import pt.mataventuras.domain.model.Mascot
import pt.mataventuras.domain.parent.PinPolicy
import pt.mataventuras.domain.progress.AvatarCode

@RunWith(RobolectricTestRunner::class)
class LocalRepositoryTest {
    private lateinit var database: MatAventurasDatabase
    private lateinit var repository: LocalRepository

    @Before
    fun setUp() {
        database =
            Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                MatAventurasDatabase::class.java,
            ).allowMainThreadQueries().build()
        repository = LocalRepository(database, now = { 1_000L })
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun createProfilePersistsStarterAvatarAndSessions() = runTest {
        val id = repository.createProfile(" Ana ", AgeGroup.THREE_YEARS, Mascot.HERO_PUP)
        val profile = repository.getProfile(id)!!
        assertEquals("Ana", profile.name)
        assertEquals(AvatarCode.STARTER.name, profile.avatarId)
        repository.updateProfile(profile.copy(points = 12))
        assertEquals(12, repository.getProfile(id)!!.points)
        repository.saveSession(
            LearningSession(0, id, LearningModule.COUNTING, 5, 1, 2_000, 1_000),
        )
        assertEquals(1, repository.allSessions().size)
        assertEquals(1, repository.observeSessions(id).first().size)
        repository.unlockBadge(id, "FIRST_STEPS")
        repository.unlockAvatar(id, AvatarCode.RUNNER.name)
        assertTrue(repository.badgeCodes(id).contains("FIRST_STEPS"))
        assertTrue(repository.avatarIds(id).contains(AvatarCode.RUNNER.name))
        assertEquals(1, repository.observeBadges(id).first().size)
        assertEquals(2, repository.observeAvatars(id).first().size)
        assertEquals(1, repository.observeProfiles().first().size)
        assertEquals(null, repository.getProfile(9_999))
        assertEquals("Ana", repository.latestProfile()!!.name)
        val emptyDb =
            Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                MatAventurasDatabase::class.java,
            ).allowMainThreadQueries().build()
        val emptyRepo = LocalRepository(emptyDb, now = { 1L })
        assertEquals(null, emptyRepo.latestProfile())
        emptyDb.close()
    }

    @Test
    fun pinRepositoryRoundTrips() = runTest {
        val pins = PinRepository(ApplicationProvider.getApplicationContext(), storeName = "parent_pin_repo_test")
        pins.clear()
        assertEquals(false, pins.isSet())
        val state = PinPolicy(iterations = 1_000).create("2468")
        pins.save(state)
        assertEquals(true, pins.isSet())
        assertEquals(state.hashHex, pins.read()!!.hashHex)
        pins.seedPartial("aa", saltHex = null, failureCount = null, lockoutMs = null)
        assertEquals(null, pins.read())
        pins.seedPartial("aa", saltHex = "bb", failureCount = null, lockoutMs = null)
        val partial = pins.read()!!
        assertEquals(0, partial.consecutiveFailures)
        assertEquals(0L, partial.lockedUntilEpochMs)
        pins.seedPartial("cc", saltHex = "dd", failureCount = 2, lockoutMs = 9)
        val filled = pins.read()!!
        assertEquals(2, filled.consecutiveFailures)
        assertEquals(9L, filled.lockedUntilEpochMs)
        val defaults = PinRepository(ApplicationProvider.getApplicationContext())
        defaults.clear()
        assertEquals(false, defaults.isSet())
        pins.clear()
        assertEquals(false, pins.isSet())
    }

    @Test
    fun lastProfileStoreRoundTrips() = runTest {
        val store =
            pt.mataventuras.data.session.LastProfileStore(
                ApplicationProvider.getApplicationContext(),
                storeName = "last_profile_repo_test",
            )
        store.clear()
        assertEquals(null, store.read())
        store.save(7)
        assertEquals(7L, store.read())
        store.clear()
        assertEquals(null, store.read())
        val defaults = pt.mataventuras.data.session.LastProfileStore(ApplicationProvider.getApplicationContext())
        defaults.clear()
        assertEquals(null, defaults.read())
    }
}
