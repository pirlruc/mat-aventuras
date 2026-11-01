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
    private const val GCM_TAG_BITS: Int = 128
    private const val IV_BYTES: Int = 12

    override fun seal(plain: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val iv = cipher.iv
        val body = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        val packed = ByteArray(iv.size + body.size)
        iv.copyInto(packed)
        body.copyInto(packed, destinationOffset = iv.size)
        return "gcm:" + Base64.encodeToString(packed, Base64.NO_WRAP)
    }

    override fun open(sealed: String): String {
        check(sealed.startsWith("gcm:")) { "corrupt PIN record" }
        val packed = Base64.decode(sealed.removePrefix("gcm:"), Base64.NO_WRAP)
        check(packed.size > IV_BYTES) { "corrupt PIN record" }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            key(),
            GCMParameterSpec(GCM_TAG_BITS, packed, 0, IV_BYTES),
        )
        return cipher.doFinal(packed, IV_BYTES, packed.size - IV_BYTES).toString(Charsets.UTF_8)
    }

    /**
     * Creates the Keystore key when it is missing. Throws when the device
     * has no Android Keystore, which is the fail-closed path.
     */
    fun ensureKey() {
        key()
    }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore")
        store.load(null)
        val existing = store.getKey(ALIAS, null) as? SecretKey
        if (existing != null) return existing
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val spec =
            KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        generator.init(spec)
        return generator.generateKey()
    }
}
