package pt.mataventuras.dados.pin

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
import pt.mataventuras.dominio.pais.EstadoPin

private val Context.pinDataStore: DataStore<Preferences> by preferencesDataStore(name = "pin_pais")

class RepositorioPin(
    private val contexto: Context,
) {
    private val hash = stringPreferencesKey("hash")
    private val sal = stringPreferencesKey("sal")
    private val falhas = intPreferencesKey("falhas")
    private val bloqueio = longPreferencesKey("bloqueio")

    suspend fun ler(): EstadoPin? = contexto.pinDataStore.data.map { prefs ->
        val hashValor = prefs[hash] ?: return@map null
        val salValor = prefs[sal] ?: return@map null
        EstadoPin(
            hashHex = hashValor,
            salHex = salValor,
            falhasSeguidas = prefs[falhas] ?: 0,
            bloqueadoAteEpochMs = prefs[bloqueio] ?: 0L,
        )
    }.first()

    suspend fun guardar(estado: EstadoPin) {
        contexto.pinDataStore.edit { prefs ->
            prefs[hash] = estado.hashHex
            prefs[sal] = estado.salHex
            prefs[falhas] = estado.falhasSeguidas
            prefs[bloqueio] = estado.bloqueadoAteEpochMs
        }
    }

    suspend fun definido(): Boolean = ler() != null
}
