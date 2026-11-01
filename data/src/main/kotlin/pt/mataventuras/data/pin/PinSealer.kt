package pt.mataventuras.data.pin

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Seals the PIN record. Production uses a Keystore AES-GCM key. Robolectric
 * tests opt into [PlainPinSealer].
 */
internal interface PinSealer {
    fun seal(plain: String): String

    fun open(sealed: String): String
}

internal object PlainPinSealer : PinSealer {
    override fun seal(plain: String): String = "plain:$plain"

    override fun open(sealed: String): String {
        check(sealed.startsWith("plain:")) { "corrupt PIN record" }
        return sealed.removePrefix("plain:")
    }
}

/**
 * AES-GCM under an Android Keystore key that never leaves the device.
 */
internal object KeystorePinSealer : PinSealer {
    private const val ALIAS: String = "mat_aventuras_pin_aes"

    override fun seal(plain: String): String = sealWith(key(), plain)

    override fun open(sealed: String): String = openWith(key(), sealed)

    /**
     * Creates the Keystore key when it is missing. Throws when the device
     * has no Android Keystore, which is the fail-closed path.
     */
    fun ensureKey() {
        key()
    }

    private fun key(): SecretKey = secretKey(existingKey(), ::newKey)

    private fun existingKey(): SecretKey? {
        val store = KeyStore.getInstance("AndroidKeyStore")
        store.load(null)
        return store.getKey(ALIAS, null) as? SecretKey
    }

    private fun newKey(): SecretKey {
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(keySpec())
        return generator.generateKey()
    }
}

internal fun sealWith(
    key: SecretKey,
    plain: String,
): String {
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, key)
    val iv = cipher.iv
    val body = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
    val packed = ByteArray(iv.size + body.size)
    iv.copyInto(packed)
    body.copyInto(packed, destinationOffset = iv.size)
    return "gcm:" + Base64.encodeToString(packed, Base64.NO_WRAP)
}

internal fun openWith(
    key: SecretKey,
    sealed: String,
): String {
    check(sealed.startsWith("gcm:")) { "corrupt PIN record" }
    val packed = Base64.decode(sealed.removePrefix("gcm:"), Base64.NO_WRAP)
    check(packed.size > 12) { "corrupt PIN record" }
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(
        Cipher.DECRYPT_MODE,
        key,
        GCMParameterSpec(128, packed, 0, 12),
    )
    return cipher.doFinal(packed, 12, packed.size - 12).toString(Charsets.UTF_8)
}

internal fun secretKey(
    existing: SecretKey?,
    generate: () -> SecretKey,
): SecretKey = existing ?: generate()

internal fun keySpec(): KeyGenParameterSpec =
    KeyGenParameterSpec
        .Builder(
            "mat_aventuras_pin_aes",
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setKeySize(256)
        .build()
