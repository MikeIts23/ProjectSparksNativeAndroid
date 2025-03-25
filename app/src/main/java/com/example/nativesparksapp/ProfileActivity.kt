package com.example.nativesparksapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

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
    private lateinit var textLanguage: TextView
    private lateinit var textCurrentLanguage: TextView

    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Applica la lingua salvata prima di impostare il layout
        val currentLanguage = LocaleHelper.getLanguage(this)
        val context = LocaleHelper.setLocale(this, currentLanguage)

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
        textLanguage = findViewById(R.id.textLanguage)
        textCurrentLanguage = findViewById(R.id.textCurrentLanguage)

        // Imposta i testi localizzati
        buttonEditProfile.text = getString(R.string.edit_profile_information_text)
        findViewById<TextView>(R.id.textNotifications).text = getString(R.string.notifications_text)
        textLanguage.text = getString(R.string.language_text)
        buttonLogOut.text = getString(R.string.log_out_text)
        buttonContactUs.text = getString(R.string.contact_us_text)
        buttonPrivacyPolicy.text = getString(R.string.privacy_policy_text)

        // Imposta il testo della lingua corrente con emoji
        updateCurrentLanguageText()

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

        // Selezione lingua
        findViewById<LinearLayout>(R.id.languageRow).setOnClickListener {
            showLanguageSelectionDialog()
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

        // Inizializza la bottom navigation bar moderna
        BottomNavigationHelper.setupBottomNavigation(this)
    }

    /**
     * Mostra il popup di selezione lingua
     */
    private fun showLanguageSelectionDialog() {
        val languages = arrayOf(
            "${LocaleHelper.getLanguageFlag("en")} ${getString(R.string.english)}",
            "${LocaleHelper.getLanguageFlag("it")} ${getString(R.string.italian)}",
            "${LocaleHelper.getLanguageFlag("fr")} ${getString(R.string.french)}"
        )

        val currentLanguage = LocaleHelper.getLanguage(this)
        val currentIndex = when(currentLanguage) {
            "en" -> 0
            "it" -> 1
            "fr" -> 2
            else -> 0
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.select_language))
        builder.setSingleChoiceItems(languages, currentIndex) { dialog, which ->
            val selectedLanguage = when(which) {
                0 -> "en"
                1 -> "it"
                2 -> "fr"
                else -> "en"
            }

            if (selectedLanguage != currentLanguage) {
                // Cambia la lingua
                val context = LocaleHelper.setLocale(this, selectedLanguage)

                // Aggiorna le risorse
                updateLocaleResources(context)

                // Aggiorna il testo della lingua corrente
                updateCurrentLanguageText()

                // Mostra un messaggio di conferma
                Toast.makeText(this, getString(R.string.language_changed), Toast.LENGTH_SHORT).show()
            }

            dialog.dismiss()
        }

        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    /**
     * Aggiorna le risorse dell'app con la nuova lingua
     */
    private fun updateLocaleResources(context: Context) {
        // Aggiorna i testi nell'interfaccia utente
        buttonEditProfile.text = context.getString(R.string.edit_profile_information_text)
        findViewById<TextView>(R.id.textNotifications).text = context.getString(R.string.notifications_text)
        textLanguage.text = context.getString(R.string.language_text)
        buttonLogOut.text = context.getString(R.string.log_out_text)
        buttonContactUs.text = context.getString(R.string.contact_us_text)
        buttonPrivacyPolicy.text = context.getString(R.string.privacy_policy_text)
    }

    /**
     * Aggiorna il testo della lingua corrente con emoji
     */
    private fun updateCurrentLanguageText() {
        val currentLanguage = LocaleHelper.getLanguage(this)
        val languageName = LocaleHelper.getLanguageName(this, currentLanguage)
        val languageFlag = LocaleHelper.getLanguageFlag(currentLanguage)
        textCurrentLanguage.text = "$languageFlag $languageName"
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
                    Toast.makeText(this, getString(R.string.profile_image_updated), Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, getString(R.string.profile_image_error), Toast.LENGTH_SHORT).show()
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

    // Aggiungi il supporto per l'attachamento al contesto della lingua corrente
    override fun attachBaseContext(newBase: Context) {
        val language = LocaleHelper.getLanguage(newBase)
        super.attachBaseContext(LocaleHelper.setLocale(newBase, language))
    }
}
