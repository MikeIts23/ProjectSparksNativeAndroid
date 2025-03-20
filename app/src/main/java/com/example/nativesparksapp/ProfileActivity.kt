package com.example.nativesparksapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    companion object {
        private const val PREFS_NAME = "UserPrefs"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    }

    private lateinit var switchNotifications: SwitchCompat
    private lateinit var buttonEditProfile: TextView
    private lateinit var buttonContactUs: TextView
    private lateinit var buttonPrivacyPolicy: TextView
    private lateinit var buttonLogOut: TextView

    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Inizializzazione Switch e TextView
        switchNotifications = findViewById(R.id.switchNotifications)
        buttonEditProfile = findViewById(R.id.textEditProfile)
        buttonContactUs = findViewById(R.id.textContactUs)
        buttonPrivacyPolicy = findViewById(R.id.textPrivacyPolicy)
        buttonLogOut = findViewById(R.id.textLogOut)

        // Recupero preferenze Notifiche
        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isNotificationsOn = sharedPrefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        switchNotifications.isChecked = isNotificationsOn

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit()
                .putBoolean(KEY_NOTIFICATIONS_ENABLED, isChecked)
                .apply()
        }

        // Apre EditProfileActivity
        buttonEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        // Apre ContactActivity
        buttonContactUs.setOnClickListener {
            val intent = Intent(this, ContactsActivity::class.java)
            startActivity(intent)
        }

        // Apre PrivacyPolicyActivity
        buttonPrivacyPolicy.setOnClickListener {
            val intent = Intent(this, PrivacyPolicyActivity::class.java)
            startActivity(intent)
        }

        // Logout
        buttonLogOut.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // ---- Aggiunta gestione Bottom Navigation ----
        val btnPlay = findViewById<ImageButton>(R.id.btn_home)
        val btnProfile = findViewById<ImageButton>(R.id.btn_profile)

        btnPlay.setOnClickListener {
            startActivity(Intent(this, GameLaunchActivity::class.java))
        }

        btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        // ---------------------------------------------
    }
}
