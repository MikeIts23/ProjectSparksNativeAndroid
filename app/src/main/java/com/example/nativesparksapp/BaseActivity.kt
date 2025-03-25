package com.example.nativesparksapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

/**
 * Classe base per tutte le activity che devono supportare il cambio lingua
 */
open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val language = LocaleHelper.getLanguage(newBase)
        super.attachBaseContext(LocaleHelper.setLocale(newBase, language))
    }
}
