package pt.mataventuras.app.engine

import android.content.Intent
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

    /** Runtime plugin name exposed to GDScript as `Engine.get_singleton`. */
    const val PLUGIN_NAME: String = "MatAventuras"

    /** Intent extra GodotActivity reads for launch arguments. Unused by GodotFragment. */
    const val EXTRA_COMMAND_LINE: String = "command_line_params"

    /** Per-process files-dir marker so a GLES rebirth cannot loop. */
    const val REBIRTH_MARKER: String = "godot_isolated_rebirth"

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
     * True when the isolated `:engine2d` / `:engine3d` process should be killed
     * and the same plugin Activity relaunched. Activity.recreate() cannot unload
     * `libgodot_android`. ProcessPhoenix would restart the Compose host.
     */
    fun shouldRestartHost(
        alreadyRestarted: Boolean,
        finishing: Boolean,
        destroyed: Boolean,
        rebirthConsumed: Boolean,
    ): Boolean = !alreadyRestarted && !finishing && !destroyed && !rebirthConsumed

    /**
     * Intent that relaunches [className] in a new isolated process after this one exits.
     */
    fun isolatedRebirthIntent(
        source: Intent,
        className: String,
    ): Intent {
        val next = Intent(source)
        val pkg = source.component?.packageName ?: source.`package`
        if (!pkg.isNullOrBlank()) {
            next.setClassName(pkg, className)
        }
        return next.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }

    /**
     * True when [fingerprint] is a Robolectric VM.
     */
    fun isRobolectricFingerprint(fingerprint: String): Boolean =
        fingerprint.contains("robolectric", ignoreCase = true)

    private fun processFingerprint(): String = Build.FINGERPRINT.orEmpty()
}
