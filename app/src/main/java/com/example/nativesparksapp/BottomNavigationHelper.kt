package com.example.nativesparksapp

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
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

        /**
         * Inizializza la bottom navigation bar con le animazioni e gli eventi
         */
        fun setupBottomNavigation(activity: AppCompatActivity)  {
            val leaderboardContainer = activity.findViewById<LinearLayout>(R.id.leaderboard_container)
            val homeContainer = activity.findViewById<FrameLayout>(R.id.home_container)
            val profileContainer = activity.findViewById<LinearLayout>(R.id.profile_container)
            val homeButton = activity.findViewById<MaterialCardView>(R.id.home_button_background)

            // Imposta l'elemento iniziale come selezionato
            when (currentSelectedItemId) {
                R.id.leaderboard_container -> selectItem(leaderboardContainer, activity)
                R.id.profile_container -> selectItem(profileContainer, activity)
                else -> pulseHomeButton(homeButton)
            }

            // Imposta i listener per i click
            leaderboardContainer.setOnClickListener {
                if (currentSelectedItemId != R.id.leaderboard_container) {
                    playClickAnimation(it, activity)
                    selectItem(leaderboardContainer, activity)
                    currentSelectedItemId = R.id.leaderboard_container

                }
            }

            homeContainer.setOnClickListener {
                playClickAnimation(homeButton, activity)
                pulseHomeButton(homeButton)
                currentSelectedItemId = R.id.home_container

                // Naviga alla GameLaunchActivity
                activity.startActivity(android.content.Intent(activity, GameLaunchActivity::class.java)
                    .addFlags(android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
            }

            profileContainer.setOnClickListener {
                if (currentSelectedItemId != R.id.profile_container) {
                    playClickAnimation(it, activity)
                    selectItem(profileContainer, activity)
                    currentSelectedItemId = R.id.profile_container

                    // Naviga alla ProfileActivity
                    activity.startActivity(android.content.Intent(activity, ProfileActivity::class.java)
                        .addFlags(android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                }
            }

            // Aggiorna le icone con le versioni moderne
            activity.findViewById<ImageView>(R.id.btn_leaderboard).setImageResource(R.drawable.ic_leaderboard)
            activity.findViewById<ImageView>(R.id.btn_home).setImageResource(R.drawable.ic_play)
            activity.findViewById<ImageView>(R.id.btn_profile).setImageResource(R.drawable.ic_profile)
        }

        /**
         * Seleziona un elemento della bottom navigation
         */
        private fun selectItem(item: LinearLayout, activity: AppCompatActivity) {
            // Deseleziona l'elemento corrente
            currentSelectedItem?.isSelected = false

            // Seleziona il nuovo elemento
            item.isSelected = true
            currentSelectedItem = item

            // Riproduci l'animazione di selezione
            val anim = AnimationUtils.loadAnimation(activity, R.anim.bottom_nav_item_selected)
            item.startAnimation(anim)
        }

        /**
         * Riproduce l'animazione di click
         */
        private fun playClickAnimation(view: View, activity: AppCompatActivity) {
            val anim = AnimationUtils.loadAnimation(activity, R.anim.bottom_nav_item_clicked)
            view.startAnimation(anim)
        }

        /**
         * Crea un effetto pulsante per il pulsante home
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
