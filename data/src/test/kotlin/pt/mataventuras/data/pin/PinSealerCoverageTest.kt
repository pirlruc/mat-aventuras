package pt.mataventuras.data.pin

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.test.core.app.ApplicationProvider
import com.google.crypto.tink.Aead
import com.google.crypto.tink.DeterministicAead
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pt.mataventuras.domain.parent.PinPolicy

@RunWith(RobolectricTestRunner::class)
class PinSealerCoverageTest {
    private val key = SecretKeySpec(ByteArray(32) { 4 }, "AES")

    @Test
    fun gcmRoundTripRejectsAShortRecord() {
        val sealed = sealWith(key, "secret")
        assertEquals("secret", openWith(key, sealed))
        assertEquals(key, secretKey(key) { error("present") })
        val generated = SecretKeySpec(ByteArray(16) { 1 }, "AES")
        assertEquals(generated, secretKey(null) { generated })
        assertEquals(256, keySpec().keySize)
        assertFails { openWith(key, "plain:secret") }
        val short = Base64.encodeToString(ByteArray(4), Base64.NO_WRAP)
        assertFails { openWith(key, "gcm:$short") }
        assertFails { PlainPinSealer.open("gcm:secret") }
        assertFails { KeystorePinSealer.ensureKey() }
    }

    @Test
    fun plaintextRecordCoversMissingIterations() =
        runTest {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val repository = PinRepository(context, "pin_plain", allowPlaintextFallback = true)
            val prefs = context.getSharedPreferences("pin_plain_plain", Context.MODE_PRIVATE)
            prefs.edit().putString("record", "plain:ab\ncd\n1\n2").commit()
            assertEquals(PinPolicy.LEGACY_ITERATIONS, repository.read()?.iterations)
            prefs.edit().putString("record", "plain:ab\ncd\n1\n2\nnope").commit()
            assertEquals(PinPolicy.LEGACY_ITERATIONS, repository.read()?.iterations)
            prefs.edit().putString("record", "plain:zz\ncd\n1\n2\n1").commit()
            assertRejects { repository.read() }
            prefs.edit().putString("record", "plain:ab\nzz\n1\n2\n1").commit()
            assertRejects { repository.read() }
            prefs.edit().putString("record", "plain:ab\n1\n2").commit()
            assertRejects { repository.read() }
            assertFails { PinRepository(context) }
        }

    @Test
    fun elapsedLockoutRoundTripsAndAForcedReadFailsClosed() =
        runTest {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val repository = PinRepository(context, "pin_elapsed", allowPlaintextFallback = true)
            val prefs = context.getSharedPreferences("pin_elapsed_plain", Context.MODE_PRIVATE)
            prefs.edit().putString("record", "plain:ab\ncd\n1\n2\n1000\n50").commit()
            assertEquals(50L, repository.read()?.lockedUntilElapsedMs)
            prefs.edit().putString("record", "plain:ab\ncd\n1\n2\n1000\nnope").commit()
            assertEquals(0L, repository.read()?.lockedUntilElapsedMs)
            repository.failNextRead(PinKeyException(java.io.IOException("gone")))
            assertRejects { repository.read() }
            repository.failNextRead(IllegalStateException("torn"))
            assertRejects { repository.isSet() }
            repository.failNextRead(IllegalArgumentException("no"))
            assertRejects {
                repository.update { state -> Unit to state }
            }
        }

    @Test
    fun keystoreSealerIsChosenWhenPlaintextIsOff() =
        runTest {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val prefs = context.getSharedPreferences("pin_ks", Context.MODE_PRIVATE)
            val repository = PinRepository(context, "pin_ks", prefs = prefs)
            assertNull(repository.read())
            prefs
                .edit()
                .putString(KEY_KEYSET, "keyset")
                .putString(VALUE_KEYSET, "keyset")
                .commit()
            assertRejects { repository.read() }
            assertFails { LegacyEncryptedPin.read(context, "pin_ks") }
        }

