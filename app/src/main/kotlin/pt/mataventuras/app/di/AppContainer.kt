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

/**
 * Process-local graph. Constructed in the default process only — not in `:engine3d`.
 */
class AppContainer(context: Context) {
    /** Room database for this process. */
    val database: MatAventurasDatabase = Room.databaseBuilder(
        context.applicationContext,
        MatAventurasDatabase::class.java,
        "mat_aventuras.db",
    ).fallbackToDestructiveMigration().build()

    /** Profile and session repository. */
    val repository = LocalRepository(database)

    /** Parental PIN store. */
    val pinRepository = PinRepository(context.applicationContext)

    /** Exercise factory. */
    val generator = ExerciseGenerator()

    /** Points and unlocks. */
    val rewards = RewardsEngine()

    /** Local ranking. */
    val leaderboard = LeaderboardCalculator()

    /** Parental summary. */
    val analytics = ParentAnalytics()

    /** PIN hashing policy. */
    val pinPolicy = PinPolicy()
}
