package com.example.nativesparksapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
        setContentView(R.layout.activity_profile)  // <-- Sostituisci col tuo layout

        // 1. Recupera le View
        switchNotifications = findViewById(R.id.switchNotifications)
        buttonEditProfile   = findViewById(R.id.textEditProfile)      // Esempio di ID
        buttonContactUs     = findViewById(R.id.textContactUs)
        buttonPrivacyPolicy = findViewById(R.id.textPrivacyPolicy)
        buttonLogOut        = findViewById(R.id.textLogOut)

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

        // Esegue logout e porta alla LoginActivity
        buttonLogOut.setOnClickListener {
            // Logout da Firebase
            auth.signOut()

            // Torna a LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            // Per sicurezza, se vuoi che l’utente non torni a questa Activity col tasto back:
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()  // Chiude l’Activity corrente
        }
    }
}
