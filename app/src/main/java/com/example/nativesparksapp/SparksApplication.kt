package com.example.nativesparksapp

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import java.util.*

class SparksApplication : Application() {

    companion object {
        private var instance: SparksApplication? = null

        fun getInstance(): SparksApplication {
            return instance!!
        }

        fun updateLocale(context: Context): Context {
            val savedLanguage = LocaleHelper.getLanguage(context)
            val languageToApply = if (savedLanguage.isNullOrEmpty()) "en" else savedLanguage
            return setLocale(context, languageToApply)
        }

        fun setLocale(context: Context, language: String): Context {
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
        super.attachBaseContext(updateLocale(base))
    }
}
