package pt.mataventuras.app.di

import android.content.Context
import androidx.room.Room
import pt.mataventuras.dados.local.BaseDadosMatAventuras
import pt.mataventuras.dados.pin.RepositorioPin
import pt.mataventuras.dados.repositorio.RepositorioLocal
import pt.mataventuras.dominio.matematica.GeradorExercicios
import pt.mataventuras.dominio.pais.AnaliseParental
import pt.mataventuras.dominio.pais.PoliticaPin
import pt.mataventuras.dominio.progresso.CalculadoraClassificacao
import pt.mataventuras.dominio.progresso.MotorRecompensas

class ContentorAplicacao(contexto: Context) {
    val baseDados: BaseDadosMatAventuras = Room.databaseBuilder(
        contexto.applicationContext,
        BaseDadosMatAventuras::class.java,
        "mat_aventuras.db",
    ).fallbackToDestructiveMigration().build()

    val repositorio = RepositorioLocal(baseDados)
    val repositorioPin = RepositorioPin(contexto.applicationContext)
    val gerador = GeradorExercicios()
    val recompensas = MotorRecompensas()
    val classificacao = CalculadoraClassificacao()
    val analise = AnaliseParental()
    val politicaPin = PoliticaPin()
}
