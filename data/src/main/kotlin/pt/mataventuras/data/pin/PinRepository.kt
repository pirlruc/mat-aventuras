package pt.mataventuras.data.pin

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import pt.mataventuras.domain.parent.PinState

/**
 * Parental PIN hash in EncryptedSharedPreferences. Plaintext PIN digits are
 * never persisted. Device Keystore failures fail closed; Robolectric tests
 * opt into a distinct private prefs file via [allowPlaintextFallback].
 */
class PinRepository(
    context: Context,
    storeName: String = "parent_pin",
    allowPlaintextFallback: Boolean = false,
    private val prefs: SharedPreferences =
        pinPreferences(
            context.applicationContext,
            storeName,
            allowPlaintextFallback = allowPlaintextFallback,
        ),
) {
    private val lock = Mutex()

    /**
     * Stored PIN, or null when none has been set.
     */
    suspend fun read(): PinState? =
        lock.withLock {
            withContext(Dispatchers.IO) { readLocked() }
        }

    /**
     * Writes hash, salt, failure count, and lockout timestamp.
     */
    suspend fun save(state: PinState) {
        lock.withLock {
            withContext(Dispatchers.IO) { persistLocked(state) }
        }
    }

    /**
     * Holds the store lock across read-modify-write so concurrent unlocks
     * cannot skip lockout by racing on the same failure count.
     */
    suspend fun <T> update(transform: (PinState?) -> Pair<T, PinState?>): T =
        lock.withLock {
            withContext(Dispatchers.IO) {
                val current = readLocked()
                val (result, next) = transform(current)
                if (next != null) persistLocked(next)
                result
            }
        }

    /**
     * True when a PIN hash is already stored.
     */
    suspend fun isSet(): Boolean =
        lock.withLock {
            withContext(Dispatchers.IO) { prefs.contains(KEY_HASH) && prefs.contains(KEY_SALT) }
        }

    /**
     * Drops stored PIN state (tests and factory reset).
     */
    suspend fun clear() {
        lock.withLock {
            withContext(Dispatchers.IO) {
                check(prefs.edit().clear().commit()) { "PIN clear failed" }
            }
        }
    }

    /**
     * Test helper: writes a partial PIN record to cover missing-key branches.
     */
    internal suspend fun seedPartial(
        hashHex: String,
        saltHex: String?,
        failureCount: Int?,
        lockoutMs: Long?,
    ) {
        lock.withLock {
            withContext(Dispatchers.IO) {
                val editor = prefs.edit().clear().putString(KEY_HASH, hashHex)
                if (saltHex != null) editor.putString(KEY_SALT, saltHex)
                if (failureCount != null) editor.putInt(KEY_FAILURES, failureCount)
                if (lockoutMs != null) editor.putLong(KEY_LOCKOUT, lockoutMs)
                check(editor.commit()) { "PIN seed failed" }
            }
        }
    }

    private fun persistLocked(state: PinState) {
        val ok =
            prefs.edit()
                .putString(KEY_HASH, state.hashHex)
                .putString(KEY_SALT, state.saltHex)
                .putInt(KEY_FAILURES, state.consecutiveFailures)
                .putLong(KEY_LOCKOUT, state.lockedUntilEpochMs)
                .commit()
        check(ok) { "PIN persist failed" }
    }

    private fun readLocked(): PinState? {
        val hashValue = prefs.getString(KEY_HASH, null) ?: return null
        val saltValue = prefs.getString(KEY_SALT, null) ?: return null
        check(isHex(hashValue) && isHex(saltValue)) { "corrupt PIN record" }
        return PinState(
            hashHex = hashValue,
            saltHex = saltValue,
            consecutiveFailures = prefs.getInt(KEY_FAILURES, 0),
            lockedUntilEpochMs = prefs.getLong(KEY_LOCKOUT, 0L),
        )
    }
}

private fun isHex(value: String): Boolean =
    value.length >= 2 &&
        value.length % 2 == 0 &&
        value.all { ch -> ch in '0'..'9' || ch in 'a'..'f' || ch in 'A'..'F' }

private const val KEY_HASH: String = "hash"
private const val KEY_SALT: String = "salt"
private const val KEY_FAILURES: String = "failures"
private const val KEY_LOCKOUT: String = "lockout"

/**
 * Encrypted store. Plaintext fallback is opt-in (Robolectric) and uses a
 * distinct filename so an encrypted prefs XML is never mixed with cleartext.
 */
internal fun pinPreferences(
    context: Context,
    storeName: String,
    allowPlaintextFallback: Boolean = false,
    encrypted: (Context, String) -> SharedPreferences = ::encryptedPinPreferences,
    fallback: (Context, String) -> SharedPreferences = { ctx, name ->
        ctx.getSharedPreferences("${name}_plain", Context.MODE_PRIVATE)
    },
): SharedPreferences =
    try {
        encrypted(context, storeName)
    } catch (error: Exception) {
        if (!allowPlaintextFallback) throw error
        fallback(context, storeName)
    }

internal fun encryptedPinPreferences(
    context: Context,
    storeName: String,
): SharedPreferences {
    val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    return EncryptedSharedPreferences.create(
        storeName,
        masterKey,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )
}
