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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
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
    private lateinit var googleSignInClient: GoogleSignInClient

    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Inizializzazione Google Sign-In
        googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )

        // View bindings
        switchNotifications = findViewById(R.id.switchNotifications)
        buttonEditProfile = findViewById(R.id.textEditProfile)
        buttonContactUs = findViewById(R.id.textContactUs)
        buttonPrivacyPolicy = findViewById(R.id.textPrivacyPolicy)
        buttonLogOut = findViewById(R.id.textLogOut)
        imageProfile = findViewById(R.id.imageProfile)
        iconEditProfile = findViewById(R.id.iconEditProfile)

        loadProfileImage()

        imageProfile.setOnClickListener { openGallery() }
        iconEditProfile.setOnClickListener { openGallery() }

        // Notifiche
        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isNotificationsOn = sharedPrefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        switchNotifications.isChecked = isNotificationsOn
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, isChecked).apply()
        }

        // Navigazione altre pagine
        buttonEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        buttonContactUs.setOnClickListener {
            startActivity(Intent(this, ContactsActivity::class.java))
        }

        buttonPrivacyPolicy.setOnClickListener {
            startActivity(Intent(this, PrivacyPolicyActivity::class.java))
        }

        // Logout + Google
        buttonLogOut.setOnClickListener {
            clearUserPreferences()
            auth.signOut()
            googleSignInClient.signOut().addOnCompleteListener {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }

        // ---- Bottom Navigation ----
        val btnPlay = findViewById<ImageButton>(R.id.btn_home)
        val btnProfile = findViewById<ImageButton>(R.id.btn_profile)

        btnPlay.setOnClickListener {
            val intent = Intent(this, GameLaunchActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }

        btnProfile.setOnClickListener {
            // Siamo già nel profilo, non fare nulla
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GALLERY && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri = data.data
            if (selectedImageUri != null) {
                try {
                    imageProfile.setImageURI(selectedImageUri)
                    saveProfileImage(selectedImageUri)
                    Toast.makeText(this, "Immagine del profilo aggiornata", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Errore nel caricamento dell'immagine", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        }
    }

    private fun saveProfileImage(imageUri: Uri) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            val resizedBitmap = getResizedBitmap(bitmap, 500)
            val encodedImage = encodeToBase64(resizedBitmap)
            val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            sharedPrefs.edit().putString(KEY_PROFILE_IMAGE, encodedImage).apply()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

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

    private fun encodeToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun decodeBase64(input: String): Bitmap {
        val decodedBytes = Base64.decode(input, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    private fun getResizedBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        var width = bitmap.width
        var height = bitmap.height
        val ratio = width.toFloat() / height
        if (ratio > 1) {
            width = maxSize
            height = (width / ratio).toInt()
        } else {
            height = maxSize
            width = (height * ratio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    private fun clearUserPreferences() {
        Log.d(TAG, "Pulizia delle SharedPreferences dell'utente")
        val profilePrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        profilePrefs.edit().clear().apply()

        val editProfilePrefs = getSharedPreferences(EditProfileActivity.PREFS_NAME, Context.MODE_PRIVATE)
        editProfilePrefs.edit().clear().apply()

        Log.d(TAG, "SharedPreferences pulite con successo")
    }
}
