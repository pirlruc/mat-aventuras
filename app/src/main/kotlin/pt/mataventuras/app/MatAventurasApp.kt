package pt.mataventuras.app

import android.app.Application
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
        if (!shouldOpenContainer(currentProcessName())) return
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
internal fun currentProcessName(name: String = Application.getProcessName()): String = name
