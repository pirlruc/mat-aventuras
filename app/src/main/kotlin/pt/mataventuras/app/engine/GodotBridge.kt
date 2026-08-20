package pt.mataventuras.app.engine

/**
 * JVM bridge used by GDScript (`Engine.get_singleton("MatAventuras")`).
 * Kept free of Godot types so Robolectric can cover it.
 */
internal object GodotBridge {
    /**
     * Mascot extra for the Godot HUD tint.
     */
    fun mascotCode(host: IsolatedEngineActivity): String = host.mascotCode()

    /**
     * Child display name extra.
     */
    fun childName(host: IsolatedEngineActivity): String = host.childName()

    /**
     * Completes the reward on the UI thread and finishes the isolated process.
     */
    fun finish(
        host: IsolatedEngineActivity,
        ok: Boolean,
    ) {
        host.completeRewardOnUi(ok)
    }
}
