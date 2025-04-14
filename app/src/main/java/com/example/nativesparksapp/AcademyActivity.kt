package com.example.nativesparksapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import android.content.Intent
import java.io.ByteArrayOutputStream

class AcademyActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AcademyActivity"
        private const val PREFS_NAME = "UserPrefs"
        private const val KEY_PROFILE_IMAGE = "profile_image"

        // URL quiz / corso
        private const val DRIMIFY_QUIZ_URL_1 = "https://apps.drimify.com/pQyLQx9P/"
        private const val MIND_SMITH_COURSE_URL = "https://app.mindsmith.ai/learn/cm17nwd7a00i2rhavpy0vobxs"
    }

    private lateinit var imageProfile: ImageView
    private lateinit var buttonStartQuiz1: Button
    private lateinit var buttonStartQuiz2: Button
    private lateinit var buttonStartCourse: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_academy)

        // Bottom navigation
        BottomNavigationHelper.setupBottomNavigation(this)

        // Riferimenti layout
        imageProfile = findViewById(R.id.imageProfile)
        buttonStartQuiz1 = findViewById(R.id.buttonStartQuiz1)
        buttonStartCourse = findViewById(R.id.buttonStartCourse)

        // Carica l’immagine profilo da SharedPreferences (se esiste)
        loadProfileImage()

        // Gestione click quiz 1
        buttonStartQuiz1.setOnClickListener {
            openWebView(DRIMIFY_QUIZ_URL_1)
        }


        // Gestione click corso
        buttonStartCourse.setOnClickListener {
            openWebView(MIND_SMITH_COURSE_URL)
        }

        Log.d(TAG, "AcademyActivity avviata correttamente.")
    }


    private fun openWebView(url: String) {
        // Presuppone che tu abbia una WebViewActivity con EXTRA_URL
        val intent = Intent(this, WebViewActivity::class.java)
        intent.putExtra(WebViewActivity.EXTRA_URL, url)
        startActivity(intent)
    }

    private fun loadProfileImage() {
        try {
            val sp = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val encodedImage = sp.getString(KEY_PROFILE_IMAGE, null)
            if (encodedImage != null) {
                val bitmap = decodeBase64(encodedImage)
                imageProfile.setImageBitmap(bitmap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Errore caricamento immagine profilo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun decodeBase64(input: String): Bitmap {
        val decodedBytes = Base64.decode(input, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    private fun encodeToBase64(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        val byteArray = baos.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}
