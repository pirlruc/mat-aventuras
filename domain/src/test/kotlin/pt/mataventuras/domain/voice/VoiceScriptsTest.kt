package pt.mataventuras.domain.voice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.model.Mascot
import pt.mataventuras.domain.model.UnlockedAvatar
import pt.mataventuras.domain.model.UnlockedBadge

class VoiceScriptsTest {
    @Test
    fun copyIsEuropeanPortuguese() {
        assertTrue(VoiceScripts.AGE_SELECTION.contains("Escolhe"))
        assertTrue(VoiceScripts.APP_TITLE.contains("Aventuras"))
        assertTrue(VoiceScripts.APP_DESCRIPTION.contains("aparelho"))
        assertEquals(VoiceScripts.AGE_THREE_PREVIEW, VoiceScripts.agePreview(AgeBand.THREE_TO_FIVE))
        assertEquals(VoiceScripts.AGE_SEVEN_PREVIEW, VoiceScripts.agePreview(AgeBand.SIX_TO_EIGHT))
        assertEquals("Continuar como Ana", VoiceScripts.continueAs("Ana"))
        assertEquals(VoiceScripts.REWARD_FINISHED, VoiceScripts.rewardReturn(true))
        assertEquals(VoiceScripts.REWARD_RETURN, VoiceScripts.rewardReturn(false))
        assertTrue(VoiceScripts.greeting(Mascot.SPEEDY_HEDGEHOG, AgeBand.THREE_TO_FIVE).contains("brincar"))
        assertTrue(VoiceScripts.greeting(Mascot.HERO_PUP, AgeBand.SIX_TO_EIGHT).contains("desafio"))
        assertEquals(null, VoiceScripts.confirmExit(AgeBand.THREE_TO_FIVE))
        assertTrue(VoiceScripts.confirmExit(AgeBand.SIX_TO_EIGHT)!!.contains("sair"))
        assertTrue(VoiceScripts.WELL_DONE.contains("bem"))
        assertTrue(VoiceScripts.TRY_AGAIN.contains("Tenta"))
        assertTrue(VoiceScripts.LETS_PLAY.contains("prémio"))
        assertTrue(VoiceScripts.STAYS_ON_DEVICE.contains("aparelho"))
        assertTrue(VoiceScripts.STEER_HINT.contains("META"))
        assertTrue(VoiceScripts.JUMP_HINT.contains("saltar"))
        assertTrue(VoiceScripts.INVADERS_HINT.contains("nave"))
        assertTrue(VoiceScripts.INVADERS_HINT.contains("5 vidas"))
        assertTrue(VoiceScripts.CHOMP_HINT.contains("3 vidas"))
        assertTrue(VoiceScripts.CLIMB_HINT.contains("3 vidas"))
        assertTrue(VoiceScripts.CHOMP_HINT.contains("bolinhas"))
        assertTrue(VoiceScripts.CLIMB_HINT.contains("barris"))
    }

    @Test
    fun rewardModelsCoverPublicDataClasses() {
        val badge = UnlockedBadge("X", 1)
        val avatar = UnlockedAvatar("Y", 2)
        assertEquals("X", badge.code)
        assertEquals("Y", avatar.avatarId)
    }
}