    @Test
    fun legacyFieldsRoundTripAndDropOddValues() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("pin_fields", Context.MODE_PRIVATE)
        val echo = EchoAead()
        writeFields(prefs, "pin_fields", echo, happy())
        val state = LegacyEncryptedPin.legacyState(LegacyEncryptedPin.legacyFields(prefs, "pin_fields", echo, echo))
        assertEquals("abcd", state?.hashHex)
        assertEquals(3, state?.consecutiveFailures)
        assertEquals(9L, state?.lockedUntilEpochMs)
        assertEquals(1_000, state?.iterations)
        writeFields(prefs, "pin_fields", echo, happy() - "hash")
        assertNull(LegacyEncryptedPin.legacyState(LegacyEncryptedPin.legacyFields(prefs, "pin_fields", echo, echo)))
        writeFields(prefs, "pin_fields", echo, happy() - "salt")
        assertNull(LegacyEncryptedPin.legacyState(LegacyEncryptedPin.legacyFields(prefs, "pin_fields", echo, echo)))
        writeFields(prefs, "pin_fields", echo, happy("hash" to "a"))
        assertFails { LegacyEncryptedPin.legacyState(LegacyEncryptedPin.legacyFields(prefs, "pin_fields", echo, echo)) }
        writeFields(prefs, "pin_fields", echo, happy("hash" to "abc"))
        assertFails { LegacyEncryptedPin.legacyState(LegacyEncryptedPin.legacyFields(prefs, "pin_fields", echo, echo)) }
        writeFields(prefs, "pin_fields", echo, happy("salt" to "zz"))
        assertFails { LegacyEncryptedPin.legacyState(LegacyEncryptedPin.legacyFields(prefs, "pin_fields", echo, echo)) }
        writeFields(
            prefs,
            "pin_fields",
            echo,
            happy(
                "failures" to "nope",
                "lockout" to "nope",
                "iterations" to "nope",
                "extra" to 7,
                "__NULL__" to "__NULL__",
            ),
            intPref = true,
        )
        val odd = LegacyEncryptedPin.legacyState(LegacyEncryptedPin.legacyFields(prefs, "pin_fields", echo, echo))
        assertEquals(0, odd?.consecutiveFailures)
        assertEquals(0L, odd?.lockedUntilEpochMs)
        assertEquals(PinPolicy.LEGACY_ITERATIONS, odd?.iterations)
    }
}

private fun happy(vararg override: Pair<String, Any?>): Map<String, Any?> {
    val fields =
        linkedMapOf<String, Any?>(
            "hash" to "abcd",
            "salt" to "ef",
            "failures" to 3,
            "lockout" to 9L,
            "iterations" to 1_000,
        )
    override.forEach { (key, value) -> fields[key] = value }
    return fields
}

private fun writeFields(
    prefs: SharedPreferences,
    fileName: String,
    echo: EchoAead,
    fields: Map<String, Any?>,
    intPref: Boolean = false,
) {
    val editor =
        prefs
            .edit()
            .clear()
            .putString(KEY_KEYSET, "keyset")
            .putString(VALUE_KEYSET, "keyset")
    fields.forEach { (name, value) ->
        val clearKey = if (name == "__NULL__") "__NULL__" else name
        val encryptedKey =
            Base64.encodeToString(
                echo.encryptDeterministically(
                    clearKey.toByteArray(StandardCharsets.UTF_8),
                    fileName.toByteArray(StandardCharsets.UTF_8),
                ),
                Base64.DEFAULT,
            )
        val payload =
            when (value) {
                is Int -> typed(2, value)
                is Long -> typedLong(value)
                is String -> typedString(value)
                else -> typed(7, 0)
            }
        editor.putString(
            encryptedKey,
            Base64.encodeToString(
                echo.encrypt(payload, encryptedKey.toByteArray(StandardCharsets.UTF_8)),
                Base64.DEFAULT,
            ),
        )
    }
    if (intPref) editor.putInt("plain-int", 1)
    check(editor.commit())
}

private fun typedString(text: String): ByteArray {
    val bytes = text.toByteArray(StandardCharsets.UTF_8)
    val buffer = ByteBuffer.allocate(8 + bytes.size)
    buffer.putInt(0)
    buffer.putInt(bytes.size)
    buffer.put(bytes)
    return buffer.array()
}

private fun typed(
    type: Int,
    value: Int,
): ByteArray {
    val buffer = ByteBuffer.allocate(8)
    buffer.putInt(type)
    buffer.putInt(value)
    return buffer.array()
}

private fun typedLong(value: Long): ByteArray {
    val buffer = ByteBuffer.allocate(12)
    buffer.putInt(3)
    buffer.putLong(value)
    return buffer.array()
}

private class EchoAead :
    Aead,
    DeterministicAead {
    override fun encrypt(
        plaintext: ByteArray,
        associatedData: ByteArray,
    ): ByteArray = plaintext

    override fun decrypt(
        ciphertext: ByteArray,
        associatedData: ByteArray,
    ): ByteArray = ciphertext

    override fun encryptDeterministically(
        plaintext: ByteArray,
        associatedData: ByteArray,
    ): ByteArray = plaintext

    override fun decryptDeterministically(
        ciphertext: ByteArray,
        associatedData: ByteArray,
    ): ByteArray = ciphertext
}

private suspend fun assertRejects(block: suspend () -> Unit) {
    var failed = false
    try {
        block()
    } catch (_: Exception) {
        failed = true
    }
    assertTrue(failed)
}

private fun assertFails(block: () -> Unit) {
    var failed = false
    try {
        block()
    } catch (_: Exception) {
        failed = true
    }
    assertTrue(failed)
}

private const val KEY_KEYSET: String = "__androidx_security_crypto_encrypted_prefs_key_keyset__"
private const val VALUE_KEYSET: String = "__androidx_security_crypto_encrypted_prefs_value_keyset__"
