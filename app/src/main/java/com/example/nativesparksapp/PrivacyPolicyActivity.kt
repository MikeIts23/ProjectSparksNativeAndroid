package com.example.nativesparksapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PrivacyPolicyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)

        // 1. Recupera la freccia indietro (iconaBack) e il TextView
        val iconBack = findViewById<ImageView>(R.id.iconBack)
        val textPrivacy = findViewById<TextView>(R.id.textTitle)

        // 3. Navigazione indietro → torna a ProfileActivity
        iconBack.setOnClickListener {
            finish()
            overridePendingTransition(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
        }
    }

    // Se vuoi usare la back navigation di sistema (tasto indietro)
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(
            android.R.anim.slide_in_left,  // animazione entrata
            android.R.anim.slide_out_right // animazione uscita
        )
    }
}
