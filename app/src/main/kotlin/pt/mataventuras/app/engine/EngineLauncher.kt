package pt.mataventuras.app.engine

import android.content.Context
import android.content.Intent
import pt.mataventuras.domain.engine.EnginePluginContract
import pt.mataventuras.domain.engine.EnginePluginResolver
import pt.mataventuras.domain.model.AgeGroup
import pt.mataventuras.domain.model.EngineKind
import pt.mataventuras.domain.model.Mascot
import pt.mataventuras.domain.model.engineKindFor
import pt.mataventuras.domain.model.pickRewardKind

/**
 * Native host → reward engine. Age 3 launches the Godot runner in `:engine2d`;
 * age 7 randomly launches that platformer or the Godot kart in `:engine3d`.
 * Native Canvas/GLES Activities remain as the fallback when plugin classes
 * are absent and for unit tests.
 *
 * Plugin contract (MAT-003): a Godot Activity replaces
 * [Kart3dActivity] or [Platformer2dActivity] when it:
 * 1. uses `android:process=":engine3d"` (or `:engine2d`) so the heap dies on finish,
 * 2. reads [EXTRA_MASCOT] and [EXTRA_NAME] from the launching Intent,
 * 3. returns [RESULT_FINISHED] via `setResult`,
 * 4. does not open Room, request INTERNET, or start analytics/cloud save.
 *
 * The Compose host must not hold a Godot view.
 */
object EngineLauncher {
    /** Intent extra: mascot code. */
    const val EXTRA_MASCOT: String = EnginePluginContract.EXTRA_MASCOT

    /** Intent extra: child display name. */
    const val EXTRA_NAME: String = EnginePluginContract.EXTRA_NAME

    /** Result extra: the reward level finished. */
    const val RESULT_FINISHED: String = EnginePluginContract.RESULT_FINISHED

    /**
     * Isolated process name for the 3D (Godot or native GLES) Activity.
     */
    const val PROCESS_ENGINE_3D: String = EnginePluginContract.PROCESS_ENGINE_3D

    /**
     * Isolated process name for the Godot 2D runner.
     */
    const val PROCESS_ENGINE_2D: String = EnginePluginContract.PROCESS_ENGINE_2D

    /**
     * True when the reward Activity completed the level (host may award bonus points).
     */
    fun isFinished(
        resultCode: Int,
        finishedExtra: Boolean,
    ): Boolean = resultCode == android.app.Activity.RESULT_OK && finishedExtra

    /**
     * True when [className] can be loaded by the app class loader.
     */
    fun isClassPresent(className: String): Boolean =
        try {
            Class.forName(className)
            true
        } catch (_: ClassNotFoundException) {
            false
        }

    /**
     * Intent for the reward Activity. [kind] defaults to a random pick at age 7.
     * [pluginPresent] is injected in tests; production uses [isClassPresent].
     */
    fun intentFor(
        context: Context,
        ageGroup: AgeGroup,
        mascot: Mascot,
        name: String,
        kind: EngineKind = pickRewardKind(ageGroup),
        pluginPresent: (String) -> Boolean = { isClassPresent(it) },
    ): Intent {
        val className =
            EnginePluginResolver.classNameFor(
                kind = kind,
                pluginPresent = pluginPresent,
                nativeTwoD = Platformer2dActivity::class.java.name,
                nativeThreeD = Kart3dActivity::class.java.name,
            )
        return Intent().setClassName(context.packageName, className).apply {
            EnginePluginContract.launchExtras(mascot.code, name).forEach { (key, value) ->
                putExtra(key, value)
            }
        }
    }

    /**
     * Isolated process the destination Activity should declare for [ageGroup].
     */
    fun processFor(
        ageGroup: AgeGroup,
        usingPlugin: Boolean,
    ): String? {
        val kind = engineKindFor(ageGroup)
        if (!EnginePluginContract.requiresIsolatedProcess(kind, usingPlugin)) return null
        return EnginePluginContract.processFor(kind)
    }

    /**
     * True when [kind] would launch a plugin class under [pluginPresent].
     */
    fun wouldUsePlugin(
        kind: EngineKind,
        pluginPresent: (String) -> Boolean,
    ): Boolean = pluginPresent(EnginePluginContract.pluginClassName(kind))
}
