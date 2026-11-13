package pt.mataventuras.domain.entitlement

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.prize.PrizeOffers

class FamilyPlusTest {
    @Test
    fun flagCoversEveryFeature() {
        val open = FlagEntitlements(PremiumFeature.entries.toSet())
        val shut = FlagEntitlements(emptySet())
        PremiumFeature.entries.forEach { feature ->
            assertTrue(open.isUnlocked(feature))
            assertTrue(!shut.isUnlocked(feature))
        }
    }

    @Test
    fun themeAndSeasonComeFromTheDateAndTheParent() {
        assertEquals(WorldTheme.MEADOW, WorldTheme.fromStored(null))
        assertEquals(WorldTheme.MEADOW, WorldTheme.fromStored("NOPE"))
        assertEquals(WorldTheme.SEA, WorldTheme.fromStored("SEA"))
        assertEquals(Season.WINTER, Season.of(12))
        assertEquals(Season.WINTER, Season.of(1))
        assertEquals(Season.SPRING, Season.of(4))
        assertEquals(Season.SUMMER, Season.of(7))
        assertEquals(Season.AUTUMN, Season.of(10))
        assertEquals(Season.AUTUMN, Season.of(0))
        assertEquals(Season.AUTUMN, Season.of(13))
        assertEquals(SeasonalCalendar.index(3, 14), SeasonalCalendar.index(3, 14))
        assertTrue(SeasonalCalendar.index(3, 14) != SeasonalCalendar.index(3, 15))
        assertEquals(0, SeasonalCalendar.index(0, 0))
        assertEquals(0, SeasonalCalendar.index(13, 40))
        assertEquals(SeasonalCalendar.index(1, 1), 0)
        assertEquals(SeasonalCalendar.index(12, 31), 11 * 31 + 30)
    }

    @Test
    fun lockedFeaturesAddNoExtras() {
        val locked = FamilyGate.extras(FlagEntitlements(emptySet()), WorldTheme.NIGHT, 12, 25)
        assertNull(locked.theme)
        assertNull(locked.season)
        assertNull(locked.prizeIndex)
        val worlds = FamilyGate.extras(FlagEntitlements(setOf(PremiumFeature.THEMED_WORLDS)), WorldTheme.NIGHT, 6, 2)
        assertEquals(WorldTheme.NIGHT.ordinal, worlds.theme)
        assertNull(worlds.season)
        assertNull(worlds.prizeIndex)
        val seasonal =
            FamilyGate.extras(FlagEntitlements(setOf(PremiumFeature.SEASONAL_PRIZES)), WorldTheme.SEA, 6, 2)
        assertNull(seasonal.theme)
        assertEquals(Season.SUMMER.ordinal, seasonal.season)
        assertEquals(SeasonalCalendar.index(6, 2), seasonal.prizeIndex)
    }

    @Test
    fun seasonalIndexRepeatsTheSamePrize() {
        val young = PrizeOffers.pickAt(AgeBand.THREE_TO_FIVE, SeasonalCalendar.index(1, 1))
        assertEquals(young, PrizeOffers.pickAt(AgeBand.THREE_TO_FIVE, SeasonalCalendar.index(1, 1)))
        val older = PrizeOffers.pickAt(AgeBand.SIX_TO_EIGHT, -1)
        assertEquals(older, PrizeOffers.pickAt(AgeBand.SIX_TO_EIGHT, PrizeOffers.offers(AgeBand.SIX_TO_EIGHT).size - 1))
        assertTrue(PrizeOffers.offers(AgeBand.THREE_TO_FIVE).isNotEmpty())
        assertTrue(PrizeOffers.offers(AgeBand.SIX_TO_EIGHT).size > PrizeOffers.offers(AgeBand.THREE_TO_FIVE).size)
    }

    private class FlagEntitlements(
        private val unlocked: Set<PremiumFeature>,
    ) : Entitlements {
        override fun isUnlocked(feature: PremiumFeature): Boolean = feature in unlocked
    }
}
