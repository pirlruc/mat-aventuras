package pt.mataventuras.app.entitlement

import android.app.Activity
import android.content.Context
import android.os.LocaleList
import java.util.Locale
import pt.mataventuras.app.BuildConfig
import pt.mataventuras.domain.entitlement.Entitlements
import pt.mataventuras.domain.entitlement.PremiumFeature
import pt.mataventuras.domain.entitlement.WorldTheme

/**
 * Reads [BuildConfig.PREMIUM_UNLOCKED]. Gradle property `premiumUnlocked` defaults to true.
 * The APK holds a boolean, not a billing secret.
 */
class BuildConfigEntitlements(
    private val unlocked: Boolean = BuildConfig.PREMIUM_UNLOCKED,
) : Entitlements {
    override fun isUnlocked(feature: PremiumFeature): Boolean =
        when (feature) {
            PremiumFeature.THEMED_WORLDS,
            PremiumFeature.SEASONAL_PRIZES,
            PremiumFeature.LANGUAGE_PACKS,
            -> unlocked
        }
}

/**
 * Bundled language tags. The child UI does not list these.
 */
object LanguagePacks {
    /** Portuguese from Portugal. */
    const val PORTUGUESE: String = "pt-PT"

    /** Packs shipped in resources. */
    val TAGS: List<String> = listOf(PORTUGUESE, "en", "es", "fr", "pt-BR")

    /**
     * A supported tag, or [PORTUGUESE] when [raw] is not a bundled pack.
     */
    fun canonical(raw: String?): String = TAGS.firstOrNull { it.equals(raw, ignoreCase = true) } ?: PORTUGUESE
}

/**
 * On-device Family Plus choices. No account and no network.
 */
class FamilySettings(
    context: Context,
) {
    private val prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    /**
     * Parent-selected runner theme.
     */
    fun theme(): WorldTheme = WorldTheme.fromStored(prefs.getString(THEME, null))

    /**
     * Stores [theme] before the next prize starts.
     */
    fun saveTheme(theme: WorldTheme) {
        prefs.edit().putString(THEME, theme.name).commit()
    }

    /**
     * BCP-47 tag for the bundled pack. Defaults to Portuguese from Portugal.
     */
    fun language(): String = LanguagePacks.canonical(prefs.getString(LANGUAGE, null))

    /**
     * Stores [tag] for the next composition and the Godot process.
     */
    fun saveLanguage(tag: String) {
        prefs.edit().putString(LANGUAGE, LanguagePacks.canonical(tag)).commit()
    }

    private companion object {
        const val FILE: String = "family_settings"
        const val THEME: String = "theme"
        const val LANGUAGE: String = "language"
    }
}

/**
 * Applies the stored pack so string resources and Godot copy follow it.
 */
object AppLanguage {
    /**
     * Context whose configuration locale is the stored pack.
     */
    fun wrap(base: Context): Context {
        val tag = FamilySettings(base).language()
        val locale = Locale.forLanguageTag(tag)
        Locale.setDefault(locale)
        val config = android.content.res.Configuration(base.resources.configuration)
        config.setLocales(LocaleList.forLanguageTags(tag))
        config.setLayoutDirection(locale)
        @Suppress("DEPRECATION")
        base.resources.updateConfiguration(config, base.resources.displayMetrics)
        return base.createConfigurationContext(config)
    }
}

/**
 * When false, the language is stored and the activity stays up. Tests use this so a
 * Robolectric recreate does not drop the preference write before the assertion.
 */
internal var recreateOnLanguage: Boolean = true

/**
 * Saves [tag] and recreates [activity] when the parent picker is on screen.
 */
internal fun applyLanguage(
    activity: Activity?,
    settings: FamilySettings,
    tag: String,
) {
    settings.saveLanguage(tag)
    if (!recreateOnLanguage) return
    activity?.recreate()
}
