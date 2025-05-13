package com.example.nativesparksapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

class ProfileActivity : BaseActivity() {

    companion object {
        const val PREFS_NAME = "UserPrefs"
        private const val KEY_PROFILE_IMAGE = "profile_image"
        private const val REQUEST_GALLERY = 100
        private const val TAG = "ProfileActivity"
    }

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

        val currentLanguage = LocaleHelper.getLanguage(this)
        LocaleHelper.setLocale(this, currentLanguage)

        setContentView(R.layout.activity_profile)

        googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )

        // View bindings
        buttonEditProfile   = findViewById(R.id.textEditProfile)
        buttonContactUs     = findViewById(R.id.textContactUs)
        buttonPrivacyPolicy = findViewById(R.id.textPrivacyPolicy)
        buttonLogOut        = findViewById(R.id.textLogOut)
        imageProfile        = findViewById(R.id.imageProfile)
        iconEditProfile     = findViewById(R.id.iconEditProfile)
        textLanguage        = findViewById(R.id.textLanguage)
        textCurrentLanguage = findViewById(R.id.textCurrentLanguage)

        buttonEditProfile.text       = getString(R.string.edit_profile_information_text)
        textLanguage.text            = getString(R.string.language_text)
        buttonLogOut.text            = getString(R.string.log_out_text)
        buttonContactUs.text         = getString(R.string.contact_us_text)
        buttonPrivacyPolicy.text     = getString(R.string.privacy_policy_text)

        updateCurrentLanguageText()
        loadProfileImage()

        imageProfile.setOnClickListener    { openGallery() }
        iconEditProfile.setOnClickListener { openGallery() }

        val isRegisteredViaWallet = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean("registered_via_wallet", false)
        if (isRegisteredViaWallet) {
            buttonEditProfile.visibility = View.GONE
            iconEditProfile.visibility  = View.GONE
        }

        buttonEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
        buttonContactUs.setOnClickListener {
            startActivity(Intent(this, ContactsActivity::class.java))
        }
        buttonPrivacyPolicy.setOnClickListener {
            startActivity(Intent(this, PrivacyPolicyActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.languageRow).setOnClickListener {
            showLanguageSelectionDialog()
        }

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

        BottomNavigationHelper.setupBottomNavigation(this)
    }

    private fun showLanguageSelectionDialog() {
        val languages = arrayOf(
            "${LocaleHelper.getLanguageFlag("en")} ${getString(R.string.english)}",
            "${LocaleHelper.getLanguageFlag("it")} ${getString(R.string.italian)}",
            "${LocaleHelper.getLanguageFlag("fr")} ${getString(R.string.french)}"
        )

        val currentLanguage = LocaleHelper.getLanguage(this)
        val currentIndex = when (currentLanguage) {
            "en" -> 0
            "it" -> 1
            "fr" -> 2
            else -> 0
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_language))
            .setSingleChoiceItems(languages, currentIndex) { dialog, which ->
                val selectedLanguage = when (which) {
                    0 -> "en"
                    1 -> "it"
                    2 -> "fr"
                    else -> "en"
                }
                if (selectedLanguage != currentLanguage) {
                    val ctx = LocaleHelper.setLocale(this, selectedLanguage)
                    updateLocaleResources(ctx)
                    updateCurrentLanguageText()
                    Toast.makeText(this, getString(R.string.language_changed), Toast.LENGTH_SHORT).show()
                    recreate()
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateLocaleResources(context: Context) {
        buttonEditProfile.text   = context.getString(R.string.edit_profile_information_text)
        textLanguage.text        = context.getString(R.string.language_text)
        buttonLogOut.text        = context.getString(R.string.log_out_text)
        buttonContactUs.text     = context.getString(R.string.contact_us_text)
        buttonPrivacyPolicy.text = context.getString(R.string.privacy_policy_text)
    }

    private fun updateCurrentLanguageText() {
        val lang = LocaleHelper.getLanguage(this)
        val name = LocaleHelper.getLanguageName(this, lang)
        val flag = LocaleHelper.getLanguageFlag(lang)
        textCurrentLanguage.text = "$flag $name"
    }

    private fun openGallery() {
        startActivityForResult(
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
            REQUEST_GALLERY
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GALLERY && resultCode == Activity.RESULT_OK && data?.data != null) {
            val uri = data.data!!
            try {
                imageProfile.setImageURI(uri)
                saveProfileImage(uri)
                Toast.makeText(this, getString(R.string.profile_image_updated), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, getString(R.string.profile_image_error), Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Error setting profile image", e)
            }
        }
    }

    private fun saveProfileImage(imageUri: Uri) {
        try {
            val bitmap  = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            val resized = bitmap.scalePreservingAspectRatio(500)
            val encoded = resized.toBase64()
            getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_PROFILE_IMAGE, encoded).apply()
        } catch (e: IOException) {
            Log.e(TAG, "Failed to save image", e)
        }
    }

    private fun loadProfileImage() {
        val encoded = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PROFILE_IMAGE, null)
        encoded?.let {
            try {
                imageProfile.setImageBitmap(it.fromBase64())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load image", e)
            }
        }
    }

    private fun clearUserPreferences() {
        Log.d(TAG, "Clearing user prefs")
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
        getSharedPreferences(EditProfileActivity.PREFS_NAME, Context.MODE_PRIVATE)
            .edit().clear().apply()
    }

    private fun Bitmap.scalePreservingAspectRatio(maxSize: Int): Bitmap {
        val ratio = width.toFloat() / height
        val newWidth  = if (ratio > 1) maxSize else (maxSize * ratio).toInt()
        val newHeight = if (ratio > 1) (maxSize / ratio).toInt() else maxSize
        return Bitmap.createScaledBitmap(this, newWidth, newHeight, true)
    }

    private fun Bitmap.toBase64(): String {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 80, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
    }

    private fun String.fromBase64(): Bitmap =
        Base64.decode(this, Base64.DEFAULT).let { BitmapFactory.decodeByteArray(it, 0, it.size) }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(
            LocaleHelper.setLocale(newBase, LocaleHelper.getLanguage(newBase))
        )
    }
}
