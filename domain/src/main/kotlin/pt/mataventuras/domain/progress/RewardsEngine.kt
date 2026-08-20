package pt.mataventuras.domain.progress

/**
 * Digital badge catalogue. Codes are stable Room keys. [title]/[description] are pt-PT UI copy.
 */
enum class BadgeCode(val title: String, val description: String) {
    FIRST_STEPS("Primeiros passos", "Completaste a primeira sessão."),
    STAR_COUNTER("Contador de estrelas", "Acertaste 10 contagens."),
    SHAPE_MASTER("Mestre das formas", "Acertaste 10 formas."),
    QUICK_CALCULATOR("Calculador rápido", "Acertaste 20 contas."),
    STAR_OF_THE_DAY("Estrela do dia", "Sessão perfeita com pelo menos 5 perguntas."),
    MARATHONER("Maratonista", "Jogaste 30 minutos no total."),
}

/**
 * Unlockable avatars. `STARTER` is always available. [title] is pt-PT UI copy.
 */
enum class AvatarCode(val title: String, val minPoints: Int) {
    STARTER("Explorador", 0),
    RUNNER("Corredor", 50),
    ASTRONAUT("Astronauta", 150),
    CAPTAIN("Capitão", 300),
    LEGEND("Lenda", 500),
}

/**
 * Totals used to decide newly earned badges.
 */
data class ProgressTotals(
    val completedSessions: Int,
    val countingHits: Int,
    val shapeHits: Int,
    val arithmeticHits: Int,
    val perfectSessionWithMinimum: Boolean,
    val totalTimeMs: Long,
)

/**
 * Local scoring and unlocks. No network.
 */
class RewardsEngine {
    /**
     * Points earned for one attempt.
     */
    fun pointsForAttempt(correct: Boolean): Int = if (correct) HIT_POINTS else MISS_POINTS

    /**
     * New total, never negative.
     */
    fun applyPoints(
        current: Int,
        delta: Int,
    ): Int = (current + delta).coerceAtLeast(0)

    /**
     * Badges newly earned after a session.
     */
    fun newBadges(
        alreadyUnlocked: Set<String>,
        totals: ProgressTotals,
    ): List<BadgeCode> {
        val candidates = mutableListOf<BadgeCode>()
        if (totals.completedSessions >= 1) candidates += BadgeCode.FIRST_STEPS
        if (totals.countingHits >= 10) candidates += BadgeCode.STAR_COUNTER
        if (totals.shapeHits >= 10) candidates += BadgeCode.SHAPE_MASTER
        if (totals.arithmeticHits >= 20) candidates += BadgeCode.QUICK_CALCULATOR
        if (totals.perfectSessionWithMinimum) candidates += BadgeCode.STAR_OF_THE_DAY
        if (totals.totalTimeMs >= THIRTY_MINUTES_MS) candidates += BadgeCode.MARATHONER
        return candidates.filter { it.name !in alreadyUnlocked }
    }

    /**
     * Avatars the point total already unlocks that are not in the collection.
     */
    fun newAvatars(
        alreadyUnlocked: Set<String>,
        points: Int,
    ): List<AvatarCode> =
        AvatarCode.entries.filter { avatar ->
            points >= avatar.minPoints && avatar.name !in alreadyUnlocked
        }

    /**
     * Bonus points when a reward mini-game reports that it finished.
     */
    fun pointsForRewardFinish(finished: Boolean): Int = if (finished) REWARD_FINISH_POINTS else 0

    /**
     * Opens a reward level every [HITS_FOR_REWARD] consecutive hits.
     */
    fun shouldOpenReward(consecutiveHits: Int): Boolean {
        if (consecutiveHits <= 0) return false
        return consecutiveHits % HITS_FOR_REWARD == 0
    }

    /** Scoring constants and thresholds. */
    companion object {
        const val HIT_POINTS: Int = 10
        const val MISS_POINTS: Int = -2
        const val REWARD_FINISH_POINTS: Int = 15
        const val HITS_FOR_REWARD: Int = 3
        const val THIRTY_MINUTES_MS: Long = 30L * 60L * 1000L
        const val PERFECT_MINIMUM: Int = 5
    }
}
