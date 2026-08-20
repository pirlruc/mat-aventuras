package pt.mataventuras.app

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import pt.mataventuras.app.di.AppContainer
import pt.mataventuras.domain.engine.EnginePluginContract

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
        if (!shouldOpenContainer(currentProcessName(this))) return
        container = AppContainer(this)
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
internal fun currentProcessName(
    context: Context,
    sdk: Int = Build.VERSION.SDK_INT,
    pid: Int = android.os.Process.myPid(),
): String {
    if (sdk >= Build.VERSION_CODES.P) {
        return getProcessName()
    }
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    return manager.runningAppProcesses?.firstOrNull { it.pid == pid }?.processName.orEmpty()
}
