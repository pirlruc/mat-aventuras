package pt.mataventuras.app.di

import android.content.Context
import androidx.room.Room
import pt.mataventuras.data.local.MatAventurasDatabase
import pt.mataventuras.data.pin.PinRepository
import pt.mataventuras.data.repository.LocalRepository
import pt.mataventuras.domain.math.ExerciseGenerator
import pt.mataventuras.domain.parent.ParentAnalytics
import pt.mataventuras.domain.parent.PinPolicy
import pt.mataventuras.domain.progress.LeaderboardCalculator
import pt.mataventuras.domain.progress.RewardsEngine

class AppContainer(context: Context) {
    val database: MatAventurasDatabase = Room.databaseBuilder(
        context.applicationContext,
        MatAventurasDatabase::class.java,
        "mat_aventuras.db",
    ).fallbackToDestructiveMigration().build()

    val repository = LocalRepository(database)
    val pinRepository = PinRepository(context.applicationContext)
    val generator = ExerciseGenerator()
    val rewards = RewardsEngine()
    val leaderboard = LeaderboardCalculator()
    val analytics = ParentAnalytics()
    val pinPolicy = PinPolicy()
}
