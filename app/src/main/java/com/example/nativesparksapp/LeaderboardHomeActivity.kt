package com.example.nativesparksapp

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import com.google.android.material.card.MaterialCardView

/** Home della sezione Leaderboard (due card: “My Score” e “Leaderboard”). */
class LeaderboardHomeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard_home)

        /* ---------- Bottom‑Navigation ---------- */
        BottomNavigationHelper.setupBottomNavigation(this)

        /* ---------- Card & animazioni ---------- */
        val cardMyScore     = findViewById<MaterialCardView>(R.id.cardMyScore)
        val cardLeaderboard = findViewById<MaterialCardView>(R.id.cardLeaderboard)
        val fadeAnim        = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)

        cardMyScore.setOnClickListener {
            it.startAnimation(fadeAnim)
            startActivity(
                Intent(this, MyScoreActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            )
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        cardLeaderboard.setOnClickListener {
            it.startAnimation(fadeAnim)
            startActivity(
                Intent(this, LeaderboardActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            )
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}
