package pt.mataventuras.domain.model

/**
 * Lesson host character. Display names are pt-PT UI copy; codes are stable English ids.
 */
enum class Mascot(
    val code: String,
    val displayName: String,
    val primaryArgb: Long,
) {
    SPEEDY_HEDGEHOG("speedy_hedgehog", "Ouriço Veloz", 0xFF1E88E5),
    HERO_PUP("hero_pup", "Cão Herói", 0xFFFFB300),
    PINK_PIGLET("pink_piglet", "Porquinho Rosa", 0xFFEC407A),
    BRAVE_PLUMBER("brave_plumber", "Canalizador Valente", 0xFF43A047),
    MISCHIEVOUS_ALIEN("mischievous_alien", "Extraterrestre Travesso", 0xFF7E57C2),
    ;

    companion object {
        /**
         * Resolves a persisted code. Unknown values fall back to [SPEEDY_HEDGEHOG].
         */
        fun fromCode(code: String): Mascot = entries.firstOrNull { it.code == code } ?: SPEEDY_HEDGEHOG
    }
}

/**
 * Host mascot for a learning module.
 */
fun mascotFor(module: LearningModule): Mascot =
    when (module) {
        LearningModule.COUNTING -> Mascot.SPEEDY_HEDGEHOG
        LearningModule.SHAPES -> Mascot.PINK_PIGLET
        LearningModule.NUMBERS -> Mascot.HERO_PUP
        LearningModule.ADDITION, LearningModule.SUBTRACTION -> Mascot.BRAVE_PLUMBER
        LearningModule.MULTIPLICATION -> Mascot.MISCHIEVOUS_ALIEN
        LearningModule.LOGIC -> Mascot.HERO_PUP
    }
