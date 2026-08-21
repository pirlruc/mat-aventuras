package pt.mataventuras.app.engine

import android.os.Build

/**
 * When to host a GodotFragment. Robolectric cannot load Godot native libraries,
 * so unit tests always take the native Canvas/GLES fallback inside the plugin
 * Activity (still declared on `:engine2d` / `:engine3d` in the manifest).
 *
 * On a real device the Maven `org.godotengine:godot` AAR is on the classpath
 * and [shouldEmbed] is true.
 */
object GodotRuntime {
    /** Kart scene path inside `assets/`. */
    const val SCENE_KART: String = "res://kart.tscn"

    /** Ring-runner scene path inside `assets/`. */
    const val SCENE_RUNNER: String = "res://runner.tscn"

    /** Letter-invaders scene. */
    const val SCENE_INVADERS: String = "res://invaders.tscn"

    /** Maze-chomp scene. */
    const val SCENE_CHOMP: String = "res://chomp.tscn"

    /** Letter-climb scene. */
    const val SCENE_CLIMB: String = "res://climb.tscn"

    /** Runtime plugin name exposed to GDScript as `Engine.get_singleton`. */
    const val PLUGIN_NAME: String = "MatAventuras"

    /** Intent extra GodotActivity reads for launch arguments. Unused by GodotFragment. */
    const val EXTRA_COMMAND_LINE: String = "command_line_params"

    /** Smallest SurfaceView that may host a Godot swap. Below this the first frame is black. */
    const val SURFACE_MIN_PX: Int = 32

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
     * True when the host FrameLayout has a real pixel size for a GLES SurfaceView.
     */
    fun isSurfaceReady(
        widthPx: Int,
        heightPx: Int,
    ): Boolean = widthPx > SURFACE_MIN_PX && heightPx > SURFACE_MIN_PX

    /**
     * True when [fingerprint] is a Robolectric VM.
     */
    fun isRobolectricFingerprint(fingerprint: String): Boolean =
        fingerprint.contains("robolectric", ignoreCase = true)

    private fun processFingerprint(): String = Build.FINGERPRINT.orEmpty()
}
