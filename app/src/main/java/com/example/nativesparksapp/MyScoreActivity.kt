package com.example.nativesparksapp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class MyScoreActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_score)

        BottomNavigationHelper.setupBottomNavigation(this)

        val textScore1 = findViewById<TextView>(R.id.textScore1)
        val textScore2 = findViewById<TextView>(R.id.textScore2)
        val cardMode1  = findViewById<MaterialCardView>(R.id.cardMode1)
        val cardMode2  = findViewById<MaterialCardView>(R.id.cardMode2)

        textScore1.text = "0"
        textScore2.text = "0"

        cardMode1.setOnClickListener {
            startActivity(Intent(this, GameLaunchActivity::class.java))
        }

        cardMode2.setOnClickListener {
            startActivity(Intent(this, GameLaunchActivity::class.java))
        }
    }
}
