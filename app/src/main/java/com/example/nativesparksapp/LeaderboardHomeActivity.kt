package com.example.nativesparksapp

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class LeaderboardHomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard_home)

        val cardMyScore = findViewById<MaterialCardView>(R.id.cardMyScore)
        val cardLeaderboard = findViewById<MaterialCardView>(R.id.cardLeaderboard)

        val anim = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)

        cardMyScore.setOnClickListener {
            cardMyScore.startAnimation(anim)
            val intent = Intent(this, MyScoreActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        cardLeaderboard.setOnClickListener {
            cardLeaderboard.startAnimation(anim)
            val intent = Intent(this, LeaderboardActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}
