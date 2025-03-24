package com.example.nativesparksapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.google.firebase.auth.FirebaseAuth
import java.io.ByteArrayOutputStream
import java.io.IOException

class ProfileActivity : AppCompatActivity() {

    companion object {
        const val PREFS_NAME = "UserPrefs"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_PROFILE_IMAGE = "profile_image"
        private const val REQUEST_GALLERY = 100
        private const val TAG = "ProfileActivity"
    }

    private lateinit var switchNotifications: SwitchCompat
    private lateinit var buttonEditProfile: TextView
    private lateinit var buttonContactUs: TextView
    private lateinit var buttonPrivacyPolicy: TextView
    private lateinit var buttonLogOut: TextView
    private lateinit var imageProfile: ImageView
    private lateinit var iconEditProfile: ImageView

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

        // Inizializzazione ImageView per il profilo e l'icona di modifica
        imageProfile = findViewById(R.id.imageProfile)
        iconEditProfile = findViewById(R.id.iconEditProfile)

        // Carica l'immagine del profilo dalle SharedPreferences
        loadProfileImage()

        // Gestione click sull'immagine del profilo e sull'icona di modifica
        imageProfile.setOnClickListener {
            openGallery()
        }

        iconEditProfile.setOnClickListener {
            openGallery()
        }

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
            // Pulisci le SharedPreferences prima del logout
            clearUserPreferences()

            // Esegui il logout da Firebase
            auth.signOut()

            // Torna alla schermata di login
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

    // Apre la galleria per selezionare un'immagine
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    // Gestisce il risultato della selezione dell'immagine dalla galleria
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GALLERY && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri = data.data
            if (selectedImageUri != null) {
                try {
                    // Imposta l'immagine selezionata nell'ImageView
                    imageProfile.setImageURI(selectedImageUri)

                    // Salva l'immagine nelle SharedPreferences
                    saveProfileImage(selectedImageUri)

                    Toast.makeText(this, "Immagine del profilo aggiornata", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Errore nel caricamento dell'immagine", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        }
    }

    // Salva l'immagine del profilo nelle SharedPreferences
    private fun saveProfileImage(imageUri: Uri) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            val resizedBitmap = getResizedBitmap(bitmap, 500) // Ridimensiona per risparmiare spazio
            val encodedImage = encodeToBase64(resizedBitmap)

            val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            sharedPrefs.edit()
                .putString(KEY_PROFILE_IMAGE, encodedImage)
                .apply()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Carica l'immagine del profilo dalle SharedPreferences
    private fun loadProfileImage() {
        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val encodedImage = sharedPrefs.getString(KEY_PROFILE_IMAGE, null)

        if (encodedImage != null) {
            try {
                val bitmap = decodeBase64(encodedImage)
                imageProfile.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Converte un Bitmap in una stringa Base64
    private fun encodeToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    // Converte una stringa Base64 in un Bitmap
    private fun decodeBase64(input: String): Bitmap {
        val decodedBytes = Base64.decode(input, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    // Ridimensiona un Bitmap per risparmiare spazio
    private fun getResizedBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        var width = bitmap.width
        var height = bitmap.height

        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            // Immagine più larga che alta
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            // Immagine più alta che larga
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    // Pulisce tutte le SharedPreferences dell'utente
    private fun clearUserPreferences() {
        Log.d(TAG, "Pulizia delle SharedPreferences dell'utente")

        // Pulisci le SharedPreferences di ProfileActivity
        val profilePrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        profilePrefs.edit().clear().apply()

        // Pulisci le SharedPreferences di EditProfileActivity
        val editProfilePrefs = getSharedPreferences(EditProfileActivity.PREFS_NAME, Context.MODE_PRIVATE)
        editProfilePrefs.edit().clear().apply()

        // Pulisci eventuali altre SharedPreferences dell'app
        // Se ci sono altre SharedPreferences specifiche dell'utente, aggiungerle qui

        Log.d(TAG, "SharedPreferences pulite con successo")
    }
}
