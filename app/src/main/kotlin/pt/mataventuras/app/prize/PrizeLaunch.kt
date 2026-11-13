package pt.mataventuras.app.prize

import android.content.Context
import android.content.Intent
import pt.mataventuras.app.engine.EngineLauncher
import pt.mataventuras.domain.engine.EnginePluginContract
import pt.mataventuras.domain.engine.RewardCatalog
import pt.mataventuras.domain.entitlement.FamilyExtras
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.model.Mascot
import pt.mataventuras.domain.prize.PrizeOffer
import pt.mataventuras.domain.prize.PrizeOffers

/**
 * Reward intent. Compose prizes stay in this process. Godot prizes use the engine process.
 */
object PrizeLaunch {
    /**
     * Intent for the next prize. [offer] is injected in tests.
     */
    fun intent(
        context: Context,
        age: AgeBand,
        mascot: Mascot,
        name: String,
        offer: PrizeOffer = PrizeOffers.pick(age),
        limitSeconds: Int = 0,
    ): Intent =
        when (offer) {
            is PrizeOffer.Godot ->
                EngineLauncher.intentFor(
                    context,
                    age,
                    mascot,
                    name,
                    kind = RewardCatalog.engineKind(offer.game),
                    game = offer.game,
                    limitSeconds = limitSeconds,
                )
            is PrizeOffer.Compose -> ComposePrizeActivity.intent(context, offer.prize, limitSeconds = limitSeconds)
        }

    /**
     * Next prize, using [family] when those features are unlocked.
     */
    fun intentForReward(
        context: Context,
        age: AgeBand,
        mascot: Mascot,
        name: String,
        limitSeconds: Int,
        family: FamilyExtras,
    ): Intent {
        val offer =
            family.prizeIndex?.let { PrizeOffers.pickAt(age, it) } ?: PrizeOffers.pick(age)
        return withFamily(intent(context, age, mascot, name, offer, limitSeconds), family)
    }

    /**
     * Copies unlocked theme and season extras. Null fields are omitted.
     */
    fun withFamily(
        intent: Intent,
        family: FamilyExtras,
    ): Intent {
        family.theme?.let { intent.putExtra(EnginePluginContract.EXTRA_WORLD_THEME, it) }
        family.season?.let { intent.putExtra(EnginePluginContract.EXTRA_SEASON, it) }
        return intent
    }
}
