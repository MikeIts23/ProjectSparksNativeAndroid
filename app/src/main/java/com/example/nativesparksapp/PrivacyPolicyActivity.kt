package com.example.nativesparksapp

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView

class PrivacyPolicyActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)

        // Freccia per tornare indietro
        val iconBack = findViewById<ImageView>(R.id.iconBack)
        // Titolo o testo privacy (se necessario)
        val textPrivacy = findViewById<TextView>(R.id.textTitle)

        // Bottone “back”
        iconBack.setOnClickListener {
            finish()
            overridePendingTransition(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(
            android.R.anim.slide_in_left,
            android.R.anim.slide_out_right
        )
    }
}
