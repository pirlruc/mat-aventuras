package pt.mataventuras.domain.engine

import pt.mataventuras.domain.model.EngineKind

/**
 * Shared Intent and process contract for native rewards and the Godot 4 plugin.
 * Identifiers are English; the plugin Activity still speaks pt-PT HUD.
 *
 * Godot 4 is the adopted engine (MIT, official Android library, smaller heap
 * than Unity-as-a-library). It must not share the Compose process: after
 * `finish()` the runtime keeps a large native heap. Plugin Activities therefore
 * run in [PROCESS_ENGINE_3D] or [PROCESS_ENGINE_2D] so the OS kills that heap.
 */
object EnginePluginContract {
    /** Adopted engine. Unity-as-a-library is not used. */
    const val ADOPTED_ENGINE: String = "godot"

    /** Intent extra: mascot code (`speedy_hedgehog`, …). */
    const val EXTRA_MASCOT: String = "mascot"

    /** Intent extra: child display name. */
    const val EXTRA_NAME: String = "name"

    /** Intent extra: reward mini-game name (`RUNNER`, `KART`, …). */
    const val EXTRA_SCENE: String = "scene"

    /** Result extra: true when the reward level completed. */
    const val RESULT_FINISHED: String = "finished"

    /**
     * Result extra: true when Godot asked the isolated process to restart
     * (first-time GLES). The Compose host relaunches the same plugin Activity.
     */
    const val RESULT_RESTART: String = "restart"

    /** Isolated process for the 3D kart (Godot or native GLES). */
    const val PROCESS_ENGINE_3D: String = ":engine3d"

    /** Isolated process for the Godot 2D runner (and the native Canvas fallback). */
    const val PROCESS_ENGINE_2D: String = ":engine2d"

    /**
     * Fully-qualified Activity the Godot host provides for the 2D platformer.
     * Absent class → native Canvas 2D Activity.
     */
    const val PLUGIN_RUNNER_CLASS: String = "pt.mataventuras.plugin.RunnerPluginActivity"

    /**
     * Fully-qualified Activity the Godot host provides for the 3D kart.
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
     * Native 2D Canvas may stay in the Compose process for the plugin resolver
     * (`usingPlugin = false`). The fallback Activity still declares `:engine2d`
     * so a device fallback kills the Canvas heap. Any plugin, and all 3D, must
     * use an isolated process so the engine heap dies on `finish()`.
     */
    fun requiresIsolatedProcess(
        kind: EngineKind,
        usingPlugin: Boolean,
    ): Boolean = kind == EngineKind.THREE_D || usingPlugin

    /**
     * True when [processName] is an isolated reward process (`:engine2d` / `:engine3d`).
     */
    fun isIsolatedProcessName(processName: String): Boolean =
        processName.endsWith(PROCESS_ENGINE_2D) || processName.endsWith(PROCESS_ENGINE_3D)

    /**
     * True when [processName] is not the default app process. Covers `:engine2d`,
     * `:engine3d`, and Godot's unused `:phoenix` so Room never opens there.
     */
    fun isNonDefaultProcessName(processName: String): Boolean = ':' in processName

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

    /**
     * Launch extras including the packed Godot scene name.
     */
    fun launchExtras(
        mascotCode: String,
        childName: String,
        game: RewardGame,
    ): Map<String, String> = launchExtras(mascotCode, childName) + (EXTRA_SCENE to game.name)

    private val FORBIDDEN_PERMISSIONS =
        setOf(
            "INTERNET",
            "ACCESS_NETWORK_STATE",
            "ACCESS_WIFI_STATE",
            "CHANGE_NETWORK_STATE",
        )
}
