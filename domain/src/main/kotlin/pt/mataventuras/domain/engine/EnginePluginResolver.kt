package pt.mataventuras.domain.engine

import pt.mataventuras.domain.model.EngineKind

/**
 * Chooses the reward Activity class. A Godot/Unity AAR wins when its contract
 * class is on the classpath; otherwise the native Kotlin engine is used.
 */
object EnginePluginResolver {
    /**
     * Fully-qualified Activity to start for [kind].
     */
    fun classNameFor(
        kind: EngineKind,
        pluginPresent: (String) -> Boolean,
        nativeTwoD: String,
        nativeThreeD: String,
    ): String {
        val plugin = EnginePluginContract.pluginClassName(kind)
        if (pluginPresent(plugin)) return plugin
        return if (kind == EngineKind.TWO_D) nativeTwoD else nativeThreeD
    }
}
