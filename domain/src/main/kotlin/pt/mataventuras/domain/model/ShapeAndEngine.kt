package pt.mataventuras.domain.model

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
 * Reward engine kind. 2D for age 3; 3D for age 7.
 */
enum class EngineKind {
    TWO_D,
    THREE_D,
}

/**
 * Reward engine for an age band.
 */
fun engineKindFor(age: AgeGroup): EngineKind =
    when (age) {
        AgeGroup.THREE_YEARS -> EngineKind.TWO_D
        AgeGroup.SEVEN_YEARS -> EngineKind.THREE_D
    }
