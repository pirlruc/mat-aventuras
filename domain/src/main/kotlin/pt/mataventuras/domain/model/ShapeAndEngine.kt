package pt.mataventuras.domain.model

import kotlin.random.Random

/**
 * Shape used in age-3 exercises. [displayName] is pt-PT UI copy; colour and silhouette travel together.
 */
enum class GeometricShape(val displayName: String) {
    CIRCLE("círculo"),
    SQUARE("quadrado"),
    TRIANGLE("triângulo"),
    RECTANGLE("rectângulo"),
    STAR("estrela"),
}

/**
 * Reward engine kind. Age 3 stays on the 2D platformer; age 7 mixes 2D and 3D.
 */
enum class EngineKind {
    TWO_D,
    THREE_D,
}

/**
 * Default reward engine for an age band (kart at seven, platformer at three).
 */
fun engineKindFor(age: AgeGroup): EngineKind =
    when (age) {
        AgeGroup.THREE_YEARS -> EngineKind.TWO_D
        AgeGroup.SEVEN_YEARS -> EngineKind.THREE_D
    }

/**
 * Engine launched for one reward. Age 7 picks the platformer or the kart at random.
 */
fun pickRewardKind(
    age: AgeGroup,
    random: Random = Random.Default,
): EngineKind =
    when (age) {
        AgeGroup.THREE_YEARS -> EngineKind.TWO_D
        AgeGroup.SEVEN_YEARS -> if (random.nextBoolean()) EngineKind.TWO_D else EngineKind.THREE_D
    }
