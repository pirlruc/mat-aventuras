package pt.mataventuras.app

import android.app.Application
import pt.mataventuras.app.di.ContentorAplicacao

class MatAventurasApp : Application() {
    lateinit var contentor: ContentorAplicacao
        private set

    override fun onCreate() {
        super.onCreate()
        contentor = ContentorAplicacao(this)
    }
}
