package com.example.nativesparksapp

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import java.util.*

/**
 * Classe di supporto per la gestione delle lingue nell'app
 */
object LocaleHelper {
    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"

    /**
     * Salva la lingua selezionata nelle SharedPreferences
     */
    fun setLocale(context: Context, language: String): Context {
        saveLanguage(context, language)
        return updateResources(context, language)
    }

    /**
     * Ottiene la lingua corrente dalle SharedPreferences
     */
    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(ProfileActivity.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(SELECTED_LANGUAGE, "en") ?: "en"
    }

    /**
     * Salva la lingua selezionata nelle SharedPreferences
     */
    private fun saveLanguage(context: Context, language: String) {
        val prefs = context.getSharedPreferences(ProfileActivity.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(SELECTED_LANGUAGE, language).apply()
    }

    /**
     * Aggiorna le risorse dell'app con la lingua selezionata
     */
    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)

        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }

    /**
     * Ottiene il nome della lingua corrente in base al codice
     */
    fun getLanguageName(context: Context, languageCode: String): String {
        return when (languageCode) {
            "en" -> context.getString(R.string.english)
            "it" -> context.getString(R.string.italian)
            "fr" -> context.getString(R.string.french)
            else -> context.getString(R.string.english)
        }
    }

    /**
     * Ottiene l'emoji della bandiera in base al codice lingua
     */
    fun getLanguageFlag(languageCode: String): String {
        return when (languageCode) {
            "en" -> "🇬🇧"
            "it" -> "🇮🇹"
            "fr" -> "🇫🇷"
            else -> "🇬🇧"
        }
    }
}
