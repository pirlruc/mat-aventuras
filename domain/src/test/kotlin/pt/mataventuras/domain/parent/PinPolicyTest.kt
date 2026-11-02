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
    fun forwardWallClockDoesNotEndTheElapsedLock() {
        val elapsed = mutableListOf(5_000L)
        val wall = mutableListOf(1_000_000L)
        val locked =
            PinPolicy(
                random = SecureRandom.getInstance("SHA1PRNG").apply { setSeed(7) },
                now = { wall.last() },
                elapsed = { elapsed.last() },
                iterations = 1_000,
            )
        var state = locked.create("1234")
        repeat(PinPolicy.MAX_FAILURES) {
            state = locked.attempt(state, "9999").second
        }
        wall += wall.last() + PinPolicy.LOCKOUT_MS + 5_000L
        val (result, _) = locked.attempt(state, "1234")
        assertTrue(result is PinResult.Locked)
        elapsed += state.lockedUntilElapsedMs
        val (open, _) = locked.attempt(state, "1234")
        assertEquals(PinResult.Correct, open)
    }

    @Test
    fun rebootIgnoresTheElapsedLockout() {
        var boot = 3
        val elapsed = mutableListOf(5_000L)
        val wall = mutableListOf(1_000_000L)
        val locked =
            PinPolicy(
                random = SecureRandom.getInstance("SHA1PRNG").apply { setSeed(7) },
                now = { wall.last() },
                elapsed = { elapsed.last() },
                bootCount = { boot },
                iterations = 1_000,
            )
        var state = locked.create("1234")
        repeat(PinPolicy.MAX_FAILURES) {
            state = locked.attempt(state, "9999").second
        }
        assertEquals(3, state.lockoutBootCount)
        wall += state.lockedUntilEpochMs + 1
        boot = 4
        val (result, _) = locked.attempt(state, "1234")
        assertEquals(PinResult.Correct, result)
    }

    @Test
    fun adultProductUsesTwoDigitsTimesOne() {
        val challenge = AdultChallenge.draw(kotlin.random.Random(1))
        val drawn = AdultChallenge.draw()
        assertTrue(challenge.left in 10..99)
        assertTrue(challenge.right in 2..9)
        assertEquals(challenge.left * challenge.right, challenge.product)
        assertTrue(challenge.prompt.contains("×"))
        assertTrue(drawn.left in 10..99)
    }

    @Test
    fun matchingButShortPinStaysOnTheGate() {
        val gate = PinGate(policy)
        val stay = gate.setPin("12", "12").first
        assertEquals(PinNotice.MISMATCH, (stay as PinGateResult.Stay).notice)
    }

    @Test
    fun elapsedLockoutDoesNotApplyWhenTheClockIsZero() {
        val state =
            policy.create("1234").copy(
                lockedUntilEpochMs = 0L,
                lockedUntilElapsedMs = 50_000L,
                lockoutBootCount = 0,
            )
        val (result, _) = policy.attempt(state, "1234")
        assertEquals(PinResult.Correct, result)
    }

    @Test
    fun invalidFormatIsRejected() {
        assertFalse(policy.isValidFormat("12"))
        assertFalse(policy.isValidFormat("abcd"))
        assertFalse(policy.isValidFormat("12345"))
        assertFalse(policy.isValidFormat("\u0661\u0662\u0663\u0664"))
        assertFalse(policy.isValidFormat("\uFF11\uFF12\uFF13\uFF14"))
        val state = policy.create("1234")
        val (result, same) = policy.attempt(state, "12ab")
        assertEquals(PinResult.InvalidFormat, result)
        assertEquals(state, same)
        val (unicode, unchanged) = policy.attempt(state, "\u0661\u0662\u0663\u0664")
        assertEquals(PinResult.InvalidFormat, unicode)
        assertEquals(state, unchanged)
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
    fun olderHashIsReplacedWhenThePinUnlocks() {
        val state = policy.create("1234")
        assertEquals(1_000, state.iterations)
        val stronger =
            PinPolicy(
                random =
                    java.security.SecureRandom
                        .getInstance("SHA1PRNG")
                        .apply { setSeed(9) },
                now = { now.last() },
                iterations = 2_000,
            )
        val (result, next) = stronger.attempt(state, "1234")
        assertEquals(PinResult.Correct, result)
        assertEquals(2_000, next.iterations)
        assertTrue(next.hashHex != state.hashHex)
        val (again, stable) = stronger.attempt(next, "1234")
        assertEquals(PinResult.Correct, again)
        assertEquals(next.hashHex, stable.hashHex)
        assertEquals(2_000, stable.iterations)
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
        assertEquals(PinNotice.LOCKED, (blocked.first as PinGateResult.Stay).notice)
    }
}
