package pt.mataventuras.data.pin

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.google.crypto.tink.Aead
import com.google.crypto.tink.DeterministicAead
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.daead.DeterministicAeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import pt.mataventuras.domain.parent.PinPolicy
import pt.mataventuras.domain.parent.PinState

/**
 * Reads a PIN written by EncryptedSharedPreferences (security-crypto) so the
 * next unlock can copy it into the Keystore record. The library itself is
 * not a dependency; Tink opens the keysets that library stored.
 */
internal object LegacyEncryptedPin {
    private const val KEY_KEYSET: String = "__androidx_security_crypto_encrypted_prefs_key_keyset__"
    private const val VALUE_KEYSET: String = "__androidx_security_crypto_encrypted_prefs_value_keyset__"
    private const val MASTER: String = "android-keystore://_androidx_security_master_key_"
    private const val NULL_VALUE: String = "__NULL__"

    fun hasKeyset(prefs: SharedPreferences): Boolean = prefs.contains(KEY_KEYSET) && prefs.contains(VALUE_KEYSET)

    fun read(
        context: Context,
        fileName: String,
    ): PinState? {
        val prefs = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        if (!hasKeyset(prefs)) return null
        return stateFrom(decryptFields(context, fileName, prefs))
    }

    private fun decryptFields(
        context: Context,
        fileName: String,
        prefs: SharedPreferences,
    ): Map<String, Any?> {
        DeterministicAeadConfig.register()
        AeadConfig.register()
        val app = context.applicationContext
        val registry = RegistryConfiguration.get()
        val keys = keyset(app, KEY_KEYSET, fileName).getPrimitive(registry, DeterministicAead::class.java)
        val values = keyset(app, VALUE_KEYSET, fileName).getPrimitive(registry, Aead::class.java)
        return legacyFields(prefs, fileName, keys, values)
    }

    internal fun legacyFields(
        prefs: SharedPreferences,
        fileName: String,
        keys: DeterministicAead,
        values: Aead,
    ): Map<String, Any?> {
        val fields = HashMap<String, Any?>()
        for (entry in prefs.all) {
            val storedKey = entry.key
            val stored = entry.value as? String
            if (stored != null && storedKey != KEY_KEYSET && storedKey != VALUE_KEYSET) {
                fields[decryptKey(keys, fileName, storedKey)] = decryptValue(values, storedKey, stored)
            }
        }
        return fields
    }

    internal fun legacyState(fields: Map<String, Any?>): PinState? = stateFrom(fields)

    private fun stateFrom(fields: Map<String, Any?>): PinState? {
        val hash = fields["hash"] as? String ?: return null
        val salt = fields["salt"] as? String ?: return null
        check(isLegacyHex(hash) && isLegacyHex(salt)) { "corrupt PIN record" }
        return PinState(
            hashHex = hash,
            saltHex = salt,
            consecutiveFailures = fields["failures"] as? Int ?: 0,
            lockedUntilEpochMs = fields["lockout"] as? Long ?: 0L,
            iterations = fields["iterations"] as? Int ?: PinPolicy.LEGACY_ITERATIONS,
        )
    }

    private fun keyset(
        context: Context,
        alias: String,
        fileName: String,
    ) = AndroidKeysetManager
        .Builder()
        .withSharedPref(context, alias, fileName)
        .withMasterKeyUri(MASTER)
        .build()
        .keysetHandle

    private fun decryptKey(
        keys: DeterministicAead,
        fileName: String,
        encryptedKey: String,
    ): String {
        val clear =
            keys.decryptDeterministically(
                Base64.decode(encryptedKey, Base64.DEFAULT),
                fileName.toByteArray(StandardCharsets.UTF_8),
            )
        val key = String(clear, StandardCharsets.UTF_8)
        return if (key == NULL_VALUE) "" else key
    }

    private fun decryptValue(
        values: Aead,
        encryptedKey: String,
        encryptedValue: String,
    ): Any? {
        val clear =
            values.decrypt(
                Base64.decode(encryptedValue, Base64.DEFAULT),
                encryptedKey.toByteArray(StandardCharsets.UTF_8),
            )
        val buffer = ByteBuffer.wrap(clear)
        return when (buffer.int) {
            0 -> readString(buffer)
            2 -> buffer.int
            3 -> buffer.long
            else -> null
        }
    }

    private fun readString(buffer: ByteBuffer): String? {
        val length = buffer.int
        val slice = buffer.slice()
        slice.limit(length)
        val text = StandardCharsets.UTF_8.decode(slice).toString()
        return if (text == NULL_VALUE) null else text
    }
}

private fun isLegacyHex(value: String): Boolean =
    value.length >= 2 && value.length % 2 == 0 && value.all { it.digitToIntOrNull(radix = 16) != null }
