package pt.mataventuras.domain.parent

import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Parental PIN policy: 4 digits, PBKDF2, lockout after failures.
 */
data class PinState(
    val hashHex: String,
    val saltHex: String,
    val consecutiveFailures: Int,
    val lockedUntilEpochMs: Long,
)

/**
 * Outcome of one PIN attempt.
 */
sealed class PinResult {
    /** PIN matched; failure count is cleared. */
    data object Correct : PinResult()

    /** Wrong PIN; [remaining] attempts until lockout. */
    data class Incorrect(val remaining: Int) : PinResult()

    /** Too many failures; [unlocksAtEpochMs] is when lockout ends. */
    data class Locked(val unlocksAtEpochMs: Long) : PinResult()

    /** Value is not four digits. */
    data object InvalidFormat : PinResult()
}

/**
 * PIN rules and hashing. Persistence lives in the Android data layer.
 */
class PinPolicy(
    private val random: SecureRandom = SecureRandom(),
    private val now: () -> Long = { System.currentTimeMillis() },
    private val iterations: Int = ITERATIONS,
) {
    /**
     * Initial state from a 4-digit PIN.
     */
    fun create(pin: String): PinState {
        require(isValidFormat(pin)) { "PIN must be four digits." }
        val salt = ByteArray(SALT_BYTES)
        random.nextBytes(salt)
        val hash = derive(pin, salt)
        return PinState(
            hashHex = toHex(hash),
            saltHex = toHex(salt),
            consecutiveFailures = 0,
            lockedUntilEpochMs = 0L,
        )
    }

    /**
     * Validates [pin] and returns the state to persist.
     */
    fun attempt(
        state: PinState,
        pin: String,
    ): Pair<PinResult, PinState> {
        val nowMs = now()
        if (state.lockedUntilEpochMs > nowMs) {
            return PinResult.Locked(state.lockedUntilEpochMs) to state
        }
        if (!isValidFormat(pin)) {
            return PinResult.InvalidFormat to state
        }
        val expected = fromHex(state.hashHex)
        val actual = derive(pin, fromHex(state.saltHex))
        return if (constantTimeEquals(expected, actual)) {
            PinResult.Correct to state.copy(consecutiveFailures = 0, lockedUntilEpochMs = 0L)
        } else {
            failed(state, nowMs)
        }
    }

    /**
     * Accepts exactly four digits 0-9.
     */
    fun isValidFormat(pin: String): Boolean = pin.length == 4 && pin.all { it.isDigit() }

    private fun failed(
        state: PinState,
        nowMs: Long,
    ): Pair<PinResult, PinState> {
        val failures = state.consecutiveFailures + 1
        val lockUntil = if (failures >= MAX_FAILURES) nowMs + LOCKOUT_MS else 0L
        val next = state.copy(consecutiveFailures = failures, lockedUntilEpochMs = lockUntil)
        val result =
            if (lockUntil > 0L) {
                PinResult.Locked(lockUntil)
            } else {
                PinResult.Incorrect(MAX_FAILURES - failures)
            }
        return result to next
    }

    private fun derive(
        pin: String,
        salt: ByteArray,
    ): ByteArray {
        val spec: KeySpec = PBEKeySpec(pin.toCharArray(), salt, iterations, HASH_BITS)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        return factory.generateSecret(spec).encoded
    }

    /** PBKDF2 parameters and lockout policy. */
    companion object {
        const val MAX_FAILURES: Int = 5
        const val LOCKOUT_MS: Long = 60_000L
        const val SALT_BYTES: Int = 16
        const val HASH_BITS: Int = 256
        const val ITERATIONS: Int = 120_000
        const val ALGORITHM: String = "PBKDF2WithHmacSHA256"
    }
}

internal fun toHex(bytes: ByteArray): String = bytes.joinToString(separator = "") { b -> "%02x".format(b) }

internal fun fromHex(hex: String): ByteArray {
    require(hex.length % 2 == 0) { "Invalid hex." }
    return ByteArray(hex.length / 2) { i ->
        hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
    }
}

internal fun constantTimeEquals(
    a: ByteArray,
    b: ByteArray,
): Boolean {
    if (a.size != b.size) return false
    var acc = 0
    for (i in a.indices) {
        acc = acc or (a[i].toInt() xor b[i].toInt())
    }
    return acc == 0
}
