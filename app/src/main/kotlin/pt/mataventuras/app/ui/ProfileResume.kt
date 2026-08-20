package pt.mataventuras.app.ui

import pt.mataventuras.data.repository.LocalRepository
import pt.mataventuras.data.session.LastProfileStore
import pt.mataventuras.domain.model.AgeGroup
import pt.mataventuras.domain.model.ChildProfile
import pt.mataventuras.domain.model.Mascot

/**
 * Loads or creates the child that the entry screen should resume.
 */
internal object ProfileResume {
    /**
     * Stored profile if it still exists, otherwise the latest Room row.
     */
    suspend fun continueCandidate(
        store: LastProfileStore,
        repository: LocalRepository,
    ): ChildProfile? {
        val stored = store.read()?.let { repository.getProfile(it) }
        if (stored != null) return stored
        return repository.latestProfile()
    }

    /**
     * Creates a profile and remembers it as the continue target.
     */
    suspend fun openNew(
        store: LastProfileStore,
        repository: LocalRepository,
        name: String,
        ageGroup: AgeGroup,
        mascot: Mascot,
    ): ChildProfile? {
        val id = repository.createProfile(name, ageGroup, mascot)
        store.save(id)
        return repository.getProfile(id)
    }

    /**
     * Remembers [profile] as the continue target after a resume tap.
     */
    suspend fun remember(
        store: LastProfileStore,
        profile: ChildProfile,
    ) {
        store.save(profile.id)
    }
}
