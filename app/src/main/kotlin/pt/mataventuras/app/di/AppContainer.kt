package pt.mataventuras.app.di

import android.content.Context
import android.os.Build
import androidx.room.Room
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import pt.mataventuras.data.local.MatAventurasDatabase
import pt.mataventuras.data.pin.PinRepository
import pt.mataventuras.data.repository.LocalRepository
import pt.mataventuras.data.session.LastProfileStore
import pt.mataventuras.domain.math.ExerciseGenerator
import pt.mataventuras.domain.parent.ParentAnalytics
import pt.mataventuras.domain.parent.PinPolicy
import pt.mataventuras.domain.progress.LeaderboardCalculator
import pt.mataventuras.domain.progress.RewardsEngine

/**
 * Process-local graph. Constructed in the default process only — not in
 * `:engine2d` or `:engine3d`.
 */
class AppContainer(
    context: Context,
    val pinPolicy: PinPolicy = pinPolicyForProcess(),
    val generator: ExerciseGenerator = ExerciseGenerator(),
) {
    /** Room database for this process. */
    val database: MatAventurasDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            MatAventurasDatabase::class.java,
            "mat_aventuras.db",
        ).fallbackToDestructiveMigration(dropAllTables = true)
            .apply { if (roomAllowsMainThread(processFingerprint())) allowMainThreadQueries() }
            .build()

    /** Profile and session repository. */
    val repository = LocalRepository(database)

    /** Last opened child, for the entry-screen continue shortcut. */
    val lastProfile = LastProfileStore(context.applicationContext)

    /** Parental PIN store. */
    val pinRepository = PinRepository(context.applicationContext)

    /** Points and unlocks. */
    val rewards = RewardsEngine()

    /** Local ranking. */
    val leaderboard = LeaderboardCalculator()

    /** Parental summary. */
    val analytics = ParentAnalytics()

    private val profileTouchFlow = MutableSharedFlow<Long>(extraBufferCapacity = 8)

    /**
     * Profile ids whose Room row changed outside Compose (reward bonus).
     * Lesson and home collectors reload so the +15 finish bonus is visible.
     */
    val profileTouches: SharedFlow<Long> = profileTouchFlow.asSharedFlow()

    /** Notifies open screens to reload [id] from Room. */
    fun publishProfile(id: Long) {
        profileTouchFlow.tryEmit(id)
    }
}

/**
 * Production uses 120k PBKDF2 iterations. Robolectric tests use 1k so PIN UI stays fast.
 */
internal fun pinPolicyForProcess(): PinPolicy =
    PinPolicy(iterations = pinIterationsFor(processFingerprint()))

internal fun pinIterationsFor(fingerprint: String): Int =
    if (isRobolectricFingerprint(fingerprint)) 1_000 else PinPolicy.ITERATIONS

internal fun isRobolectricFingerprint(fingerprint: String): Boolean =
    fingerprint.contains("robolectric", ignoreCase = true)

internal fun roomAllowsMainThread(fingerprint: String): Boolean = isRobolectricFingerprint(fingerprint)

internal fun processFingerprint(): String = Build.FINGERPRINT ?: ""
