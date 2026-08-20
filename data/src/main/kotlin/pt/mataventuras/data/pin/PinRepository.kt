package pt.mataventuras.data.pin

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import pt.mataventuras.domain.parent.PinState

/**
 * Parental PIN hash in DataStore. Plaintext is never persisted.
 */
class PinRepository(
    context: Context,
    storeName: String = "parent_pin",
) {
    private val dataStore: DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.applicationContext.preferencesDataStoreFile(storeName) },
        )
    private val hash = stringPreferencesKey("hash")
    private val salt = stringPreferencesKey("salt")
    private val failures = intPreferencesKey("failures")
    private val lockout = longPreferencesKey("lockout")

    /**
     * Stored PIN, or null when none has been set.
     */
    suspend fun read(): PinState? = dataStore.data.map { prefs ->
        val hashValue = prefs[hash] ?: return@map null
        val saltValue = prefs[salt] ?: return@map null
        PinState(
            hashHex = hashValue,
            saltHex = saltValue,
            consecutiveFailures = prefs[failures] ?: 0,
            lockedUntilEpochMs = prefs[lockout] ?: 0L,
        )
    }.first()

    /**
     * Writes hash, salt, failure count, and lockout timestamp.
     */
    suspend fun save(state: PinState) {
        dataStore.edit { prefs ->
            prefs[hash] = state.hashHex
            prefs[salt] = state.saltHex
            prefs[failures] = state.consecutiveFailures
            prefs[lockout] = state.lockedUntilEpochMs
        }
    }

    /**
     * True when a PIN hash is already stored.
     */
    suspend fun isSet(): Boolean = read() != null

    /**
     * Drops stored PIN state (tests and factory reset).
     */
    suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}
