package pt.mataventuras.app

import android.app.Application
import pt.mataventuras.app.di.AppContainer

class MatAventurasApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
