package pt.mataventuras.data.pin

import android.content.Context
import android.content.SharedPreferences
import java.io.IOException
import java.security.GeneralSecurityException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import pt.mataventuras.domain.parent.PinPolicy
import pt.mataventuras.domain.parent.PinState

/**
 * Parental PIN hash sealed with a Keystore AES-GCM key. Plaintext PIN digits
 * are never persisted. An EncryptedSharedPreferences record is copied into
 * that key on the next save (the unlock). Keystore failures fail closed;
 * Robolectric tests opt into a distinct private prefs file via
 * [allowPlaintextFallback].
 */
class PinRepository(
    context: Context,
    private val storeName: String = "parent_pin",
    allowPlaintextFallback: Boolean = false,
    private val prefs: SharedPreferences =
        pinPreferences(
            context.applicationContext,
            storeName,
            allowPlaintextFallback = allowPlaintextFallback,
        ),
) {
    private val appContext = context.applicationContext
    private val plaintext = allowPlaintextFallback
    private val sealer: PinSealer = if (plaintext) PlainPinSealer else KeystorePinSealer
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
            withContext(Dispatchers.IO) {
                prefs.contains(KEY_RECORD) ||
                    (prefs.contains(KEY_HASH) && prefs.contains(KEY_SALT)) ||
                    LegacyEncryptedPin.hasKeyset(prefs)
            }
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
        val payload =
            listOf(
                state.hashHex,
                state.saltHex,
                state.consecutiveFailures.toString(),
                state.lockedUntilEpochMs.toString(),
                state.iterations.toString(),
            ).joinToString(separator = "\n")
        val ok =
            prefs.edit()
                .putString(KEY_RECORD, sealer.seal(payload))
                .remove(KEY_HASH)
                .remove(KEY_SALT)
                .remove(KEY_FAILURES)
                .remove(KEY_LOCKOUT)
                .remove(KEY_ITERATIONS)
                .commit()
        check(ok) { "PIN persist failed" }
    }

    private fun readLocked(): PinState? = readRecord() ?: readPlainLegacy() ?: readEncryptedLegacy()

    private fun readRecord(): PinState? {
        val record = prefs.getString(KEY_RECORD, null) ?: return null
        return decodeRecord(sealer.open(record))
    }

    private fun readPlainLegacy(): PinState? {
        val hashValue = prefs.getString(KEY_HASH, null) ?: return null
        val saltValue = prefs.getString(KEY_SALT, null) ?: return null
        check(isHex(hashValue) && isHex(saltValue)) { "corrupt PIN record" }
        return PinState(
            hashHex = hashValue,
            saltHex = saltValue,
            consecutiveFailures = prefs.getInt(KEY_FAILURES, 0),
            lockedUntilEpochMs = prefs.getLong(KEY_LOCKOUT, 0L),
            iterations = prefs.getInt(KEY_ITERATIONS, PinPolicy.LEGACY_ITERATIONS),
        )
    }

    private fun readEncryptedLegacy(): PinState? {
        if (plaintext || !LegacyEncryptedPin.hasKeyset(prefs)) return null
        return LegacyEncryptedPin.read(appContext, storeName)
    }
}

private fun isHex(value: String): Boolean =
    value.length >= 2 && value.length % 2 == 0 && value.all { it.digitToIntOrNull(radix = 16) != null }

private const val KEY_RECORD: String = "record"
private const val KEY_HASH: String = "hash"
private const val KEY_SALT: String = "salt"
private const val KEY_FAILURES: String = "failures"
private const val KEY_LOCKOUT: String = "lockout"
private const val KEY_ITERATIONS: String = "iterations"

private fun decodeRecord(payload: String): PinState {
    val lines = payload.split('\n')
    check(lines.size >= 4 && isHex(lines[0]) && isHex(lines[1])) { "corrupt PIN record" }
    return PinState(
        hashHex = lines[0],
        saltHex = lines[1],
        consecutiveFailures = lines[2].toInt(),
        lockedUntilEpochMs = lines[3].toLong(),
        iterations = lines.getOrNull(4)?.toIntOrNull() ?: PinPolicy.LEGACY_ITERATIONS,
    )
}

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
    } catch (error: GeneralSecurityException) {
        if (!allowPlaintextFallback) throw error
        fallback(context, storeName)
    } catch (error: IllegalStateException) {
        if (!allowPlaintextFallback) throw error
        fallback(context, storeName)
    } catch (error: IOException) {
        if (!allowPlaintextFallback) throw error
        fallback(context, storeName)
    }

internal fun encryptedPinPreferences(
    context: Context,
    storeName: String,
): SharedPreferences {
    KeystorePinSealer.ensureKey()
    return context.getSharedPreferences(storeName, Context.MODE_PRIVATE)
}
