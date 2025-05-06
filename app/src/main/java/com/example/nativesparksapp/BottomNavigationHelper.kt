package com.example.nativesparksapp

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class BottomNavigationHelper {

    companion object {
        private var currentSelectedItem: LinearLayout? = null
        private var currentSelectedItemId: Int = R.id.leaderboard_container

        private const val PREFS_NAME = "SparksPrefs"
        private const val KEY_ACADEMY_MODE = "is_academy_mode"

        fun setupBottomNavigation(activity: AppCompatActivity) {
            val leaderboardContainer =
                activity.findViewById<LinearLayout>(R.id.leaderboard_container)
            val homeContainer = activity.findViewById<FrameLayout>(R.id.home_container)
            val profileContainer = activity.findViewById<LinearLayout>(R.id.profile_container)
            val homeButton = activity.findViewById<MaterialCardView>(R.id.home_button_background)

            val academyContainer = activity.findViewById<LinearLayout>(R.id.academy_container)

            when (currentSelectedItemId) {
                R.id.leaderboard_container -> selectItem(leaderboardContainer, activity)
                R.id.profile_container -> selectItem(profileContainer, activity)
                R.id.academy_container -> selectItem(academyContainer, activity)
                else -> pulseHomeButton(homeButton)
            }

            /* ------- LEADERBOARD ------- */
            leaderboardContainer.setOnClickListener {
                if (currentSelectedItemId != R.id.leaderboard_container) {
                    playClickAnimation(it, activity)
                    selectItem(leaderboardContainer, activity)
                    currentSelectedItemId = R.id.leaderboard_container

                    // Apri la nuova pagina con le due card
                    activity.startActivity(
                        Intent(activity, LeaderboardHomeActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    )
                }
            }

            /* ------- HOME ------- */
            homeContainer.setOnClickListener {
                playClickAnimation(homeButton, activity)
                pulseHomeButton(homeButton)
                currentSelectedItemId = R.id.home_container

                val prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().putBoolean(KEY_ACADEMY_MODE, false).apply()

                activity.startActivity(
                    Intent(activity, GameLaunchActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                )
            }

            /* ------- PROFILE ------- */
            profileContainer.setOnClickListener {
                if (currentSelectedItemId != R.id.profile_container) {
                    playClickAnimation(it, activity)
                    selectItem(profileContainer, activity)
                    currentSelectedItemId = R.id.profile_container

                    activity.startActivity(
                        Intent(activity, ProfileActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    )
                }
            }

            /* ------- ACADEMY ------- */
            academyContainer.setOnClickListener {
                if (currentSelectedItemId != R.id.academy_container) {
                    playClickAnimation(it, activity)
                    selectItem(academyContainer, activity)
                    currentSelectedItemId = R.id.academy_container
                }

                val prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().putBoolean(KEY_ACADEMY_MODE, true).apply()

                activity.startActivity(
                    Intent(activity, AcademyActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                )
            }

            /* ------- Aggiorna icone se necessario ------- */
            activity.findViewById<ImageView>(R.id.btn_leaderboard)
                ?.setImageResource(R.drawable.ic_leaderboard)
            activity.findViewById<ImageView>(R.id.btn_home)
                ?.setImageResource(R.drawable.joystick)
            activity.findViewById<ImageView>(R.id.btn_profile)
                ?.setImageResource(R.drawable.ic_profile)
            activity.findViewById<ImageView>(R.id.btn_academy)
                ?.setImageResource(R.drawable.academy)
        }

        private fun selectItem(item: LinearLayout?, activity: AppCompatActivity) {
            currentSelectedItem?.isSelected = false
            item?.isSelected = true
            currentSelectedItem = item

            if (item != null) {
                val anim =
                    AnimationUtils.loadAnimation(activity, R.anim.bottom_nav_item_selected)
                item.startAnimation(anim)
            }
        }

        private fun playClickAnimation(view: View, activity: AppCompatActivity) {
            val anim = AnimationUtils.loadAnimation(activity, R.anim.bottom_nav_item_clicked)
            view.startAnimation(anim)
        }

        private fun pulseHomeButton(homeButton: MaterialCardView) {
            homeButton.clearAnimation()

            val scaleX = ObjectAnimator.ofFloat(homeButton, "scaleX", 1f, 1.1f, 1f)
            val scaleY = ObjectAnimator.ofFloat(homeButton, "scaleY", 1f, 1.1f, 1f)

            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 500
                start()
            }
        }
    }
}
