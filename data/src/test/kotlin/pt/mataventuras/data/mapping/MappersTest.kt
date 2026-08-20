package pt.mataventuras.data.mapping

import org.junit.Assert.assertEquals
import org.junit.Test
import pt.mataventuras.data.local.AvatarEntity
import pt.mataventuras.data.local.BadgeEntity
import pt.mataventuras.data.local.ProfileEntity
import pt.mataventuras.data.local.SessionEntity
import pt.mataventuras.domain.model.AgeGroup
import pt.mataventuras.domain.model.ChildProfile
import pt.mataventuras.domain.model.LearningModule
import pt.mataventuras.domain.model.LearningSession
import pt.mataventuras.domain.model.Mascot

class MappersTest {
    @Test
    fun profileRoundTrips() {
        val domain =
            ChildProfile(
                id = 3,
                name = "Ana",
                ageGroup = AgeGroup.SEVEN_YEARS,
                favouriteMascot = Mascot.PINK_PIGLET,
                avatarId = "STARTER",
                points = 40,
                createdAtEpochMs = 9,
            )
        val entity = domain.toEntity()
        assertEquals("pink_piglet", entity.mascotCode)
        assertEquals(domain, entity.toDomain())
    }

    @Test
    fun sessionRoundTrips() {
        val domain =
            LearningSession(2, 3, LearningModule.LOGIC, 4, 1, 500, 10)
        assertEquals(domain, domain.toEntity().toDomain())
        val entity = SessionEntity(2, 3, "LOGIC", 4, 1, 500, 10)
        assertEquals(domain, entity.toDomain())
    }

    @Test
    fun badgeAndAvatarMapToDomain() {
        assertEquals("STAR", BadgeEntity(1, 3, "STAR", 8).toDomain().code)
        assertEquals("CAPTAIN", AvatarEntity(1, 3, "CAPTAIN", 8).toDomain().avatarId)
        val profile = ProfileEntity(1, "Rui", "THREE_YEARS", "unknown", "STARTER", 0, 1)
        assertEquals(Mascot.SPEEDY_HEDGEHOG, profile.toDomain().favouriteMascot)
    }
}
