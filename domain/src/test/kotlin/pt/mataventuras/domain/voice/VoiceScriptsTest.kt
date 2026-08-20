package pt.mataventuras.domain.voice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.domain.math.AttemptResult
import pt.mataventuras.domain.model.AgeGroup
import pt.mataventuras.domain.model.Mascot
import pt.mataventuras.domain.model.UnlockedAvatar
import pt.mataventuras.domain.model.UnlockedBadge

class VoiceScriptsTest {
    @Test
    fun copyIsEuropeanPortuguese() {
        assertTrue(VoiceScripts.AGE_SELECTION.contains("Escolhe"))
        assertTrue(VoiceScripts.greeting(Mascot.SPEEDY_HEDGEHOG, AgeGroup.THREE_YEARS).contains("brincar"))
        assertTrue(VoiceScripts.greeting(Mascot.HERO_PUP, AgeGroup.SEVEN_YEARS).contains("desafio"))
        assertEquals(null, VoiceScripts.confirmExit(AgeGroup.THREE_YEARS))
        assertTrue(VoiceScripts.confirmExit(AgeGroup.SEVEN_YEARS)!!.contains("sair"))
        assertTrue(VoiceScripts.WELL_DONE.contains("bem"))
        assertTrue(VoiceScripts.TRY_AGAIN.contains("Tenta"))
        assertTrue(VoiceScripts.LETS_PLAY.contains("prémio"))
        assertTrue(VoiceScripts.STAYS_ON_DEVICE.contains("aparelho"))
    }

    @Test
    fun rewardModelsAndAttemptResultCoverUnusedLines() {
        val badge = UnlockedBadge("X", 1)
        val avatar = UnlockedAvatar("Y", 2)
        val result = AttemptResult(true, VoiceScripts.WELL_DONE)
        assertEquals("X", badge.code)
        assertEquals("Y", avatar.avatarId)
        assertTrue(result.correct)
        assertEquals("Muito bem!", result.spoken)
    }
}
