package pt.mataventuras.app.ui.lesson

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pt.mataventuras.app.ui.UiLogic

@RunWith(RobolectricTestRunner::class)
class AnswerCuePlayerTest {
    @Test
    fun playSendsToneAndHapticForHitAndMiss() {
        val tones = mutableListOf<Pair<Int, Int>>()
        val haptics = mutableListOf<Int>()
        val player = AnswerCuePlayer(playTone = { tone, ms -> tones += tone to ms })
        player.play(true) { haptics += it }
        player.play(false) { haptics += it }
        assertEquals(listOf(UiLogic.answerTone(true) to 180, UiLogic.answerTone(false) to 260), tones)
        assertEquals(listOf(UiLogic.answerHaptic(true), UiLogic.answerHaptic(false)), haptics)
        player.release()
    }

    @Test
    fun playSwallowsToneAndHapticFailures() {
        val player =
            AnswerCuePlayer(
                playTone = { _, _ -> error("no speaker") },
                releaseTone = { error("already gone") },
            )
        player.play(true) { error("no vibrator") }
        player.play(false)
        player.release()
        assertTrue(true)
    }

    @Test
    fun silentGeneratorStillAcceptsPlayAndRelease() {
        val player = AnswerCuePlayer.wrapGenerator(null)
        player.play(true)
        player.play(false)
        player.release()
        val created = AnswerCuePlayer.createGenerator()
        val wrapped = AnswerCuePlayer.wrapGenerator(created)
        wrapped.play(true)
        wrapped.release()
        val device = AnswerCuePlayer.device()
        device.play(false)
        device.release()
    }
}
