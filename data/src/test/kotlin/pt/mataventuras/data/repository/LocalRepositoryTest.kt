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
import pt.mataventuras.data.pin.encryptedPinPreferences
import pt.mataventuras.data.pin.pinPreferences
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
        assertEquals(27, repository.addPoints(id, 15)!!.points)
        assertEquals(27, repository.addPoints(id, 0)!!.points)
        assertEquals(0, repository.addPoints(id, -100)!!.points)
        assertEquals(null, repository.addPoints(9_999, 5))
        repository.saveSession(
            LearningSession(0, id, LearningModule.COUNTING, 5, 1, 2_000, 1_000),
        )
        assertEquals(1, repository.allSessions().size)
        assertEquals(1, repository.sessionsFor(id).size)
        assertEquals(0, repository.sessionsFor(9_999).size)
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
        val pins =
            PinRepository(
                ApplicationProvider.getApplicationContext(),
                storeName = "parent_pin_repo_test",
                allowPlaintextFallback = true,
            )
        pins.clear()
        assertEquals(false, pins.isSet())
        val state = PinPolicy(iterations = 1_000).create("2468")
        pins.save(state)
        assertEquals(true, pins.isSet())
        assertEquals(state.hashHex, pins.read()!!.hashHex)
        pins.seedPartial("aa", saltHex = null, failureCount = null, lockoutMs = null)
        assertEquals(null, pins.read())
        assertEquals(false, pins.isSet())
        pins.seedPartial("aa", saltHex = "bb", failureCount = null, lockoutMs = null)
        val partial = pins.read()!!
        assertEquals(0, partial.consecutiveFailures)
        assertEquals(0L, partial.lockedUntilEpochMs)
        pins.seedPartial("cc", saltHex = "dd", failureCount = 2, lockoutMs = 9)
        val filled = pins.read()!!
        assertEquals(2, filled.consecutiveFailures)
        assertEquals(9L, filled.lockedUntilEpochMs)
        val defaults =
            PinRepository(
                ApplicationProvider.getApplicationContext(),
                allowPlaintextFallback = true,
            )
        defaults.clear()
        assertEquals(false, defaults.isSet())
        pins.save(state)
        val bumped =
            pins.update { current ->
                "ok" to current!!.copy(consecutiveFailures = current.consecutiveFailures + 1)
            }
        assertEquals("ok", bumped)
        assertEquals(1, pins.read()!!.consecutiveFailures)
        val skipped = pins.update { "keep" to null }
        assertEquals("keep", skipped)
        assertEquals(1, pins.read()!!.consecutiveFailures)
        pins.seedPartial("AA", saltHex = "BB", failureCount = 1, lockoutMs = 2)
        assertEquals("AA", pins.read()!!.hashHex)
        assertEquals("BB", pins.read()!!.saltHex)
        pins.seedPartial("09afAF", saltHex = "0a", failureCount = 0, lockoutMs = 0)
        assertEquals("09afAF", pins.read()!!.hashHex)
        assertEquals("0a", pins.read()!!.saltHex)
        pins.seedPartial("a", saltHex = "bb", failureCount = 0, lockoutMs = 0)
        try {
            pins.read()
            throw AssertionError("odd PIN hex must fail closed")
        } catch (error: IllegalStateException) {
            assertEquals("corrupt PIN record", error.message)
        }
        pins.seedPartial("abc", saltHex = "bb", failureCount = 0, lockoutMs = 0)
        try {
            pins.read()
            throw AssertionError("uneven PIN hex must fail closed")
        } catch (error: IllegalStateException) {
            assertEquals("corrupt PIN record", error.message)
        }
        pins.seedPartial("", saltHex = "bb", failureCount = 0, lockoutMs = 0)
        try {
            pins.read()
            throw AssertionError("blank PIN hex must fail closed")
        } catch (error: IllegalStateException) {
            assertEquals("corrupt PIN record", error.message)
        }
        pins.seedPartial("aa", saltHex = "zz", failureCount = 0, lockoutMs = 0)
        try {
            pins.read()
            throw AssertionError("corrupt salt must fail closed")
        } catch (error: IllegalStateException) {
            assertEquals("corrupt PIN record", error.message)
        }
        pins.seedPartial("zz", saltHex = "bb", failureCount = 0, lockoutMs = 0)
        try {
            pins.read()
            throw AssertionError("corrupt PIN must fail closed")
        } catch (error: IllegalStateException) {
            assertEquals("corrupt PIN record", error.message)
        }
        assertEquals(true, pins.isSet())
        pins.clear()
        assertEquals(false, pins.isSet())
        val none = pins.update { "none" to null }
        assertEquals("none", none)
        assertEquals(null, pins.read())
        val created = pins.update { "created" to state }
        assertEquals("created", created)
        assertEquals(true, pins.isSet())
        assertEquals(state.hashHex, pins.read()!!.hashHex)
    }

    @Test
    fun pinPreferencesFallsBackWhenEncryptedStoreFails() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val prefs =
            pinPreferences(
                ctx,
                "pin_fallback_test",
                allowPlaintextFallback = true,
                encrypted = { _, _ -> error("no keystore") },
            )
        prefs.edit().putString("hash", "aa").commit()
        assertEquals("aa", prefs.getString("hash", null))
        val opened =
            pinPreferences(
                ctx,
                "pin_enc_ok_test",
                encrypted = { c, n -> c.getSharedPreferences("enc_$n", android.content.Context.MODE_PRIVATE) },
                fallback = { _, _ -> error("should not fallback") },
            )
        opened.edit().putString("hash", "bb").commit()
        assertEquals("bb", opened.getString("hash", null))
        try {
            encryptedPinPreferences(ctx, "pin_keystore_probe")
        } catch (_: Exception) {
            // Robolectric has no Android Keystore; production uses this path.
        }
        try {
            pinPreferences(ctx, "pin_fail_closed", encrypted = { _, _ -> error("no keystore") })
            throw AssertionError("device path must fail closed")
        } catch (error: IllegalStateException) {
            assertEquals("no keystore", error.message)
        }
    }

    @Test
    fun pinRepositoryFailsClosedWhenCommitRejected() =
        runTest {
            val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
            val inner = ctx.getSharedPreferences("pin_fail_commit", android.content.Context.MODE_PRIVATE)
            val pins =
                PinRepository(
                    ctx,
                    storeName = "pin_fail_commit_unused",
                    allowPlaintextFallback = true,
                    prefs = FailCommitPreferences(inner),
                )
            val state = PinPolicy(iterations = 1_000).create("2468")
            try {
                pins.save(state)
                throw AssertionError("save must fail closed")
            } catch (error: IllegalStateException) {
                assertEquals("PIN persist failed", error.message)
            }
            try {
                pins.clear()
                throw AssertionError("clear must fail closed")
            } catch (error: IllegalStateException) {
                assertEquals("PIN clear failed", error.message)
            }
            try {
                pins.seedPartial("aa", saltHex = "bb", failureCount = 0, lockoutMs = 0)
                throw AssertionError("seed must fail closed")
            } catch (error: IllegalStateException) {
                assertEquals("PIN seed failed", error.message)
            }
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

private class FailCommitPreferences(
    private val inner: android.content.SharedPreferences,
) : android.content.SharedPreferences by inner {
    override fun edit(): android.content.SharedPreferences.Editor = FailCommitEditor(inner.edit())
}

private class FailCommitEditor(
    private val inner: android.content.SharedPreferences.Editor,
) : android.content.SharedPreferences.Editor {
    override fun putString(
        key: String?,
        value: String?,
    ): android.content.SharedPreferences.Editor {
        inner.putString(key, value)
        return this
    }

    override fun putStringSet(
        key: String?,
        values: MutableSet<String>?,
    ): android.content.SharedPreferences.Editor {
        inner.putStringSet(key, values)
        return this
    }

    override fun putInt(
        key: String?,
        value: Int,
    ): android.content.SharedPreferences.Editor {
        inner.putInt(key, value)
        return this
    }

    override fun putLong(
        key: String?,
        value: Long,
    ): android.content.SharedPreferences.Editor {
        inner.putLong(key, value)
        return this
    }

    override fun putFloat(
        key: String?,
        value: Float,
    ): android.content.SharedPreferences.Editor {
        inner.putFloat(key, value)
        return this
    }

    override fun putBoolean(
        key: String?,
        value: Boolean,
    ): android.content.SharedPreferences.Editor {
        inner.putBoolean(key, value)
        return this
    }

    override fun remove(key: String?): android.content.SharedPreferences.Editor {
        inner.remove(key)
        return this
    }

    override fun clear(): android.content.SharedPreferences.Editor {
        inner.clear()
        return this
    }

    override fun commit(): Boolean = false

    override fun apply() {
        inner.apply()
    }
}
