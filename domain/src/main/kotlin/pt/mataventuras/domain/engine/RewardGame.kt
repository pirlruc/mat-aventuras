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
     * Playable native fallback when the Godot plugin Activity is absent.
     * Arcade 2D prizes stay Godot-only; Canvas hosts the runner or dirt race.
     */
    fun nativeFallback(kind: EngineKind): RewardGame =
        when (kind) {
            EngineKind.THREE_D -> RewardGame.KART
            EngineKind.TWO_D -> RewardGame.RUNNER
        }

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
     * Parses an extra, falling back to [nativeFallback] when the name is missing
     * or belongs to the other engine process.
     */
    fun fromName(
        raw: String?,
        kind: EngineKind,
    ): RewardGame {
        val match = RewardGame.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }
        if (match != null && engineKind(match) == kind) return match
        return nativeFallback(kind)
    }

    /**
     * Godot `res://` path from a fragment extra. Unknown or cross-engine paths
     * become [nativeFallback] so GDScript cannot `change_scene_to_file` boot
     * or another process's packed scene.
     */
    fun packedScenePath(
        raw: String?,
        kind: EngineKind,
    ): String {
        val match = RewardGame.entries.firstOrNull { scenePath(it) == raw }
        val game = if (match != null && engineKind(match) == kind) match else nativeFallback(kind)
        return scenePath(game)
    }
}
