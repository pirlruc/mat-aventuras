package pt.mataventuras.domain.prize

import kotlin.random.Random
import pt.mataventuras.domain.engine.RewardCatalog
import pt.mataventuras.domain.engine.RewardGame
import pt.mataventuras.domain.model.AgeBand
import pt.mataventuras.domain.model.EngineKind

/**
 * Compose prize. Balloons are tap-only. The snake eats the right answer.
 */
enum class ComposePrize {
    BALLOONS,
    FISHING,
    COLOURING,
    BUBBLES,
    SNAKE,
    BREAKOUT,
    TOWER,
    RUNNER,
}

/**
 * A reward the child is about to open.
 */
sealed class PrizeOffer {
    /**
     * Godot or Canvas prize already in the catalog.
     */
    data class Godot(
        val game: RewardGame,
    ) : PrizeOffer()

    /**
     * Compose prize for this age band.
     */
    data class Compose(
        val prize: ComposePrize,
    ) : PrizeOffer()
}

/**
 * Mixes the Godot prizes for an age with that band's Compose prize.
 */
object PrizeOffers {
    /**
     * One prize. Ages 3–5 can pop balloons. Ages 6–8 feed the snake.
     */
    fun pick(
        age: AgeBand,
        random: Random = Random.Default,
    ): PrizeOffer {
        val godot = RewardCatalog.gamesFor(age, EngineKind.TWO_D).map { PrizeOffer.Godot(it) }
        val kart =
            if (age == AgeBand.SIX_TO_EIGHT) {
                RewardCatalog.gamesFor(age, EngineKind.THREE_D).map { PrizeOffer.Godot(it) }
            } else {
                emptyList()
            }
        val compose = composePrizes(age).map { PrizeOffer.Compose(it) }
        val offers = godot + kart + compose
        return offers[random.nextInt(offers.size)]
    }

    /**
     * Compose prizes for [age]. Ages 3–5 fish, colour, and pop. Ages 6–8 keep the snake.
     */
    fun composePrizes(age: AgeBand): List<ComposePrize> =
        if (age == AgeBand.THREE_TO_FIVE) {
            listOf(ComposePrize.BALLOONS, ComposePrize.FISHING, ComposePrize.COLOURING, ComposePrize.BUBBLES)
        } else {
            listOf(ComposePrize.SNAKE, ComposePrize.BREAKOUT, ComposePrize.TOWER, ComposePrize.RUNNER)
        }
}
