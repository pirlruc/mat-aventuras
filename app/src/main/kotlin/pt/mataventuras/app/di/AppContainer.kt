package pt.mataventuras.app.di

import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import androidx.room.Room
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import pt.mataventuras.app.engine.GodotRuntime
import pt.mataventuras.data.local.MIGRATION_1_2
import pt.mataventuras.data.local.MIGRATION_2_3
import pt.mataventuras.data.local.MIGRATION_3_4
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
    val pinPolicy: PinPolicy = pinPolicyForProcess(context),
    generator: ExerciseGenerator = ExerciseGenerator(),
) {
    private var exerciseGenerator: ExerciseGenerator = generator

    /** Lesson generator. Tests replace it with [useGenerator]. */
    val generator: ExerciseGenerator
        get() = exerciseGenerator

    /**
     * Swaps the lesson generator. Production never calls this.
     */
    internal fun useGenerator(next: ExerciseGenerator) {
        exerciseGenerator = next
    }

    /** Room database for this process. */
    val database: MatAventurasDatabase =
        Room
            .databaseBuilder(
                context.applicationContext,
                MatAventurasDatabase::class.java,
                "mat_aventuras.db",
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .apply { if (roomAllowsMainThread(processFingerprint())) allowMainThreadQueries() }
            .build()

    /** Profile and session repository. */
    val repository = LocalRepository(database)

    /** Last opened child, for the entry-screen continue shortcut. */
    val lastProfile = LastProfileStore(context.applicationContext)

    /** Parental PIN store. */
    val pinRepository =
        PinRepository(
            context.applicationContext,
            allowPlaintextFallback = GodotRuntime.isRobolectricFingerprint(processFingerprint()),
        )

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
 * Production uses 600k PBKDF2 iterations. Robolectric tests use 1k so PIN UI stays fast.
 */
internal fun pinPolicyForProcess(context: Context): PinPolicy =
    PinPolicy(
        iterations = pinIterationsFor(processFingerprint()),
        elapsed = { SystemClock.elapsedRealtime() },
        bootCount = {
            Settings.Global.getInt(context.contentResolver, Settings.Global.BOOT_COUNT, 0)
        },
    )

internal fun pinIterationsFor(fingerprint: String): Int =
    if (GodotRuntime.isRobolectricFingerprint(fingerprint)) 1_000 else PinPolicy.ITERATIONS

internal fun roomAllowsMainThread(fingerprint: String): Boolean = GodotRuntime.isRobolectricFingerprint(fingerprint)

internal fun processFingerprint(): String = Build.FINGERPRINT ?: ""
