package pt.mataventuras.data.pin

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import pt.mataventuras.domain.parent.PinState

private val Context.pinDataStore: DataStore<Preferences> by preferencesDataStore(name = "parent_pin")

class PinRepository(
    private val context: Context,
) {
    private val hash = stringPreferencesKey("hash")
    private val salt = stringPreferencesKey("salt")
    private val failures = intPreferencesKey("failures")
    private val lockout = longPreferencesKey("lockout")

    suspend fun read(): PinState? = context.pinDataStore.data.map { prefs ->
        val hashValue = prefs[hash] ?: return@map null
        val saltValue = prefs[salt] ?: return@map null
        PinState(
            hashHex = hashValue,
            saltHex = saltValue,
            consecutiveFailures = prefs[failures] ?: 0,
            lockedUntilEpochMs = prefs[lockout] ?: 0L,
        )
    }.first()

    suspend fun save(state: PinState) {
        context.pinDataStore.edit { prefs ->
            prefs[hash] = state.hashHex
            prefs[salt] = state.saltHex
            prefs[failures] = state.consecutiveFailures
            prefs[lockout] = state.lockedUntilEpochMs
        }
    }

    suspend fun isSet(): Boolean = read() != null
}
