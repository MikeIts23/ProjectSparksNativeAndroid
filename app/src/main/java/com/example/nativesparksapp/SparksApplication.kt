package com.example.nativesparksapp

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import java.util.*

/**
 * Classe Application personalizzata per gestire la localizzazione a livello di applicazione
 */
class SparksApplication : Application() {

    companion object {
        private var instance: SparksApplication? = null

        fun getInstance(): SparksApplication {
            return instance!!
        }

        /**
         * Aggiorna la configurazione della lingua per tutta l'applicazione
         */
        fun updateLocale(context: Context): Context {
            val language = LocaleHelper.getLanguage(context)
            return setLocale(context, language)
        }

        /**
         * Imposta la lingua per il contesto fornito
         */
        fun setLocale(context: Context, language: String): Context {
            // Salva la lingua nelle preferenze
            LocaleHelper.setLocale(context, language)

            // Aggiorna la configurazione
            val locale = Locale(language)
            Locale.setDefault(locale)

            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)

            return context.createConfigurationContext(config)
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun attachBaseContext(base: Context) {
        // Applica la lingua corrente al contesto base dell'applicazione
        super.attachBaseContext(updateLocale(base))
    }
}
