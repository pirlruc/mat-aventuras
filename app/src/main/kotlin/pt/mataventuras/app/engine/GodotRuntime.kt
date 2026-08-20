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

    /** Runtime plugin name exposed to GDScript as `Engine.get_singleton`. */
    const val PLUGIN_NAME: String = "MatAventuras"

    /**
     * True when this process should create a GodotFragment.
     */
    fun shouldEmbed(fingerprint: String = processFingerprint()): Boolean =
        !isRobolectricFingerprint(fingerprint)

    /**
     * Command line for a packaged Godot project in the APK assets root.
     */
    fun commandLineFor(scene: String): List<String> = listOf("--path", ".", "--scene", scene)

    /**
     * True when [fingerprint] is a Robolectric VM.
     */
    fun isRobolectricFingerprint(fingerprint: String): Boolean =
        fingerprint.contains("robolectric", ignoreCase = true)

    private fun processFingerprint(): String = Build.FINGERPRINT.orEmpty()
}
