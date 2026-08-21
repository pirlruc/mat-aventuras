package pt.mataventuras.domain.engine

/**
 * Enemies and transforming pickups for the age-3 runner.
 */
object PlatformerHazards {
    /** Grown mascot after a mushroom. */
    const val FORM_GROWN: Int = 1

    /** Star form: stomps on contact. */
    const val FORM_STAR: Int = 2

    /**
     * Patrol X for [enemy] at [time] seconds, ping-ponging between min and max.
     */
    fun enemyX(
        enemy: PlatformerEnemy,
        time: Float,
    ): Float {
        val span = (enemy.maxX - enemy.minX).coerceAtLeast(0.5f)
        val cycle = span * 2f
        val phase = ((time * enemy.speed) % cycle + cycle) % cycle
        return if (phase <= span) enemy.minX + phase else enemy.maxX - (phase - span)
    }

    /**
     * Collects pickups and resolves enemy bumps on [state].
     */
    fun apply(
        state: Platformer2dState,
        level: PlatformerLevel,
        dt: Float,
    ): Platformer2dState {
        val collected = collectPower(state, level)
        val faded = fadeStar(collected, dt)
        return bump(faded, level)
    }

    private fun collectPower(
        state: Platformer2dState,
        level: PlatformerLevel,
    ): Platformer2dState {
        var form = state.form
        var star = state.starTimer
        var mask = state.powerMask
        level.powerups.forEachIndexed { i, item ->
            val bit = 1 shl i
            if (mask and bit != 0) return@forEachIndexed
            if (kotlin.math.abs(state.x - item.x) > 1.1f) return@forEachIndexed
            mask = mask or bit
            if (item.grow) form = maxOf(form, FORM_GROWN)
            if (!item.grow) {
                form = FORM_STAR
                star = 3.2f
            }
        }
        return state.copy(form = form, starTimer = star, powerMask = mask)
    }

    private fun fadeStar(
        state: Platformer2dState,
        dt: Float,
    ): Platformer2dState {
        val timer = (state.starTimer - dt).coerceAtLeast(0f)
        val form = if (timer <= 0f && state.form == FORM_STAR) FORM_GROWN else state.form
        return state.copy(starTimer = timer, form = form)
    }

    private fun bump(
        state: Platformer2dState,
        level: PlatformerLevel,
    ): Platformer2dState {
        var mask = state.stompedMask
        var form = state.form
        var hurt = false
        level.enemies.forEachIndexed { i, enemy ->
            val bit = 1 shl i
            if (mask and bit != 0) return@forEachIndexed
            val ex = enemyX(enemy, state.elapsed)
            if (kotlin.math.abs(state.x - ex) > 1.0f) return@forEachIndexed
            if (!state.onGround || form == FORM_STAR) {
                mask = mask or bit
            } else if (form >= FORM_GROWN) {
                form = 0
            } else {
                hurt = true
            }
        }
        if (hurt) {
            return state.copy(
                x = level.lastSafeX(state.x),
                y = 0f,
                vx = 0f,
                vy = 0f,
                onGround = true,
                inPitFall = false,
                stompedMask = mask,
                form = 0,
            )
        }
        return state.copy(stompedMask = mask, form = form)
    }
}
