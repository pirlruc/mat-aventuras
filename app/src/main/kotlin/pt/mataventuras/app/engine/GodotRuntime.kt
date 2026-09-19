package pt.mataventuras.app.engine

import android.os.Build

/**
 * When to host a GodotFragment. Robolectric cannot load Godot native libraries,
 * so unit tests always take the native Canvas fallback inside the plugin
 * Activity (still declared on `:engine2d` / `:engine3d` in the manifest).
 *
 * On a real device the Maven `org.godotengine:godot` AAR is on the classpath
 * and [shouldEmbed] is true.
 */
object GodotRuntime {
    /** Runtime plugin name exposed to GDScript as `Engine.get_singleton`. */
    const val PLUGIN_NAME: String = "MatAventuras"

    /**
     * True when this process should create a GodotFragment.
     */
    fun shouldEmbed(fingerprint: String = processFingerprint()): Boolean =
        !isRobolectricFingerprint(fingerprint)

    /**
     * Command line for a packaged Godot 4 Android-library project.
     *
     * Keep this empty. Godot 4.6+ loads `project.godot` from APK assets and
     * treats `--path` as a CWD override (blank screen). `--scene` races the
     * packaged `run/main_scene` (`boot.tscn`). GLES is already set in
     * `project.godot`; repeating it on the CLI is what asked the engine to
     * restart, which then blinked the splash.
     */
    fun commandLineFor(): List<String> = emptyList()

    /**
     * True when this isolated plugin Activity should ask the Compose host to
     * relaunch it. Activity.recreate() cannot unload `libgodot_android`.
     * Godot's ProcessPhoenix stays stripped: a default-intent rebirth would
     * reincarnate the Compose host, and starting the same `singleInstance`
     * Activity from a dying `:engine2d` / `:engine3d` process drops the
     * host's `StartActivityForResult` contract.
     */
    fun shouldRestartHost(
        alreadyRestarted: Boolean,
        finishing: Boolean,
        destroyed: Boolean,
        fromRelaunch: Boolean,
    ): Boolean = !alreadyRestarted && !finishing && !destroyed && !fromRelaunch

    /**
     * True when [fingerprint] is a Robolectric VM.
     */
    fun isRobolectricFingerprint(fingerprint: String): Boolean =
        fingerprint.contains("robolectric", ignoreCase = true)

    private fun processFingerprint(): String = Build.FINGERPRINT.orEmpty()
}
