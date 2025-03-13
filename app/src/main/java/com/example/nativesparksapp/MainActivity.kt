package com.example.nativesparksapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Abilita la modalità edge-to-edge
        window.decorView.systemUiVisibility = (
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                )
        setContentView(R.layout.activity_main)

        // Assicurati che nel layout activity_main.xml esista un View con id "main"
        val mainView = findViewById<android.view.View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { view, insets ->
            // Ottieni gli inset dei system bars (status bar, navigation bar, ecc.)
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Applica i padding al view
            view.updatePadding(
                left = systemBarsInsets.left,
                top = systemBarsInsets.top,
                right = systemBarsInsets.right,
                bottom = systemBarsInsets.bottom
            )
            insets
        }
    }
}
