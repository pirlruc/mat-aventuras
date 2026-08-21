package pt.mataventuras.domain.engine

/**
 * One floor gap the runner can fall through.
 */
data class PlatformerPit(
    val left: Float,
    val right: Float,
)

/**
 * Floating brick the runner can land on.
 */
data class PlatformerLedge(
    val x: Float,
    val y: Float,
    val width: Float,
)

/**
 * Palette + layout for one age-3 reward run.
 */
data class PlatformerLevel(
    val pits: List<PlatformerPit>,
    val ledges: List<PlatformerLedge>,
    val coins: List<Float>,
    val length: Float,
    val skyArgb: Long,
    val skyBandArgb: Long,
    val grassArgb: Long,
    val brickArgb: Long,
    val pitArgb: Long,
) {
    /**
     * True when [x] is over a floor gap.
     */
    fun inPit(x: Float): Boolean = pits.any { x > it.left && x < it.right }

    /**
     * Last solid floor X at or before [x], used after a fall.
     */
    fun lastSafeX(x: Float): Float {
        val before = pits.filter { it.left < x }.maxByOrNull { it.left } ?: return x
        return (before.left - 1.5f).coerceAtLeast(0f)
    }
}

/**
 * Game Boy-like brick layout for the age-3 runner, in world units.
 */
object PlatformerWorld {
    /** Left edge of the default floor gap. */
    const val PIT_LEFT: Float = 28f

    /** Right edge of the default floor gap. */
    const val PIT_RIGHT: Float = 36f

    /** Horizontal coin centres on the default short course. */
    val COIN_X: FloatArray = floatArrayOf(8f, 16f, 24f, 42f, 54f)

    /**
     * Ledge tops on the default course: x, y, width.
     */
    val LEDGES: Array<FloatArray> =
        arrayOf(
            floatArrayOf(14f, 3.5f, 8f),
            floatArrayOf(40f, 4.2f, 8f),
        )

    /** Playable default used by tests and as a fallback seed. */
    val DEFAULT: PlatformerLevel =
        PlatformerLevel(
            pits = listOf(PlatformerPit(PIT_LEFT, PIT_RIGHT)),
            ledges = LEDGES.map { PlatformerLedge(it[0], it[1], it[2]) },
            coins = COIN_X.toList(),
            length = 64f,
            skyArgb = 0xFF7EC0ED,
            skyBandArgb = 0xFF5BA3D9,
            grassArgb = 0xFF3D9E2F,
            brickArgb = 0xFFC75A1A,
            pitArgb = 0xFF1A0A08,
        )

    private val SKY: LongArray = longArrayOf(0xFF7EC0ED, 0xFFFFB74D, 0xFF4FC3F7, 0xFF5C6BC0)
    private val BAND: LongArray = longArrayOf(0xFF5BA3D9, 0xFFFF8A65, 0xFF29B6F6, 0xFF3949AB)
    private val GRASS: LongArray = longArrayOf(0xFF3D9E2F, 0xFFC0CA33, 0xFF2E7D32, 0xFF8D6E63)
    private val BRICK: LongArray = longArrayOf(0xFFC75A1A, 0xFF6D4C41, 0xFFEF6C00, 0xFF5D4037)

    /**
     * Longer course with extra pits, ledges, and coins. [seed] picks colours and layout.
     */
    fun random(seed: Int): PlatformerLevel {
        val theme = kotlin.math.abs(seed) % SKY.size
        val shift = (kotlin.math.abs(seed) % 7) * 2f
        val pits =
            listOf(
                PlatformerPit(18f + shift * 0.15f, 22.5f + shift * 0.15f),
                PlatformerPit(38f + shift * 0.1f, 42f + shift * 0.1f),
                PlatformerPit(58f, 62.5f),
                PlatformerPit(78f + theme, 81.5f + theme),
            )
        val ledges =
            listOf(
                PlatformerLedge(10f, 3.2f, 7f),
                PlatformerLedge(26f, 4.6f, 6f),
                PlatformerLedge(46f, 3.8f, 7f),
                PlatformerLedge(66f, 5.0f, 6.5f),
                PlatformerLedge(86f, 3.4f, 8f),
            )
        val coins = listOf(6f, 14f, 28f, 34f, 50f, 56f, 70f, 88f, 96f)
        return PlatformerLevel(
            pits = pits,
            ledges = ledges,
            coins = coins,
            length = 110f,
            skyArgb = SKY[theme],
            skyBandArgb = BAND[theme],
            grassArgb = GRASS[theme],
            brickArgb = BRICK[theme],
            pitArgb = 0xFF1A0A08,
        )
    }
}
