package pt.mataventuras.app

import android.app.Application
import android.os.Build
import pt.mataventuras.app.di.AppContainer
import pt.mataventuras.domain.engine.EnginePluginContract
import java.io.File

/**
 * Default-process application. Holds [AppContainer] for Compose screens.
 * Isolated engine processes (`:engine2d`, `:engine3d`) skip Room so Godot/GLES
 * heaps stay killable and never share the host database.
 */
class MatAventurasApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        bindHostGraph()
    }

    /**
     * Opens Room only in the Compose process. Isolated engine processes return false.
     */
    internal fun bindHostGraph(processName: String = currentProcessName()): Boolean {
        if (!shouldOpenContainer(processName)) return false
        container = AppContainer(this)
        return true
    }
}

/**
 * Room stays in the Compose process only.
 */
internal fun shouldOpenContainer(processName: String): Boolean =
    !EnginePluginContract.isIsolatedProcessName(processName)

/**
 * Process name for this VM (`package:engine3d` in an isolated reward process).
 */
internal fun currentProcessName(name: String = resolveProcessName()): String = name

/**
 * Resolves the VM process name. [Application.getProcessName] is API 28+;
 * API 26–27 tablets read `/proc/self/cmdline` so `:engine2d` / `:engine3d`
 * still skip Room.
 */
internal fun resolveProcessName(
    sdk: Int = Build.VERSION.SDK_INT,
    api28Name: String? = null,
    procCmdline: String? = null,
): String {
    if (sdk >= Build.VERSION_CODES.P) {
        return api28Name ?: hostProcessName()
    }
    return parseProcCmdline(
        procCmdline ?: procCmdlineOrEmpty { File("/proc/self/cmdline").readText() },
    )
}

/**
 * Null-terminated cmdline from procfs, trimmed to the process name.
 */
internal fun parseProcCmdline(raw: String): String = raw.trim('\u0000', ' ', '\n', '\t', '\r')

/**
 * API 28+ process name. Guarded so lint accepts the minSdk 26 compile.
 */
internal fun hostProcessName(): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        Application.getProcessName()
    } else {
        parseProcCmdline(procCmdlineOrEmpty { File("/proc/self/cmdline").readText() })
    }

/**
 * Reads [block] and returns an empty string when procfs is unreadable.
 */
internal fun procCmdlineOrEmpty(block: () -> String): String =
    try {
        block()
    } catch (_: Exception) {
        ""
    }
