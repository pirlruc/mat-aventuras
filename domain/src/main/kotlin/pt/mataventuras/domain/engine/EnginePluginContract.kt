package pt.mataventuras.domain.engine

import pt.mataventuras.domain.model.EngineKind

/**
 * Shared Intent and process contract for native rewards and a later Godot/Unity
 * plugin. Identifiers are English; the plugin Activity still speaks pt-PT HUD.
 *
 * Godot and Unity are valid on Android. They must not share the Compose
 * process: after `finish()` those runtimes keep a large native heap. The
 * plugin Activity therefore runs in [PROCESS_ENGINE_3D] or [PROCESS_ENGINE_2D]
 * so the OS kills that heap. The game does **not** need a custom engine
 * library — only a thin Activity that wraps a stock Godot 4 `aar` or
 * Unity-as-a-library export and keeps this contract.
 */
object EnginePluginContract {
    /** Intent extra: mascot code (`speedy_hedgehog`, …). */
    const val EXTRA_MASCOT: String = "mascot"

    /** Intent extra: child display name. */
    const val EXTRA_NAME: String = "name"

    /** Result extra: true when the reward level completed. */
    const val RESULT_FINISHED: String = "finished"

    /** Isolated process for the 3D kart (native GLES or plugin). */
    const val PROCESS_ENGINE_3D: String = ":engine3d"

    /** Isolated process for a plugin 2D runner (Godot/Unity). Native Canvas stays in-process. */
    const val PROCESS_ENGINE_2D: String = ":engine2d"

    /**
     * Fully-qualified Activity a Godot/Unity AAR must provide for age 3.
     * Absent class → native Canvas 2D Activity.
     */
    const val PLUGIN_RUNNER_CLASS: String = "pt.mataventuras.plugin.RunnerPluginActivity"

    /**
     * Fully-qualified Activity a Godot/Unity AAR must provide for age 7.
     * Absent class → native GLES kart Activity.
     */
    const val PLUGIN_KART_CLASS: String = "pt.mataventuras.plugin.KartPluginActivity"

    /**
     * Process name the plugin Activity must declare.
     */
    fun processFor(kind: EngineKind): String =
        when (kind) {
            EngineKind.TWO_D -> PROCESS_ENGINE_2D
            EngineKind.THREE_D -> PROCESS_ENGINE_3D
        }

    /**
     * Class name the host looks up on the classpath before falling back to native.
     */
    fun pluginClassName(kind: EngineKind): String =
        when (kind) {
            EngineKind.TWO_D -> PLUGIN_RUNNER_CLASS
            EngineKind.THREE_D -> PLUGIN_KART_CLASS
        }

    /**
     * Native 2D Canvas may stay in the Compose process. Any plugin, and all 3D,
     * must use an isolated process so the engine heap dies on `finish()`.
     */
    fun requiresIsolatedProcess(
        kind: EngineKind,
        usingPlugin: Boolean,
    ): Boolean = kind == EngineKind.THREE_D || usingPlugin

    /**
     * True when [permission] must not appear on a plugin (or host) manifest.
     */
    fun isForbiddenPermission(permission: String): Boolean {
        val name = permission.substringAfterLast('.')
        return name in FORBIDDEN_PERMISSIONS
    }

    /**
     * True when a merged manifest permission set is still local-only.
     */
    fun manifestAllowed(permissions: Collection<String>): Boolean = permissions.none { isForbiddenPermission(it) }

    /**
     * Launch extras a plugin Activity must read from its Intent.
     */
    fun launchExtras(
        mascotCode: String,
        childName: String,
    ): Map<String, String> =
        mapOf(
            EXTRA_MASCOT to mascotCode,
            EXTRA_NAME to childName,
        )

    private val FORBIDDEN_PERMISSIONS =
        setOf(
            "INTERNET",
            "ACCESS_NETWORK_STATE",
            "ACCESS_WIFI_STATE",
            "CHANGE_NETWORK_STATE",
        )
}
