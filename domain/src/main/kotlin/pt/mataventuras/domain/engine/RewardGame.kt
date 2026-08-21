package pt.mataventuras.domain.engine

import kotlin.random.Random
import pt.mataventuras.domain.model.AgeGroup
import pt.mataventuras.domain.model.EngineKind

/**
 * Packaged reward mini-game. [sceneFile] is the Godot asset name under `assets/`.
 */
enum class RewardGame(
    val sceneFile: String,
) {
    RUNNER("runner.tscn"),
    KART("kart.tscn"),
    INVADERS("invaders.tscn"),
    CHOMP("chomp.tscn"),
    CLIMB("climb.tscn"),
}

/**
 * Picks a prize game for an age band and engine process.
 */
object RewardCatalog {
    /**
     * Godot `res://` path for [game].
     */
    fun scenePath(game: RewardGame): String = "res://${game.sceneFile}"

    /**
     * Process kind that must host [game].
     */
    fun engineKind(game: RewardGame): EngineKind = if (game == RewardGame.KART) EngineKind.THREE_D else EngineKind.TWO_D

    /**
     * Games that fit [age] on [kind]'s process.
     */
    fun gamesFor(
        age: AgeGroup,
        kind: EngineKind,
    ): List<RewardGame> =
        when (kind) {
            EngineKind.THREE_D -> listOf(RewardGame.KART)
            EngineKind.TWO_D ->
                if (age == AgeGroup.THREE_YEARS) {
                    listOf(RewardGame.RUNNER, RewardGame.CLIMB, RewardGame.CHOMP)
                } else {
                    listOf(RewardGame.RUNNER, RewardGame.INVADERS, RewardGame.CHOMP, RewardGame.CLIMB)
                }
        }

    /**
     * One game from [gamesFor].
     */
    fun pick(
        age: AgeGroup,
        kind: EngineKind,
        random: Random = Random.Default,
    ): RewardGame {
        val games = gamesFor(age, kind)
        return games[random.nextInt(games.size)]
    }

    /**
     * Parses an extra, falling back to [RewardGame.RUNNER] or [RewardGame.KART].
     */
    fun fromName(
        raw: String?,
        kind: EngineKind,
    ): RewardGame {
        val match = RewardGame.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }
        if (match != null) return match
        return if (kind == EngineKind.THREE_D) RewardGame.KART else RewardGame.RUNNER
    }
}
