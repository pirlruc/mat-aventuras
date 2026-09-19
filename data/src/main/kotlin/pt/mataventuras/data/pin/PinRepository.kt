package pt.mataventuras.data.pin

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import pt.mataventuras.domain.parent.PinState

/**
 * Parental PIN hash in encrypted prefs when Android Keystore is available.
 * Plaintext PIN digits are never persisted. Robolectric (no Keystore) falls
 * back to process-private SharedPreferences so unit tests still round-trip.
 */
class PinRepository(
    context: Context,
    storeName: String = "parent_pin",
    private val prefs: SharedPreferences = pinPreferences(context.applicationContext, storeName),
) {
    /**
     * Stored PIN, or null when none has been set.
     */
    suspend fun read(): PinState? {
        val hashValue = prefs.getString(KEY_HASH, null) ?: return null
        val saltValue = prefs.getString(KEY_SALT, null) ?: return null
        return PinState(
            hashHex = hashValue,
            saltHex = saltValue,
            consecutiveFailures = prefs.getInt(KEY_FAILURES, 0),
            lockedUntilEpochMs = prefs.getLong(KEY_LOCKOUT, 0L),
        )
    }

    /**
     * Writes hash, salt, failure count, and lockout timestamp.
     */
    suspend fun save(state: PinState) {
        prefs.edit()
            .putString(KEY_HASH, state.hashHex)
            .putString(KEY_SALT, state.saltHex)
            .putInt(KEY_FAILURES, state.consecutiveFailures)
            .putLong(KEY_LOCKOUT, state.lockedUntilEpochMs)
            .commit()
    }

    /**
     * True when a PIN hash is already stored.
     */
    suspend fun isSet(): Boolean = read() != null

    /**
     * Drops stored PIN state (tests and factory reset).
     */
    suspend fun clear() {
        prefs.edit().clear().commit()
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
        val editor = prefs.edit().clear().putString(KEY_HASH, hashHex)
        if (saltHex != null) editor.putString(KEY_SALT, saltHex)
        if (failureCount != null) editor.putInt(KEY_FAILURES, failureCount)
        if (lockoutMs != null) editor.putLong(KEY_LOCKOUT, lockoutMs)
        editor.commit()
    }
}

private const val KEY_HASH: String = "hash"
private const val KEY_SALT: String = "salt"
private const val KEY_FAILURES: String = "failures"
private const val KEY_LOCKOUT: String = "lockout"

/**
 * Encrypted store, or a private prefs file when Keystore cannot open.
 */
internal fun pinPreferences(
    context: Context,
    storeName: String,
    encrypted: (Context, String) -> SharedPreferences = ::encryptedPinPreferences,
    fallback: (Context, String) -> SharedPreferences = { ctx, name ->
        ctx.getSharedPreferences(name, Context.MODE_PRIVATE)
    },
): SharedPreferences =
    try {
        encrypted(context, storeName)
    } catch (_: Exception) {
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
