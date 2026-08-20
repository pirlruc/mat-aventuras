package pt.mataventuras.domain.parent

import java.security.SecureRandom
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PinPolicyTest {
    private val now = mutableListOf(1_000_000L)
    private val policy =
        PinPolicy(
            random = SecureRandom.getInstance("SHA1PRNG").apply { setSeed(7) },
            now = { now.last() },
            iterations = 1_000,
        )

    @Test
    fun correctPinUnlocks() {
        val state = policy.create("1234")
        val (result, next) = policy.attempt(state, "1234")
        assertEquals(PinResult.Correct, result)
        assertEquals(0, next.consecutiveFailures)
    }

    @Test
    fun wrongPinCountsFailures() {
        val state = policy.create("1234")
        val (result, next) = policy.attempt(state, "0000")
        assertTrue(result is PinResult.Incorrect)
        assertEquals(PinPolicy.MAX_FAILURES - 1, (result as PinResult.Incorrect).remaining)
        assertEquals(1, next.consecutiveFailures)
    }

    @Test
    fun lockoutAfterMaxFailures() {
        var state = policy.create("1234")
        repeat(PinPolicy.MAX_FAILURES) {
            state = policy.attempt(state, "9999").second
        }
        assertTrue(state.lockedUntilEpochMs > 0L)
        val (result, _) = policy.attempt(state, "1234")
        assertTrue(result is PinResult.Locked)
    }

    @Test
    fun invalidFormatIsRejected() {
        assertFalse(policy.isValidFormat("12"))
        assertFalse(policy.isValidFormat("abcd"))
        assertFalse(policy.isValidFormat("12345"))
        val state = policy.create("1234")
        val (result, same) = policy.attempt(state, "12ab")
        assertEquals(PinResult.InvalidFormat, result)
        assertEquals(state, same)
    }

    @Test(expected = IllegalArgumentException::class)
    fun createRejectsInvalidFormat() {
        policy.create("1")
    }

    @Test
    fun hexRoundTripAndConstantTimeCompare() {
        val bytes = byteArrayOf(0x0f, 0x10, 0xff.toByte())
        assertEquals("0f10ff", toHex(bytes))
        assertTrue(constantTimeEquals(bytes, fromHex("0f10ff")))
        assertFalse(constantTimeEquals(bytes, byteArrayOf(1)))
        assertFalse(constantTimeEquals(bytes, fromHex("0f10fe")))
    }

    @Test(expected = IllegalArgumentException::class)
    fun oddLengthHexFails() {
        fromHex("abc")
    }

    @Test
    fun unlocksAfterLockoutElapses() {
        var state = policy.create("4321")
        repeat(PinPolicy.MAX_FAILURES) {
            state = policy.attempt(state, "0000").second
        }
        now += state.lockedUntilEpochMs + 1
        val (result, _) = policy.attempt(state, "4321")
        assertEquals(PinResult.Correct, result)
    }

    @Test
    fun pinGateSetAndUnlock() {
        val gate = PinGate(policy)
        val mismatch = gate.setPin("1234", "0000")
        assertTrue(mismatch.first is PinGateResult.Stay)
        val (ok, created) = gate.setPin("1234", "1234")
        assertEquals(PinGateResult.Unlocked, ok)
        val state = created!!
        val wrong = gate.unlock(state, "0000")
        assertTrue(wrong.first is PinGateResult.Stay)
        val right = gate.unlock(state, "1234")
        assertEquals(PinGateResult.Unlocked, right.first)
        val badFormat = gate.unlock(state, "ab")
        assertTrue(badFormat.first is PinGateResult.Stay)
        var locked = state
        repeat(PinPolicy.MAX_FAILURES) { locked = gate.unlock(locked, "9999").second }
        val blocked = gate.unlock(locked, "1234")
        assertTrue((blocked.first as PinGateResult.Stay).message.contains("Demasiadas"))
    }
}
