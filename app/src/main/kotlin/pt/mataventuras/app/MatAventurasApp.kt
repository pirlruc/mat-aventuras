package pt.mataventuras.app

import android.app.Application
import pt.mataventuras.app.di.AppContainer

/**
 * Default-process application. Holds [AppContainer] for Compose screens.
 */
class MatAventurasApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
