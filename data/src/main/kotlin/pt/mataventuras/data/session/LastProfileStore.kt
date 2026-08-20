package pt.mataventuras.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Remembers the last child profile opened on this device. No network.
 */
class LastProfileStore(
    context: Context,
    storeName: String = "last_profile",
) {
    private val dataStore: DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.applicationContext.preferencesDataStoreFile(storeName) },
        )
    private val profileId = longPreferencesKey("profile_id")

    /**
     * Stored profile id, or null when none has been saved.
     */
    suspend fun read(): Long? = dataStore.data.map { prefs -> prefs[profileId] }.first()

    /**
     * Remembers [id] as the profile to offer on the next launch.
     */
    suspend fun save(id: Long) {
        dataStore.edit { prefs -> prefs[profileId] = id }
    }

    /**
     * Drops the stored id (tests and factory reset).
     */
    suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}
