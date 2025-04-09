package com.example.nativesparksapp

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

/**
 * Classe di supporto per gestire la bottom navigation bar moderna
 */
class BottomNavigationHelper {

    companion object {
        private var currentSelectedItem: LinearLayout? = null
        private var currentSelectedItemId: Int = R.id.leaderboard_container

        // Nome SharedPreferences e chiave per la modalità Academy
        private const val PREFS_NAME = "SparksPrefs"
        private const val KEY_ACADEMY_MODE = "is_academy_mode"

        /**
         * Inizializza la bottom navigation bar con le animazioni e gli eventi
         */
        fun setupBottomNavigation(activity: AppCompatActivity)  {
            val leaderboardContainer = activity.findViewById<LinearLayout>(R.id.leaderboard_container)
            val homeContainer = activity.findViewById<FrameLayout>(R.id.home_container)
            val profileContainer = activity.findViewById<LinearLayout>(R.id.profile_container)
            val homeButton = activity.findViewById<MaterialCardView>(R.id.home_button_background)

            // Aggiunta: Academy
            val academyContainer = activity.findViewById<LinearLayout>(R.id.academy_container)

            // Imposta l'elemento iniziale come selezionato
            when (currentSelectedItemId) {
                R.id.leaderboard_container -> selectItem(leaderboardContainer, activity)
                R.id.profile_container -> selectItem(profileContainer, activity)
                R.id.academy_container -> selectItem(academyContainer, activity)
                else -> pulseHomeButton(homeButton)
            }

            // Click su Leaderboard
            leaderboardContainer.setOnClickListener {
                if (currentSelectedItemId != R.id.leaderboard_container) {
                    playClickAnimation(it, activity)
                    selectItem(leaderboardContainer, activity)
                    currentSelectedItemId = R.id.leaderboard_container
                    // Avvia LeaderboardActivity (senza ricreare ogni volta)
                    activity.startActivity(
                        Intent(activity, LeaderboardActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    )
                }
            }

            // Click su Home
            homeContainer.setOnClickListener {
                playClickAnimation(homeButton, activity)
                pulseHomeButton(homeButton)
                currentSelectedItemId = R.id.home_container

                // Disattiva Academy mode se vuoi tornare al “gioco classico”
                val prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().putBoolean(KEY_ACADEMY_MODE, false).apply()

                // Naviga a GameLaunchActivity
                activity.startActivity(
                    Intent(activity, GameLaunchActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                )
            }

            // Click su Profile
            profileContainer.setOnClickListener {
                if (currentSelectedItemId != R.id.profile_container) {
                    playClickAnimation(it, activity)
                    selectItem(profileContainer, activity)
                    currentSelectedItemId = R.id.profile_container
                    // Avvia ProfileActivity
                    activity.startActivity(
                        Intent(activity, ProfileActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    )
                }
            }

            // Click su Academy
            academyContainer.setOnClickListener {
                if (currentSelectedItemId != R.id.academy_container) {
                    playClickAnimation(it, activity)
                    selectItem(academyContainer, activity)
                    currentSelectedItemId = R.id.academy_container
                }
                // Attiva Academy mode nelle SharedPreferences
                val prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().putBoolean(KEY_ACADEMY_MODE, true).apply()

                // Avvia la GameLaunchActivity in REORDER_TO_FRONT
                activity.startActivity(
                    Intent(activity, GameLaunchActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                )
            }

            activity.findViewById<ImageView>(R.id.btn_leaderboard)
                ?.setImageResource(R.drawable.ic_leaderboard)
            activity.findViewById<ImageView>(R.id.btn_home)
                ?.setImageResource(R.drawable.ic_play)
            activity.findViewById<ImageView>(R.id.btn_profile)
                ?.setImageResource(R.drawable.ic_profile)
            // Imposta l’icona di “libro segnalibro” come placeholder
            activity.findViewById<ImageView>(R.id.btn_academy)
                ?.setImageResource(android.R.drawable.ic_menu_agenda)
        }
        private fun selectItem(item: LinearLayout?, activity: AppCompatActivity) {
            // Deseleziona l'elemento corrente
            currentSelectedItem?.isSelected = false

            // Seleziona il nuovo elemento
            item?.isSelected = true
            currentSelectedItem = item

            // Riproduci l'animazione di selezione
            if (item != null) {
                val anim = AnimationUtils.loadAnimation(activity, R.anim.bottom_nav_item_selected)
                item.startAnimation(anim)
            }
        }

        /**
         * Riproduce l'animazione di click
         */
        private fun playClickAnimation(view: View, activity: AppCompatActivity) {
            val anim = AnimationUtils.loadAnimation(activity, R.anim.bottom_nav_item_clicked)
            view.startAnimation(anim)
        }

        /**
         * Crea un effetto pulsazione per il pulsante home
         */
        private fun pulseHomeButton(homeButton: MaterialCardView) {
            // Cancella eventuali animazioni precedenti
            homeButton.clearAnimation()

            // Crea l'animazione di pulsazione
            val scaleX = ObjectAnimator.ofFloat(homeButton, "scaleX", 1f, 1.1f, 1f)
            val scaleY = ObjectAnimator.ofFloat(homeButton, "scaleY", 1f, 1.1f, 1f)

            val animatorSet = AnimatorSet()
            animatorSet.playTogether(scaleX, scaleY)
            animatorSet.duration = 500
            animatorSet.start()
        }
    }
}
